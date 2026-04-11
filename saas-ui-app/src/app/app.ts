import { ChangeDetectionStrategy, Component } from '@angular/core';

interface MetricCard {
  readonly label: string;
  readonly value: string;
  readonly trend: string;
  readonly isPositive: boolean;
}

interface ActionItem {
  readonly title: string;
  readonly detail: string;
  readonly href: string;
}

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly appTitle = 'Avira Control Plane';
  protected readonly subtitle =
    'Manage tenants, applications, and runtime posture from one operational workspace.';

  protected readonly metrics: readonly MetricCard[] = [
    {
      label: 'Active Tenants',
      value: '1,284',
      trend: '+8.4% this month',
      isPositive: true
    },
    {
      label: 'Applications Running',
      value: '3,912',
      trend: '+142 in 24h',
      isPositive: true
    },
    {
      label: 'Auth Failures',
      value: '0.6%',
      trend: '-0.2% from baseline',
      isPositive: true
    }
  ];

  protected readonly quickActions: readonly ActionItem[] = [
    {
      title: 'Create tenant',
      detail: 'Provision tenant identity mode and ownership metadata.',
      href: '#create-tenant'
    },
    {
      title: 'Register application',
      detail: 'Attach an app to a tenant and assign auth_mode safely.',
      href: '#register-application'
    },
    {
      title: 'Review auth strategy',
      detail: 'Validate INTERNAL and ANONYMOUS flow configuration.',
      href: '#review-auth'
    }
  ];
}
