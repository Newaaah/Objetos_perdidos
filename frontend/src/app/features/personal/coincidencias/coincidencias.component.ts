import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CoincidenciaService } from '../../../core/services/coincidencia.service';
import { AlertService } from '../../../core/services/alert.service';
import {
  CoincidenciaRevisada, EvaluacionCoincidencia, HallazgoCandidato, PerdidaCandidata
} from '../../../core/interfaces/coincidencia.interface';

@Component({
  selector: 'app-coincidencias',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './coincidencias.component.html',
  styleUrl: './coincidencias.component.css'
})
export class CoincidenciasComponent implements OnInit, OnDestroy {
  private coincidenciaService = inject(CoincidenciaService);
  private alertService = inject(AlertService);

  // ===== Listas para elegir =====
  cargandoListas = true;
  perdidas: PerdidaCandidata[] = [];
  hallazgos: HallazgoCandidato[] = [];
  filtroPerdida = '';
  filtroHallazgo = '';
  perdidaSel: PerdidaCandidata | null = null;
  hallazgoSel: HallazgoCandidato | null = null;

  idReportePerdida: number | null = null;
  idHallazgo: number | null = null;
  evaluando = false;
  evaluacion: EvaluacionCoincidencia | null = null;

  observacion = '';
  guardando = false;
  historial: CoincidenciaRevisada[] = [];

  // Foto privada del hallazgo
  fotoHallazgo: string | null = null;
  cargandoFoto = false;

  ngOnInit(): void {
    this.cargarListas();
  }

  ngOnDestroy(): void {
    this.soltarFoto();
  }

  cargarListas() {
    this.cargandoListas = true;
    this.coincidenciaService.candidatos()
      .pipe(finalize(() => this.cargandoListas = false))
      .subscribe({
        next: (c) => {
          this.perdidas = c.perdidas;
          this.hallazgos = c.hallazgos;
        },
        error: () => this.alertService.error('No se pudieron cargar los reportes y hallazgos')
      });
  }

  // ===== Filtros y orden =====
  private coincideTexto(texto: string, ...campos: (string | number | undefined)[]): boolean {
    const t = texto.trim().toLowerCase();
    return !t || campos.some(c => String(c ?? '').toLowerCase().includes(t));
  }

  get perdidasFiltradas(): PerdidaCandidata[] {
    return this.perdidas.filter(p =>
      this.coincideTexto(this.filtroPerdida, p.nombreObjeto, p.categoria, p.ubicacionPerdida, p.idReportePerdida));
  }

  // Si hay una perdida elegida, primero los hallazgos de la misma categoria
  get hallazgosFiltrados(): HallazgoCandidato[] {
    const lista = this.hallazgos.filter(h =>
      this.coincideTexto(this.filtroHallazgo, h.nombreObjeto, h.categoria, h.ubicacionDestino, h.idHallazgo));
    if (!this.perdidaSel) return lista;
    return [...lista].sort((a, b) => Number(this.mismaCategoria(b)) - Number(this.mismaCategoria(a)));
  }

  mismaCategoria(h: HallazgoCandidato): boolean {
    return !!this.perdidaSel && this.perdidaSel.categoria === h.categoria;
  }

  // ===== Seleccion =====
  elegirPerdida(p: PerdidaCandidata) {
    this.perdidaSel = this.perdidaSel?.idReportePerdida === p.idReportePerdida ? null : p;
    this.intentarEvaluar();
  }

  elegirHallazgo(h: HallazgoCandidato) {
    this.hallazgoSel = this.hallazgoSel?.idHallazgo === h.idHallazgo ? null : h;
    this.intentarEvaluar();
  }

  // Cuando hay uno de cada lado, se evalua solo
  private intentarEvaluar() {
    if (this.perdidaSel && this.hallazgoSel) {
      this.idReportePerdida = this.perdidaSel.idReportePerdida;
      this.idHallazgo = this.hallazgoSel.idHallazgo;
      this.evaluar();
    } else {
      this.evaluacion = null;
      this.historial = [];
      this.soltarFoto();
    }
  }

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
          this.cargarFoto(e.idHallazgo);
        },
        error: (err) => {
          this.evaluacion = null;
          this.soltarFoto();
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

  private cargarFoto(idHallazgo: number) {
    this.soltarFoto();
    this.cargandoFoto = true;
    this.coincidenciaService.imagenHallazgo(idHallazgo)
      .pipe(finalize(() => this.cargandoFoto = false))
      .subscribe({
        next: (blob) => this.fotoHallazgo = URL.createObjectURL(blob),
        error: () => this.fotoHallazgo = null
      });
  }

  private soltarFoto() {
    if (this.fotoHallazgo) URL.revokeObjectURL(this.fotoHallazgo);
    this.fotoHallazgo = null;
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
    this.perdidaSel = null;
    this.hallazgoSel = null;
    this.idReportePerdida = null;
    this.idHallazgo = null;
    this.evaluacion = null;
    this.historial = [];
    this.observacion = '';
    this.soltarFoto();
  }

  // ===== Diseño =====
  coincide(a?: string, b?: string): boolean {
    const n = (x?: string) => (x || '').trim().toLowerCase();
    return !!n(a) && n(a) === n(b);
  }

  nivel(puntaje: number): string {
    if (puntaje >= 75) return 'ALTA';
    if (puntaje >= 55) return 'MEDIA';
    return 'BAJA';
  }

  colorPuntaje(valor: number): string {
    if (valor >= 70) return '#22c55e';
    if (valor >= 40) return '#f59e0b';
    return '#ef4444';
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

  bonito(texto: string): string {
    const t = (texto || '').replace(/_/g, ' ').toLowerCase();
    return t.charAt(0).toUpperCase() + t.slice(1);
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
}