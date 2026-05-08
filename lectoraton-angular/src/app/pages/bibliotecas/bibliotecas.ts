import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { BibliotecaDTO, BibliotecaService } from '../../core/biblioteca/biblioteca.service';

@Component({
  selector: 'app-bibliotecas',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslatePipe],
  templateUrl: './bibliotecas.html',
  styleUrl: './bibliotecas.css',
})
export class BibliotecasPage implements OnInit {
  protected readonly librosPorEstante = 4;
  protected readonly bibliotecasFijas = ['Leyendo', 'Leído', 'Por Leer'];

  protected readonly items = signal<BibliotecaDTO[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly creating = signal(false);
  protected readonly renaming = signal(false);
  protected readonly deleting = signal(false);
  protected readonly showCreateModal = signal(false);
  protected readonly showRenameModal = signal(false);
  protected readonly showDeleteModal = signal(false);
  protected readonly createError = signal<string | null>(null);
  protected readonly renameError = signal<string | null>(null);
  protected readonly deleteError = signal<string | null>(null);
  protected bibliotecaSeleccionada: BibliotecaDTO | null = null;
  protected nombreNuevaBiblioteca = '';
  protected nombreEditarBiblioteca = '';

  /* Constructor */
  constructor(
    private readonly bibliotecaService: BibliotecaService,
    private readonly translate: TranslateService,
  ) {}

  /* On init */
  ngOnInit(): void {
    this.cargarBibliotecas();
  }

  /* Abrir modal crear */
  protected abrirModalCrear(): void {
    this.nombreNuevaBiblioteca = '';
    this.createError.set(null);
    this.showCreateModal.set(true);
  }

  /* Cerrar modal crear */
  protected cerrarModalCrear(): void {
    if (this.creating()) {
      return;
    }
    this.showCreateModal.set(false);
  }

  /* Crear biblioteca */
  protected crearBiblioteca(): void {
    const nombre = this.nombreNuevaBiblioteca.trim();
    if (!nombre) {
      this.createError.set(this.translate.instant('libraries.errorEnterName'));
      return;
    }
    this.createError.set(null);
    this.creating.set(true);
    /* Crear biblioteca */
    this.bibliotecaService.createBiblioteca({ nombre }).subscribe({
      next: () => {
        this.creating.set(false);
        this.showCreateModal.set(false);
        this.cargarBibliotecas();
        /* Cargar bibliotecas */
      },
      error: (err: HttpErrorResponse) => {
        this.creating.set(false);
        const body = err.error as { message?: string } | string | undefined;
        if (typeof body === 'string') {
          this.createError.set(body);
          return;
        }
        this.createError.set(body?.message || this.translate.instant('libraries.errorCreate'));
      },
    });
  }

  /* Abrir modal renombrar */
  protected abrirModalRenombrar(b: BibliotecaDTO, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.bibliotecaSeleccionada = b;
    this.nombreEditarBiblioteca = b.nombre;
    this.renameError.set(null);
    this.showRenameModal.set(true);
  }

  /* Cerrar modal renombrar */
  protected cerrarModalRenombrar(): void {
    if (this.renaming()) {
      return;
    }
    this.showRenameModal.set(false);
  }

  /* Renombrar biblioteca */  
  protected renombrarBiblioteca(): void {
    const b = this.bibliotecaSeleccionada;
    if (!b) {
      return;
    }
    const nombre = this.nombreEditarBiblioteca.trim();
    if (!nombre) {
      this.renameError.set(this.translate.instant('libraries.errorEnterValidName'));
      return;
    }
    /* Renombrar biblioteca */
    this.renaming.set(true);
    this.renameError.set(null);
    this.bibliotecaService.renameBiblioteca(b.id, { nombre }).subscribe({
      /* Renombrar biblioteca */
      next: () => {
        this.renaming.set(false);
        this.showRenameModal.set(false);
        this.cargarBibliotecas();
      },
      error: (err: HttpErrorResponse) => {
        this.renaming.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.renameError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('libraries.errorRename'));
      },
    });
  }

  /* Abrir modal eliminar */
  protected abrirModalEliminar(b: BibliotecaDTO, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.bibliotecaSeleccionada = b;
    this.deleteError.set(null);
    this.showDeleteModal.set(true);
  }

  /* Es biblioteca fija */
  protected esBibliotecaFija(nombre: string): boolean {
    return this.bibliotecasFijas.some((fija) => fija.toLowerCase() === nombre.toLowerCase());
  }

  /* Cerrar modal eliminar */
  protected cerrarModalEliminar(): void {
    if (this.deleting()) {
      return;
    }
    this.showDeleteModal.set(false);
  }

  /* Eliminar biblioteca */
  protected eliminarBiblioteca(): void {
    const b = this.bibliotecaSeleccionada;
    if (!b) {
      return;
    }
    /* Eliminar biblioteca */
    this.deleting.set(true);
    this.deleteError.set(null);
    this.bibliotecaService.deleteBiblioteca(b.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.showDeleteModal.set(false);
        this.cargarBibliotecas();
      },
      error: (err: HttpErrorResponse) => {
        this.deleting.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.deleteError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('libraries.errorDelete'));
      },
    });
  }

  /* Filas estante */
  protected filasEstante(): BibliotecaDTO[][] {
    const list = this.items();
    const n = this.librosPorEstante;
    const rows: BibliotecaDTO[][] = [];
    for (let i = 0; i < list.length; i += n) {
      rows.push(list.slice(i, i + n));
    }
    return rows;
  }

  /* Cargar bibliotecas */
  private cargarBibliotecas(): void {
    this.loading.set(true);
    this.error.set(null);
    this.bibliotecaService.getMisBibliotecas().subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('libraries.errorLoad'));
        this.loading.set(false);
      },
    });
  }
}
