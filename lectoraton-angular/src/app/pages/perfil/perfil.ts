import { Component, OnInit, computed, signal } from '@angular/core';
import { UsuarioPerfilDTO, UsuarioService } from '../../core/usuario/usuario.service';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class PerfilPage implements OnInit {
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly perfil = signal<UsuarioPerfilDTO | null>(null);
  protected readonly editandoBio = signal(false);
  protected readonly guardandoBio = signal(false);
  protected readonly bioError = signal<string | null>(null);
  protected readonly bioOk = signal<string | null>(null);
  protected bioTemporal = '';

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

  constructor(private readonly usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.usuarioService.getMiPerfil().subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.bioTemporal = perfil.bio || '';
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar tu perfil.');
        this.loading.set(false);
      },
    });
  }

  protected iniciarEdicionBio(): void {
    this.bioError.set(null);
    this.bioOk.set(null);
    this.bioTemporal = this.perfil()?.bio || '';
    this.editandoBio.set(true);
  }

  protected cancelarEdicionBio(): void {
    this.editandoBio.set(false);
    this.bioTemporal = this.perfil()?.bio || '';
  }

  protected guardarBio(): void {
    this.guardandoBio.set(true);
    this.bioError.set(null);
    this.bioOk.set(null);
    this.usuarioService.updateMiBio(this.bioTemporal).subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.editandoBio.set(false);
        this.guardandoBio.set(false);
        this.bioOk.set('Biografía actualizada.');
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoBio.set(false);
        const body = err.error as string | { message?: string } | undefined;
        this.bioError.set(typeof body === 'string' ? body : body?.message || 'No se pudo actualizar la biografía.');
      },
    });
  }

  protected solicitarEditor(): void {
    this.solicitudMsg.set(null);
    this.solicitudError.set(false);
    this.solicitudEnviando.set(true);
    this.usuarioService.solicitarSerEditor().subscribe({
      next: (msg) => {
        this.solicitudEnviando.set(false);
        this.solicitudError.set(false);
        this.solicitudMsg.set(msg || 'Solicitud enviada.');
      },
      error: (err: HttpErrorResponse) => {
        this.solicitudEnviando.set(false);
        this.solicitudError.set(true);
        const body = err.error;
        const msg =
          typeof body === 'string' && body.trim()
            ? body
            : 'No se pudo enviar la solicitud. Configura MAIL_USER y MAIL_PASS en el backend (Gmail: contraseña de aplicación).';
        this.solicitudMsg.set(msg);
      },
    });
  }
}
