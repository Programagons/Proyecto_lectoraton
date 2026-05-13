import { Component, OnInit, computed, signal } from '@angular/core';
import { UsuarioPerfilDTO, UsuarioService } from '../../core/usuario/usuario.service';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { environment } from '../../../environments/environments';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})

export class PerfilPage implements OnInit {
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly perfil = signal<UsuarioPerfilDTO | null>(null);
  protected readonly editandoBio = signal(false);
  protected readonly guardandoBio = signal(false);
  protected readonly subiendoIcono = signal(false);
  protected readonly bioError = signal<string | null>(null);
  protected readonly bioOk = signal<string | null>(null);
  protected readonly iconoError = signal<string | null>(null);
  protected bioTemporal = '';
  protected iconoTemporal: File | null = null;
  private readonly publicBaseUrl = environment.apiUrl.replace(/\/api\/?$/, '');

  protected readonly solicitudEnviando = signal(false);
  protected readonly solicitudMsg = signal<string | null>(null);
  protected readonly solicitudError = signal(false);

  protected readonly mostrarSolicitudEditor = computed(() => {
    const p = this.perfil();
    if (!p?.rolesEtiqueta?.length) {
      return true;
    }
    const r = p.rolesEtiqueta;
    return !r.includes('Editor') && !r.includes('Administrador');
  });

  constructor(
    private readonly usuarioService: UsuarioService,
    private readonly translate: TranslateService,
  ) {}

  /* Inicialización de la página */
  ngOnInit(): void {
    this.usuarioService.getMiPerfil().subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.bioTemporal = perfil.bio ?? '';
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('profile.errorLoad'));
        this.loading.set(false);
      },
    });
  }

  /* Método para iniciar la edición de la biografía */
  protected iniciarEdicionBio(): void {
    this.bioError.set(null);
    this.bioOk.set(null);
    this.bioTemporal = this.perfil()?.bio ?? '';
    this.editandoBio.set(true);
  }

  /* Método para cancelar la edición de la biografía */
  protected cancelarEdicionBio(): void {
    this.editandoBio.set(false);
    this.bioTemporal = this.perfil()?.bio ?? '';
  }

  /* Método para guardar la biografía */
  protected guardarBio(): void {
    this.guardandoBio.set(true);
    this.bioError.set(null);
    this.bioOk.set(null);
    this.usuarioService.updateMiBio(this.bioTemporal).subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.editandoBio.set(false);
        this.guardandoBio.set(false);
        this.bioOk.set(this.translate.instant('profile.bioUpdated'));
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoBio.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.bioError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('profile.errorBioUpdate'));
      },
    });
  }

  /* Método para seleccionar el icono */
  protected onIconoSeleccionado(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    this.iconoTemporal = input.files?.[0] ?? null;
    this.iconoError.set(null);
  }

  protected iconoUrl(icono: string | null | undefined): string {
    if (!icono) {
      return '';
    }
    if (icono.startsWith('http://') || icono.startsWith('https://')) {
      return icono;
    }
    if (icono.startsWith('/uploads/')) {
      return `${this.publicBaseUrl}${icono}`;
    }
    return icono;
  }

  /* Método para subir el icono */
  protected subirIcono(): void {
    if (!this.iconoTemporal) {
      this.iconoError.set(this.translate.instant('profile.selectImageFirst'));
      return;
    }
    this.subiendoIcono.set(true);
    this.iconoError.set(null);
    this.bioOk.set(null);
    this.usuarioService.updateMiIcono(this.iconoTemporal).subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.subiendoIcono.set(false);
        this.iconoTemporal = null;
        this.bioOk.set(this.translate.instant('profile.photoUpdated'));
      },
      error: (err: HttpErrorResponse) => {
        this.subiendoIcono.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.iconoError.set(typeof body === 'string' ? body : body?.message || this.translate.instant('profile.errorPhotoUpdate'));
      },
    });
  }

  /* Método para solicitar ser editor */
  protected solicitarEditor(): void {
    this.solicitudMsg.set(null);
    this.solicitudError.set(false);
    this.solicitudEnviando.set(true);
    this.usuarioService.solicitarSerEditor().subscribe({
      next: (msg) => {
        this.solicitudEnviando.set(false);
        this.solicitudError.set(false);
        this.solicitudMsg.set(msg || this.translate.instant('profile.requestSent'));
      },
      error: (err: HttpErrorResponse) => {
        this.solicitudEnviando.set(false);
        this.solicitudError.set(true);
        const body = err.error;
        const msg =
          typeof body === 'string' && body.trim()
            ? body
            : this.translate.instant('profile.errorRequestEditor');
        this.solicitudMsg.set(msg);
      },
    });
  }
}
