import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { PerdidaService } from '../../../core/services/perdida.service';
import { CoincidenciaService } from '../../../core/services/coincidencia.service';
import { AlertService } from '../../../core/services/alert.service';
import { MiPerdida } from '../../../core/interfaces/perdida.interface';
import { ResultadoSugerencias } from '../../../core/interfaces/coincidencia.interface';

@Component({
  selector: 'app-mis-reportes',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './mis-reportes.component.html',
  styleUrl: './mis-reportes.component.css'
})
export class MisReportesComponent implements OnInit {
  private perdidaService = inject(PerdidaService);
  private coincidenciaService = inject(CoincidenciaService);
  private alertService = inject(AlertService);

  reportes: MiPerdida[] = [];
  cargando = true;

  seleccionado: MiPerdida | null = null;
  resultado: ResultadoSugerencias | null = null;
  buscandoSugerencias = false;

  ngOnInit(): void {
    this.loadingReportes();
  }

  loadingReportes() {
    this.cargando = true;
    this.perdidaService.misPerdidas().subscribe({
      next: (data) => {
        // los mas recientes primero
        this.reportes = data.sort((a, b) => b.idReportePerdida - a.idReportePerdida);
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar tus reportes');
      }
    });
  }

  verSugerencias(r: MiPerdida) {
    this.seleccionado = r;
    this.resultado = null;
    this.buscandoSugerencias = true;
    this.coincidenciaService.sugerenciasMias(r.idReportePerdida)
      .pipe(finalize(() => this.buscandoSugerencias = false))
      .subscribe({
        next: (data) => this.resultado = data,
        error: (err) => {
          this.seleccionado = null;
          this.alertService.error(err.error?.mensaje || err.error?.message || 'No se pudieron obtener sugerencias');
        }
      });
  }

  cerrarSugerencias() {
    this.seleccionado = null;
    this.resultado = null;
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'REPORTADO': return 'bg-warning text-dark';
      case 'RECUPERADO': return 'bg-success';
      case 'CANCELADO': return 'bg-secondary';
      default: return 'bg-light text-dark';
    }
  }

  claseNivel(nivel: string): string {
    const n = (nivel || '').toUpperCase();
    if (n.includes('ALT')) return 'bg-success';
    if (n.includes('MED')) return 'bg-warning text-dark';
    return 'bg-secondary';
  }

  textoOrigen(origen: string): string {
    const o = (origen || '').toUpperCase();
    if (o.includes('CUSTODIA')) return 'Objeto en custodia';
    if (o.includes('HALLAZGO')) return 'Objeto reportado como encontrado';
    return origen;
  }
}