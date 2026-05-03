import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LibroExplorarDTO, LibroMiniDTO, LibroRecomendacionDTO, LibroService } from '../../core/libro/libro.service';

@Component({
  selector: 'app-recomendador',
  standalone: true,
  imports: [FormsModule, RouterLink],
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

  private debounceId: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly libroService: LibroService) {}

  protected onBusquedaInput(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    const q = this.busqueda.trim();
    if (q.length < 2) {
      this.sugerencias.set([]);
      this.cargandoSugerencias.set(false);
      return;
    }
    this.debounceId = setTimeout(() => this.buscarPorTitulo(q), 300);
  }

  private buscarPorTitulo(q: string): void {
    this.cargandoSugerencias.set(true);
    this.libroService.explorarLibros({ titulo: q, page: 0, size: 20, sort: 'titulo,asc' }).subscribe({
      next: (page) => {
        this.sugerencias.set(page.content ?? []);
        this.cargandoSugerencias.set(false);
      },
      error: () => {
        this.sugerencias.set([]);
        this.cargandoSugerencias.set(false);
      },
    });
  }

  protected elegirLibro(l: LibroExplorarDTO, event?: Event): void {
    event?.preventDefault();
    this.libroElegido.set(l);
    this.sugerencias.set([]);
    this.busqueda = l.titulo ?? '';
  }

  protected quitarSeleccion(): void {
    this.libroElegido.set(null);
    this.busqueda = '';
    this.sugerencias.set([]);
    this.resultado.set(null);
    this.errorReco.set(null);
  }

  protected pedirRecomendacion(): void {
    const id = this.libroElegido()?.id;
    if (id == null) {
      return;
    }
    this.cargandoReco.set(true);
    this.errorReco.set(null);
    this.resultado.set(null);
    this.libroService.getRecomendacionLibro(id).subscribe({
      next: (dto) => {
        this.resultado.set(dto);
        this.cargandoReco.set(false);
      },
      error: () => {
        this.errorReco.set('No se pudo obtener una recomendación. Prueba otro libro.');
        this.cargandoReco.set(false);
      },
    });
  }

  protected portadaReco(r: LibroMiniDTO): string | null {
    const p = r.portada?.trim();
    return p ? p : null;
  }

  /** Portada en listados de explorar (miniaturas en sugerencias). */
  protected portadaExplorar(l: LibroExplorarDTO): string | null {
    const p = l.portada?.trim();
    return p ? p : null;
  }

  protected iniciales(titulo: string | undefined): string {
    const t = (titulo ?? '').trim();
    if (!t) return '?';
    return t.slice(0, 2).toUpperCase();
  }
}
