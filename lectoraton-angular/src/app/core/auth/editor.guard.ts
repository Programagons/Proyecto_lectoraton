import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

// Guard por rol: solo Editor/Admin puede entrar en pantallas de publicación.
export const editorGuard: CanActivateFn = (_route, _state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  // Si no está autenticado, redirige a login.
  if (!auth.isLoggedIn()) {
    void router.navigate(['/']);
    return false;
  }
  // Si no tiene permisos de editor/admin, redirige a perfil.
  if (!auth.canPublishBooks()) {
    void router.navigate(['/perfil']);
    return false;
  }
  // Si tiene permisos, permite acceso.
  return true;
};
