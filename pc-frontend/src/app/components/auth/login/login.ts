import { Component, AfterViewInit, NgZone } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

type LoginMode = 'user' | 'pharmacy' | 'admin';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements AfterViewInit {
  email = '';
  password = '';
  rememberMe = false;
  loginMode: LoginMode = 'user';
  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private ngZone: NgZone
  ) {}

  ngAfterViewInit() {
    const tryInit = () => {
      const g = (window as any).google;
      if (!g) { setTimeout(tryInit, 200); return; }
      g.accounts.id.initialize({
        client_id: '637661381312-0r1rl4q9c9c2ku8abjrj08bld26p6mi7.apps.googleusercontent.com',
        callback: (res: any) => this.ngZone.run(() => this.handleGoogleCallback(res))
      });
      g.accounts.id.renderButton(
        document.getElementById('google-signin-btn'),
        { theme: 'outline', size: 'large', text: 'continue_with', shape: 'rectangular', width: 340 }
      );
    };
    setTimeout(tryInit, 100);
  }

  handleGoogleCallback(res: any) {
    this.loading = true;
    this.errorMessage = '';
    this.authService.googleLogin(res.credential).subscribe({
      next: (user) => this.redirectByRole(user.role),
      error: () => {
        this.loading = false;
        this.errorMessage = 'Google sign-in failed. Please try again.';
      }
    });
  }

  get loginTitle(): string {
    return this.loginMode === 'pharmacy' ? 'Pharmacy Login'
         : this.loginMode === 'admin'    ? 'Admin Login'
         : 'User Login';
  }

  get loginSubtitle(): string {
    return this.loginMode === 'pharmacy' ? 'Sign in to manage your pharmacy'
         : this.loginMode === 'admin'    ? 'Sign in to the admin panel'
         : 'Sign in to search and reserve medicines';
  }

  setMode(mode: LoginMode) {
    this.loginMode = mode;
    this.errorMessage = '';
  }

  onSubmit() {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please fill in all fields.';
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.email, this.password).subscribe({
      next: (user) => this.redirectByRole(user.role),
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Invalid email or password.';
      }
    });
  }

  private redirectByRole(role: string) {
    if (role === 'ADMIN') this.router.navigate(['/admin/sellers']);
    else if (role === 'SELLER') this.router.navigate(['/seller/dashboard']);
    else this.router.navigate(['/search']);
  }
}
