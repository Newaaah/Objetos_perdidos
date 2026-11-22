import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EntregaRegistrada, EstadoCustodiaEntrega, MiEntrega } from '../interfaces/entrega.interface';

@Injectable({
  providedIn: 'root'
})
export class EntregaService {
  private http = inject(HttpClient);
  //solo PERSONAL y ADMIN
  private apiUrl = '/api/v1/entregas';

  estadoCustodia(idCustodia: number): Observable<EstadoCustodiaEntrega> {
    return this.http.get<EstadoCustodiaEntrega>(`${this.apiUrl}/custodias/${idCustodia}/estado`);
  }

  registrar(datos: FormData): Observable<EntregaRegistrada> {
    return this.http.post<EntregaRegistrada>(this.apiUrl, datos);
  }

  mias(): Observable<MiEntrega[]> {
    return this.http.get<MiEntrega[]>(`${this.apiUrl}/mias`);
  }
}