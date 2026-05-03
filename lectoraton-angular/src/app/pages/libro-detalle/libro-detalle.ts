import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';
import { BibliotecaDTO, BibliotecaService } from '../../core/biblioteca/biblioteca.service';
import { ComentarioDTO, LibroDetalleDTO, LibroService, ProgresoLecturaDTO, ResenaDTO } from '../../core/libro/libro.service';

@Component({
  selector: 'app-libro-detalle',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './libro-detalle.html',
  styleUrl: './libro-detalle.css',
})
export class LibroDetallePage implements OnInit, OnDestroy {
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly detalle = signal<LibroDetalleDTO | null>(null);
  protected readonly resenas = signal<ResenaDTO[]>([]);
  protected readonly cargandoResenas = signal(false);
  protected readonly showAgregarModal = signal(false);
  protected readonly agregando = signal(false);
  protected readonly agregarError = signal<string | null>(null);
  protected readonly bibliotecas = signal<BibliotecaDTO[]>([]);
  protected readonly comentariosPorResena = signal<Record<number, ComentarioDTO[]>>({});
  protected readonly comentariosAbiertos = signal<Record<number, boolean>>({});
  protected readonly cargandoComentarios = signal<Record<number, boolean>>({});
  protected readonly enviandoComentario = signal<Record<number, boolean>>({});
  protected readonly comentarioTextoPorResena = signal<Record<number, string>>({});
  protected readonly comentarioSpoilerPorResena = signal<Record<number, boolean>>({});
  protected readonly comentarioErrorPorResena = signal<Record<number, string | null>>({});
  protected readonly spoilersResenasVisibles = signal<Record<number, boolean>>({});
  protected readonly spoilersComentariosVisibles = signal<Record<number, Record<number, boolean>>>({});
  protected readonly miResena = signal<ResenaDTO | null>(null);
  protected readonly borrandoMiResena = signal(false);
  protected readonly creandoResena = signal(false);
  protected readonly crearResenaError = signal<string | null>(null);
  protected readonly crearResenaOk = signal<string | null>(null);
  protected readonly guardandoProgreso = signal(false);
  protected readonly progresoError = signal<string | null>(null);
  protected readonly progresoOk = signal<string | null>(null);
  protected progresoPagina = 0;
  /** Cadena vacía = enviar sin estado (el servidor deduce según página). */
  protected progresoEstado = '';
  protected resenaNuevaTitulo = '';
  protected resenaNuevaContenido = '';
  protected resenaNuevaCalificacion = 0;
  protected resenaHoverCalificacion = 0;
  protected resenaNuevaSpoiler = false;
  protected bibliotecaSeleccionadaId: number | null = null;
  protected textoResenas = '';
  protected filtroResenas = 'fechaCreacion,desc';

  private libroId: number | null = null;
  private usuarioActualId: number | null = null;
  private debounceId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly libroService: LibroService,
    private readonly bibliotecaService: BibliotecaService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const libroId = Number(idParam);
    if (!idParam || Number.isNaN(libroId)) {
      this.error.set('Libro no válido.');
      this.loading.set(false);
      return;
    }
    this.libroId = libroId;
    this.usuarioActualId = this.authService.getUserId();
    this.bibliotecaService.getMisBibliotecas().subscribe({
      next: (list) => {
        this.bibliotecas.set(list);
        if (list.length > 0) {
          this.bibliotecaSeleccionadaId = list[0].id;
        }
      },
    });
    this.cargarDetalle();
    this.cargarMiResena();
    this.cargarResenas();
  }

  ngOnDestroy(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
  }

  protected estrellas(valor: number | null | undefined): string[] {
    const redondeado = Math.round(valor || 0);
    return Array.from({ length: 5 }, (_, i) => (i < redondeado ? '★' : '☆'));
  }

  protected clampPorcentaje(valor: number): number {
    return Math.min(100, Math.max(0, valor));
  }

  protected guardarProgreso(det: LibroDetalleDTO): void {
    if (!this.libroId) {
      return;
    }
    this.progresoError.set(null);
    this.progresoOk.set(null);

    const max = det.paginas;
    if (max != null && max > 0 && this.progresoPagina > max) {
      this.progresoError.set(`La página no puede superar ${max} (total del libro).`);
      return;
    }
    if (this.progresoPagina < 0 || !Number.isFinite(this.progresoPagina)) {
      this.progresoError.set('Indica un número de página válido (0 o más).');
      return;
    }

    const payload: { paginaActual: number; estado?: string } = {
      paginaActual: Math.floor(this.progresoPagina),
    };
    if (this.progresoEstado !== '') {
      payload.estado = this.progresoEstado;
    }

    this.guardandoProgreso.set(true);
    this.libroService.actualizarProgresoLibro(this.libroId, payload).subscribe({
      next: (actualizado) => {
        this.guardandoProgreso.set(false);
        this.progresoOk.set('Progreso guardado.');
        this.aplicarMiProgresoEnDetalle(actualizado);
        this.progresoPagina = actualizado.paginaActual;
        this.progresoEstado = '';
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoProgreso.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.progresoError.set(typeof body === 'string' ? body : body?.message || 'No se pudo guardar el progreso.');
      },
    });
  }

  protected getSagaTexto(det: LibroDetalleDTO | null): string | null {
    if (!det?.sagaNombre || !det.numeroSaga) {
      return null;
    }
    return `${det.sagaNombre} #${det.numeroSaga}`;
  }

  protected abrirAgregarModal(): void {
    this.agregarError.set(null);
    const primeraDisponible = this.bibliotecas().find((b) => !this.bibliotecaTieneLibro(b));
    this.bibliotecaSeleccionadaId = primeraDisponible?.id ?? null;
    this.showAgregarModal.set(true);
  }

  protected cerrarAgregarModal(): void {
    if (this.agregando()) {
      return;
    }
    this.showAgregarModal.set(false);
  }

  protected agregarLibroABiblioteca(): void {
    if (!this.libroId || !this.bibliotecaSeleccionadaId) {
      this.agregarError.set('Selecciona una biblioteca.');
      return;
    }
    this.agregando.set(true);
    this.agregarError.set(null);
    this.bibliotecaService.addLibroABiblioteca(this.bibliotecaSeleccionadaId, this.libroId).subscribe({
      next: () => {
        this.agregando.set(false);
        this.showAgregarModal.set(false);
        this.cargarDetalle();
      },
      error: (err: HttpErrorResponse) => {
        this.agregando.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.agregarError.set(typeof body === 'string' ? body : body?.message || 'No se pudo agregar el libro.');
      },
    });
  }

  protected bibliotecaTieneLibro(b: BibliotecaDTO): boolean {
    return !!b.libroIds?.includes(this.libroId ?? -1);
  }

  protected hayBibliotecasDisponibles(): boolean {
    return this.bibliotecas().some((b) => !this.bibliotecaTieneLibro(b));
  }

  protected toggleLikeResena(resenaId: number): void {
    this.libroService.toggleLikeResena(resenaId).subscribe({
      next: (resenaActualizada) => {
        this.resenas.update((lista) => lista.map((r) => (r.id === resenaId ? resenaActualizada : r)));
      },
    });
  }

  protected toggleComentarios(resenaId: number): void {
    const abierto = this.comentariosAbiertos()[resenaId];
    this.comentariosAbiertos.update((actual) => ({
      ...actual,
      [resenaId]: !abierto,
    }));

    if (!abierto && !this.comentariosPorResena()[resenaId]) {
      this.cargarComentarios(resenaId);
    }
  }

  protected comentariosEstanAbiertos(resenaId: number): boolean {
    return !!this.comentariosAbiertos()[resenaId];
  }

  protected getComentariosResena(resenaId: number): ComentarioDTO[] {
    return this.comentariosPorResena()[resenaId] ?? [];
  }

  protected getComentarioTexto(resenaId: number): string {
    return this.comentarioTextoPorResena()[resenaId] ?? '';
  }

  protected setComentarioTexto(resenaId: number, texto: string): void {
    this.comentarioTextoPorResena.update((actual) => ({
      ...actual,
      [resenaId]: texto,
    }));
  }

  protected getComentarioEsSpoiler(resenaId: number): boolean {
    return !!this.comentarioSpoilerPorResena()[resenaId];
  }

  protected setComentarioEsSpoiler(resenaId: number, valor: boolean): void {
    this.comentarioSpoilerPorResena.update((actual) => ({
      ...actual,
      [resenaId]: valor,
    }));
  }

  protected toggleSpoilerResena(resenaId: number): void {
    this.spoilersResenasVisibles.update((actual) => ({
      ...actual,
      [resenaId]: !actual[resenaId],
    }));
  }

  protected spoilerResenaVisible(resenaId: number): boolean {
    return !!this.spoilersResenasVisibles()[resenaId];
  }

  protected toggleSpoilerComentario(resenaId: number, comentarioId: number): void {
    this.spoilersComentariosVisibles.update((actual) => ({
      ...actual,
      [resenaId]: {
        ...(actual[resenaId] ?? {}),
        [comentarioId]: !actual[resenaId]?.[comentarioId],
      },
    }));
  }

  protected spoilerComentarioVisible(resenaId: number, comentarioId: number): boolean {
    return !!this.spoilersComentariosVisibles()[resenaId]?.[comentarioId];
  }

  protected enviarComentario(resenaId: number): void {
    const contenido = this.getComentarioTexto(resenaId).trim();
    if (!contenido) {
      this.comentarioErrorPorResena.update((actual) => ({
        ...actual,
        [resenaId]: 'Escribe un comentario.',
      }));
      return;
    }

    this.enviandoComentario.update((actual) => ({
      ...actual,
      [resenaId]: true,
    }));
    this.comentarioErrorPorResena.update((actual) => ({
      ...actual,
      [resenaId]: null,
    }));

    this.libroService.createComentarioResena({
      resenaId,
      contenido,
      contieneSpoiler: this.getComentarioEsSpoiler(resenaId),
    }).subscribe({
      next: (comentario) => {
        this.comentariosPorResena.update((actual) => ({
          ...actual,
          [resenaId]: [...(actual[resenaId] ?? []), comentario],
        }));
        this.comentarioTextoPorResena.update((actual) => ({
          ...actual,
          [resenaId]: '',
        }));
        this.comentarioSpoilerPorResena.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
        this.enviandoComentario.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
        this.resenas.update((lista) =>
          lista.map((r) => (r.id === resenaId ? { ...r, numComentarios: (r.numComentarios || 0) + 1 } : r)),
        );
      },
      error: (err: HttpErrorResponse) => {
        const body = err.error as string | { message?: string } | undefined;
        this.enviandoComentario.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
        this.comentarioErrorPorResena.update((actual) => ({
          ...actual,
          [resenaId]: typeof body === 'string' ? body : body?.message || 'No se pudo enviar el comentario.',
        }));
      },
    });
  }

  protected onBusquedaResenasChange(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    this.debounceId = setTimeout(() => this.cargarResenas(), 250);
  }

  protected onFiltroResenasChange(): void {
    this.cargarResenas();
  }

  protected esMiResena(r: ResenaDTO): boolean {
    return this.usuarioActualId !== null && r.usuarioId === this.usuarioActualId;
  }

  protected resenasSinMiResena(): ResenaDTO[] {
    return this.resenas().filter((r) => !this.esMiResena(r));
  }

  protected borrarMiResena(): void {
    const mi = this.miResena();
    if (!mi) {
      return;
    }
    this.borrandoMiResena.set(true);
    this.libroService.deleteMiResena(mi.id).subscribe({
      next: () => {
        this.borrandoMiResena.set(false);
        this.miResena.set(null);
        this.resenaNuevaTitulo = '';
        this.resenaNuevaContenido = '';
        this.resenaNuevaCalificacion = 0;
        this.resenaHoverCalificacion = 0;
        this.resenaNuevaSpoiler = false;
        this.crearResenaOk.set('Reseña eliminada correctamente.');
        this.cargarDetalle();
        this.cargarResenas();
      },
      error: (err: HttpErrorResponse) => {
        this.borrandoMiResena.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.crearResenaError.set(typeof body === 'string' ? body : body?.message || 'No se pudo eliminar la reseña.');
      },
    });
  }

  protected estrellasInput(): number[] {
    return [1, 2, 3, 4, 5];
  }

  protected seleccionarCalificacionResena(valor: number): void {
    this.resenaNuevaCalificacion = valor;
  }

  protected onHoverCalificacionResena(valor: number): void {
    this.resenaHoverCalificacion = valor;
  }

  protected clearHoverCalificacionResena(): void {
    this.resenaHoverCalificacion = 0;
  }

  protected estrellaResenaActiva(valor: number): boolean {
    const referencia = this.resenaHoverCalificacion || this.resenaNuevaCalificacion;
    return valor <= referencia;
  }

  protected crearResena(): void {
    if (!this.libroId) {
      return;
    }
    if (this.resenaNuevaCalificacion < 0 || this.resenaNuevaCalificacion > 5) {
      this.crearResenaError.set('La calificación debe estar entre 1 y 5.');
      return;
    }

    this.creandoResena.set(true);
    this.crearResenaError.set(null);
    this.crearResenaOk.set(null);

    const payload = {
      titulo: this.resenaNuevaTitulo.trim() || undefined,
      contenido: this.resenaNuevaContenido.trim() || undefined,
      calificacion: this.resenaNuevaCalificacion > 0 ? this.resenaNuevaCalificacion : undefined,
      contieneSpoiler: this.resenaNuevaSpoiler,
    };

    const peticion$ = this.miResena()
      ? this.libroService.updateMiResena(this.miResena()!.id, payload)
      : this.libroService.createResena({
          libroId: this.libroId,
          ...payload,
        });

    peticion$.subscribe({
      next: (resena) => {
        this.creandoResena.set(false);
        this.crearResenaOk.set(this.miResena() ? 'Reseña actualizada correctamente.' : 'Reseña creada correctamente.');
        this.miResena.set(resena);
        this.cargarDetalle();
        this.cargarResenas();
      },
      error: (err: HttpErrorResponse) => {
        this.creandoResena.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.crearResenaError.set(typeof body === 'string' ? body : body?.message || 'No se pudo guardar la reseña.');
      },
    });
  }

  private cargarComentarios(resenaId: number): void {
    this.cargandoComentarios.update((actual) => ({
      ...actual,
      [resenaId]: true,
    }));
    this.libroService.getComentariosResena(resenaId).subscribe({
      next: (comentarios) => {
        this.comentariosPorResena.update((actual) => ({
          ...actual,
          [resenaId]: comentarios,
        }));
        this.cargandoComentarios.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
      },
      error: () => {
        this.cargandoComentarios.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
        this.comentarioErrorPorResena.update((actual) => ({
          ...actual,
          [resenaId]: 'No se pudieron cargar los comentarios.',
        }));
      },
    });
  }

  private cargarDetalle(): void {
    if (!this.libroId) {
      return;
    }
    this.loading.set(true);
    this.libroService.getDetalleLibro(this.libroId).subscribe({
      next: (det) => {
        this.detalle.set(det);
        this.syncProgresoFormulario(det);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el libro.');
        this.loading.set(false);
      },
    });
  }

  private cargarMiResena(): void {
    if (!this.libroId) {
      return;
    }
    this.libroService.getMiResenaEnLibro(this.libroId).subscribe({
      next: (resena) => {
        this.miResena.set(resena);
        this.resenaNuevaTitulo = resena.titulo ?? '';
        this.resenaNuevaContenido = resena.contenido ?? '';
        this.resenaNuevaCalificacion = resena.calificacion ?? 0;
        this.resenaHoverCalificacion = 0;
        this.resenaNuevaSpoiler = !!resena.contieneSpoiler;
      },
      error: () => {
        this.miResena.set(null);
        this.resenaNuevaTitulo = '';
        this.resenaNuevaContenido = '';
        this.resenaNuevaCalificacion = 0;
        this.resenaHoverCalificacion = 0;
        this.resenaNuevaSpoiler = false;
      },
    });
  }

  private syncProgresoFormulario(det: LibroDetalleDTO): void {
    const p = det.miProgreso;
    if (!p) {
      this.progresoPagina = 0;
      this.progresoEstado = '';
      return;
    }
    this.progresoPagina = p.paginaActual;
    this.progresoEstado = '';
  }

  private aplicarMiProgresoEnDetalle(actualizado: ProgresoLecturaDTO): void {
    this.detalle.update((d) => {
      if (!d) {
        return d;
      }
      return { ...d, miProgreso: actualizado };
    });
  }

  private cargarResenas(): void {
    if (!this.libroId) {
      return;
    }
    this.cargandoResenas.set(true);
    this.libroService.getResenasLibro(this.libroId, this.textoResenas.trim(), this.filtroResenas).subscribe({
      next: (page) => {
        this.resenas.set(page.content || []);
        this.cargandoResenas.set(false);
      },
      error: () => {
        this.cargandoResenas.set(false);
      },
    });
  }
}
