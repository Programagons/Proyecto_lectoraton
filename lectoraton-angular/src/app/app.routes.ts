import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { editorGuard } from './core/auth/editor.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/login/login').then((m) => m.LoginPage) },
  { path: 'registro', loadComponent: () => import('./pages/login/login').then((m) => m.LoginPage) },
  {
    path: 'sobre-nosotros',
    loadComponent: () => import('./pages/sobre-nosotros/sobre-nosotros').then((m) => m.SobreNosotrosPage),
  },
  {
    path: 'ayuda',
    loadComponent: () => import('./pages/ayuda/ayuda').then((m) => m.AyudaPage),
  },
  {
    path: 'inicio',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/inicio/inicio').then((m) => m.InicioPage),
  },
  {
    path: 'bibliotecas',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/bibliotecas/bibliotecas').then((m) => m.BibliotecasPage),
  },
  {
    path: 'bibliotecas/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/biblioteca-detalle/biblioteca-detalle').then((m) => m.BibliotecaDetallePage),
  },
  {
    path: 'libros/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/libro-detalle/libro-detalle').then((m) => m.LibroDetallePage),
  },
  {
    path: 'explorar',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/explorar/explorar').then((m) => m.ExplorarPage),
  },
  { path: 'recomendar', redirectTo: 'recomendador', pathMatch: 'full' },
  {
    path: 'recomendador',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/recomendador/recomendador').then((m) => m.RecomendadorPage),
  },
  {
    path: 'agregar-libro',
    canActivate: [authGuard, editorGuard],
    loadComponent: () => import('./pages/agregar-libro/agregar-libro').then((m) => m.AgregarLibroPage),
  },
  {
    path: 'comunidad',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/comunidad/comunidad').then((m) => m.ComunidadPage),
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/perfil/perfil').then((m) => m.PerfilPage),
  },
  {
    path: 'notificaciones',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/notificaciones/notificaciones').then((m) => m.NotificacionesPage),
  },
  {
    path: 'preferencias',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/preferencias/preferencias').then((m) => m.PreferenciasPage),
  },
];
