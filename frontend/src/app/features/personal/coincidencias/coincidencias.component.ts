import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CoincidenciaService } from '../../../core/services/coincidencia.service';
import { AlertService } from '../../../core/services/alert.service';
import { CoincidenciaRevisada, EvaluacionCoincidencia } from '../../../core/interfaces/coincidencia.interface';

@Component({
  selector: 'app-coincidencias',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './coincidencias.component.html',
  styleUrl: './coincidencias.component.css'
})
export class CoincidenciasComponent {
  private coincidenciaService = inject(CoincidenciaService);
  private alertService = inject(AlertService);

  idReportePerdida: number | null = null;
  idHallazgo: number | null = null;
  evaluando = false;
  evaluacion: EvaluacionCoincidencia | null = null;

  observacion = '';
  guardando = false;
  historial: CoincidenciaRevisada[] = [];

  evaluar() {
    if (!this.idReportePerdida || !this.idHallazgo) return;
    this.evaluando = true;
    this.coincidenciaService.evaluar(this.idReportePerdida, this.idHallazgo)
      .pipe(finalize(() => this.evaluando = false))
      .subscribe({
        next: (e) => {
          this.evaluacion = e;
          this.observacion = '';
          this.cargarHistorial(e.idReportePerdida);
        },
        error: (err) => {
          this.evaluacion = null;
          this.alertService.error(err.error?.mensaje || err.error?.message ||
            'No se pudo evaluar. Verifica que el reporte y el hallazgo existan.');
        }
      });
  }

  cargarHistorial(idReporte: number) {
    this.coincidenciaService.porReporte(idReporte).subscribe({
      next: (h) => this.historial = h,
      error: () => this.historial = []
    });
  }

  async decidir(decision: 'CONFIRMADA' | 'DESCARTADA') {
    if (!this.evaluacion) return;
    const obs = this.observacion.trim();
    if (!obs) {
      this.alertService.error('Escribe una observación que justifique la decisión');
      return;
    }
    const texto = decision === 'CONFIRMADA'
      ? `Confirmar que el hallazgo #${this.evaluacion.idHallazgo} corresponde al reporte #${this.evaluacion.idReportePerdida}. Es definitivo.`
      : `Descartar esta pareja. Es definitivo.`;
    const ok = await this.alertService.confirm(texto);
    if (!ok) return;

    this.guardando = true;
    this.coincidenciaService.registrar(this.evaluacion.idReportePerdida, this.evaluacion.idHallazgo, decision, obs)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: (c) => {
          this.alertService.success(`Coincidencia #${c.idCoincidencia} registrada como ${c.estado}.`, 'Decisión guardada');
          this.evaluar();   // refresca: ahora mostrara la decision existente
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo registrar la decisión')
      });
  }

  limpiar() {
    this.idReportePerdida = null;
    this.idHallazgo = null;
    this.evaluacion = null;
    this.historial = [];
    this.observacion = '';
  }
  
  claseBarra(valor: number): string {
    if (valor >= 70) return 'bg-success';
    if (valor >= 40) return 'bg-warning';
    return 'bg-danger';
  }

  claseEstado(estado: string): string {
    const e = (estado || '').toUpperCase();
    if (e.includes('CONFIRM')) return 'bg-success';
    if (e.includes('DESCART')) return 'bg-secondary';
    return 'bg-warning text-dark';
  }
}