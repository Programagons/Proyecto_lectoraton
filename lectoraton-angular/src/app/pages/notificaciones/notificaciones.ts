import { Component, OnInit, signal } from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NotificacionDTO, NotificacionService } from '../../core/notificacion/notificacion.service';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './notificaciones.html',
  styleUrl: './notificaciones.css',
})
export class NotificacionesPage implements OnInit {
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly items = signal<NotificacionDTO[]>([]);

  constructor(
    private readonly notificacionService: NotificacionService,
    private readonly translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.notificacionService.getMias().subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.translate.instant('notifications.errorLoad'));
        this.loading.set(false);
      },
    });
  }
}
