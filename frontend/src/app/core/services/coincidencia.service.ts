import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CandidatosRevision, CoincidenciaRevisada, EvaluacionCoincidencia, ResultadoSugerencias
} from '../interfaces/coincidencia.interface';

@Injectable({
  providedIn: 'root'
})
export class CoincidenciaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/coincidencias';

  // ===== ESTUDIANTE =====
  // Sugerencias para UNO de mis reportes (el backend valida que sea mio)
  sugerenciasMias(idReportePerdida: number): Observable<ResultadoSugerencias> {
    return this.http.get<ResultadoSugerencias>(`${this.apiUrl}/sugerencias/mias/${idReportePerdida}`);
  }

  // ===== PERSONAL / ADMIN =====
  // Perdidas en busqueda y hallazgos aprobados para elegir la pareja
  candidatos(): Observable<CandidatosRevision> {
    return this.http.get<CandidatosRevision>(`${this.apiUrl}/revision/candidatos`);
  }

  evaluar(idReportePerdida: number, idHallazgo: number): Observable<EvaluacionCoincidencia> {
    return this.http.get<EvaluacionCoincidencia>(`${this.apiUrl}/revision/evaluar`,
      { params: { idReportePerdida, idHallazgo } });
  }

  registrar(idReportePerdida: number, idHallazgo: number,
            decision: 'CONFIRMADA' | 'DESCARTADA', observacion: string): Observable<CoincidenciaRevisada> {
    return this.http.post<CoincidenciaRevisada>(`${this.apiUrl}/revision`,
      { idReportePerdida, idHallazgo, decision, observacion });
  }

  porReporte(idReportePerdida: number): Observable<CoincidenciaRevisada[]> {
    return this.http.get<CoincidenciaRevisada[]>(`${this.apiUrl}/revision/reporte/${idReportePerdida}`);
  }

  // Foto privada del hallazgo (solo PERSONAL / ADMIN, va con token)
  imagenHallazgo(idHallazgo: number): Observable<Blob> {
    return this.http.get(`/api/v1/hallazgos/revision/${idHallazgo}/imagen`, { responseType: 'blob' });
  }
}