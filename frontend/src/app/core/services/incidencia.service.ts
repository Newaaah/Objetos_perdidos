import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Reclamo } from '../interfaces/reclamo.interface';

// Las incidencias usan la misma respuesta que los reclamos (CasoRegistradoDTO)
@Injectable({
  providedIn: 'root'
})
export class IncidenciaService {
  private http = inject(HttpClient);
  //solo PERSONAL y ADMIN
  private apiUrl = '/api/v1/incidencias';

  // Registrar: FormData porque puede llevar foto de evidencia
  crear(datos: FormData): Observable<Reclamo> {
    return this.http.post<Reclamo>(this.apiUrl, datos);
  }

  listar(estado?: string): Observable<Reclamo[]> {
    const params: Record<string, string> = {};
    if (estado) params['estado'] = estado;
    return this.http.get<Reclamo[]>(this.apiUrl, { params });
  }

  evidencia(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/evidencia`, { responseType: 'blob' });
  }

  // Pasar a EN_REVISION o cerrar como RESUELTO
  revisar(id: number, estado: string, resolucion?: string): Observable<Reclamo> {
    return this.http.patch<Reclamo>(`${this.apiUrl}/${id}/revision`, { estado, resolucion });
  }
}