import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Solo usuarios con rol Editor (o Admin) en el JWT (tras iniciar sesión). */
export const editorGuard: CanActivateFn = (_route, _state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) {
    void router.navigate(['/']);
    return false;
  }
  if (!auth.canPublishBooks()) {
    void router.navigate(['/perfil']);
    return false;
  }
  return true;
};
