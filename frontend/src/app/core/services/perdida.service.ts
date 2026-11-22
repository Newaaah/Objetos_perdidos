import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrearPerdidaRequest, MiPerdida } from '../interfaces/perdida.interface';

@Injectable({
  providedIn: 'root'
})
export class PerdidaService {
  private http = inject(HttpClient);
  //usamos la ruta del backend (solo ESTUDIANTE)
  private apiUrl = '/api/v1/perdidas';

  crear(datos: CrearPerdidaRequest): Observable<unknown> {
    return this.http.post(this.apiUrl, datos);
  }

  misPerdidas(): Observable<MiPerdida[]> {
    return this.http.get<MiPerdida[]>(`${this.apiUrl}/mias`);
  }
}