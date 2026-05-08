import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/auth/auth.service';
import { BibliotecaDTO, BibliotecaService } from '../../core/biblioteca/biblioteca.service';
import { ComentarioDTO, LibroDetalleDTO, LibroService, ProgresoLecturaDTO, ResenaDTO } from '../../core/libro/libro.service';

@Component({
  selector: 'app-libro-detalle',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe, TranslatePipe],
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
  // Cadena vacía = enviar sin estado (el servidor deduce según página).
  //  Esto es para que el servidor pueda deducir el estado de lectura del usuario sin tener que enviarlo en cada petición.
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
    private readonly translate: TranslateService,
  ) {}

  // Inicialización de la página
  ngOnInit(): void {
    // Se obtiene el ID del libro de la ruta
    const idParam = this.route.snapshot.paramMap.get('id');
    const libroId = Number(idParam);
    if (!idParam || Number.isNaN(libroId)) {
      this.error.set(this.translate.instant('bookDetail.errorInvalid'));
      this.loading.set(false);
      return;
    }
    // Se asigna el ID del libro a la variable libroId
    this.libroId = libroId;
    // Se obtiene el ID del usuario actual
    this.usuarioActualId = this.authService.getUserId();
    // Se obtienen las bibliotecas del usuario actual
    // Se asignan a la variable bibliotecas y se obtiene la primera biblioteca disponible
    this.bibliotecaService.getMisBibliotecas().subscribe({
      next: (list) => {
        this.bibliotecas.set(list);
        if (list.length > 0) {
          this.bibliotecaSeleccionadaId = list[0].id;
        }
      },
    });
    // Se carga el detalle del libro
    this.cargarDetalle();
    // Se carga la resena propia del libro
    this.cargarMiResena();
    // Se cargan las resenas del libro
    this.cargarResenas();
  }

  ngOnDestroy(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
  }

  // Método para obtener las estrellas de la calificación
  protected estrellas(valor: number | null | undefined): string[] {
    const redondeado = Math.round(valor || 0);
    return Array.from({ length: 5 }, (_, i) => (i < redondeado ? '★' : '☆'));
  }

  // Método para clonar el porcentaje
  protected clampPorcentaje(valor: number): number {
    return Math.min(100, Math.max(0, valor));
  }

  // Método para guardar el progreso de lectura
  protected guardarProgreso(det: LibroDetalleDTO): void {
    
    if (!this.libroId) {
      return;
    }
    this.progresoError.set(null);
    this.progresoOk.set(null);

    // Se obtiene el número de páginas del libro
    const max = det.paginas;
    // Si el número de páginas es mayor que 0 y la página actual es mayor que el número de páginas, se muestra un error
    if (max != null && max > 0 && this.progresoPagina > max) {
      this.progresoError.set(this.translate.instant('bookDetail.errorPageMax', { max }));
      return;
    }
    // Si la página actual es menor que 0 o no es un número finito, se muestra un error
    if (this.progresoPagina < 0 || !Number.isFinite(this.progresoPagina)) {
      this.progresoError.set(this.translate.instant('bookDetail.errorPageValid'));
      return;
    }

    // Se crea el payload para la actualización del progreso de lectura
    const payload: { paginaActual: number; estado?: string } = {
      paginaActual: Math.floor(this.progresoPagina),
    };
    // Si el estado de lectura no es vacío, se agrega al payload
    if (this.progresoEstado !== '') {
      payload.estado = this.progresoEstado;
    }

    // Se guarda el progreso de lectura
    this.guardandoProgreso.set(true);
    // Se actualiza el progreso de lectura
    this.libroService.actualizarProgresoLibro(this.libroId, payload).subscribe({
      next: (actualizado) => {
        this.guardandoProgreso.set(false);
        this.progresoOk.set(this.translate.instant('bookDetail.progressSaved'));
        // Se aplica el progreso de lectura en el detalle del libro
        this.aplicarMiProgresoEnDetalle(actualizado);
        this.progresoPagina = actualizado.paginaActual;
        this.progresoEstado = '';
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoProgreso.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.progresoError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('bookDetail.errorSaveProgress'));
      },
    });
  }

  // Método para obtener el texto de la saga
  protected getSagaTexto(det: LibroDetalleDTO | null): string | null {
    if (!det?.sagaNombre || !det.numeroSaga) {
      return null;
    }
    return `${det.sagaNombre} #${det.numeroSaga}`;
  }

  // Método para abrir el modal de agregar libro a biblioteca
  protected abrirAgregarModal(): void {
    this.agregarError.set(null);
    const primeraDisponible = this.bibliotecas().find((b) => !this.bibliotecaTieneLibro(b));
    this.bibliotecaSeleccionadaId = primeraDisponible?.id ?? null;
    this.showAgregarModal.set(true);
  }

  // Método para cerrar el modal de agregar libro a biblioteca
  protected cerrarAgregarModal(): void {
    if (this.agregando()) {
      return;
    }
    this.showAgregarModal.set(false);
  }

  // Método para agregar un libro a la biblioteca
  protected agregarLibroABiblioteca(): void {
    if (!this.libroId || !this.bibliotecaSeleccionadaId) {
      this.agregarError.set(this.translate.instant('bookDetail.errorSelectLibrary'));
      return;
    }
    // Se inicia el proceso de agregación
    this.agregando.set(true);
    this.agregarError.set(null);
    this.bibliotecaService.addLibroABiblioteca(this.bibliotecaSeleccionadaId, this.libroId).subscribe({
      next: () => {
        // Se finaliza el proceso de agregación
        this.agregando.set(false);
        this.showAgregarModal.set(false);
        // Se carga el detalle del libro
        this.cargarDetalle();
      },
      error: (err: HttpErrorResponse) => {
        this.agregando.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.agregarError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('bookDetail.errorAddBook'));
      },
    });
  }

  // Método para verificar si la biblioteca tiene el libro
  protected bibliotecaTieneLibro(b: BibliotecaDTO): boolean {
    return !!b.libroIds?.includes(this.libroId ?? -1);
  }

  // Método para verificar si hay bibliotecas disponibles
  protected hayBibliotecasDisponibles(): boolean {
    return this.bibliotecas().some((b) => !this.bibliotecaTieneLibro(b));
  }

  // Método para dar like a una resena
  protected toggleLikeResena(resenaId: number): void {
    this.libroService.toggleLikeResena(resenaId).subscribe({
      next: (resenaActualizada) => {
        this.resenas.update((lista) => lista.map((r) => (r.id === resenaId ? resenaActualizada : r)));
      },
    });
  }

  // Método para abrir los comentarios de una resena
  protected toggleComentarios(resenaId: number): void {
    const abierto = this.comentariosAbiertos()[resenaId];
    // Se actualiza el estado de los comentarios de la resena
    this.comentariosAbiertos.update((actual) => ({
      ...actual,
      [resenaId]: !abierto,
    }));
    // Si los comentarios de la resena no están abiertos y no hay comentarios, se cargan los comentarios
    if (!abierto && !this.comentariosPorResena()[resenaId]) {
      this.cargarComentarios(resenaId);
    }
  }

  // Método para verificar si los comentarios de una resena están abiertos
  protected comentariosEstanAbiertos(resenaId: number): boolean {
    return !!this.comentariosAbiertos()[resenaId];
  }

  // Método para obtener los comentarios de una resena
  protected getComentariosResena(resenaId: number): ComentarioDTO[] {
    return this.comentariosPorResena()[resenaId] ?? [];
  }

  // Método para obtener el texto de un comentario
  protected getComentarioTexto(resenaId: number): string {
    return this.comentarioTextoPorResena()[resenaId] ?? '';
  }

  // Método para establecer el texto de un comentario
  protected setComentarioTexto(resenaId: number, texto: string): void {
    this.comentarioTextoPorResena.update((actual) => ({
      ...actual,
      [resenaId]: texto,
    }));
  }

  // Método para verificar si un comentario es spoiler
  protected getComentarioEsSpoiler(resenaId: number): boolean {
    return !!this.comentarioSpoilerPorResena()[resenaId];
  }

  // Método para establecer si un comentario es spoiler
  protected setComentarioEsSpoiler(resenaId: number, valor: boolean): void {
    this.comentarioSpoilerPorResena.update((actual) => ({
      ...actual,
      [resenaId]: valor,
    }));
  }

  // Método para alternar el spoiler de una resena
  protected toggleSpoilerResena(resenaId: number): void {
    this.spoilersResenasVisibles.update((actual) => ({
      ...actual,
      [resenaId]: !actual[resenaId],
    }));
  }

  // Método para verificar si el spoiler de una resena está visible
  protected spoilerResenaVisible(resenaId: number): boolean {
    return !!this.spoilersResenasVisibles()[resenaId];
  }

  // Método para alternar el spoiler de un comentario
  protected toggleSpoilerComentario(resenaId: number, comentarioId: number): void {
    this.spoilersComentariosVisibles.update((actual) => ({
      ...actual,
      [resenaId]: {
        ...(actual[resenaId] ?? {}),
        [comentarioId]: !actual[resenaId]?.[comentarioId],
      },
    }));
  }

  // Método para verificar si el spoiler de un comentario está visible
  protected spoilerComentarioVisible(resenaId: number, comentarioId: number): boolean {
    return !!this.spoilersComentariosVisibles()[resenaId]?.[comentarioId];
  }

  // Método para enviar un comentario
  protected enviarComentario(resenaId: number): void {
    const contenido = this.getComentarioTexto(resenaId).trim();
    if (!contenido) {
      this.comentarioErrorPorResena.update((actual) => ({
        ...actual,
        [resenaId]: this.translate.instant('bookDetail.errorWriteComment'),
      }));
      return;
    }

    // Se inicia el proceso de envío del comentario
    this.enviandoComentario.update((actual) => ({
      ...actual,
      [resenaId]: true,
    }));
    // Se limpia el error del comentario
    this.comentarioErrorPorResena.update((actual) => ({
      ...actual,
      [resenaId]: null,
    }));

    // Se crea el comentario
    this.libroService.createComentarioResena({
      resenaId,
      contenido,
      contieneSpoiler: this.getComentarioEsSpoiler(resenaId),
    }).subscribe({
      next: (comentario) => {
        // Se actualiza el estado de los comentarios de la resena
        this.comentariosPorResena.update((actual) => ({
          ...actual,
          [resenaId]: [...(actual[resenaId] ?? []), comentario],
        }));
        // Se limpia el texto del comentario
        this.comentarioTextoPorResena.update((actual) => ({
          ...actual,
          [resenaId]: '',
        }));
        // Se limpia el spoiler del comentario
        this.comentarioSpoilerPorResena.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
        // Se finaliza el proceso de envío del comentario
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
          [resenaId]: typeof body === 'string' ? body : body?.message || this.translate.instant('bookDetail.errorSendComment'),
        }));
      },
    });
  }

  // Método para buscar las resenas del libro
  protected onBusquedaResenasChange(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    this.debounceId = setTimeout(() => this.cargarResenas(), 250);
  }

  // Método para filtrar las resenas del libro
  protected onFiltroResenasChange(): void {
    this.cargarResenas();
  }

  // Método para verificar si una resena es la propia del usuario
  protected esMiResena(r: ResenaDTO): boolean {
    return this.usuarioActualId !== null && r.usuarioId === this.usuarioActualId;
  }

  // Método para obtener las resenas sin la propia del usuario
  protected resenasSinMiResena(): ResenaDTO[] {
    return this.resenas().filter((r) => !this.esMiResena(r));
  }

  // Método para borrar la propia del usuario
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
        this.crearResenaOk.set(this.translate.instant('bookDetail.reviewDeleted'));
        this.cargarDetalle();
        this.cargarResenas();
      },
      error: (err: HttpErrorResponse) => {
        this.borrandoMiResena.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.crearResenaError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('bookDetail.errorDeleteReview'));
      },
    });
  }

  // Método para obtener las estrellas de la calificación
  protected estrellasInput(): number[] {
    return [1, 2, 3, 4, 5];
  }

  // Método para seleccionar la calificación de una reseña
  protected seleccionarCalificacionResena(valor: number): void {
    this.resenaNuevaCalificacion = valor;
  }

  // Método para establecer la calificación de una reseña al pasar el ratón
  protected onHoverCalificacionResena(valor: number): void {
    this.resenaHoverCalificacion = valor;
  }

  // Método para limpiar la calificación de una reseña al salir del ratón
  protected clearHoverCalificacionResena(): void {
    this.resenaHoverCalificacion = 0;
  }

  // Método para verificar si una estrella de la calificación está activa
  protected estrellaResenaActiva(valor: number): boolean {
    const referencia = this.resenaHoverCalificacion || this.resenaNuevaCalificacion;
    return valor <= referencia;
  }

  // Método para crear una reseña
  protected crearResena(): void {
    if (!this.libroId) {
      return;
    }
    // Si la calificación no es válida, se muestra un error
    if (this.resenaNuevaCalificacion < 0 || this.resenaNuevaCalificacion > 5) {
      this.crearResenaError.set(this.translate.instant('bookDetail.errorRatingRange'));
      return;
    }

    // Se inicia el proceso de creación de la reseña
    this.creandoResena.set(true);
    this.crearResenaError.set(null);
    this.crearResenaOk.set(null);

    // Se crea el payload para la creación de la reseña
    const payload = {
      titulo: this.resenaNuevaTitulo.trim() || undefined,
      contenido: this.resenaNuevaContenido.trim() || undefined,
      calificacion: this.resenaNuevaCalificacion > 0 ? this.resenaNuevaCalificacion : undefined,
      contieneSpoiler: this.resenaNuevaSpoiler,
    };

    // Se crea la petición para la creación de la reseña
    const peticion$ = this.miResena()
      ? this.libroService.updateMiResena(this.miResena()!.id, payload)
      : this.libroService.createResena({
          libroId: this.libroId,
          ...payload,
        });

    // Se suscribe a la petición para la creación de la reseña
    peticion$.subscribe({
      next: (resena) => {
        this.creandoResena.set(false);
        this.crearResenaOk.set(this.miResena() ? this.translate.instant('bookDetail.reviewUpdated') : this.translate.instant('bookDetail.reviewCreated'));
        this.miResena.set(resena);
        this.cargarDetalle();
        this.cargarResenas();
      },
      error: (err: HttpErrorResponse) => {
        this.creandoResena.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.crearResenaError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('bookDetail.errorSaveReview'));
      },
    });
  }

  // Método para cargar los comentarios de una reseña
  private cargarComentarios(resenaId: number): void {
    // Se inicia el proceso de carga de los comentarios de la reseña
    this.cargandoComentarios.update((actual) => ({
      ...actual,
      [resenaId]: true,
    }));
    // Se carga los comentarios de la reseña
    this.libroService.getComentariosResena(resenaId).subscribe({
      next: (comentarios) => {
        // Se actualiza el estado de los comentarios de la reseña
        this.comentariosPorResena.update((actual) => ({
          ...actual,
          [resenaId]: comentarios,
        }));
        // Se finaliza el proceso de carga de los comentarios de la reseña
        this.cargandoComentarios.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
      },
      error: () => {
        // Se finaliza el proceso de carga de los comentarios de la reseña
        this.cargandoComentarios.update((actual) => ({
          ...actual,
          [resenaId]: false,
        }));
        // Se muestra un error al cargar los comentarios de la reseña
        this.comentarioErrorPorResena.update((actual) => ({
          ...actual,
          [resenaId]: this.translate.instant('bookDetail.errorLoadComments'),
        }));
      },
    });
  }

  // Método para cargar el detalle del libro
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
        this.error.set(this.translate.instant('bookDetail.errorLoadBook'));
        this.loading.set(false);
      },
    });
  }

  // Método para cargar la reseña propia del libro
  private cargarMiResena(): void {
    if (!this.libroId) {
      return;
    }
    // Se carga la reseña propia del libro
    this.libroService.getMiResenaEnLibro(this.libroId).subscribe({
      next: (resena) => {
        this.miResena.set(resena);
        // Se actualiza el estado de la reseña propia del libro
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

  // Método para sincronizar el progreso de lectura del formulario
  private syncProgresoFormulario(det: LibroDetalleDTO): void {
    // Se obtiene el progreso de lectura del libro
    const p = det.miProgreso;
    if (!p) {
      this.progresoPagina = 0;
      this.progresoEstado = '';
      return;
    }
    // Se actualiza el progreso de lectura del formulario
    this.progresoPagina = p.paginaActual;
    this.progresoEstado = '';
  }

  // Método para aplicar el progreso de lectura en el detalle del libro
  private aplicarMiProgresoEnDetalle(actualizado: ProgresoLecturaDTO): void {
    this.detalle.update((d) => {
      if (!d) {
        return d;
      }
      return { ...d, miProgreso: actualizado };
    });
  }

  // Método para cargar las resenas del libro
  private cargarResenas(): void {
    if (!this.libroId) {
      return;
    }
    this.cargandoResenas.set(true);
    this.libroService.getResenasLibro(this.libroId, this.textoResenas.trim(), this.filtroResenas).subscribe({
      next: (page) => {
        this.resenas.set(page.content ?? []);
        this.cargandoResenas.set(false);
      },
      error: () => {
        this.cargandoResenas.set(false);
      },
    });
  }
}
