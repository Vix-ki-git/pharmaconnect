import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-seller-profile',
  imports: [CommonModule, RouterLink],
  templateUrl: './seller-profile.html',
  styleUrl: './seller-profile.css'
})
export class SellerProfile implements OnInit {
  user: any;
  dashboard: any = null;
  loading = true;
  sidebarOpen = false;

  constructor(
    private authService: AuthService,
    private sellerService: SellerService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user?.id) { this.router.navigate(['/login']); return; }
    this.sellerService.getDashboard(this.user.email).subscribe({
      next: (data) => { this.loading = false; this.dashboard = data; },
      error: () => { this.loading = false; }
    });
  }

  get initials(): string {
    const name = this.user?.name || '?';
    return name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
