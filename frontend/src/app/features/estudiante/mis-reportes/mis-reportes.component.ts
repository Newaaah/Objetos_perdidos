import { Component, inject, Input, OnInit } from '@angular/core';
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

  // true = se muestra dentro de "Mi cuenta" (sin el titulo de pagina)
  @Input() embebido = false;

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

  // Abre o cierra las sugerencias de una tarjeta
  alternarSugerencias(r: MiPerdida) {
    if (this.esSeleccionado(r)) this.cerrarSugerencias();
    else this.verSugerencias(r);
  }

  esSeleccionado(r: MiPerdida): boolean {
    return this.seleccionado?.idReportePerdida === r.idReportePerdida;
  }

  // ===== Contadores (solo visual) =====
  contar(estado: string): number {
    return this.reportes.filter(r => r.estado === estado).length;
  }

  // ===== Estilos =====
  claseEstado(estado: string): string {
    switch (estado) {
      case 'REPORTADO': return 'e-buscando';
      case 'RECUPERADO': return 'e-recuperado';
      case 'CANCELADO': return 'e-cancelado';
      default: return 'e-cancelado';
    }
  }

  textoEstado(estado: string): string {
    switch (estado) {
      case 'REPORTADO': return 'Buscando';
      case 'RECUPERADO': return 'Recuperado';
      case 'CANCELADO': return 'Cancelado';
      default: return estado;
    }
  }

  iconoCategoria(categoria: string): string {
    const c = (categoria || '').toLowerCase();
    if (c.includes('electr') || c.includes('celular') || c.includes('tecno')) return 'bi-phone';
    if (c.includes('llave')) return 'bi-key';
    if (c.includes('document') || c.includes('carnet') || c.includes('dni')) return 'bi-person-vcard';
    if (c.includes('billet') || c.includes('cartera') || c.includes('dinero')) return 'bi-wallet2';
    if (c.includes('ropa') || c.includes('prenda') || c.includes('vestim')) return 'bi-handbag';
    if (c.includes('mochila') || c.includes('bolso')) return 'bi-backpack';
    if (c.includes('libro') || c.includes('cuaderno') || c.includes('util')) return 'bi-journal-bookmark';
    if (c.includes('lente') || c.includes('accesor') || c.includes('joya') || c.includes('reloj')) return 'bi-smartwatch';
    if (c.includes('botella') || c.includes('tomatodo')) return 'bi-cup-straw';
    return 'bi-box-seam';
  }

  claseNivel(nivel: string): string {
    const n = (nivel || '').toUpperCase();
    if (n.includes('ALT')) return 'n-alto';
    if (n.includes('MED')) return 'n-medio';
    return 'n-bajo';
  }

  textoOrigen(origen: string): string {
    const o = (origen || '').toUpperCase();
    if (o.includes('CUSTODIA')) return 'Objeto en custodia';
    if (o.includes('HALLAZGO')) return 'Objeto reportado como encontrado';
    return origen;
  }
}