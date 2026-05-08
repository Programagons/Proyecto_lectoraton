import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { LibroService } from '../../core/libro/libro.service';

@Component({
  selector: 'app-agregar-libro',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslatePipe],
  templateUrl: './agregar-libro.html',
  styleUrl: './agregar-libro.css',
})
export class AgregarLibroPage implements OnInit {
  protected readonly loadingMeta = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected autores: { id: number; nombre_completo: string }[] = [];
  protected generos: { id: number; nombre: string }[] = [];
  protected tropos: { id: number; nombre: string }[] = [];

  protected isbn = '';
  protected titulo = '';
  protected sagaNombre = '';
  protected numeroSaga: number | null = null;
  protected sinopsis = '';
  protected autorId: number | null = null;
  protected numPaginas: number | null = null;
  protected fechaPublicacion = '';
  protected generoIds: number[] = [];
  protected tropoIds: number[] = [];
  protected portadaFile: File | null = null;

  constructor(
    private readonly libroService: LibroService,
    private readonly router: Router,
    private readonly translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.libroService.getAutores().subscribe({
      next: (page) => {
        this.autores = page.content.map((a) => ({ id: a.id, nombre_completo: a.nombre_completo }));
        this.loadingMeta.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('addBook.errorLoadAuthors'));
        this.loadingMeta.set(false);
      },
    });
    this.libroService.getGeneros().subscribe({
      next: (g) => (this.generos = g),
      error: () => {},
    });
    this.libroService.getTropos().subscribe({
      next: (t) => (this.tropos = t),
      error: () => {},
    });
  }

  protected onPortadaChange(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const f = input.files?.[0];
    this.portadaFile = f ?? null;
  }

  protected enviar(): void {
    this.error.set(null);
    const isbn = this.isbn.trim().replace(/[^0-9X]/gi, '');
    if (isbn.length !== 13) {
      this.error.set(this.translate.instant('addBook.errorIsbn'));
      return;
    }
    if (!this.titulo.trim()) {
      this.error.set(this.translate.instant('addBook.errorTitleRequired'));
      return;
    }
    if (this.autorId == null || this.autorId <= 0) {
      this.error.set(this.translate.instant('addBook.errorAuthorRequired'));
      return;
    }

    const fd = new FormData();
    fd.append('isbn', isbn);
    fd.append('titulo', this.titulo.trim());
    if (this.sagaNombre.trim()) {
      fd.append('sagaNombre', this.sagaNombre.trim());
    }
    if (this.numeroSaga != null && !Number.isNaN(this.numeroSaga)) {
      fd.append('numeroSaga', String(this.numeroSaga));
    }
    if (this.sinopsis.trim()) {
      fd.append('sinopsis', this.sinopsis.trim());
    }
    fd.append('autorId', String(this.autorId));
    if (this.numPaginas != null && this.numPaginas > 0) {
      fd.append('numPaginas', String(this.numPaginas));
    }
    if (this.fechaPublicacion.trim()) {
      fd.append('fechaPublicacion', this.fechaPublicacion.trim());
    }
    for (const gid of this.generoIds) {
      fd.append('generoIds', String(gid));
    }
    for (const tid of this.tropoIds) {
      fd.append('tropoIds', String(tid));
    }
    if (this.portadaFile) {
      fd.append('portadaFile', this.portadaFile);
    }

    this.enviando.set(true);
    this.libroService.crearLibro(fd).subscribe({
      next: (libro) => {
        this.enviando.set(false);
        void this.router.navigate(['/libros', libro.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        const body = err.error;
        if (typeof body === 'string') {
          this.error.set(body);
        } else if (body?.message) {
          this.error.set(String(body.message));
        } else {
          this.error.set(
            err.status === 403 ? this.translate.instant('addBook.errorNoPermission') : this.translate.instant('addBook.errorCreate'),
          );
        }
      },
    });
  }
}
