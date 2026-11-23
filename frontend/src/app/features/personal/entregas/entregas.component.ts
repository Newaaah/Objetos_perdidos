import { Component, ElementRef, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ObjetoService } from '../../../core/services/objeto.service';
import { EntregaService } from '../../../core/services/entrega.service';
import { PersonaService } from '../../../core/services/persona.service';
import { PerdidaService } from '../../../core/services/perdida.service';
import { AlertService } from '../../../core/services/alert.service';
import { Objeto } from '../../../core/interfaces/objeto.interface';
import { PersonaResumen } from '../../../core/interfaces/persona.interface';
import { MiPerdida } from '../../../core/interfaces/perdida.interface';

// Datos nuevos que manda el backend al verificar la custodia
interface EstadoConConfirmada {
  disponibleParaEntrega: boolean;
  nombreObjeto: string;
  idEntrega?: number;
  idCoincidenciaConfirmada?: number;
  idReporteConfirmado?: number;
  nombreReporteConfirmado?: string;
  idPersonaReporteConfirmado?: number;
}

// Coincidencia CONFIRMADA del objeto elegido
interface Confirmada {
  idCoincidencia: number;
  idReporte: number;
  nombre: string;
  idPersona: number;
}

@Component({
  selector: 'app-entregas',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './entregas.component.html',
  styleUrl: './entregas.component.css'
})
export class EntregasComponent implements OnInit, OnDestroy {
  private objetoService = inject(ObjetoService);
  private entregaService = inject(EntregaService);
  private personaService = inject(PersonaService);
  private perdidaService = inject(PerdidaService);
  private alertService = inject(AlertService);

  @ViewChild('archivo') archivo?: ElementRef<HTMLInputElement>;

  objetos: Objeto[] = [];
  cargando = true;
  filtro = '';
  seleccionado: Objeto | null = null;
  verificando = false;

  codigo = '';
  persona: PersonaResumen | null = null;
  buscandoPersona = false;

  // Reportes de perdida abiertos de la persona que recibe
  reportesAbiertos: MiPerdida[] = [];
  cargandoReportes = false;

  // Si el objeto tiene coincidencia CONFIRMADA, se vincula sola
  confirmada: Confirmada | null = null;
  idCoincidencia: number | null = null;

  idReportePerdida: number | null = null;
  observacion = '';
  confirmoIdentidad = false;
  confirmoEntrega = false;
  foto: File | null = null;
  preview: string | null = null;
  guardando = false;

  get objetosFiltrados(): Objeto[] {
    const f = this.filtro.trim().toLowerCase();
    if (!f) return this.objetos;
    return this.objetos.filter(o =>
      o.nombre.toLowerCase().includes(f) ||
      o.categoria.toLowerCase().includes(f) ||
      String(o.idCustodia) === f);
  }

  // La persona buscada no es la dueña del reporte confirmado
  get personaNoCoincide(): boolean {
    return !!this.confirmada && !!this.persona && this.persona.idPersona !== this.confirmada.idPersona;
  }

  get listo(): boolean {
    return !!this.seleccionado && !!this.persona && !!this.foto && !this.personaNoCoincide &&
      this.observacion.trim().length > 0 && this.confirmoIdentidad && this.confirmoEntrega;
  }

  ngOnInit(): void {
    this.loadingObjetos();
  }

  ngOnDestroy(): void {
    if (this.preview) URL.revokeObjectURL(this.preview);
  }

  loadingObjetos() {
    this.cargando = true;
    this.objetoService.getObjetos().subscribe({
      next: (data) => {
        this.objetos = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar los objetos en custodia');
      }
    });
  }

  elegir(o: Objeto) {
    this.verificando = true;
    this.entregaService.estadoCustodia(o.idCustodia)
      .pipe(finalize(() => this.verificando = false))
      .subscribe({
        next: (respuesta) => {
          const estado = respuesta as unknown as EstadoConConfirmada;
          if (!estado.disponibleParaEntrega) {
            this.alertService.error(`"${estado.nombreObjeto}" ya no está disponible` +
              (estado.idEntrega ? ` (entrega #${estado.idEntrega}).` : '.'));
            this.loadingObjetos();
            return;
          }
          this.limpiar();
          this.seleccionado = o;

          // Coincidencia confirmada: se vincula sola
          if (estado.idCoincidenciaConfirmada && estado.idReporteConfirmado && estado.idPersonaReporteConfirmado) {
            this.confirmada = {
              idCoincidencia: estado.idCoincidenciaConfirmada,
              idReporte: estado.idReporteConfirmado,
              nombre: estado.nombreReporteConfirmado || 'Reporte de pérdida',
              idPersona: estado.idPersonaReporteConfirmado
            };
            this.idCoincidencia = this.confirmada.idCoincidencia;
            this.idReportePerdida = this.confirmada.idReporte;
          }
        },
        error: () => this.alertService.error('No se pudo verificar el estado de la custodia')
      });
  }

  cancelar() {
    this.seleccionado = null;
    this.limpiar();
  }

  buscarPersona() {
    const c = this.codigo.trim();
    if (c.length < 8) return;
    this.buscandoPersona = true;
    this.personaService.buscar(c)
      .pipe(finalize(() => this.buscandoPersona = false))
      .subscribe({
        next: (p) => {
          this.persona = p;
          // con coincidencia confirmada no hace falta elegir reporte
          if (!this.confirmada) this.cargarReportes(p.idPersona);
        },
        error: () => this.alertService.error('No se encontró a nadie con ese código o DNI')
      });
  }

  // Trae los reportes "en busqueda" de esa persona para vincular uno
  private cargarReportes(idPersona: number) {
    this.reportesAbiertos = [];
    this.idReportePerdida = null;
    this.cargandoReportes = true;
    this.perdidaService.abiertas(idPersona)
      .pipe(finalize(() => this.cargandoReportes = false))
      .subscribe({
        next: (data) => this.reportesAbiertos = data,
        error: () => this.reportesAbiertos = []
      });
  }

  // Toca un reporte para vincularlo; si lo vuelve a tocar, se quita
  elegirReporte(r: MiPerdida) {
    this.idReportePerdida = this.idReportePerdida === r.idReportePerdida ? null : r.idReportePerdida;
  }

  quitarPersona() {
    this.persona = null;
    this.codigo = '';
    this.reportesAbiertos = [];
    // si hay coincidencia confirmada, su reporte se mantiene
    this.idReportePerdida = this.confirmada ? this.confirmada.idReporte : null;
  }

  seleccionarFoto(event: Event) {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(archivo.type)) {
      this.alertService.error('Solo se permiten imágenes JPG, PNG o WebP');
      input.value = '';
      return;
    }
    if (archivo.size > 5 * 1024 * 1024) {
      this.alertService.error('La imagen no puede pesar más de 5 MB');
      input.value = '';
      return;
    }
    if (this.preview) URL.revokeObjectURL(this.preview);
    this.foto = archivo;
    this.preview = URL.createObjectURL(archivo);
  }

  quitarFoto() {
    if (this.preview) URL.revokeObjectURL(this.preview);
    this.preview = null;
    this.foto = null;
    if (this.archivo) this.archivo.nativeElement.value = '';
  }

  async registrar() {
    if (!this.listo || !this.seleccionado || !this.persona || !this.foto) return;

    const nombre = `${this.persona.nombres} ${this.persona.apellidos}`;
    const ok = await this.alertService.confirm(
      `Entregar "${this.seleccionado.nombre}" a ${nombre}. Esta acción no se puede deshacer.`);
    if (!ok) return;

    const datos = new FormData();
    datos.append('idCustodia', String(this.seleccionado.idCustodia));
    datos.append('idPersonaRecibe', String(this.persona.idPersona));
    datos.append('observacionVerificacion', this.observacion.trim());
    datos.append('confirmoVerificacionPresencial', 'true');
    datos.append('confirmoEntregaFisica', 'true');
    if (this.idCoincidencia) datos.append('idCoincidencia', String(this.idCoincidencia));
    if (this.idReportePerdida) datos.append('idReportePerdida', String(this.idReportePerdida));
    datos.append('fotografia', this.foto);

    this.guardando = true;
    this.entregaService.registrar(datos)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: (e) => {
          const extra = this.idReportePerdida ? ' Su reporte de pérdida quedó como Recuperado.' : '';
          this.alertService.success(`Entrega #${e.idEntrega} registrada. El objeto ya no figura en custodia.${extra}`, 'Objeto entregado');
          this.cancelar();
          this.loadingObjetos();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo registrar la entrega')
      });
  }

  private limpiar() {
    this.confirmada = null;
    this.idCoincidencia = null;
    this.quitarPersona();
    this.quitarFoto();
    this.idReportePerdida = null;
    this.observacion = '';
    this.confirmoIdentidad = false;
    this.confirmoEntrega = false;
  }
}