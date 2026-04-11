import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/api/iam/auth/login') || req.url.includes('/api/iam/auth/refresh')) {
    return next(req);
  }

  const authService = inject(AuthService);
  const token = authService.getAccessToken();
  const tenantId = authService.getTenantId();
  const isTenantScopedApi = req.url.includes('/api/platform') || req.url.includes('/api/application');

  const headers: Record<string, string> = {};

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (tenantId && isTenantScopedApi) {
    headers['X-Tenant-Id'] = tenantId;
  }

  if (Object.keys(headers).length === 0) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: headers
    })
  );
};
