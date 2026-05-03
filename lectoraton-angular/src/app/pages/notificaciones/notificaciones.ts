import { Component, OnInit, signal } from '@angular/core';
import { NotificacionDTO, NotificacionService } from '../../core/notificacion/notificacion.service';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  templateUrl: './notificaciones.html',
  styleUrl: './notificaciones.css',
})
export class NotificacionesPage implements OnInit {
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly items = signal<NotificacionDTO[]>([]);

  constructor(private readonly notificacionService: NotificacionService) {}

  ngOnInit(): void {
    this.notificacionService.getMias().subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las notificaciones.');
        this.loading.set(false);
      },
    });
  }
}
