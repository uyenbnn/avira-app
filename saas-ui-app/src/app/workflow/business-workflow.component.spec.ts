import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BusinessWorkflowComponent } from './business-workflow.component';

describe('BusinessWorkflowComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BusinessWorkflowComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates tenant and shows tenant summary', () => {
    const fixture = TestBed.createComponent(BusinessWorkflowComponent);
    const component = fixture.componentInstance as any;

    component.tenantForm.setValue({
      name: 'Acme',
      contactEmail: 'owner@acme.dev'
    });

    component.createTenant();

    const req = httpMock.expectOne('/api/platform/tenants');
    expect(req.request.method).toBe('POST');
    req.flush({
      tenantId: 'tenant-1',
      name: 'Acme',
      contactEmail: 'owner@acme.dev',
      identityMode: 'SHARED_REALM',
      status: 'ACTIVE',
      createdAt: '2026-04-11T00:00:00Z'
    });

    expect(component.tenant()?.tenantId).toBe('tenant-1');
  });
});
