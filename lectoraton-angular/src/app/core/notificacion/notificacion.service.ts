import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';

export interface NotificacionDTO {
  tipo: 'like' | 'comentario' | string;
  actorNombre: string;
  libroTitulo: string;
  resenaTitulo?: string | null;
  mensaje: string;
  fecha?: string | null;
}

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  constructor(private readonly http: HttpClient) {}

  getMias(): Observable<NotificacionDTO[]> {
    return this.http.get<NotificacionDTO[]>(`${environment.apiUrl}/notificaciones/mias`);
  }
}
