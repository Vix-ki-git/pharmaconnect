import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ThemeService } from '../../../services/theme.service';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-sellers',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-sellers.html',
  styleUrl: './admin-sellers.css'
})
export class AdminSellers implements OnInit {
  user: any;
  pending: any[] = [];
  loading = true;
  sidebarOpen = false;
  error = '';
  actionId: string | null = null;

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

  constructor(
    private authService: AuthService,
    public themeService: ThemeService,
    private adminService: AdminService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (this.authService.getRole() !== 'ADMIN') { this.router.navigate(['/login']); return; }
    this.load();
  }

  load() {
    this.loading = true;
    this.adminService.getPendingSellers().subscribe({
      next: (data) => { this.loading = false; this.pending = data; },
      error: (err) => {
        this.loading = false;
        this.pending = [];
        // 204 No Content is not an error for us
        if (err.status !== 204) this.error = 'Failed to load pending sellers.';
      }
    });
  }

  verify(pharmacyId: string, pharmacyName: string) {
    this.actionId = pharmacyId;
    this.adminService.verifySeller(pharmacyId).subscribe({
      next: () => {
        this.actionId = null;
        this.showToast(`${pharmacyName} verified and approved.`, 'success');
        this.pending = this.pending.filter(p => p.pharmacyId !== pharmacyId);
      },
      error: (err) => {
        this.actionId = null;
        this.showToast(typeof err.error === 'string' ? err.error : 'Verification failed.', 'error');
      }
    });
  }

  reject(pharmacyId: string, pharmacyName: string) {
    this.actionId = pharmacyId;
    this.adminService.rejectSeller(pharmacyId).subscribe({
      next: () => {
        this.actionId = null;
        this.showToast(`${pharmacyName} rejected.`, 'error');
        this.pending = this.pending.filter(p => p.pharmacyId !== pharmacyId);
      },
      error: (err) => {
        this.actionId = null;
        this.showToast(typeof err.error === 'string' ? err.error : 'Rejection failed.', 'error');
      }
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
