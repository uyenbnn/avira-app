import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
	selector: 'app-login',
	standalone: true,
	imports: [CommonModule, ReactiveFormsModule],
	template: `
		<section class="panel">
			<h1>Business User Login</h1>
			<p class="hint">Uses /api/iam/auth/login and stores token pair locally for MVP flow testing.</p>

			<form [formGroup]="loginForm" (ngSubmit)="submit()" class="stack">
				<label>
					Tenant ID
					<input type="text" formControlName="tenantId" />
				</label>

				<label>
					Username
					<input type="text" formControlName="username" />
				</label>

				<label>
					Password
					<input type="password" formControlName="password" />
				</label>

				<label>
					App ID (optional)
					<input type="text" formControlName="appId" />
				</label>

				<button type="submit" [disabled]="loginForm.invalid || loading()">{{ loading() ? 'Signing in...' : 'Sign in' }}</button>
			</form>

			@if (error()) {
				<p class="error">{{ error() }}</p>
			}
		</section>
	`,
	styles: `
		.panel { max-width: 540px; margin: 1.5rem auto; padding: 1rem; border: 1px solid #ccd9cf; border-radius: 12px; background: #fff; }
		.hint { margin-top: 0; color: #4a5f51; }
		.stack { display: grid; gap: 0.8rem; }
		label { display: grid; gap: 0.35rem; font-weight: 600; }
		input { border: 1px solid #8ba996; border-radius: 8px; padding: 0.6rem; font: inherit; }
		button { justify-self: start; border: 0; background: #0e6f54; color: #fff; border-radius: 8px; padding: 0.55rem 1rem; cursor: pointer; }
		button:disabled { opacity: 0.6; cursor: not-allowed; }
		.error { color: #a31313; font-weight: 600; }
	`
})
export class LoginComponent {
	private readonly formBuilder = inject(FormBuilder);
	private readonly authService = inject(AuthService);
	private readonly router = inject(Router);

	protected readonly loading = signal(false);
	protected readonly error = signal('');

	protected readonly loginForm = this.formBuilder.nonNullable.group({
		tenantId: ['', Validators.required],
		username: ['', Validators.required],
		password: ['', Validators.required],
		appId: ['']
	});

	protected submit(): void {
		if (this.loginForm.invalid) {
			return;
		}

		this.loading.set(true);
		this.error.set('');

		this.authService.login(this.loginForm.getRawValue()).subscribe({
			next: () => {
				this.loading.set(false);
				this.router.navigateByUrl('/business/apps');
			},
			error: () => {
				this.loading.set(false);
				this.error.set('Login failed. Verify tenant and credentials.');
			}
		});
	}
}
