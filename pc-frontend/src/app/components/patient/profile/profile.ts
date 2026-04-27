import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile {
  user: any;
  sidebarOpen = false;

  constructor(private authService: AuthService, private router: Router) {
    this.user = this.authService.getCurrentUser();
    if (!this.user?.id) this.router.navigate(['/login']);
  }

  get initials(): string {
    const name = this.user?.name || '?';
    return name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
