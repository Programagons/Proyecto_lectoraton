import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environments';

/* Interfaz para los usuarios de la comunidad */
export interface UsuarioMiniDTO {
  id: number;
  username: string;
  nombreCompleto: string;
  icono?: string | null;
}

/* Interfaz para los libros */
export interface LibroMiniDTO {
  id: number;
  titulo: string;
  portada?: string | null;
  autorNombre?: string;
}

/* Interfaz para los autores */
export interface AutorDTO {
  id: number;
  nombre_completo: string;
}

/* Interfaz para los géneros */
export interface GeneroDTO {
  id: number;
  nombre: string;
}

/* Interfaz para los tropos */
export interface TropoDTO {
  id: number;
  nombre: string;
}

/* Interfaz para las resenas */
export interface ResenaResumenDTO {
  mediaCalificaciones: number;
  totalCalificaciones: number;
  totalResenas: number;
  distribucionEstrellas: Record<string, number>;
}

/* Interfaz para el progreso de lectura */
export interface ProgresoLecturaDTO {
  paginaActual: number;
  paginasTotales?: number | null;
  porcentaje: number;
  estado: string;
  fechaActualizacion?: string | null;
}

/* Interfaz para el progreso de lectura */
export interface ProgresoLecturaUpdateRequest {
  paginaActual: number;
  estado?: string | null;
}

/* Interfaz para el último progreso de lectura */
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

/* Interfaz para el detalle de un libro */
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

/* Interfaz para las resenas */
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

/* Interfaz para los comentarios */
export interface ComentarioDTO {
  id: number;
  resenaId: number;
  usuarioId: number;
  usuarioNombre: string;
  contenido: string;
  contieneSpoiler?: boolean;
  fechaCreacion: string;
}

/* Interfaz para la creación de un comentario */
export interface ComentarioCreateRequest {
  resenaId: number;
  contenido: string;
  contieneSpoiler?: boolean;
}

/* Interfaz para la creación de una resena */
export interface ResenaCreateRequest {
  libroId: number;
  titulo?: string;
  contenido?: string;
  calificacion?: number;
  contieneSpoiler?: boolean;
}

/* Interfaz para la actualización de una resena */
export interface ResenaUpdateRequest {
  titulo?: string;
  contenido?: string;
  calificacion?: number;
  contieneSpoiler?: boolean;
}

/* Interfaz para la paginación */
export interface PageDTO<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last?: boolean;
}

/* Interfaz para los filtros de exploración */
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

/* Interfaz para el explorar libros */
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

/* Interfaz para el libro creado */
export interface LibroCreadoDTO {
  id: number;
  isbn: string;
  titulo: string;
}

/* Interfaz para la recomendación de un libro */
export interface LibroRecomendacionDTO {
  origen: LibroMiniDTO;
  recomendado: LibroMiniDTO | null;
  explicacion: string;
}

/* Servicio para los libros */
@Injectable({ providedIn: 'root' })
export class LibroService {
  constructor(private readonly http: HttpClient) {}

  /* Método para obtener el detalle de un libro */
  getDetalleLibro(id: number): Observable<LibroDetalleDTO> {
    return this.http.get<LibroDetalleDTO>(`${environment.apiUrl}/libros/${id}/detalle`);
  }

  /* Método para obtener la recomendación de un libro */
  getRecomendacionLibro(libroId: number): Observable<LibroRecomendacionDTO> {
    return this.http.get<LibroRecomendacionDTO>(`${environment.apiUrl}/libros/${libroId}/recomendacion`);
  }

  /* Método para obtener el último progreso de lectura */
  getMiUltimoProgreso(): Observable<UltimoProgresoLibroDTO | null> {
    return this.http
      .get<UltimoProgresoLibroDTO>(`${environment.apiUrl}/libros/mi/ultimo-progreso`, { observe: 'response' })
      .pipe(
        map((res) => (res.status === 204 || res.body == null ? null : res.body)),
        catchError(() => of(null)),
      );
  }

  /* Método para actualizar el progreso de lectura */
  actualizarProgresoLibro(libroId: number, payload: ProgresoLecturaUpdateRequest): Observable<ProgresoLecturaDTO> {
    return this.http.put<ProgresoLecturaDTO>(`${environment.apiUrl}/libros/${libroId}/progreso`, payload);
  }

  /* Método para obtener las resenas de un libro */
  getResenasLibro(id: number, q: string, sort: string): Observable<PageDTO<ResenaDTO>> {
    return this.http.get<PageDTO<ResenaDTO>>(`${environment.apiUrl}/libros/${id}/resenas`, {
      params: { q, sort },
    });
  }

  /* Método para dar like a una resena */
  toggleLikeResena(resenaId: number): Observable<ResenaDTO> {
    return this.http.post<ResenaDTO>(`${environment.apiUrl}/libros/resenas/${resenaId}/like`, null);
  }

  /* Método para obtener los comentarios de una resena */
  getComentariosResena(resenaId: number): Observable<ComentarioDTO[]> {
    return this.http.get<ComentarioDTO[]>(`${environment.apiUrl}/comentarios/resena/${resenaId}`);
  }

  /* Método para crear un comentario */
  createComentarioResena(payload: ComentarioCreateRequest): Observable<ComentarioDTO> {
    return this.http.post<ComentarioDTO>(`${environment.apiUrl}/comentarios`, payload);
  }

  /* Método para crear una resena */
  createResena(payload: ResenaCreateRequest): Observable<ResenaDTO> {
    return this.http.post<ResenaDTO>(`${environment.apiUrl}/resenas`, payload);
  }

  /* Método para obtener la resena propia de un libro */
  getMiResenaEnLibro(libroId: number): Observable<ResenaDTO> {
    return this.http.get<ResenaDTO>(`${environment.apiUrl}/resenas/libro/${libroId}/mia`);
  }

  /* Método para eliminar la resena propia de un libro */
  deleteMiResena(resenaId: number): Observable<string> {
    return this.http.delete(`${environment.apiUrl}/resenas/${resenaId}/mia`, { responseType: 'text' });
  }

  /* Método para actualizar la resena propia de un libro */
  updateMiResena(resenaId: number, payload: ResenaUpdateRequest): Observable<ResenaDTO> {
    return this.http.put<ResenaDTO>(`${environment.apiUrl}/resenas/${resenaId}/mia`, payload);
  }

  /* Método para obtener las novedades */
  getNovedades(size = 12): Observable<LibroExplorarDTO[]> {
    return this.http.get<LibroExplorarDTO[]>(`${environment.apiUrl}/libros/novedades`, {
      params: { size: String(size) },
    });
  }

  /* Método para obtener los libros más leídos */
  getMasLeidos(size = 12): Observable<LibroExplorarDTO[]> {
    return this.http.get<LibroExplorarDTO[]>(`${environment.apiUrl}/libros/mas-leidos`, {
      params: { size: String(size) },
    });
  }

  /* Método para explorar libros */
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

  /* Método para obtener los autores */
  getAutores(): Observable<PageDTO<AutorDTO>> {
    return this.http.get<PageDTO<AutorDTO>>(`${environment.apiUrl}/autores`, { params: { size: '200', sort: 'nombreCompleto' } });
  }

  /* Método para obtener los géneros */
  getGeneros(): Observable<GeneroDTO[]> {
    return this.http.get<GeneroDTO[]>(`${environment.apiUrl}/generos`);
  }

  /* Método para obtener los tropos */
  getTropos(): Observable<TropoDTO[]> {
    return this.http.get<TropoDTO[]>(`${environment.apiUrl}/tropos`);
  }

  /* Método para crear un libro */
  crearLibro(formData: FormData): Observable<LibroCreadoDTO> {
    return this.http.post<LibroCreadoDTO>(`${environment.apiUrl}/libros`, formData);
  }
}
