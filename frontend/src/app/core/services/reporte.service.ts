import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ConteoEstado, ConteoMes, EntregasCategoria, ResumenReclamos, SeguimientoReclamo } from '../interfaces/reporte.interface';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private http = inject(HttpClient);
  //solo PERSONAL y ADMIN
  private apiUrl = '/api/v1/reportes';

  perdidasPorEstado(): Observable<ConteoEstado[]> {
    return this.http.get<ConteoEstado[]>(`${this.apiUrl}/perdidas/por-estado`);
  }

  // NUEVO: "estado" trae el nombre de la zona del campus
  perdidasPorUbicacion(): Observable<ConteoEstado[]> {
    return this.http.get<ConteoEstado[]>(`${this.apiUrl}/perdidas/por-ubicacion`);
  }

  hallazgosPorEstado(): Observable<ConteoEstado[]> {
    return this.http.get<ConteoEstado[]>(`${this.apiUrl}/hallazgos/por-estado`);
  }

  custodiasPorSituacion(): Observable<ConteoEstado[]> {
    return this.http.get<ConteoEstado[]>(`${this.apiUrl}/custodias/por-situacion`);
  }

  entregasPorCategoria(): Observable<EntregasCategoria[]> {
    return this.http.get<EntregasCategoria[]>(`${this.apiUrl}/entregas/por-categoria`);
  }

  perdidasPorMes(): Observable<ConteoMes[]> {
    return this.http.get<ConteoMes[]>(`${this.apiUrl}/perdidas/por-mes`);
  }

  entregasPorMes(): Observable<ConteoMes[]> {
    return this.http.get<ConteoMes[]>(`${this.apiUrl}/entregas/por-mes`);
  }

  reclamosPorEstado(): Observable<ConteoEstado[]> {
    return this.http.get<ConteoEstado[]>(`${this.apiUrl}/reclamos/por-estado`);
  }

  resumenReclamos(): Observable<ResumenReclamos> {
    return this.http.get<ResumenReclamos>(`${this.apiUrl}/reclamos/resumen`);
  }

  seguimientoReclamos(): Observable<SeguimientoReclamo[]> {
    return this.http.get<SeguimientoReclamo[]>(`${this.apiUrl}/reclamos/seguimiento`);
  }
}