import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Reclamo } from '../interfaces/reclamo.interface';

@Injectable({
  providedIn: 'root'
})
export class ReclamoService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/reclamos';

  // ===== ESTUDIANTE =====

  // Presentar reclamo: FormData porque puede llevar foto de evidencia
  crear(datos: FormData): Observable<Reclamo> {
    return this.http.post<Reclamo>(this.apiUrl, datos);
  }

  // Solo MIS reclamos
  mios(): Observable<Reclamo[]> {
    return this.http.get<Reclamo[]>(`${this.apiUrl}/mios`);
  }

  // ===== PERSONAL / ADMIN =====

  // Bandeja (estado opcional: PENDIENTE, EN_REVISION, APROBADO, RECHAZADO)
  listar(estado?: string): Observable<Reclamo[]> {
    const params: Record<string, string> = {};
    if (estado) params['estado'] = estado;
    return this.http.get<Reclamo[]>(this.apiUrl, { params });
  }

  // Foto de evidencia (privada: va con token)
  evidencia(idReclamo: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${idReclamo}/evidencia`, { responseType: 'blob' });
  }

  // Pasar a EN_REVISION o cerrar como APROBADO / RECHAZADO
  revisar(idReclamo: number, estado: string, resolucion?: string): Observable<Reclamo> {
    return this.http.patch<Reclamo>(`${this.apiUrl}/${idReclamo}/revision`, { estado, resolucion });
  }
}