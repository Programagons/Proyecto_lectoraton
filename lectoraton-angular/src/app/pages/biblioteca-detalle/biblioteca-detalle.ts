import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { BibliotecaDTO, BibliotecaService, LibroBibliotecaDTO } from '../../core/biblioteca/biblioteca.service';

@Component({
  selector: 'app-biblioteca-detalle',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './biblioteca-detalle.html',
  styleUrl: './biblioteca-detalle.css',
})
export class BibliotecaDetallePage implements OnInit {
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly libros = signal<LibroBibliotecaDTO[]>([]);
  protected readonly nombreBiblioteca = signal('Biblioteca');
  protected readonly bibliotecas = signal<BibliotecaDTO[]>([]);
  protected readonly bibliotecaActualId = signal<number | null>(null);
  protected readonly showSelector = signal(false);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bibliotecaService: BibliotecaService,
    private readonly translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.bibliotecaService.getMisBibliotecas().subscribe({
      next: (bibliotecas) => {
        this.bibliotecas.set(bibliotecas);
        const actualId = this.bibliotecaActualId();
        if (actualId != null) {
          const biblioteca = bibliotecas.find((b) => b.id === actualId);
          if (biblioteca?.nombre) {
            this.nombreBiblioteca.set(biblioteca.nombre);
          }
        }
      },
    });

    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      const bibliotecaId = Number(idParam);
      if (!idParam || Number.isNaN(bibliotecaId)) {
        this.error.set(this.translate.instant('libraryDetail.errorInvalid'));
        this.loading.set(false);
        return;
      }
      this.bibliotecaActualId.set(bibliotecaId);
      this.cargarBiblioteca(bibliotecaId);
    });
  }

  protected abrirSelector(): void {
    this.showSelector.set(true);
  }

  protected cerrarSelector(): void {
    this.showSelector.set(false);
  }

  private cargarBiblioteca(bibliotecaId: number): void {
    this.loading.set(true);
    this.error.set(null);
    const biblioteca = this.bibliotecas().find((b) => b.id === bibliotecaId);
    if (biblioteca?.nombre) {
      this.nombreBiblioteca.set(biblioteca.nombre);
    }
    this.bibliotecaService.getLibrosDeBiblioteca(bibliotecaId).subscribe({
      next: (items) => {
        this.libros.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('libraryDetail.errorLoad'));
        this.loading.set(false);
      },
    });
  }
}
