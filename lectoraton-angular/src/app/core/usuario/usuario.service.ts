import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';

export interface UsuarioComunidadDTO {
  id: number;
  username: string;
  nombre: string;
  apellidos: string;
  bio?: string | null;
  icono?: string | null;
}

export interface UsuarioPerfilDTO {
  id: number;
  username: string;
  nombre: string;
  apellidos: string;
  email: string;
  bio?: string | null;
  icono?: string | null;
  /** Etiquetas legibles desde el servidor (p. ej. "Lector", "Editor"). */
  rolesEtiqueta?: string[];
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  constructor(private readonly http: HttpClient) {}

  getMiPerfil(): Observable<UsuarioPerfilDTO> {
    return this.http.get<UsuarioPerfilDTO>(`${environment.apiUrl}/usuarios/me`);
  }

  updateMiBio(bio: string): Observable<UsuarioPerfilDTO> {
    return this.http.put<UsuarioPerfilDTO>(`${environment.apiUrl}/usuarios/me/bio`, { bio });
  }

  solicitarSerEditor(): Observable<string> {
    return this.http.post(`${environment.apiUrl}/usuarios/me/solicitud-editor`, null, { responseType: 'text' });
  }

  buscarUsuarios(q: string): Observable<UsuarioComunidadDTO[]> {
    return this.http.get<UsuarioComunidadDTO[]>(`${environment.apiUrl}/usuarios/buscar`, { params: { q } });
  }

  getSeguidos(): Observable<number[]> {
    return this.http.get<number[]>(`${environment.apiUrl}/usuarios/seguidos`);
  }

  getSeguidosDetalle(): Observable<UsuarioComunidadDTO[]> {
    return this.http.get<UsuarioComunidadDTO[]>(`${environment.apiUrl}/usuarios/seguidos/detalle`);
  }

  seguirUsuario(id: number): Observable<string> {
    return this.http.post(`${environment.apiUrl}/usuarios/${id}/seguir`, null, { responseType: 'text' });
  }

  dejarDeSeguirUsuario(id: number): Observable<string> {
    return this.http.delete(`${environment.apiUrl}/usuarios/${id}/seguir`, { responseType: 'text' });
  }
}
