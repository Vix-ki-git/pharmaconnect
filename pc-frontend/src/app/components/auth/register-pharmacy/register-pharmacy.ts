import { Component, NgZone } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register-pharmacy',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './register-pharmacy.html',
  styleUrl: './register-pharmacy.css'
})
export class RegisterPharmacy {
  // Pharmacy Information
  pharmacyName = '';
  ownerName = '';
  licenseNumber = '';
  gstNumber = '';

  // Contact Information
  email = '';
  phone = '';
  address = '';
  city = '';
  pincode = '';
  operatingHours = '';
  isOperated247 = false;

  // Location
  locationLatitude: number | null = null;
  locationLongitude: number | null = null;

  // Account Security
  password = '';
  confirmPassword = '';
  agreedToTerms = false;

  loading = false;
  locating = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private ngZone: NgZone
  ) {}

  useMyLocation() {
    if (!navigator.geolocation) {
      this.errorMessage = 'Geolocation is not supported by your browser.';
      return;
    }
    this.locating = true;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.ngZone.run(() => {
          this.locationLatitude = parseFloat(pos.coords.latitude.toFixed(6));
          this.locationLongitude = parseFloat(pos.coords.longitude.toFixed(6));
          this.locating = false;
        });
      },
      () => {
        this.ngZone.run(() => {
          this.locating = false;
          this.errorMessage = 'Could not get location. Coordinates are optional.';
        });
      }
    );
  }

  onSubmit() {
    if (!this.pharmacyName || !this.ownerName || !this.email || !this.phone ||
        !this.address || !this.password || !this.confirmPassword) {
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
      this.errorMessage = 'You must confirm the terms to register.';
      return;
    }

    const fullAddress = [this.address, this.city, this.pincode].filter(Boolean).join(', ');

    this.loading = true;
    this.errorMessage = '';

    this.authService.register(this.ownerName, this.email, this.password, this.phone).subscribe({
      next: () => {
        this.authService.registerPharmacy({
          pharmacyName: this.pharmacyName,
          pharmacyAddress: fullAddress,
          contactPhoneNumber: this.phone,
          locationLatitude: this.locationLatitude,
          locationLongitude: this.locationLongitude,
          isOperated247: this.isOperated247,
          sellerEmailAddress: this.email
        }).subscribe({
          next: () => {
            this.authService.login(this.email, this.password).subscribe({
              next: () => {
                this.loading = false;
                this.successMessage = 'Pharmacy registered! Taking you to your dashboard...';
                setTimeout(() => this.router.navigate(['/seller/dashboard']), 1000);
              },
              error: () => {
                this.loading = false;
                this.successMessage = 'Pharmacy registered! Pending admin approval. Please sign in.';
                setTimeout(() => this.router.navigate(['/login']), 2000);
              }
            });
          },
          error: (err) => {
            this.loading = false;
            this.errorMessage = typeof err.error === 'string' ? err.error : 'Pharmacy registration failed. Please try again.';
          }
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Account creation failed. Please try again.';
      }
    });
  }
}
