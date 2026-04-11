import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface TenantRequest {
  name: string;
  contactEmail: string;
}

export interface TenantResponse {
  tenantId: string;
  name: string;
  contactEmail: string;
  identityMode: 'SHARED_REALM';
  status: 'ACTIVE' | 'SUSPENDED' | 'PENDING';
  createdAt: string;
}

export interface ApplicationRequest {
  name: string;
  domain: string;
  authMode: 'KEYCLOAK' | 'CUSTOM_JWT' | 'PASSTHROUGH';
}

export interface ApplicationResponse {
  appId: string;
  tenantId: string;
  name: string;
  domain: string;
  authMode: 'KEYCLOAK' | 'CUSTOM_JWT' | 'PASSTHROUGH';
  status: 'ACTIVE' | 'INACTIVE' | 'PROVISIONING';
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class PlatformApiService {
  constructor(private readonly http: HttpClient) {}

  createTenant(payload: TenantRequest): Observable<TenantResponse> {
    return this.http.post<TenantResponse>('/api/platform/tenants', payload);
  }

  createApplication(tenantId: string, payload: ApplicationRequest): Observable<ApplicationResponse> {
    return this.http.post<ApplicationResponse>(`/api/platform/tenants/${tenantId}/applications`, payload);
  }

  listApplications(tenantId: string): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(`/api/platform/tenants/${tenantId}/applications`);
  }
}
