import { Component } from '@angular/core';
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
  // Account fields (maps to RegistrationRequest)
  name = '';
  email = '';
  phone = '';
  password = '';

  // Pharmacy fields (maps to SellerPharmacyRegistrationRequestDto)
  pharmacyName = '';
  pharmacyAddress = '';
  contactPhoneNumber = '';
  locationLatitude: number | null = null;
  locationLongitude: number | null = null;
  isOperated247 = false;

  loading = false;
  locating = false;
  errorMessage = '';
  successMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  useMyLocation() {
    if (!navigator.geolocation) {
      this.errorMessage = 'Geolocation is not supported by your browser.';
      return;
    }
    this.locating = true;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.locationLatitude = parseFloat(pos.coords.latitude.toFixed(6));
        this.locationLongitude = parseFloat(pos.coords.longitude.toFixed(6));
        this.locating = false;
      },
      () => {
        this.locating = false;
        this.errorMessage = 'Could not get location. Please enter coordinates manually.';
      }
    );
  }

  onSubmit() {
    if (!this.name || !this.email || !this.phone || !this.password ||
        !this.pharmacyName || !this.pharmacyAddress || !this.contactPhoneNumber) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    // Step 1: Create user account
    this.authService.register(this.name, this.email, this.password, this.phone).subscribe({
      next: () => {
        // Step 2: Register the pharmacy under that account
        this.authService.registerPharmacy({
          pharmacyName: this.pharmacyName,
          pharmacyAddress: this.pharmacyAddress,
          contactPhoneNumber: this.contactPhoneNumber,
          locationLatitude: this.locationLatitude,
          locationLongitude: this.locationLongitude,
          isOperated247: this.isOperated247,
          sellerEmailAddress: this.email
        }).subscribe({
          next: () => {
            this.loading = false;
            this.successMessage = 'Pharmacy registered! Your account is pending admin approval. Redirecting to login...';
            setTimeout(() => this.router.navigate(['/login']), 2500);
          },
          error: (err) => {
            this.loading = false;
            this.errorMessage = err.error || 'Pharmacy registration failed. Try signing in to complete setup.';
          }
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Account creation failed. Please try again.';
      }
    });
  }
}
