import { Routes } from '@angular/router';
import { LandingPage } from './components/landing-page/landing-page';
import { Login } from './components/auth/login/login';
import { Register } from './components/auth/register/register';
import { RegisterPharmacy } from './components/auth/register-pharmacy/register-pharmacy';
import { SearchPage } from './components/patient/search/search';
import { SellerDashboard } from './components/seller/dashboard/seller-dashboard';
import { AdminSellers } from './components/admin/sellers/admin-sellers';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'register-pharmacy', component: RegisterPharmacy },
  { path: 'search', component: SearchPage },
  { path: 'seller/dashboard', component: SellerDashboard },
  { path: 'admin/sellers', component: AdminSellers },
  { path: '**', redirectTo: '' }
];
