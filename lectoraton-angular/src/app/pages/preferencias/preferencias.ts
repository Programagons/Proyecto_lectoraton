import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { LanguageService, AppLanguage } from '../../core/i18n/language.service';

@Component({
  selector: 'app-preferencias',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
  templateUrl: './preferencias.html',
  styleUrl: './preferencias.css',
})
export class PreferenciasPage implements OnInit {
  protected idioma: AppLanguage = 'es';

  constructor(private readonly languageService: LanguageService) {}

  ngOnInit(): void {
    this.idioma = this.languageService.currentLanguage();
    document.documentElement.lang = this.idioma;
  }

  protected guardarIdioma(): void {
    this.languageService.setLanguage(this.idioma);
    document.documentElement.lang = this.idioma;
  }
}
