import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { CallbackComponent } from './auth/callback.component';
import { LoginComponent } from './auth/login.component';
import { BusinessWorkflowComponent } from './workflow/business-workflow.component';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'auth/login' },
	{ path: 'auth/login', component: LoginComponent },
	{ path: 'auth/callback', component: CallbackComponent },
	{
		path: 'business/apps',
		canActivate: [authGuard],
		component: BusinessWorkflowComponent
	},
	{ path: '**', redirectTo: 'auth/login' }
];
