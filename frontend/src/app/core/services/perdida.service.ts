import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrearPerdidaRequest, MiPerdida } from '../interfaces/perdida.interface';

@Injectable({
  providedIn: 'root'
})
export class PerdidaService {
  private http = inject(HttpClient);
  //usamos la ruta del backend
  private apiUrl = '/api/v1/perdidas';

  // ESTUDIANTE
  crear(datos: CrearPerdidaRequest): Observable<unknown> {
    return this.http.post(this.apiUrl, datos);
  }

  // ESTUDIANTE: solo mis reportes
  misPerdidas(): Observable<MiPerdida[]> {
    return this.http.get<MiPerdida[]>(`${this.apiUrl}/mias`);
  }

  // ESTUDIANTE: ya recupere mi objeto (solo mi propio reporte)
  marcarRecuperado(idReportePerdida: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${idReportePerdida}/recuperado`, {});
  }

  // PERSONAL / ADMIN: reportes en busqueda de una persona (al registrar una entrega)
  abiertas(idPersona: number): Observable<MiPerdida[]> {
    return this.http.get<MiPerdida[]>(`${this.apiUrl}/abiertas`, { params: { idPersona } });
  }
}