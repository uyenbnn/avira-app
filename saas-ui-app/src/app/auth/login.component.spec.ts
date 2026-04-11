import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [LoginComponent],
			providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
		}).compileComponents();
	});

	it('renders login heading', () => {
		const fixture = TestBed.createComponent(LoginComponent);
		fixture.detectChanges();

		const compiled = fixture.nativeElement as HTMLElement;
		expect(compiled.textContent).toContain('Business User Login');
	});
});
