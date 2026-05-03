import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { environment, googleOAuthAuthorizeUrl } from '../../../environments/environments';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginPage implements OnInit {
  protected nombre = '';
  protected apellidos = '';
  protected email = '';
  protected username = '';
  protected password = '';
  protected confirmPassword = '';
  protected rememberMe = true;
  protected showPassword = signal(false);
  protected submitting = signal(false);
  protected errorMessage = signal<string | null>(null);
  protected successMessage = signal<string | null>(null);
  /** El botón Google solo se muestra si el backend expone OAuth (GET /api/v1/oauth/google-enabled). */
  protected readonly googleOAuthConfigured = signal(false);
  protected readonly googleHref = googleOAuthAuthorizeUrl(environment.apiUrl);

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly http: HttpClient,
  ) {}

  ngOnInit(): void {
    const hash = typeof window !== 'undefined' ? (window.location.hash ?? '') : '';
    if (hash.startsWith('#token=')) {
      const rawToken = decodeURIComponent(hash.substring('#token='.length));
      this.auth.applyBackendJwt(rawToken, true);
      window.history.replaceState(null, '', window.location.pathname + window.location.search);
      void this.router.navigate(['/inicio']);
      return;
    }

    const qErr = this.route.snapshot.queryParamMap.get('oauthError');
    if (qErr === 'missing_email') {
      this.errorMessage.set('Tu cuenta Google no proporciona email público.');
    } else if (qErr === 'email_unverified') {
      this.errorMessage.set('Verifica tu correo en Google antes de continuar.');
    } else if (qErr === 'provision_failed') {
      this.errorMessage.set('No se pudo crear o actualizar tu usuario. Inténtalo de nuevo.');
    } else if (qErr === 'oauth_failed' || qErr === 'authentication_failed') {
      this.errorMessage.set('Google ha rechazado o cancelado el acceso.');
    } else if (qErr === 'no_oauth_principal') {
      this.errorMessage.set('Error interno tras el login con Google.');
    }

    if (this.isRegisterMode()) {
      return;
    }

    this.http.get<{ googleEnabled: boolean }>(`${environment.apiUrl}/v1/oauth/google-enabled`).subscribe({
      next: (r) => this.googleOAuthConfigured.set(!!r?.googleEnabled),
      error: () => this.googleOAuthConfigured.set(false),
    });
  }

  togglePassword(): void {
    this.showPassword.update((v) => !v);
  }

  protected isRegisterMode(): boolean {
    return this.router.url.startsWith('/registro');
  }

  onSubmit(): void {
    if (this.isRegisterMode()) {
      this.onRegister();
      return;
    }

    const u = this.username.trim();
    if (!u || !this.password) {
      this.errorMessage.set('Introduce usuario y contraseña.');
      return;
    }
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.submitting.set(true);
    this.auth.login(u, this.password, this.rememberMe).subscribe({
      next: (res) => {
        this.submitting.set(false);
        if (!res.token) {
          this.errorMessage.set(res.message || 'No se ha recibido token.');
          return;
        }
        void this.router.navigate(['/inicio']);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        const body = err.error as { message?: string } | undefined;
        if (err.status === 0) {
          this.errorMessage.set('No hay conexión con el servidor. ¿Está arrancado el backend?');
          return;
        }
        this.errorMessage.set(body?.message || 'No se pudo iniciar sesión.');
      },
    });
  }

  private onRegister(): void {
    const username = this.username.trim();
    const nombre = this.nombre.trim();
    const apellidos = this.apellidos.trim();
    const email = this.email.trim().toLowerCase();

    if (!username || !this.password || !nombre || !apellidos || !email) {
      this.errorMessage.set('Completa todos los campos obligatorios.');
      return;
    }
    if (this.password.length < 8) {
      this.errorMessage.set('La contraseña debe tener al menos 8 caracteres.');
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage.set('Las contraseñas no coinciden.');
      return;
    }

    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.submitting.set(true);

    this.auth
      .register({
        username,
        password: this.password,
        nombre,
        apellidos,
        email,
      })
      .subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.successMessage.set(res.message || 'Registro completado. Ya puedes iniciar sesión.');
          void this.router.navigate(['/']);
        },
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = err.error as { message?: string } | undefined;
          if (err.status === 0) {
            this.errorMessage.set('No hay conexión con el servidor. ¿Está arrancado el backend?');
            return;
          }
          this.errorMessage.set(body?.message || 'No se pudo completar el registro.');
        },
      });
  }
}
