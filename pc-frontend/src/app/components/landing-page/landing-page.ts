import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-landing-page',
  imports: [CommonModule],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css',
})
export class LandingPage {
  constructor(private router: Router, public themeService: ThemeService) {}

  toggleTheme() {
    this.themeService.toggle();
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  goToSearch() {
    this.router.navigate(['/search']);
  }

  goToRegister() {
    this.router.navigate(['/register-pharmacy']);
  }
}
