import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Notificacion } from '../interfaces/notificacion.interface';

@Injectable({
  providedIn: 'root'
})
export class NotificacionService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/notificaciones';

  mias(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.apiUrl}/mias`);
  }

  marcarLeida(id: number): Observable<Notificacion> {
    return this.http.patch<Notificacion>(`${this.apiUrl}/${id}/leida`, {});
  }
}