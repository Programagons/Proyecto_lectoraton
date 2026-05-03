import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';
import { PageDTO } from '../libro/libro.service';

export interface FeedItemDTO {
  id: number;
  tipo: string;
  fechaCreacion: string;
  actorId: number;
  actorUsername: string;
  actorNombreCompleto: string;
  actorIcono?: string | null;
  libroId?: number | null;
  libroTitulo?: string | null;
  libroPortada?: string | null;
  libroNumPaginas?: number | null;
  resenaId?: number | null;
  texto?: string | null;
}

@Injectable({ providedIn: 'root' })
export class FeedService {
  constructor(private readonly http: HttpClient) {}

  getFeedMio(page = 0, size = 20, incluirPropias = true): Observable<PageDTO<FeedItemDTO>> {
    return this.http.get<PageDTO<FeedItemDTO>>(`${environment.apiUrl}/feed/mio`, {
      params: {
        page: String(page),
        size: String(size),
        incluirPropias: String(incluirPropias),
      },
    });
  }
}
