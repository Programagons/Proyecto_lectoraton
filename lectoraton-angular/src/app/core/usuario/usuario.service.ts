import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';

/* Interfaz para los usuarios de la comunidad */
export interface UsuarioComunidadDTO {
  id: number;
  username: string;
  nombre: string;
  apellidos: string;
  bio?: string | null;
  icono?: string | null;
}

/* Interfaz para el perfil de usuario */
export interface UsuarioPerfilDTO {
  id: number;
  username: string;
  nombre: string;
  apellidos: string;
  email: string;
  bio?: string | null;
  icono?: string | null;
  /* Etiquetas legibles desde el servidor (p. ej. "Lector", "Editor"). */
  rolesEtiqueta?: string[];
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  constructor(private readonly http: HttpClient) {}

  /* Método para obtener el perfil del usuario autenticado */
  getMiPerfil(): Observable<UsuarioPerfilDTO> {
    return this.http.get<UsuarioPerfilDTO>(`${environment.apiUrl}/usuarios/me`);
  }

  /* Método para actualizar la biografía del usuario autenticado */
  updateMiBio(bio: string): Observable<UsuarioPerfilDTO> {
    return this.http.put<UsuarioPerfilDTO>(`${environment.apiUrl}/usuarios/me/bio`, { bio });
  }

  /* Método para actualizar el icono del usuario autenticado */
  updateMiIcono(iconoFile: File): Observable<UsuarioPerfilDTO> {
    const fd = new FormData();
    fd.append('iconoFile', iconoFile);
    return this.http.put<UsuarioPerfilDTO>(`${environment.apiUrl}/usuarios/me/icono`, fd);
  }

  /* Método para solicitar ser editor */
  solicitarSerEditor(): Observable<string> {
    return this.http.post(`${environment.apiUrl}/usuarios/me/solicitud-editor`, null, { responseType: 'text' });
  }

  /* Método para buscar usuarios */
  buscarUsuarios(q: string): Observable<UsuarioComunidadDTO[]> {
    return this.http.get<UsuarioComunidadDTO[]>(`${environment.apiUrl}/usuarios/buscar`, { params: { q } });
  }

  /* Método para obtener los usuarios seguidos */
  getSeguidos(): Observable<number[]> {
    return this.http.get<number[]>(`${environment.apiUrl}/usuarios/seguidos`);
  }

  /* Método para obtener los usuarios seguidos en detalle */
  getSeguidosDetalle(): Observable<UsuarioComunidadDTO[]> {
    return this.http.get<UsuarioComunidadDTO[]>(`${environment.apiUrl}/usuarios/seguidos/detalle`);
  }

  /* Método para seguir a un usuario */
  seguirUsuario(id: number): Observable<string> {
    return this.http.post(`${environment.apiUrl}/usuarios/${id}/seguir`, null, { responseType: 'text' });
  }

  /* Método para dejar de seguir a un usuario */
  dejarDeSeguirUsuario(id: number): Observable<string> {
    return this.http.delete(`${environment.apiUrl}/usuarios/${id}/seguir`, { responseType: 'text' });
  }
}
