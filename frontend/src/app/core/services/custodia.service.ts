import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CustodiaRegistrada, PendienteRecepcion } from '../interfaces/custodia.interface';

@Injectable({
  providedIn: 'root'
})
export class CustodiaService {
  private http = inject(HttpClient);
  //solo PERSONAL y ADMIN
  private apiUrl = '/api/v1/custodias/recepcion';

  // Hallazgos aprobados que aun no llegan fisicamente
  pendientes(): Observable<PendienteRecepcion[]> {
    return this.http.get<PendienteRecepcion[]>(`${this.apiUrl}/pendientes`);
  }

  // Recepcion de un hallazgo aprobado (foto opcional)
  recibirConHallazgo(datos: FormData): Observable<CustodiaRegistrada> {
    return this.http.post<CustodiaRegistrada>(`${this.apiUrl}/con-hallazgo`, datos);
  }

  // Recepcion sin reporte previo (foto obligatoria)
  recibirDirecta(datos: FormData): Observable<CustodiaRegistrada> {
    return this.http.post<CustodiaRegistrada>(`${this.apiUrl}/directa`, datos);
  }
}