import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/* Interceptor global: adjunta JWT a peticiones protegidas.
Es invocado por el cliente HTTP global en app.config.ts.*/
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // En login/registro no mandamos token para evitar mezclar sesiones antiguas.
  if (req.url.endsWith('/v1/authenticate') || req.url.endsWith('/v1/register')) {
    return next(req);
  }

  const auth = inject(AuthService);
  const token = auth.getToken();
    /* Si hay sesión, enviamos Authorization Bearer automáticamente.
     Si no hay sesión, el servidor no validará el JWT y permitirá acceso sin autenticación.*/
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
