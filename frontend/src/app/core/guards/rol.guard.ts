import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const rolGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const roles = (route.data?.['roles'] ?? []) as string[];

  if (!authService.getToken()) {
    return router.createUrlTree(['/login']);
  }

  if (roles.length && !authService.tieneRol(...roles)) {
    return router.createUrlTree([authService.rutaInicio()]);
  }
  return true;
};

export const invitadoGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.getToken() ? router.createUrlTree([authService.rutaInicio()]) : true;
};