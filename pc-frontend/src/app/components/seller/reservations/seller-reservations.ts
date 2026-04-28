import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ThemeService } from '../../../services/theme.service';
import { ReservationService } from '../../../services/reservation.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-seller-reservations',
  imports: [CommonModule, RouterLink],
  templateUrl: './seller-reservations.html',
  styleUrl: './seller-reservations.css'
})
export class SellerReservations implements OnInit {
  user: any;
  pharmacyId = '';
  reservations: any[] = [];
  filtered: any[] = [];
  loading = true;
  error = '';
  filterStatus: 'ALL' | 'PENDING' | 'CLAIMED' | 'EXPIRED' | 'CANCELLED' = 'ALL';
  sidebarOpen = false;

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

  constructor(
    private authService: AuthService,
    public themeService: ThemeService,
    private reservationService: ReservationService,
    private sellerService: SellerService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user?.id) { this.router.navigate(['/login']); return; }
    this.sellerService.getDashboard(this.user.email).subscribe({
      next: (d) => { this.pharmacyId = d.pharmacyId; this.loadReservations(); },
      error: () => { this.loading = false; this.error = 'Could not load pharmacy info.'; }
    });
  }

  loadReservations() {
    this.loading = true;
    this.reservationService.getPharmacyReservations(this.pharmacyId).subscribe({
      next: (data) => { this.loading = false; this.reservations = data; this.applyFilter(); },
      error: () => { this.loading = false; this.error = 'Failed to load reservations.'; }
    });
  }

  setFilter(status: typeof this.filterStatus) {
    this.filterStatus = status;
    this.applyFilter();
  }

  private applyFilter() {
    this.filtered = this.filterStatus === 'ALL'
      ? [...this.reservations]
      : this.reservations.filter(r => r.status === this.filterStatus);
  }

  claim(id: string) {
    this.reservationService.claim(id).subscribe({
      next: () => { this.showToast('Reservation claimed successfully.', 'success'); this.loadReservations(); },
      error: (err) => this.showToast(typeof err.error === 'string' ? err.error : 'Claim failed.', 'error')
    });
  }

  parseDate(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      const d = new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
      return d.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
    }
    return new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
