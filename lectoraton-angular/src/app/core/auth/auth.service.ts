import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environments';

const STORAGE_KEY = 'lectoraton_token';

export interface AuthResponse {
  token: string | null;
  message: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  nombre: string;
  apellidos: string;
  email: string;
}

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

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly token = signal<string | null>(this.readTokenFromStorage());

  readonly isLoggedIn = computed(() => !!this.token());

  /** Roles tal como vienen en el JWT (coherentes tras iniciar sesión de nuevo). */
  readonly jwtRoles = computed(() => {
    const t = this.token();
    if (!t) {
      return [] as string[];
    }
    const r = parseJwtPayload(t)?.['roles'];
    return Array.isArray(r) ? r.filter((x): x is string => typeof x === 'string') : [];
  });

  readonly canPublishBooks = computed(() => {
    const r = this.jwtRoles();
    return r.includes('Editor') || r.includes('Admin');
  });

  constructor(private readonly http: HttpClient) {}

  private readTokenFromStorage(): string | null {
    return localStorage.getItem(STORAGE_KEY) ?? sessionStorage.getItem(STORAGE_KEY);
  }

  private persistToken(token: string, remember: boolean): void {
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
    if (remember) {
      localStorage.setItem(STORAGE_KEY, token);
    } else {
      sessionStorage.setItem(STORAGE_KEY, token);
    }
  }

  getToken(): string | null {
    return this.token();
  }

  getUsername(): string | null {
    const t = this.token();
    if (!t) {
      return null;
    }
    const sub = parseJwtPayload(t)?.['sub'];
    return typeof sub === 'string' ? sub : null;
  }

  getUserId(): number | null {
    const t = this.token();
    if (!t) {
      return null;
    }
    const id = parseJwtPayload(t)?.['id'];
    return typeof id === 'number' ? id : null;
  }

  /** Persiste JWT devuelto por OAuth2 backend (fragmento tras login Google). */
  applyBackendJwt(token: string, rememberMe = true): void {
    if (!token) {
      return;
    }
    this.persistToken(token, rememberMe);
    this.token.set(token);
  }

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

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/v1/register`, payload);
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    sessionStorage.removeItem(STORAGE_KEY);
    this.token.set(null);
  }
}
