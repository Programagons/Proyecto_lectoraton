import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { UsuarioComunidadDTO, UsuarioService } from '../../core/usuario/usuario.service';

@Component({
  selector: 'app-comunidad',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
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

  constructor(
    private readonly usuarioService: UsuarioService,
    private readonly translate: TranslateService,
  ) {}


  /* On init */
  ngOnInit(): void {
    /* Se obtienen los seguidos */
    this.usuarioService.getSeguidos().subscribe({
      next: (ids) => {
        this.seguidos.set(new Set(ids));
        this.cargarListaSiguiendo();
      },
    });
  }

  /* On destroy */
  ngOnDestroy(): void {
    /* Se limpia el debounce id */
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
  }

  /* On busqueda change */
  protected onBusquedaChange(): void {
    /* Se limpia el debounce id */
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    /* Se establece el debounce id */
    this.debounceId = setTimeout(() => {
      void this.buscar();
    }, 250);
  }

  /* Cambiar tab */
  protected cambiarTab(tab: 'buscar' | 'siguiendo'): void {
    /* Se establece la tab activa */
    this.tabActiva.set(tab);
    this.error.set(null);
    if (tab === 'siguiendo') {
      this.cargarListaSiguiendo();
    }
  }

  /* Es seguido */
  protected esSeguido(usuarioId: number): boolean {
    /* Se devuelve si el usuario está seguido */
    return this.seguidos().has(usuarioId);
  }

  /* Esta actualizando */
  protected estaActualizando(usuarioId: number): boolean {
    /* Se devuelve si el usuario está actualizando */
    return this.actualizando().has(usuarioId);
  }

  /* Toggle seguir */
  protected toggleSeguir(usuario: UsuarioComunidadDTO): void {
    /* Se obtiene el id del usuario */
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
          this.error.set(this.translate.instant('community.errorUnfollow'));
          finalizar();
        },
      });
      return;
    }

    /* Se sigue el usuario */
    this.usuarioService.seguirUsuario(usuarioId).subscribe({
      next: () => {
        /* Se actualiza el set de seguidos */
        const s = new Set(this.seguidos());
        s.add(usuarioId);
        this.seguidos.set(s);
        /* Si la tab activa es siguiendo, se cargan los seguidos */
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
          this.error.set(body?.message || this.translate.instant('community.errorFollow'));
        }
        finalizar();
      },
    });
  }

  /* Buscar usuarios */
  private buscar(): void {
    /* Se obtiene la búsqueda */
    const q = this.textoBusqueda.trim();
    this.error.set(null);
    /* Si la búsqueda es menor a 2 caracteres, se limpian los resultados */
    if (q.length < 2) {
      this.resultados.set([]);
      this.buscando.set(false);
      return;
    }
    /* Se establece el cargando buscando a true */
    this.buscando.set(true);
    this.usuarioService.buscarUsuarios(q).subscribe({
      next: (list) => {
        /* Se establecen los resultados */
        this.resultados.set(list);
        this.buscando.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('community.errorSearch'));
        this.buscando.set(false);
      },
    });
  }

  /* Cargar lista de seguidos */
  private cargarListaSiguiendo(): void {
    /* Se establece el cargando siguiendo a true */
    this.cargandoSiguiendo.set(true);
    this.usuarioService.getSeguidosDetalle().subscribe({
      next: (list) => {
        this.listaSiguiendo.set(list);
        this.cargandoSiguiendo.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('community.errorLoadFollowing'));
        this.cargandoSiguiendo.set(false);
      },
    });
  }
}
