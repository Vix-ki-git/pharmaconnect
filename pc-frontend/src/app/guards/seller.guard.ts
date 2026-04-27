import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const sellerGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.getRole() === 'SELLER') return true;
  return router.createUrlTree(auth.isLoggedIn() ? ['/'] : ['/login']);
};
