// Componente raíz de la SPA: monta header, footer y router-outlet.
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './layout/header/header';
import { FooterComponent } from './layout/footer/footer';
import { LanguageService } from './core/i18n/language.service';

// Decorador que define el componente raíz de la SPA.
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  // Inicializa el servicio de idioma.
  constructor(private readonly languageService: LanguageService) {
    this.languageService.init();
  }
}
