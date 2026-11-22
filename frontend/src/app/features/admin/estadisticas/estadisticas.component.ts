import { Component, inject, OnInit } from '@angular/core';
import { DatePipe, DecimalPipe, NgTemplateOutlet } from '@angular/common';
import { catchError, forkJoin, of } from 'rxjs';
import { ReporteService } from '../../../core/services/reporte.service';
import { ConteoEstado, EntregasCategoria, ResumenReclamos, SeguimientoReclamo } from '../../../core/interfaces/reporte.interface';

interface FilaMes {
  periodo: string;
  perdidas: number;
  entregas: number;
}

@Component({
  selector: 'app-estadisticas',
  standalone: true,
  imports: [DatePipe, DecimalPipe, NgTemplateOutlet],
  templateUrl: './estadisticas.component.html',
  styleUrl: './estadisticas.component.css'
})
export class EstadisticasComponent implements OnInit {
  private reporteService = inject(ReporteService);

  cargando = true;
  perdidas: ConteoEstado[] = [];
  hallazgos: ConteoEstado[] = [];
  custodias: ConteoEstado[] = [];
  reclamos: ConteoEstado[] = [];
  categorias: EntregasCategoria[] = [];
  meses: FilaMes[] = [];
  resumen: ResumenReclamos | null = null;
  seguimiento: SeguimientoReclamo[] = [];

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    this.cargando = true;
    // Todas las consultas a la vez; si una falla, las demas siguen
    forkJoin({
      perdidas: this.reporteService.perdidasPorEstado().pipe(catchError(() => of([]))),
      hallazgos: this.reporteService.hallazgosPorEstado().pipe(catchError(() => of([]))),
      custodias: this.reporteService.custodiasPorSituacion().pipe(catchError(() => of([]))),
      reclamos: this.reporteService.reclamosPorEstado().pipe(catchError(() => of([]))),
      categorias: this.reporteService.entregasPorCategoria().pipe(catchError(() => of([]))),
      perdidasMes: this.reporteService.perdidasPorMes().pipe(catchError(() => of([]))),
      entregasMes: this.reporteService.entregasPorMes().pipe(catchError(() => of([]))),
      resumen: this.reporteService.resumenReclamos().pipe(catchError(() => of(null))),
      seguimiento: this.reporteService.seguimientoReclamos().pipe(catchError(() => of([])))
    }).subscribe(d => {
      this.perdidas = d.perdidas;
      this.hallazgos = d.hallazgos;
      this.custodias = d.custodias;
      this.reclamos = d.reclamos;
      this.categorias = d.categorias;
      this.resumen = d.resumen;
      this.seguimiento = d.seguimiento;
      this.meses = this.unirMeses(d.perdidasMes, d.entregasMes);
      this.cargando = false;
    });
  }

  // Suma de una lista de conteos
  total(lista: ConteoEstado[]): number {
    return lista.reduce((s, x) => s + Number(x.total), 0);
  }

  // Ancho de la barra en % respecto al mayor de la lista
  ancho(valor: number, lista: ConteoEstado[]): number {
    const max = Math.max(...lista.map(x => Number(x.total)), 1);
    return (Number(valor) / max) * 100;
  }

  // Alto de las barras por mes respecto al mayor valor
  get maxMes(): number {
    return Math.max(...this.meses.flatMap(m => [m.perdidas, m.entregas]), 1);
  }

  // Junta "perdidas por mes" y "entregas por mes" en una sola tabla por periodo
  private unirMeses(perdidas: { periodo: string, total: number }[], entregas: { periodo: string, total: number }[]): FilaMes[] {
    const mapa = new Map<string, FilaMes>();
    perdidas.forEach(p => mapa.set(p.periodo, { periodo: p.periodo, perdidas: Number(p.total), entregas: 0 }));
    entregas.forEach(e => {
      const fila = mapa.get(e.periodo) || { periodo: e.periodo, perdidas: 0, entregas: 0 };
      fila.entregas = Number(e.total);
      mapa.set(e.periodo, fila);
    });
    return [...mapa.values()].sort((a, b) => a.periodo.localeCompare(b.periodo));
  }

  colorEstado(estado: string): string {
    const e = (estado || '').toUpperCase();
    if (e.includes('RECUPER') || e.includes('APROB') || e.includes('ENTREG') || e.includes('RESUEL')) return 'bg-success';
    if (e.includes('PEND') || e.includes('REPORT')) return 'bg-warning';
    if (e.includes('RECHAZ') || e.includes('CANCEL')) return 'bg-danger';
    if (e.includes('REVISION')) return 'bg-info';
    return 'bg-primary';
  }
}