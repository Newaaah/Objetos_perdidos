import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Objeto, ObjetoEntregado } from '../interfaces/objeto.interface';

@Injectable({
  providedIn: 'root'
})
export class ObjetoService {
  private http = inject(HttpClient);
  //usamos la ruta publica del backend
  private apiUrl = '/api/v1/publico/objetos';

  getObjetos(): Observable<Objeto[]> {
    return this.http.get<Objeto[]>(`${this.apiUrl}/disponibles`);
  }

  getEntregados(dias: number): Observable<ObjetoEntregado[]> {
    return this.http.get<ObjetoEntregado[]>(`${this.apiUrl}/entregados`, { params: { dias } });
  }
}