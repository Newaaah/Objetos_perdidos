import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CuentaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/admin/estudiantes';

  // Habilitar o restablecer la clave de un estudiante
  definirClave(idPersona: number, nuevaClave: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${idPersona}/clave`, { nuevaClave });
  }
}