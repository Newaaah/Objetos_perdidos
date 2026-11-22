import { Component, inject, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import { ReporteService } from '../../../core/services/reporte.service';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { ConteoEstado } from '../../../core/interfaces/reporte.interface';

@Component({
  selector: 'app-panel-admin',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './panel-admin.component.html',
  styleUrl: './panel-admin.component.css'
})
export class PanelAdminComponent implements OnInit {
  private reporteService = inject(ReporteService);
  private catalogoService = inject(CatalogoService);

  cargando = true;
  perdidas: number | null = null;
  enCustodia: number | null = null;
  entregados: number | null = null;
  sinRevisar: number | null = null;
  categorias: number | null = null;
  ubicaciones: number | null = null;

  secciones = [
    {
      icono: 'bi-person-badge', color: 'azul', titulo: 'Gestión de objetos', ruta: '/personal',
      descripcion: 'Hallazgos, custodias, entregas, coincidencias, reclamos e incidencias.'
    },
    {
      icono: 'bi-journal-bookmark', color: 'verde', titulo: 'Catálogos', ruta: '/catalogos',
      descripcion: 'Categorías, ubicaciones y carreras que usa todo el sistema.'
    },
    {
      icono: 'bi-key', color: 'amarillo', titulo: 'Claves de estudiantes', ruta: '/claves',
      descripcion: 'Habilita o restablece la contraseña de un estudiante.'
    },
    {
      icono: 'bi-bar-chart', color: 'morado', titulo: 'Estadísticas', ruta: '/estadisticas',
      descripcion: 'Reportes de Oracle: consultas, vista y procedimiento almacenado.'
    }
  ];

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    this.cargando = true;
    const total = (l: ConteoEstado[]) => l.reduce((s, x) => s + Number(x.total), 0);
    const buscar = (l: ConteoEstado[], txt: string) =>
      Number(l.find(x => (x.estado || '').toUpperCase().includes(txt))?.total ?? 0);

    // Todas a la vez; si una falla, su dato queda en null ("—")
    forkJoin({
      perdidas: this.reporteService.perdidasPorEstado().pipe(map(total), catchError(() => of(null))),
      custodias: this.reporteService.custodiasPorSituacion().pipe(catchError(() => of(null))),
      resumen: this.reporteService.resumenReclamos().pipe(catchError(() => of(null))),
      categorias: this.catalogoService.getCategorias().pipe(map(l => l.length), catchError(() => of(null))),
      ubicaciones: this.catalogoService.getUbicaciones().pipe(map(l => l.length), catchError(() => of(null)))
    }).subscribe(r => {
      this.perdidas = r.perdidas;
      this.enCustodia = r.custodias ? buscar(r.custodias, 'CUSTODIA') : null;
      this.entregados = r.custodias ? buscar(r.custodias, 'ENTREG') : null;
      this.sinRevisar = r.resumen ? Number(r.resumen.sinRevisar) : null;
      this.categorias = r.categorias;
      this.ubicaciones = r.ubicaciones;
      this.cargando = false;
    });
  }

  // % de objetos que ya volvieron a su dueno
  get tasaDevolucion(): number | null {
    if (this.enCustodia === null || this.entregados === null) return null;
    const total = this.enCustodia + this.entregados;
    return total === 0 ? 0 : (this.entregados / total) * 100;
  }

  mostrar(v: number | null): string {
    return this.cargando ? '…' : (v === null ? '—' : String(v));
  }

  get saludo(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Buenos días';
    if (h < 19) return 'Buenas tardes';
    return 'Buenas noches';
  }
}