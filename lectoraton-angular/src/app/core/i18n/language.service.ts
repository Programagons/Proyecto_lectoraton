import { Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type AppLanguage = 'es' | 'en';

// Servicio único de idioma para toda la app (i18n runtime).
@Injectable({ providedIn: 'root' })
export class LanguageService {
  // Clave de almacenamiento para el idioma.
  private readonly storageKey = 'lectoraton_lang';
  // Idiomas soportados.
  private readonly supported: AppLanguage[] = ['es', 'en'];
  // Idioma actual.
  readonly currentLanguage = signal<AppLanguage>('es');

  constructor(private readonly translate: TranslateService) {}
  // Inicialización del servicio.

  init(): void {
    // Añadimos los idiomas soportados.
    this.translate.addLangs(this.supported);
    this.translate.setDefaultLang('es');

    const saved = localStorage.getItem(this.storageKey);
    const browser = (this.translate.getBrowserLang() || 'es').toLowerCase();
    // Prioridad: idioma guardado > idioma navegador > español.
    const initial: AppLanguage = this.isSupported(saved)
      ? saved
      : this.isSupported(browser)
        ? browser
        : 'es';
    this.setLanguage(initial);
  }

  setLanguage(lang: AppLanguage): void {
    // Aplica traducción activa y persiste preferencia.
    this.translate.use(lang);
    this.currentLanguage.set(lang);
    localStorage.setItem(this.storageKey, lang);
  }

  private isSupported(lang: string | null): lang is AppLanguage {
    return !!lang && this.supported.includes(lang as AppLanguage);
  }
}
