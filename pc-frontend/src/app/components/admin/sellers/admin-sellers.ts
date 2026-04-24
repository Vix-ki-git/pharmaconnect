import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-sellers',
  imports: [CommonModule],
  template: `
    <div style="padding: 2rem; font-family: sans-serif; text-align: center; color: #2e7d32;">
      <h2>Admin Panel — coming soon</h2>
      <button (click)="logout()" style="margin-top:1rem; padding: 0.5rem 1.5rem; background:#2e7d32; color:white; border:none; border-radius:8px; cursor:pointer;">Logout</button>
    </div>
  `
})
export class AdminSellers {
  constructor(private authService: AuthService, private router: Router) {}
  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
