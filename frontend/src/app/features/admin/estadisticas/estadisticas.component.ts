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

// Una porcion de cualquier torta
interface Porcion {
  clave: string;
  nombre: string;
  total: number;
  pct: number;      // % del total
  offset: number;   // donde empieza la porcion en el circulo
  color: string;
  entregadas?: number;     // solo torta de categorias
  pctEntregadas?: number;  // solo torta de categorias
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
  meses: FilaMes[] = [];
  resumen: ResumenReclamos | null = null;
  seguimiento: SeguimientoReclamo[] = [];

  // Tortas
  porcionesCategoria: Porcion[] = [];
  totalCategoria = 0;
  porcionesLugar: Porcion[] = [];
  totalLugar = 0;

  private coloresCategoria = ['#003A6C', '#FDB913', '#22c55e', '#ef4444', '#8b5cf6',
                              '#06b6d4', '#f97316', '#ec4899', '#14b8a6', '#64748b'];
  private coloresLugar = ['#0b5aa0', '#f59e0b', '#10b981', '#e11d48', '#7c3aed',
                          '#0891b2', '#f97316', '#94a3b8'];

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    this.cargando = true;
    // Todas las consultas a la vez; si una falla, las demas siguen
    forkJoin({
      perdidas: this.reporteService.perdidasPorEstado().pipe(catchError(() => of([]))),
      lugares: this.reporteService.perdidasPorUbicacion().pipe(catchError(() => of([]))),
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
      this.resumen = d.resumen;
      this.seguimiento = d.seguimiento;
      this.meses = this.unirMeses(d.perdidasMes, d.entregasMes);
      this.armarTortaCategoria(d.categorias);
      this.armarTortaLugar(d.lugares);
      this.cargando = false;
    });
  }

  // ===== TORTAS =====

  // Convierte una lista {nombre, total} en porciones del circulo (el borde mide 100)
  private porciones(items: { clave: string; nombre: string; total: number }[], colores: string[]): Porcion[] {
    const total = items.reduce((s, x) => s + x.total, 0);
    let acumulado = 0;
    return items.map((x, i) => {
      const pct = total ? (x.total / total) * 100 : 0;
      const p: Porcion = { ...x, pct, offset: 25 - acumulado, color: colores[i % colores.length] };
      acumulado += pct;
      return p;
    });
  }

  private armarTortaCategoria(categorias: EntregasCategoria[]) {
    const conDatos = categorias
      .filter(c => Number(c.totalCustodias) > 0)
      .sort((a, b) => Number(b.totalCustodias) - Number(a.totalCustodias));
    this.totalCategoria = conDatos.reduce((s, c) => s + Number(c.totalCustodias), 0);

    const base = this.porciones(
      conDatos.map(c => ({ clave: String(c.idCategoria), nombre: c.categoria, total: Number(c.totalCustodias) })),
      this.coloresCategoria);
    // le sumamos el dato de entregados de cada categoria
    this.porcionesCategoria = base.map((p, i) => ({
      ...p,
      entregadas: Number(conDatos[i].totalEntregadas),
      pctEntregadas: Number(conDatos[i].porcentaje)
    }));
  }

  // Top 7 zonas; el resto se junta en "Otros lugares"
  private armarTortaLugar(lugares: ConteoEstado[]) {
    const ordenados = lugares
      .map(l => ({ nombre: l.estado, total: Number(l.total) }))
      .filter(l => l.total > 0)
      .sort((a, b) => b.total - a.total);
    this.totalLugar = ordenados.reduce((s, l) => s + l.total, 0);

    const top = ordenados.slice(0, 7);
    const resto = ordenados.slice(7).reduce((s, l) => s + l.total, 0);
    const items = top.map(l => ({ clave: l.nombre, nombre: l.nombre, total: l.total }));
    if (resto > 0) items.push({ clave: '__otros__', nombre: 'Otros lugares', total: resto });

    this.porcionesLugar = this.porciones(items, this.coloresLugar);
  }

  // ===== UTILIDADES =====

  total(lista: ConteoEstado[]): number {
    return lista.reduce((s, x) => s + Number(x.total), 0);
  }

  ancho(valor: number, lista: ConteoEstado[]): number {
    const max = Math.max(...lista.map(x => Number(x.total)), 1);
    return (Number(valor) / max) * 100;
  }

  get maxMes(): number {
    return Math.max(...this.meses.flatMap(m => [m.perdidas, m.entregas]), 1);
  }

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

  // "EN_CUSTODIA" -> "En custodia"
  bonito(texto: string): string {
    const t = (texto || '').replace(/_/g, ' ').toLowerCase();
    return t.charAt(0).toUpperCase() + t.slice(1);
  }
}