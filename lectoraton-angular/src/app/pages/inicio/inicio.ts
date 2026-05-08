import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { FeedItemDTO, FeedService } from '../../core/feed/feed.service';
import { LibroService, PageDTO, UltimoProgresoLibroDTO } from '../../core/libro/libro.service';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe, TranslatePipe],
  templateUrl: './inicio.html',
  styleUrl: './inicio.css',
})
export class InicioPage implements OnInit {
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly items = signal<FeedItemDTO[]>([]);
  protected readonly hayMas = signal(false);
  protected readonly cargandoMas = signal(false);
  protected readonly ultimoProgreso = signal<UltimoProgresoLibroDTO | null>(null);
  protected readonly cargandoUltimo = signal(true);
  protected readonly spoilersVisibles = signal<Record<number, boolean>>({});
  protected incluirPropias = true;
  private page = 0;
  private readonly pageSize = 20;

  constructor(
    private readonly feedService: FeedService,
    private readonly libroService: LibroService,
    private readonly translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.cargarUltimoProgreso();
    this.recargar();
  }

  protected etiquetaTipo(tipo: string): string {
    switch (tipo) {
      case 'RESENA':
        return 'inicio.type.review';
      case 'PROGRESO':
        return 'inicio.type.progress';
      case 'COMENTARIO':
        return 'inicio.type.comment';
      default:
        return tipo;
    }
  }

  protected clampPct(valor: number): number {
    return Math.min(100, Math.max(0, valor));
  }

  protected porcentajeFeed(item: FeedItemDTO): number | null {
    if (item.tipo !== 'PROGRESO' || !item.libroNumPaginas || item.libroNumPaginas <= 0) {
      return null;
    }
    const m = item.texto?.match(/pág\.\s*(\d+)/i);
    if (!m) {
      return null;
    }
    const pag = Number(m[1]);
    if (!Number.isFinite(pag)) {
      return null;
    }
    return Math.round((pag * 1000) / item.libroNumPaginas) / 10;
  }

  protected onCambiarFiltroPropias(): void {
    this.recargar();
  }

  protected toggleSpoiler(itemId: number): void {
    this.spoilersVisibles.update((actual) => ({
      ...actual,
      [itemId]: !actual[itemId],
    }));
  }

  protected spoilerVisible(itemId: number): boolean {
    return !!this.spoilersVisibles()[itemId];
  }

  protected cargarMas(): void {
    if (!this.hayMas() || this.cargandoMas()) {
      return;
    }
    this.cargandoMas.set(true);
    this.page += 1;
    this.feedService.getFeedMio(this.page, this.pageSize, this.incluirPropias).subscribe({
      next: (page) => {
        this.cargandoMas.set(false);
        const nuevos = page.content ?? [];
        this.items.update((act) => [...act, ...nuevos]);
        this.hayMas.set(this.hayMasTrasPagina(page));
      },
      error: () => {
        this.cargandoMas.set(false);
        this.page -= 1;
      },
    });
  }

  private hayMasTrasPagina(page: PageDTO<FeedItemDTO>): boolean {
    if (page.last !== undefined) {
      return !page.last;
    }
    return page.totalPages > 0 && page.number < page.totalPages - 1;
  }

  private cargarUltimoProgreso(): void {
    this.cargandoUltimo.set(true);
    this.libroService.getMiUltimoProgreso().subscribe({
      next: (u) => {
        this.ultimoProgreso.set(u);
        this.cargandoUltimo.set(false);
      },
      error: () => {
        this.ultimoProgreso.set(null);
        this.cargandoUltimo.set(false);
      },
    });
  }

  private recargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.page = 0;
    this.feedService.getFeedMio(0, this.pageSize, this.incluirPropias).subscribe({
      next: (page) => {
        this.items.set(page.content ?? []);
        this.hayMas.set(this.hayMasTrasPagina(page));
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('inicio.errorFeed'));
        this.loading.set(false);
      },
    });
  }
}
