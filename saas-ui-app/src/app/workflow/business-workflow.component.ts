import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
    ApplicationResponse,
    PlatformApiService,
    TenantResponse
} from './platform-api.service';

@Component({
  selector: 'app-business-workflow',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="panel stack">
      <header>
        <h1>Business User App Setup</h1>
        <p class="hint">Create tenant then register applications via platform-service endpoints.</p>
      </header>

      <form [formGroup]="tenantForm" (ngSubmit)="createTenant()" class="stack">
        <h2>1) Create Tenant</h2>
        <label>
          Tenant Name
          <input type="text" formControlName="name" />
        </label>
        <label>
          Contact Email
          <input type="email" formControlName="contactEmail" />
        </label>
        <button type="submit" [disabled]="tenantForm.invalid || savingTenant()">Create tenant</button>
      </form>

      @if (tenant()) {
        <section class="tenant-summary">
          <strong>Active tenant:</strong> {{ tenant()?.name }} ({{ tenant()?.tenantId }})
        </section>
      }

      <form [formGroup]="appForm" (ngSubmit)="createApp()" class="stack" [class.disabled]="!tenant()">
        <h2>2) Register Application</h2>
        <label>
          App Name
          <input type="text" formControlName="name" [disabled]="!tenant()" />
        </label>
        <label>
          Domain
          <input type="text" formControlName="domain" [disabled]="!tenant()" />
        </label>
        <label>
          Auth Mode
          <select formControlName="authMode" [disabled]="!tenant()">
            <option value="KEYCLOAK">KEYCLOAK</option>
            <option value="CUSTOM_JWT">CUSTOM_JWT</option>
            <option value="PASSTHROUGH">PASSTHROUGH</option>
          </select>
        </label>
        <button type="submit" [disabled]="appForm.invalid || !tenant() || savingApp()">Create app</button>
      </form>

      <section class="stack" [class.disabled]="!tenant()">
        <h2>3) Existing Applications</h2>
        <button type="button" (click)="loadApps()" [disabled]="!tenant()">Refresh list</button>

        @if (apps().length === 0) {
          <p class="hint">No applications loaded.</p>
        } @else {
          <ul>
            @for (item of apps(); track item.appId) {
              <li>{{ item.name }} | {{ item.domain }} | {{ item.authMode }} | {{ item.status }}</li>
            }
          </ul>
        }
      </section>

      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
      @if (success()) {
        <p class="success">{{ success() }}</p>
      }
    </section>
  `,
  styles: `
    .panel { max-width: 820px; margin: 1.5rem auto; padding: 1rem; border: 1px solid #ccd9cf; border-radius: 12px; background: #fff; }
    .stack { display: grid; gap: 0.8rem; }
    .hint { color: #4a5f51; }
    label { display: grid; gap: 0.35rem; font-weight: 600; }
    input, select { border: 1px solid #8ba996; border-radius: 8px; padding: 0.6rem; font: inherit; }
    button { justify-self: start; border: 0; background: #0e6f54; color: #fff; border-radius: 8px; padding: 0.55rem 1rem; cursor: pointer; }
    .disabled { opacity: 0.7; }
    .tenant-summary { background: #f2f8f4; border: 1px solid #d4e8dc; border-radius: 10px; padding: 0.65rem; }
    .error { color: #a31313; font-weight: 600; }
    .success { color: #0e6f54; font-weight: 600; }
  `
})
export class BusinessWorkflowComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly platformApi = inject(PlatformApiService);

  protected readonly tenant = signal<TenantResponse | null>(null);
  protected readonly apps = signal<ApplicationResponse[]>([]);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected readonly savingTenant = signal(false);
  protected readonly savingApp = signal(false);

  protected readonly tenantForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    contactEmail: ['', [Validators.required, Validators.email]]
  });

  protected readonly appForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    domain: ['', Validators.required],
    authMode: ['KEYCLOAK' as const, Validators.required]
  });

  protected createTenant(): void {
    if (this.tenantForm.invalid) {
      return;
    }

    this.savingTenant.set(true);
    this.error.set('');
    this.success.set('');

    this.platformApi.createTenant(this.tenantForm.getRawValue()).subscribe({
      next: (tenant) => {
        this.savingTenant.set(false);
        this.tenant.set(tenant);
        this.success.set('Tenant created. Continue with application registration.');
      },
      error: () => {
        this.savingTenant.set(false);
        this.error.set('Tenant creation failed.');
      }
    });
  }

  protected createApp(): void {
    const activeTenant = this.tenant();

    if (!activeTenant || this.appForm.invalid) {
      return;
    }

    this.savingApp.set(true);
    this.error.set('');
    this.success.set('');

    this.platformApi.createApplication(activeTenant.tenantId, this.appForm.getRawValue()).subscribe({
      next: (app) => {
        this.savingApp.set(false);
        this.apps.update((current) => [app, ...current]);
        this.success.set('Application registered successfully.');
      },
      error: () => {
        this.savingApp.set(false);
        this.error.set('Application registration failed.');
      }
    });
  }

  protected loadApps(): void {
    const activeTenant = this.tenant();

    if (!activeTenant) {
      return;
    }

    this.error.set('');
    this.success.set('');

    this.platformApi.listApplications(activeTenant.tenantId).subscribe({
      next: (apps) => this.apps.set(apps),
      error: () => this.error.set('Loading applications failed.')
    });
  }
}
