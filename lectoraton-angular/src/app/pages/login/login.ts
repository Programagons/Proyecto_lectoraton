import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/auth/auth.service';
import { environment, googleOAuthAuthorizeUrl } from '../../../environments/environments';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, TranslatePipe],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
/* Página de login */
export class LoginPage implements OnInit {
  protected nombre = '';
  protected apellidos = '';
  protected email = '';
  protected username = '';
  protected password = '';
  protected confirmPassword = '';
  protected rememberMe = true;
  /* Señal para mostrar/ocultar la contraseña */
  protected showPassword = signal(false);
  /* Señal para indicar si se está enviando el formulario */
  protected submitting = signal(false);
  /* Señal para mostrar el mensaje de error */
  protected errorMessage = signal<string | null>(null);
  /* Señal para mostrar el mensaje de éxito */
  protected successMessage = signal<string | null>(null);
  /* Señal para indicar si se ha configurado OAuth */
  protected readonly googleOAuthConfigured = signal(false);
  protected readonly googleHref = googleOAuthAuthorizeUrl(environment.apiUrl);

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly http: HttpClient,
    private readonly translate: TranslateService,
  ) {}

  /* Inicialización de la página */
  ngOnInit(): void {
    const hash = typeof window !== 'undefined' ? (window.location.hash ?? '') : '';
    if (hash.startsWith('#token=')) {
      /* Si el hash comienza con #token=, se extrae el token y se aplica al backend */
      const rawToken = decodeURIComponent(hash.substring('#token='.length));
      this.auth.applyBackendJwt(rawToken, true);
      window.history.replaceState(null, '', window.location.pathname + window.location.search);
      /* Se redirige a la página de inicio */
      void this.router.navigate(['/inicio']);
      return;
    }

    /* Se obtiene el error de OAuth */
    const qErr = this.route.snapshot.queryParamMap.get('oauthError');
    if (qErr === 'missing_email') {
      this.errorMessage.set(this.translate.instant('auth.oauthMissingEmail'));
    } else if (qErr === 'email_unverified') {
      this.errorMessage.set(this.translate.instant('auth.oauthEmailUnverified'));
    } else if (qErr === 'provision_failed') {
      this.errorMessage.set(this.translate.instant('auth.oauthProvisionFailed'));
    } else if (qErr === 'oauth_failed' || qErr === 'authentication_failed') {
      this.errorMessage.set(this.translate.instant('auth.oauthRejected'));
    } else if (qErr === 'no_oauth_principal') {
      this.errorMessage.set(this.translate.instant('auth.oauthInternalError'));
    }

    /* Si se está registrando, se devuelve */
    if (this.isRegisterMode()) {
      return;
    }

    /* Se obtiene la configuración de OAuth */
    this.http.get<{ googleEnabled: boolean }>(`${environment.apiUrl}/v1/oauth/google-enabled`).subscribe({
      next: (r) => this.googleOAuthConfigured.set(!!r?.googleEnabled),
      error: () => this.googleOAuthConfigured.set(false),
    });
  }

  /* Método para mostrar/ocultar la contraseña */
  togglePassword(): void {
    this.showPassword.update((v) => !v);
  }

  /* Método para verificar si se está registrando */
  protected isRegisterMode(): boolean {
    return this.router.url.startsWith('/registro');
  }

  /* Método para enviar el formulario */
  onSubmit(): void {
    if (this.isRegisterMode()) {
      this.onRegister();
      return;
    }

    /* Se obtiene el nombre de usuario */
    const u = this.username.trim();
    if (!u || !this.password) {
      this.errorMessage.set(this.translate.instant('auth.errorEnterCredentials'));
      return;
    }
    /* Se limpia el mensaje de error y se muestra el mensaje de éxito */
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.submitting.set(true);
    /* Se envía el formulario */
    this.auth.login(u, this.password, this.rememberMe).subscribe({
      next: (res) => {
        this.submitting.set(false);
        /* Si no se obtiene un token, se muestra un mensaje de error */
        if (!res.token) {
          this.errorMessage.set(res.message || this.translate.instant('auth.errorNoToken'));
          return;
        }
        /* Se redirige a la página de inicio */
        void this.router.navigate(['/inicio']);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        /* Si no se obtiene un mensaje de error, se muestra un mensaje de error */
        const body = err.error as { message?: string } | undefined;
        if (err.status === 0) {
          this.errorMessage.set(this.translate.instant('auth.errorNoConnection'));
          return;
        }
        this.errorMessage.set(body?.message || this.translate.instant('auth.errorLogin'));
      },
    });
  }

  /* Método para registrar un nuevo usuario */
  private onRegister(): void {
    const username = this.username.trim();
    const nombre = this.nombre.trim();
    const apellidos = this.apellidos.trim();
    const email = this.email.trim().toLowerCase();

    /* Si no se obtiene un nombre de usuario, contraseña, nombre, apellidos o email, se muestra un mensaje de error */
    if (!username || !this.password || !nombre || !apellidos || !email) {
      this.errorMessage.set(this.translate.instant('auth.errorCompleteFields'));
      return;
    }
    /* Si la contraseña tiene menos de 8 caracteres, se muestra un mensaje de error */
    if (this.password.length < 8) {
      this.errorMessage.set(this.translate.instant('auth.errorPasswordLength'));
      return;
    }
    if (this.password !== this.confirmPassword) { 
      this.errorMessage.set(this.translate.instant('auth.errorPasswordsMismatch'));
      return;
    }

    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.submitting.set(true);

    /* Se envía el formulario */
    this.auth
      .register({
        username,
        password: this.password,
        nombre,
        apellidos,
        email,
      })
      .subscribe({
        /* Si se obtiene un mensaje de éxito, se muestra un mensaje de éxito y se redirige a la página de inicio */
        next: (res) => {
          this.submitting.set(false);
          this.successMessage.set(res.message || this.translate.instant('auth.registerSuccess'));
          void this.router.navigate(['/']);
        },
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          const body = err.error as { message?: string } | undefined;
          if (err.status === 0) {
            this.errorMessage.set(this.translate.instant('auth.errorNoConnection'));
            return;
          }
          this.errorMessage.set(body?.message || this.translate.instant('auth.errorRegister'));
        },
      });
  }
}
