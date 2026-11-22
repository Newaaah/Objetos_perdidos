import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HallazgoPendiente, MiHallazgo } from '../interfaces/hallazgo.interface';

@Injectable({
  providedIn: 'root'
})
export class HallazgoService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/hallazgos';

  // Registrar hallazgo: va como FormData porque lleva la foto
  crear(datos: FormData): Observable<unknown> {
    return this.http.post(this.apiUrl, datos);
  }

  // Solo MIS hallazgos (el backend filtra por el token)
  misHallazgos(): Observable<MiHallazgo[]> {
    return this.http.get<MiHallazgo[]>(`${this.apiUrl}/mios`);
  }

  pendientes(): Observable<HallazgoPendiente[]> {
    return this.http.get<HallazgoPendiente[]>(`${this.apiUrl}/revision/pendientes`);
  }

  // La foto es privada: la pedimos con HttpClient (lleva el token) como archivo
  imagenRevision(idHallazgo: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/revision/${idHallazgo}/imagen`, { responseType: 'blob' });
  }

  resolver(idHallazgo: number, decision: 'APROBADO' | 'RECHAZADO', observacion?: string): Observable<unknown> {
    return this.http.patch(`${this.apiUrl}/revision/${idHallazgo}/decision`, { decision, observacion });
  }
}