import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
	tenantId: string;
	username: string;
	password: string;
	appId?: string;
}

export interface TokenResponse {
	accessToken: string;
	refreshToken: string;
	expiresIn: number;
	tokenType: string;
}

const ACCESS_TOKEN_KEY = 'saas.accessToken';
const REFRESH_TOKEN_KEY = 'saas.refreshToken';
const TENANT_ID_KEY = 'saas.tenantId';

@Injectable({ providedIn: 'root' })
export class AuthService {
	private readonly authenticated = signal<boolean>(this.hasStoredToken());

	constructor(private readonly http: HttpClient) {}

	login(payload: LoginRequest): Observable<TokenResponse> {
		this.storeTenantId(payload.tenantId);

		return this.http
			.post<TokenResponse>('/api/iam/auth/login', payload)
			.pipe(tap((tokens) => this.storeTokens(tokens)));
	}

	refresh(refreshToken: string, tenantId: string): Observable<TokenResponse> {
		this.storeTenantId(tenantId);

		return this.http
			.post<TokenResponse>('/api/iam/auth/refresh', { refreshToken, tenantId })
			.pipe(tap((tokens) => this.storeTokens(tokens)));
	}

	completeCallback(params: URLSearchParams): boolean {
		const accessToken = params.get('accessToken');
		const refreshToken = params.get('refreshToken');
		const expiresIn = Number(params.get('expiresIn') ?? 300);

		if (!accessToken || !refreshToken) {
			return false;
		}

		this.storeTokens({
			accessToken,
			refreshToken,
			expiresIn,
			tokenType: 'Bearer'
		});

		return true;
	}

	getAccessToken(): string | null {
		return localStorage.getItem(ACCESS_TOKEN_KEY);
	}

	getTenantId(): string | null {
		return localStorage.getItem(TENANT_ID_KEY);
	}

	isAuthenticated(): boolean {
		return this.authenticated();
	}

	logout(): void {
		localStorage.removeItem(ACCESS_TOKEN_KEY);
		localStorage.removeItem(REFRESH_TOKEN_KEY);
		localStorage.removeItem(TENANT_ID_KEY);
		this.authenticated.set(false);
	}

	private hasStoredToken(): boolean {
		return !!localStorage.getItem(ACCESS_TOKEN_KEY);
	}

	private storeTokens(tokens: TokenResponse): void {
		localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
		localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
		this.authenticated.set(true);
	}

	private storeTenantId(tenantId: string): void {
		localStorage.setItem(TENANT_ID_KEY, tenantId);
	}
}
