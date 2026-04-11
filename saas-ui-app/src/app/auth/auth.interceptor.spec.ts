import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('keeps auth endpoints unmodified', () => {
    localStorage.setItem('saas.accessToken', 'token-1');
    localStorage.setItem('saas.tenantId', 'tenant-1');

    http.post('/api/iam/auth/login', { tenantId: 'tenant-1', username: 'u', password: 'p' }).subscribe();

    const req = httpMock.expectOne('/api/iam/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    expect(req.request.headers.has('X-Tenant-Id')).toBe(false);
    req.flush({});
  });

  it('adds Authorization header when token exists', () => {
    localStorage.setItem('saas.accessToken', 'token-1');

    http.get('/api/something').subscribe();

    const req = httpMock.expectOne('/api/something');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-1');
    expect(req.request.headers.has('X-Tenant-Id')).toBe(false);
    req.flush({});
  });

  it('adds tenant header for platform calls when tenant exists', () => {
    localStorage.setItem('saas.tenantId', 'tenant-1');

    http.get('/api/platform/tenants').subscribe();

    const req = httpMock.expectOne('/api/platform/tenants');
    expect(req.request.headers.get('X-Tenant-Id')).toBe('tenant-1');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('adds both Authorization and tenant headers for application calls', () => {
    localStorage.setItem('saas.accessToken', 'token-1');
    localStorage.setItem('saas.tenantId', 'tenant-1');

    http.get('/api/application/apps').subscribe();

    const req = httpMock.expectOne('/api/application/apps');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-1');
    expect(req.request.headers.get('X-Tenant-Id')).toBe('tenant-1');
    req.flush({});
  });
});
