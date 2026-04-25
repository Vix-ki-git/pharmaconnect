import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Redirects already-logged-in users away from auth pages to their role's home
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) return true;
  const role = auth.getRole();
  if (role === 'ADMIN') return router.createUrlTree(['/admin/sellers']);
  if (role === 'SELLER') return router.createUrlTree(['/seller/dashboard']);
  return router.createUrlTree(['/search']);
};
