import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environments';

export interface UsuarioMiniDTO {
  id: number;
  username: string;
  nombreCompleto: string;
  icono?: string | null;
}

export interface LibroMiniDTO {
  id: number;
  titulo: string;
  portada?: string | null;
  autorNombre?: string;
}

export interface AutorDTO {
  id: number;
  nombre_completo: string;
}

export interface GeneroDTO {
  id: number;
  nombre: string;
}

export interface TropoDTO {
  id: number;
  nombre: string;
}

export interface ResenaResumenDTO {
  mediaCalificaciones: number;
  totalCalificaciones: number;
  totalResenas: number;
  distribucionEstrellas: Record<string, number>;
}

export interface ProgresoLecturaDTO {
  paginaActual: number;
  paginasTotales?: number | null;
  porcentaje: number;
  estado: string;
  fechaActualizacion?: string | null;
}

export interface ProgresoLecturaUpdateRequest {
  paginaActual: number;
  estado?: string | null;
}

export interface UltimoProgresoLibroDTO {
  libroId: number;
  titulo: string;
  portada?: string | null;
  paginaActual: number;
  paginasTotales?: number | null;
  porcentaje: number;
  estado: string;
  fechaActualizacion?: string | null;
}

export interface LibroDetalleDTO {
  id: number;
  titulo: string;
  sagaNombre?: string | null;
  numeroSaga?: number | null;
  autorNombre?: string | null;
  sinopsis?: string | null;
  portada?: string | null;
  generos: string[];
  tropos: string[];
  paginas?: number | null;
  fechaPublicacion?: string | null;
  yaEnAlgunaBiblioteca: boolean;
  miProgreso?: ProgresoLecturaDTO;
  resumenResenas: ResenaResumenDTO;
  amigosQueHanLeido: UsuarioMiniDTO[];
  otrosMismoAutor: LibroMiniDTO[];
  otrosParecidos: LibroMiniDTO[];
}

export interface ResenaDTO {
  id: number;
  usuarioId: number;
  usuarioNombre: string;
  usuarioIcono?: string | null;
  titulo?: string | null;
  contenido?: string | null;
  contieneSpoiler?: boolean;
  calificacion?: number | null;
  fechaCreacion: string;
  numLikes: number;
  numComentarios: number;
  likedByCurrentUser: boolean;
}

export interface ComentarioDTO {
  id: number;
  resenaId: number;
  usuarioId: number;
  usuarioNombre: string;
  contenido: string;
  contieneSpoiler?: boolean;
  fechaCreacion: string;
}

export interface ComentarioCreateRequest {
  resenaId: number;
  contenido: string;
  contieneSpoiler?: boolean;
}

export interface ResenaCreateRequest {
  libroId: number;
  titulo?: string;
  contenido?: string;
  calificacion?: number;
  contieneSpoiler?: boolean;
}

export interface ResenaUpdateRequest {
  titulo?: string;
  contenido?: string;
  calificacion?: number;
  contieneSpoiler?: boolean;
}

export interface PageDTO<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  /** Presente en respuestas paginadas de Spring Data */
  last?: boolean;
}

export interface ExplorarFiltros {
  titulo?: string;
  autor?: string;
  saga?: string;
  generoId?: number | null;
  tropoId?: number | null;
  page?: number;
  size?: number;
  sort?: string;
}

export interface LibroExplorarDTO {
  id: number;
  titulo: string;
  sagaNombre?: string | null;
  numeroSaga?: number | null;
  autorNombre?: string | null;
  portada?: string | null;
  generoNombres?: string[];
  tropoNombres?: string[];
}

export interface LibroCreadoDTO {
  id: number;
  isbn: string;
  titulo: string;
}

export interface LibroRecomendacionDTO {
  origen: LibroMiniDTO;
  recomendado: LibroMiniDTO | null;
  explicacion: string;
}

@Injectable({ providedIn: 'root' })
export class LibroService {
  constructor(private readonly http: HttpClient) {}

  getDetalleLibro(id: number): Observable<LibroDetalleDTO> {
    return this.http.get<LibroDetalleDTO>(`${environment.apiUrl}/libros/${id}/detalle`);
  }

  /** Una sugerencia similar (géneros/tropos/autor) para un libro del catálogo. */
  getRecomendacionLibro(libroId: number): Observable<LibroRecomendacionDTO> {
    return this.http.get<LibroRecomendacionDTO>(`${environment.apiUrl}/libros/${libroId}/recomendacion`);
  }

  /** 204 → null si aún no hay progreso guardado con fecha. */
  getMiUltimoProgreso(): Observable<UltimoProgresoLibroDTO | null> {
    return this.http
      .get<UltimoProgresoLibroDTO>(`${environment.apiUrl}/libros/mi/ultimo-progreso`, { observe: 'response' })
      .pipe(
        map((res) => (res.status === 204 || res.body == null ? null : res.body)),
        catchError(() => of(null)),
      );
  }

  actualizarProgresoLibro(libroId: number, payload: ProgresoLecturaUpdateRequest): Observable<ProgresoLecturaDTO> {
    return this.http.put<ProgresoLecturaDTO>(`${environment.apiUrl}/libros/${libroId}/progreso`, payload);
  }

  getResenasLibro(id: number, q: string, sort: string): Observable<PageDTO<ResenaDTO>> {
    return this.http.get<PageDTO<ResenaDTO>>(`${environment.apiUrl}/libros/${id}/resenas`, {
      params: { q, sort },
    });
  }

  toggleLikeResena(resenaId: number): Observable<ResenaDTO> {
    return this.http.post<ResenaDTO>(`${environment.apiUrl}/libros/resenas/${resenaId}/like`, null);
  }

  getComentariosResena(resenaId: number): Observable<ComentarioDTO[]> {
    return this.http.get<ComentarioDTO[]>(`${environment.apiUrl}/comentarios/resena/${resenaId}`);
  }

  createComentarioResena(payload: ComentarioCreateRequest): Observable<ComentarioDTO> {
    return this.http.post<ComentarioDTO>(`${environment.apiUrl}/comentarios`, payload);
  }

  createResena(payload: ResenaCreateRequest): Observable<ResenaDTO> {
    return this.http.post<ResenaDTO>(`${environment.apiUrl}/resenas`, payload);
  }

  getMiResenaEnLibro(libroId: number): Observable<ResenaDTO> {
    return this.http.get<ResenaDTO>(`${environment.apiUrl}/resenas/libro/${libroId}/mia`);
  }

  deleteMiResena(resenaId: number): Observable<string> {
    return this.http.delete(`${environment.apiUrl}/resenas/${resenaId}/mia`, { responseType: 'text' });
  }

  updateMiResena(resenaId: number, payload: ResenaUpdateRequest): Observable<ResenaDTO> {
    return this.http.put<ResenaDTO>(`${environment.apiUrl}/resenas/${resenaId}/mia`, payload);
  }

  /** Carruseles Explorar (misma forma que la cuadrícula). */
  getNovedades(size = 12): Observable<LibroExplorarDTO[]> {
    return this.http.get<LibroExplorarDTO[]>(`${environment.apiUrl}/libros/novedades`, {
      params: { size: String(size) },
    });
  }

  /** Libros con más calificaciones en `usuarios_libros`. */
  getMasLeidos(size = 12): Observable<LibroExplorarDTO[]> {
    return this.http.get<LibroExplorarDTO[]>(`${environment.apiUrl}/libros/mas-leidos`, {
      params: { size: String(size) },
    });
  }

  explorarLibros(filtros: ExplorarFiltros): Observable<PageDTO<LibroExplorarDTO>> {
    const params: Record<string, string> = {};
    if (filtros.titulo) params['titulo'] = filtros.titulo;
    if (filtros.autor) params['autor'] = filtros.autor;
    if (filtros.saga) params['saga'] = filtros.saga;
    if (filtros.generoId) params['generoId'] = String(filtros.generoId);
    if (filtros.tropoId) params['tropoId'] = String(filtros.tropoId);
    if (typeof filtros.page === 'number') params['page'] = String(filtros.page);
    if (typeof filtros.size === 'number') params['size'] = String(filtros.size);
    if (filtros.sort) params['sort'] = filtros.sort;
    return this.http.get<PageDTO<LibroExplorarDTO>>(`${environment.apiUrl}/libros/explorar`, { params });
  }

  getAutores(): Observable<PageDTO<AutorDTO>> {
    return this.http.get<PageDTO<AutorDTO>>(`${environment.apiUrl}/autores`, { params: { size: '200', sort: 'nombreCompleto' } });
  }

  getGeneros(): Observable<GeneroDTO[]> {
    return this.http.get<GeneroDTO[]>(`${environment.apiUrl}/generos`);
  }

  getTropos(): Observable<TropoDTO[]> {
    return this.http.get<TropoDTO[]>(`${environment.apiUrl}/tropos`);
  }

  crearLibro(formData: FormData): Observable<LibroCreadoDTO> {
    return this.http.post<LibroCreadoDTO>(`${environment.apiUrl}/libros`, formData);
  }
}
