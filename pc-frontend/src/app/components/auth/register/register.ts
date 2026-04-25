import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  name = '';
  email = '';
  phone = '';
  city = '';
  address = '';
  pincode = '';
  password = '';
  confirmPassword = '';
  agreedToTerms = false;

  errorMessage = '';
  successMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    if (!this.name || !this.email || !this.phone || !this.password || !this.confirmPassword) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }
    if (!/^[0-9]{10}$/.test(this.phone)) {
      this.errorMessage = 'Phone must be exactly 10 digits (e.g. 9876543210).';
      return;
    }
    if (this.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }
    if (!this.agreedToTerms) {
      this.errorMessage = 'You must agree to the Terms of Service to continue.';
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    this.authService.register(this.name, this.email, this.password, this.phone).subscribe({
      next: () => {
        this.authService.login(this.email, this.password).subscribe({
          next: () => {
            this.loading = false;
            this.successMessage = 'Account created! Taking you to search...';
            setTimeout(() => this.router.navigate(['/search']), 800);
          },
          error: () => {
            this.loading = false;
            this.successMessage = 'Account created! Please sign in.';
            setTimeout(() => this.router.navigate(['/login']), 1200);
          }
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Registration failed. Check phone (10 digits) and password (min 8 chars).';
      }
    });
  }
}
