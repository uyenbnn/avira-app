import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-callback',
  standalone: true,
  template: `
    <section class="panel">
      <h1>Auth Callback</h1>
      <p>{{ message }}</p>
    </section>
  `,
  styles: `
    .panel { max-width: 540px; margin: 1.5rem auto; padding: 1rem; border: 1px solid #ccd9cf; border-radius: 12px; background: #fff; }
  `
})
export class CallbackComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected message = 'Finalizing sign-in...';

  constructor() {
    const success = this.authService.completeCallback(new URLSearchParams(window.location.search));

    if (success) {
      this.message = 'Sign-in complete. Redirecting to business workflow...';
      void this.router.navigateByUrl('/business/apps');
      return;
    }

    this.message = 'Missing callback tokens. Redirecting to login...';
    void this.router.navigateByUrl('/auth/login');
  }
}
