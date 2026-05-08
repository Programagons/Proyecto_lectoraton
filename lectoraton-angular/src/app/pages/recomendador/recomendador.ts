import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { LibroExplorarDTO, LibroMiniDTO, LibroRecomendacionDTO, LibroService } from '../../core/libro/libro.service';

@Component({
  selector: 'app-recomendador',
  standalone: true,
  imports: [FormsModule, RouterLink, TranslatePipe],
  templateUrl: './recomendador.html',
  styleUrl: './recomendador.css',
})
export class RecomendadorPage {
  protected busqueda = '';
  protected sugerencias = signal<LibroExplorarDTO[]>([]);
  protected cargandoSugerencias = signal(false);
  protected libroElegido = signal<LibroExplorarDTO | null>(null);
  protected cargandoReco = signal(false);
  protected errorReco = signal<string | null>(null);
  protected resultado = signal<LibroRecomendacionDTO | null>(null);

  /* Debounce id */
  private debounceId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly libroService: LibroService,
    private readonly translate: TranslateService,
  ) {}

  /* On busqueda input */
  protected onBusquedaInput(): void {
    /* Si el debounce id existe, se limpia */
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    /* Se obtiene la búsqueda */
    const q = this.busqueda.trim();
    /* Si la búsqueda es menor a 2 caracteres, se limpian las sugerencias */
    if (q.length < 2) {
      this.sugerencias.set([]);
      this.cargandoSugerencias.set(false);
      return;
    }
    /* Se establece el debounce id */
    this.debounceId = setTimeout(() => this.buscarPorTitulo(q), 300);
  }

  /* Buscar por título */
  private buscarPorTitulo(q: string): void {
    this.cargandoSugerencias.set(true);
    this.libroService.explorarLibros({ titulo: q, page: 0, size: 20, sort: 'titulo,asc' }).subscribe({
      next: (page) => {
        /* Se establecen las sugerencias */
        this.sugerencias.set(page.content ?? []);
        /* Se establece el cargando sugerencias a false */
        this.cargandoSugerencias.set(false);
      },
      error: () => {
        /* Se limpian las sugerencias */
        this.sugerencias.set([]);
        /* Se establece el cargando sugerencias a false */
        this.cargandoSugerencias.set(false);
      },
    });
  }

  /* Elegir libro */
  protected elegirLibro(l: LibroExplorarDTO, event?: Event): void {
    event?.preventDefault();
    /* Se establece el libro elegido */
    this.libroElegido.set(l);
    /* Se limpian las sugerencias */
    this.sugerencias.set([]);
    this.busqueda = l.titulo ?? '';
  }

  /* Quitar selección */
  protected quitarSeleccion(): void {
    /* Se establece el libro elegido a null */
    this.libroElegido.set(null);
    /* Se limpian las sugerencias */
    this.busqueda = '';
    this.sugerencias.set([]);
    this.resultado.set(null);
    this.errorReco.set(null);
  }

  /* Pedir recomendación */
  protected pedirRecomendacion(): void {
    /* Se obtiene el id del libro elegido */
    const id = this.libroElegido()?.id;
    if (id == null) {
      return;
    }
    /* Se establece el cargando recomendación a true */
    this.cargandoReco.set(true);
    this.errorReco.set(null);
    this.resultado.set(null);
    /* Se obtiene la recomendación */
    this.libroService.getRecomendacionLibro(id).subscribe({
      next: (dto) => {
        this.resultado.set(dto);
        this.cargandoReco.set(false);
      },
      error: () => {
        this.errorReco.set(this.translate.instant('recommender.errorFetch'));
        this.cargandoReco.set(false);
      },
    });
  }

  
  protected portadaReco(r: LibroMiniDTO): string | null {
    return this.normalizarPortada(r.portada);
  }

  protected portadaExplorar(l: LibroExplorarDTO): string | null {
    return this.normalizarPortada(l.portada);
  }

  protected iniciales(titulo: string | undefined): string {
    const t = (titulo ?? '').trim();
    if (!t) return '?';
    return t.slice(0, 2).toUpperCase();
  }

  private normalizarPortada(portada: string | null | undefined): string | null {
    const p = portada?.trim();
    return p ? p : null;
  }
}
