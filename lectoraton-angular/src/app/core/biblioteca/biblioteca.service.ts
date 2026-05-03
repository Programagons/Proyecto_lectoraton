import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';

export interface BibliotecaDTO {
  id: number;
  usuarioId: number;
  usuarioNombre: string;
  nombre: string;
  libroIds?: number[];
  ultimaPortada?: string | null;
}

export interface LibroBibliotecaDTO {
  id: number;
  titulo: string;
  autorNombre?: string;
  portada?: string | null;
}

export interface BibliotecaCreateRequest {
  nombre: string;
  libroIds?: number[];
}

export interface BibliotecaRenameRequest {
  nombre: string;
}

@Injectable({ providedIn: 'root' })
export class BibliotecaService {
  constructor(private readonly http: HttpClient) {}

  getMisBibliotecas(): Observable<BibliotecaDTO[]> {
    return this.http.get<BibliotecaDTO[]>(`${environment.apiUrl}/bibliotecas/mias`);
  }

  createBiblioteca(payload: BibliotecaCreateRequest): Observable<BibliotecaDTO> {
    return this.http.post<BibliotecaDTO>(`${environment.apiUrl}/bibliotecas`, payload);
  }

  renameBiblioteca(id: number, payload: BibliotecaRenameRequest): Observable<BibliotecaDTO> {
    return this.http.put<BibliotecaDTO>(`${environment.apiUrl}/bibliotecas/${id}`, payload);
  }

  deleteBiblioteca(id: number): Observable<string> {
    return this.http.delete(`${environment.apiUrl}/bibliotecas/${id}`, { responseType: 'text' });
  }

  addLibroABiblioteca(bibliotecaId: number, libroId: number): Observable<string> {
    return this.http.post(`${environment.apiUrl}/bibliotecas/${bibliotecaId}/libros/${libroId}`, null, {
      responseType: 'text',
    });
  }

  getLibrosDeBiblioteca(id: number): Observable<LibroBibliotecaDTO[]> {
    return this.http.get<LibroBibliotecaDTO[]>(`${environment.apiUrl}/bibliotecas/${id}/libros`);
  }
}
