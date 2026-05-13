import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environments';

// Clave de almacenamiento para el token JWT.
const STORAGE_KEY = 'lectoraton_token';

// Interfaz para la respuesta de autenticación.
export interface AuthResponse {
  token: string | null;
  message: string;
}

// Interfaz para el payload de registro.
export interface RegisterRequest {
  username: string;
  password: string;
  nombre: string;
  apellidos: string;
  email: string;
}

// Lectura local del payload JWT para UI/guards.
function parseJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const part = token.split('.')[1];
    const base64 = part.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    return JSON.parse(atob(padded)) as Record<string, unknown>;
  } catch {
    return null;
  }
}

// Servicio de autenticación global.
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly token = signal<string | null>(this.readTokenFromStorage());

  // Computed: indica si el usuario está autenticado.
  readonly isLoggedIn = computed(() => !!this.token());

  // Computed: roles del usuario desde el token JWT.
  readonly jwtRoles = computed(() => {
    const t = this.token();
    if (!t) {
      return [] as string[];
    }
    const r = parseJwtPayload(t)?.['roles'];
    return Array.isArray(r) ? r.filter((x): x is string => typeof x === 'string') : [];
  });

  // Computed: indica si el usuario puede publicar libros.
  readonly canPublishBooks = computed(() => {
    const r = this.jwtRoles();
    return r.includes('Editor') || r.includes('Admin');
  });

  /** Rol administrador (moderación) */
  readonly isAdmin = computed(() => this.jwtRoles().includes('Admin'));

  constructor(private readonly http: HttpClient) {}

  // Lee el token del almacenamiento local o sesión.
  private readTokenFromStorage(): string | null {
    return localStorage.getItem(STORAGE_KEY) ?? sessionStorage.getItem(STORAGE_KEY);
  }

  // Persiste el token en el almacenamiento local o sesión.
  private persistToken(token: string, remember: boolean): void {
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
    if (remember) {
      localStorage.setItem(STORAGE_KEY, token);
    } else {
      sessionStorage.setItem(STORAGE_KEY, token);
    }
  }

  // Obtiene el token actual.
  getToken(): string | null {
    return this.token();
  }

  // Obtiene el nombre de usuario del token.
  getUsername(): string | null {
    const t = this.token();
    if (!t) {
      return null;
    }
    const sub = parseJwtPayload(t)?.['sub'];
    return typeof sub === 'string' ? sub : null;
  }

  // Obtiene el ID del usuario del token.
  getUserId(): number | null {
    const t = this.token();
    if (!t) {
      return null;
    }
    const id = parseJwtPayload(t)?.['id'];
    return typeof id === 'number' ? id : null;
  }

  applyBackendJwt(token: string, rememberMe = true): void {
    if (!token) {
      return;
    }
    this.persistToken(token, rememberMe);
    this.token.set(token);
  }

  // Al hacer login, se guarda JWT y se actualiza el estado reactivo de sesión.
  login(username: string, password: string, rememberMe: boolean): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/v1/authenticate`, { username, password })
      .pipe(
        tap((res) => {
          if (res.token) {
            this.persistToken(res.token, rememberMe);
            this.token.set(res.token);
          }
        }),
      );
  }

  // Registro de nuevo usuario.
  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/v1/register`, payload);
  }

  // Cierre de sesión: limpia el token y los almacenes.
  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
    this.token.set(null);
  }
}
