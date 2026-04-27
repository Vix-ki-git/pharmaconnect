import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ThemeService } from '../../../services/theme.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-admin-analytics',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-analytics.html',
  styleUrl: './admin-analytics.css'
})
export class AdminAnalytics {
  user: any;
  pharmacyId = '';
  rows: any[] = [];
  sidebarOpen = false;
  loading = false;
  searched = false;
  error = '';

  constructor(
    private authService: AuthService,
    public themeService: ThemeService,
    private sellerService: SellerService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  fetch() {
    const id = this.pharmacyId.trim();
    if (!id) return;
    this.loading = true;
    this.error = '';
    this.rows = [];
    this.searched = false;
    this.sellerService.getAnalytics(id).subscribe({
      next: (data) => {
        this.loading = false;
        this.searched = true;
        this.rows = [...data].sort((a, b) => (b.searchCount ?? 0) - (a.searchCount ?? 0));
      },
      error: (err) => {
        this.loading = false;
        this.searched = true;
        this.error = typeof err.error === 'string' ? err.error : 'Failed to load analytics. Check the pharmacy ID.';
      }
    });
  }

  formatDate(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      return new Date(dt[0], dt[1] - 1, dt[2]).toLocaleDateString([], { dateStyle: 'medium' });
    }
    return new Date(dt).toLocaleDateString([], { dateStyle: 'medium' });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
