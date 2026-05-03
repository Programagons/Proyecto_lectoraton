import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UsuarioComunidadDTO, UsuarioService } from '../../core/usuario/usuario.service';

@Component({
  selector: 'app-comunidad',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './comunidad.html',
  styleUrl: './comunidad.css',
})
export class ComunidadPage implements OnInit, OnDestroy {
  protected readonly tabActiva = signal<'buscar' | 'siguiendo'>('buscar');
  protected textoBusqueda = '';
  protected readonly buscando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly resultados = signal<UsuarioComunidadDTO[]>([]);
  protected readonly listaSiguiendo = signal<UsuarioComunidadDTO[]>([]);
  protected readonly cargandoSiguiendo = signal(false);
  protected readonly seguidos = signal<Set<number>>(new Set<number>());
  protected readonly actualizando = signal<Set<number>>(new Set<number>());

  private debounceId: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.usuarioService.getSeguidos().subscribe({
      next: (ids) => {
        this.seguidos.set(new Set(ids));
        this.cargarListaSiguiendo();
      },
    });
  }

  ngOnDestroy(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
  }

  protected onBusquedaChange(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    this.debounceId = setTimeout(() => {
      void this.buscar();
    }, 250);
  }

  protected cambiarTab(tab: 'buscar' | 'siguiendo'): void {
    this.tabActiva.set(tab);
    this.error.set(null);
    if (tab === 'siguiendo') {
      this.cargarListaSiguiendo();
    }
  }

  protected esSeguido(usuarioId: number): boolean {
    return this.seguidos().has(usuarioId);
  }

  protected estaActualizando(usuarioId: number): boolean {
    return this.actualizando().has(usuarioId);
  }

  protected toggleSeguir(usuario: UsuarioComunidadDTO): void {
    const usuarioId = usuario.id;
    const nextActualizando = new Set(this.actualizando());
    nextActualizando.add(usuarioId);
    this.actualizando.set(nextActualizando);

    const finalizar = () => {
      const s = new Set(this.actualizando());
      s.delete(usuarioId);
      this.actualizando.set(s);
    };

    if (this.esSeguido(usuarioId)) {
      this.usuarioService.dejarDeSeguirUsuario(usuarioId).subscribe({
        next: () => {
          const s = new Set(this.seguidos());
          s.delete(usuarioId);
          this.seguidos.set(s);
          this.listaSiguiendo.update((list) => list.filter((u) => u.id !== usuarioId));
          finalizar();
        },
        error: () => {
          this.error.set('No se pudo dejar de seguir al usuario.');
          finalizar();
        },
      });
      return;
    }

    this.usuarioService.seguirUsuario(usuarioId).subscribe({
      next: () => {
        const s = new Set(this.seguidos());
        s.add(usuarioId);
        this.seguidos.set(s);
        if (this.tabActiva() === 'siguiendo') {
          this.cargarListaSiguiendo();
        }
        finalizar();
      },
      error: (err: HttpErrorResponse) => {
        const body = err.error as string | { message?: string } | undefined;
        if (typeof body === 'string') {
          this.error.set(body);
        } else {
          this.error.set(body?.message || 'No se pudo seguir al usuario.');
        }
        finalizar();
      },
    });
  }

  private buscar(): void {
    const q = this.textoBusqueda.trim();
    this.error.set(null);
    if (q.length < 2) {
      this.resultados.set([]);
      this.buscando.set(false);
      return;
    }
    this.buscando.set(true);
    this.usuarioService.buscarUsuarios(q).subscribe({
      next: (list) => {
        this.resultados.set(list);
        this.buscando.set(false);
      },
      error: () => {
        this.error.set('No se pudo realizar la búsqueda.');
        this.buscando.set(false);
      },
    });
  }

  private cargarListaSiguiendo(): void {
    this.cargandoSiguiendo.set(true);
    this.usuarioService.getSeguidosDetalle().subscribe({
      next: (list) => {
        this.listaSiguiendo.set(list);
        this.cargandoSiguiendo.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de seguidos.');
        this.cargandoSiguiendo.set(false);
      },
    });
  }
}
