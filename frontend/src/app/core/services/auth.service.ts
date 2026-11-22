import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, map, Observable, of, tap } from 'rxjs';
import { ILoginRequest, ILoginResponse } from '../interfaces/auth.interface';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = '/api/v1/auth';
  private loggedIn = new BehaviorSubject<boolean>(!!localStorage.getItem('token_objetos'));
  isLoggedIn$ = this.loggedIn.asObservable();

  login(credentials: ILoginRequest): Observable<boolean> {
    return this.http.post<ILoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response && response.token) {
          localStorage.setItem('token_objetos', response.token);
          localStorage.setItem('user_id_persona', response.idPersona.toString());
          localStorage.setItem('user_role', response.rol);
          localStorage.setItem('user_name', credentials.ingreso);
          this.loggedIn.next(true);
          this.router.navigate([this.rutaInicio(response.rol)]);
        }
      }),
      map(response => !!response.token),
      catchError(() => {
        this.logout();
        return of(false);
      })
    );
  }

  logout(): void {
    localStorage.clear();
    this.loggedIn.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token_objetos');
  }

  getRol(): string | null {
    return localStorage.getItem('user_role');
  }

  rutaInicio(rol: string | null = this.getRol()): string {
    switch (rol) {
      case 'ADMIN': return '/admin';
      case 'PERSONAL': return '/personal';
      default: return '/home';
    }
  }

  tieneRol(...roles: string[]): boolean {
    const rol = this.getRol();
    return !!rol && roles.includes(rol);
  }

  cambiarClave(claveActual: string, nuevaClave: string): Observable<void> {
    return this.http.patch<void>('/api/v1/cuenta/clave', { claveActual, nuevaClave });
  }

}