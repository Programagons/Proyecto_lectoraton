import { Component, HostListener, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class HeaderComponent {
  protected readonly darkMode = signal(false);
  protected readonly menuOpen = signal(false);

  constructor(
    protected readonly auth: AuthService,
    private readonly router: Router,
  ) {}

  toggleDarkMode(): void {
    this.darkMode.update((v) => !v);
  }

  toggleMenu(): void {
    this.menuOpen.update((v) => !v);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  cerrarSesion(): void {
    this.closeMenu();
    this.auth.logout();
    void this.router.navigate(['/']);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeMenu();
  }
}
