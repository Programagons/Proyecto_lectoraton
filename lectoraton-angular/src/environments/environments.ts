export const environment = {
  production: false,
  /** En `ng serve` el proxy reenvía `/api` → backend (mismo origen, sin bloqueos CORS). */
  apiUrl: '/api',
};

/** Inicio OAuth2 Spring sobre el mismo origen que Angular (proxy) o el host del API (`…/api` → mismo host sin `/api`). */
export function googleOAuthAuthorizeUrl(apiUrl = environment.apiUrl): string {
  const u = typeof apiUrl === 'string' ? apiUrl.trim() : '';
  const base = u.replace(/\/api\/?$/, '');
  return `${base}/oauth2/authorization/google`;
}
   