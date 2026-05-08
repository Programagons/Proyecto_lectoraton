import { Component, HostListener, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/auth/auth.service';
import { AppLanguage, LanguageService } from '../../core/i18n/language.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class HeaderComponent {
  /* Estado UI del header: tema y menú.
  Señal para el tema: false = claro, true = oscuro.
  Señal para el menú: false = cerrado, true = abierto.*/
  protected readonly darkMode = signal(false);
  protected readonly menuOpen = signal(false);
  // Clave de almacenamiento para el tema.
  private readonly darkModeKey = 'lectoraton-theme';

  constructor(
    protected readonly auth: AuthService,
    private readonly router: Router,
    private readonly languageService: LanguageService,
  ) {}

  // Inicializa tema desde preferencia guardada o preferencia del sistema.
  ngOnInit(): void {
    const saved = localStorage.getItem(this.darkModeKey);
    const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
    const enabled = saved ? saved === 'dark' : prefersDark;
    this.darkMode.set(enabled);
    this.applyTheme(enabled);
  }

  // Alterna entre claro y oscuro. Persiste preferencia.
  toggleDarkMode(): void {
    this.darkMode.update((v) => {
      const next = !v;
      this.applyTheme(next);
      localStorage.setItem(this.darkModeKey, next ? 'dark' : 'light');
      return next;
    });
  }

  // Alterna el estado del menú.
  toggleMenu(): void {
    this.menuOpen.update((v) => !v);
  }

  // Cierra el menú.
  closeMenu(): void {
    this.menuOpen.set(false);
  }

  // Cierra sesión y redirige a login.
  cerrarSesion(): void {
    this.closeMenu();
    this.auth.logout();
    void this.router.navigate(['/']);
  }

  // Cierra el menú al hacer clic fuera de él.
  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeMenu();
  }

  // Obtiene el idioma actual.
  protected currentLanguage(): AppLanguage {
    return this.languageService.currentLanguage();
  }

  // Establece el idioma actual.
  setLanguage(lang: string): void {
    const normalized: AppLanguage = lang === 'en' ? 'en' : 'es';
    this.languageService.setLanguage(normalized);
  }

  // Aplica el tema actual.
  private applyTheme(isDark: boolean): void {
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
  }
}
