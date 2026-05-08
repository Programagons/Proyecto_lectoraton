import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TRANSLATE_HTTP_LOADER_CONFIG, TranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Cliente HTTP global + interceptor para adjuntar JWT en llamadas a API.
    provideHttpClient(withInterceptors([authInterceptor])),
    // i18n global: carga traducciones desde archivos JSON en /i18n. La traducción se realiza en el servidor.
    importProvidersFrom(
      TranslateModule.forRoot({
        defaultLanguage: 'es',
        loader: {
          provide: TranslateLoader,
          useClass: TranslateHttpLoader,
        },
      }),
    ),
    // Configuración del loader de traducciones.
    {
      provide: TRANSLATE_HTTP_LOADER_CONFIG,
      useValue: { prefix: './i18n/', suffix: '.json' },
    },
// Router global de la aplicación (rutas SPA + comportamiento de scroll).
    provideRouter(
      routes,
      withInMemoryScrolling({
        anchorScrolling: 'enabled',
        scrollPositionRestoration: 'enabled',
      }),
    ),
  ],
};
