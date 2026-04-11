import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('stores tokens after login', () => {
    service.login({ tenantId: 't1', username: 'user', password: 'pw' }).subscribe();

    const req = httpMock.expectOne('/api/iam/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      accessToken: 'a1',
      refreshToken: 'r1',
      expiresIn: 300,
      tokenType: 'Bearer'
    });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.getAccessToken()).toBe('a1');
    expect(service.getTenantId()).toBe('t1');
  });

  it('clears tokens and tenant on logout', () => {
    service.login({ tenantId: 'tenant-a', username: 'user', password: 'pw' }).subscribe();

    const req = httpMock.expectOne('/api/iam/auth/login');
    req.flush({
      accessToken: 'a1',
      refreshToken: 'r1',
      expiresIn: 300,
      tokenType: 'Bearer'
    });

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.getAccessToken()).toBeNull();
    expect(service.getTenantId()).toBeNull();
  });

  it('completes callback when tokens exist in query', () => {
    const success = service.completeCallback(
      new URLSearchParams('accessToken=aaa&refreshToken=rrr&expiresIn=300')
    );

    expect(success).toBe(true);
    expect(service.isAuthenticated()).toBe(true);
    expect(service.getAccessToken()).toBe('aaa');
  });
});
