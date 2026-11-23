import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { PerdidaService } from '../../../core/services/perdida.service';
import { CoincidenciaService } from '../../../core/services/coincidencia.service';
import { ObjetoService } from '../../../core/services/objeto.service';
import { AlertService } from '../../../core/services/alert.service';
import { MiPerdida } from '../../../core/interfaces/perdida.interface';
import { ResultadoSugerencias, Sugerencia } from '../../../core/interfaces/coincidencia.interface';
import { Objeto } from '../../../core/interfaces/objeto.interface';

@Component({
  selector: 'app-mis-reportes',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './mis-reportes.component.html',
  styleUrl: './mis-reportes.component.css'
})
export class MisReportesComponent implements OnInit, OnDestroy {
  private perdidaService = inject(PerdidaService);
  private coincidenciaService = inject(CoincidenciaService);
  private objetoService = inject(ObjetoService);
  private alertService = inject(AlertService);

  // true = se muestra dentro de "Mi cuenta" (sin el titulo de pagina)
  @Input() embebido = false;

  reportes: MiPerdida[] = [];
  cargando = true;

  seleccionado: MiPerdida | null = null;
  resultado: ResultadoSugerencias | null = null;
  buscandoSugerencias = false;
  verPasos = false;

  // Animacion de cierre: el panel se encoge antes de desaparecer
  cerrando = false;
  private temporizador?: ReturnType<typeof setTimeout>;

  recuperando: number | null = null;   // id del reporte que se esta cerrando

  // Objetos en custodia (son publicos: ya salen con foto en Inicio)
  private publicos = new Map<number, Objeto>();
  imagenRota = new Set<number>();

  ngOnInit(): void {
    this.loadingReportes();
    this.objetoService.getObjetos().subscribe({
      next: (data) => data.forEach(o => this.publicos.set(o.idCustodia, o))
    });
  }

  ngOnDestroy(): void {
    clearTimeout(this.temporizador);
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

  // El estudiante confirma que ya recupero su objeto
  async marcarRecuperado(r: MiPerdida) {
    const ok = await this.alertService.confirm(
      `¿Ya tienes contigo "${r.nombreObjeto}"? El reporte pasará a Recuperado y dejaremos de buscarlo.`);
    if (!ok) return;

    this.recuperando = r.idReportePerdida;
    this.perdidaService.marcarRecuperado(r.idReportePerdida)
      .pipe(finalize(() => this.recuperando = null))
      .subscribe({
        next: () => {
          r.estado = 'RECUPERADO';
          if (this.esSeleccionado(r)) this.cerrarSugerencias();
          this.alertService.success('¡Qué bueno que lo recuperaste!', 'Reporte cerrado');
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo actualizar el reporte')
      });
  }

  verSugerencias(r: MiPerdida) {
    // si estaba cerrandose otro panel, se cancela el cierre
    clearTimeout(this.temporizador);
    this.cerrando = false;

    this.seleccionado = r;
    this.resultado = null;
    this.verPasos = false;
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

  // Primero anima el cierre y luego quita el panel
  cerrarSugerencias() {
    if (!this.seleccionado || this.cerrando) return;
    this.cerrando = true;
    this.temporizador = setTimeout(() => {
      this.seleccionado = null;
      this.resultado = null;
      this.verPasos = false;
      this.cerrando = false;
    }, 450);
  }

  // Abre o cierra las sugerencias de una tarjeta
  alternarSugerencias(r: MiPerdida) {
    if (this.esSeleccionado(r) && !this.cerrando) this.cerrarSugerencias();
    else this.verSugerencias(r);
  }

  esSeleccionado(r: MiPerdida): boolean {
    return this.seleccionado?.idReportePerdida === r.idReportePerdida;
  }

  // ===== Coincidencias =====
  // El backend ya las ordena: la primera es la mas alta
  get mejor(): Sugerencia | null {
    return this.resultado?.sugerencias?.[0] ?? null;
  }

  get otras(): Sugerencia[] {
    return this.resultado?.sugerencias?.slice(1) ?? [];
  }

  // Solo los objetos EN CUSTODIA muestran foto y nombre (ya son publicos)
  publico(s: Sugerencia): Objeto | undefined {
    return (s.origen || '').toUpperCase().includes('CUSTODIA') ? this.publicos.get(s.idReferencia) : undefined;
  }

  nivelPct(nivel: string): number {
    const n = (nivel || '').toUpperCase();
    if (n.includes('ALT')) return 92;
    if (n.includes('MED')) return 64;
    return 36;
  }

  claseMejor(nivel: string): string {
    const n = (nivel || '').toUpperCase();
    if (n.includes('ALT')) return 'm-alto';
    if (n.includes('MED')) return 'm-medio';
    return 'm-bajo';
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
    if (c.includes('instrument') || c.includes('music')) return 'bi-music-note-beamed';
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
    if (o.includes('HALLAZGO')) return 'Reportado como encontrado';
    return origen;
  }
}