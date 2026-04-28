import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ReservationService } from '../../../services/reservation.service';
import { AuthService } from '../../../services/auth.service';
import { ThemeService } from '../../../services/theme.service';

@Component({
  selector: 'app-my-reservations',
  imports: [CommonModule, RouterLink],
  templateUrl: './my-reservations.html',
  styleUrl: './my-reservations.css'
})
export class MyReservations implements OnInit {
  reservations: any[] = [];
  loading = false;
  error = '';
  message = '';
  sidebarOpen = false;

  user: any;

  constructor(
    private reservationService: ReservationService,
    private authService: AuthService,
    public themeService: ThemeService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user.id) {
      this.router.navigate(['/login']);
      return;
    }
    this.load();
  }

  load() {
    this.loading = true;
    this.reservationService.getUserReservations(this.user.id!).subscribe({
      next: (data) => { this.loading = false; this.reservations = data; },
      error: () => { this.loading = false; this.error = 'Failed to load reservations.'; }
    });
  }

  cancel(id: string) {
    this.error = '';
    this.reservationService.cancel(id).subscribe({
      next: () => { this.message = 'Reservation cancelled.'; this.load(); },
      error: (err) => { this.error = err.error || 'Cancellation failed.'; }
    });
  }

  // Spring LocalDateTime serializes as [year, month, day, hour, minute, second, nano] array or ISO string
  formatTime(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      const d = new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
      return d.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
    }
    return new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
