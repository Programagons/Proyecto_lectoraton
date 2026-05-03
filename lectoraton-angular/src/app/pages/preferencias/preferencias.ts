import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

const LANG_KEY = 'lectoraton_lang';

@Component({
  selector: 'app-preferencias',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './preferencias.html',
  styleUrl: './preferencias.css',
})
export class PreferenciasPage implements OnInit {
  protected idioma = 'es';

  ngOnInit(): void {
    this.idioma = localStorage.getItem(LANG_KEY) || 'es';
    document.documentElement.lang = this.idioma;
  }

  protected guardarIdioma(): void {
    localStorage.setItem(LANG_KEY, this.idioma);
    document.documentElement.lang = this.idioma;
  }
}
