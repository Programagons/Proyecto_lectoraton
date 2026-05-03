import { AfterViewInit, Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import Carousel from 'bootstrap/js/dist/carousel';
import { forkJoin } from 'rxjs';
import {
  GeneroDTO,
  LibroExplorarDTO,
  LibroService,
  TropoDTO,
} from '../../core/libro/libro.service';

@Component({
  selector: 'app-explorar',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './explorar.html',
  styleUrl: './explorar.css',
})
export class ExplorarPage implements OnInit, OnDestroy, AfterViewInit {
  protected titulo = '';
  protected autor = '';
  protected saga = '';
  protected generoId: number | null = null;
  protected tropoId: number | null = null;
  protected orden = 'titulo,asc';

  protected readonly generos = signal<GeneroDTO[]>([]);
  protected readonly tropos = signal<TropoDTO[]>([]);
  protected readonly libros = signal<LibroExplorarDTO[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadingMore = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly paginaActual = signal(0);
  protected readonly totalPaginas = signal(0);

  protected readonly novedades = signal<LibroExplorarDTO[]>([]);
  protected readonly masLeidos = signal<LibroExplorarDTO[]>([]);
  protected readonly loadingCarruseles = signal(true);

  protected readonly slidesNovedades = computed(() => this.partirEnSlides(this.novedades()));
  protected readonly slidesMasLeidos = computed(() => this.partirEnSlides(this.masLeidos()));

  private readonly librosPorSlide = 4;
  private debounceId: ReturnType<typeof setTimeout> | null = null;
  private instanciaNovedades?: Carousel;
  private instanciaMasLeidos?: Carousel;

  constructor(private readonly libroService: LibroService) {}

  ngOnInit(): void {
    this.libroService.getGeneros().subscribe({ next: (items) => this.generos.set(items) });
    this.libroService.getTropos().subscribe({ next: (items) => this.tropos.set(items) });

    forkJoin({
      nov: this.libroService.getNovedades(12),
      mas: this.libroService.getMasLeidos(12),
    }).subscribe({
      next: ({ nov, mas }) => {
        this.novedades.set(nov ?? []);
        this.masLeidos.set(mas ?? []);
        this.loadingCarruseles.set(false);
        queueMicrotask(() => this.inicializarCarouselesBootstrap());
      },
      error: () => {
        this.novedades.set([]);
        this.masLeidos.set([]);
        this.loadingCarruseles.set(false);
      },
    });

    this.buscar(true);
  }

  ngAfterViewInit(): void {
    queueMicrotask(() => this.inicializarCarouselesBootstrap());
  }

  ngOnDestroy(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    this.instanciaNovedades?.dispose();
    this.instanciaMasLeidos?.dispose();
  }

  protected limpiarFiltros(): void {
    this.titulo = '';
    this.autor = '';
    this.saga = '';
    this.generoId = null;
    this.tropoId = null;
    this.orden = 'titulo,asc';
    this.buscar(true);
  }

  protected onFiltroTextoChange(): void {
    if (this.debounceId) {
      clearTimeout(this.debounceId);
    }
    this.debounceId = setTimeout(() => this.buscar(true), 260);
  }

  protected buscar(reiniciar: boolean): void {
    const page = reiniciar ? 0 : this.paginaActual() + 1;
    if (reiniciar) {
      this.loading.set(true);
      this.error.set(null);
    } else {
      this.loadingMore.set(true);
    }
    this.libroService
      .explorarLibros({
        titulo: this.titulo.trim(),
        autor: this.autor.trim(),
        saga: this.saga.trim(),
        generoId: this.generoId,
        tropoId: this.tropoId,
        sort: this.orden,
        page,
        size: 16,
      })
      .subscribe({
        next: (res) => {
          this.paginaActual.set(res.number ?? 0);
          this.totalPaginas.set(res.totalPages ?? 0);
          if (reiniciar) {
            this.libros.set(res.content || []);
          } else {
            this.libros.set([...(this.libros() || []), ...((res.content as LibroExplorarDTO[]) || [])]);
          }
          this.loading.set(false);
          this.loadingMore.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar la exploración.');
          this.loading.set(false);
          this.loadingMore.set(false);
        },
      });
  }

  protected quedanMasResultados(): boolean {
    return this.paginaActual() + 1 < this.totalPaginas();
  }

  private partirEnSlides(items: LibroExplorarDTO[]): LibroExplorarDTO[][] {
    const size = this.librosPorSlide;
    const bloques: LibroExplorarDTO[][] = [];
    for (let i = 0; i < items.length; i += size) {
      bloques.push(items.slice(i, i + size));
    }
    return bloques;
  }

  private inicializarCarouselesBootstrap(): void {
    const elN = document.getElementById('carouselNovedadesExplorar');
    if (elN && this.slidesNovedades().length > 0) {
      this.instanciaNovedades?.dispose();
      this.instanciaNovedades = Carousel.getOrCreateInstance(elN, { interval: false });
    }
    const elM = document.getElementById('carouselMasLeidosExplorar');
    if (elM && this.slidesMasLeidos().length > 0) {
      this.instanciaMasLeidos?.dispose();
      this.instanciaMasLeidos = Carousel.getOrCreateInstance(elM, { interval: false });
    }
  }
}
