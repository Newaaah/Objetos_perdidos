import { Component, ElementRef, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ObjetoService } from '../../../core/services/objeto.service';
import { EntregaService } from '../../../core/services/entrega.service';
import { PersonaService } from '../../../core/services/persona.service';
import { AlertService } from '../../../core/services/alert.service';
import { Objeto } from '../../../core/interfaces/objeto.interface';
import { PersonaResumen } from '../../../core/interfaces/persona.interface';

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

  get listo(): boolean {
    return !!this.seleccionado && !!this.persona && !!this.foto &&
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
        next: (estado) => {
          if (!estado.disponibleParaEntrega) {
            this.alertService.error(`"${estado.nombreObjeto}" ya no está disponible` +
              (estado.idEntrega ? ` (entrega #${estado.idEntrega}).` : '.'));
            this.loadingObjetos();
            return;
          }
          this.limpiar();
          this.seleccionado = o;
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
        next: (p) => this.persona = p,
        error: () => this.alertService.error('No se encontró a nadie con ese código o DNI')
      });
  }

  quitarPersona() {
    this.persona = null;
    this.codigo = '';
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
    if (this.idReportePerdida) datos.append('idReportePerdida', String(this.idReportePerdida));
    datos.append('fotografia', this.foto);

    this.guardando = true;
    this.entregaService.registrar(datos)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: (e) => {
          this.alertService.success(`Entrega #${e.idEntrega} registrada. El objeto ya no figura en custodia.`, 'Objeto entregado');
          this.cancelar();
          this.loadingObjetos();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo registrar la entrega')
      });
  }

  private limpiar() {
    this.quitarPersona();
    this.quitarFoto();
    this.idReportePerdida = null;
    this.observacion = '';
    this.confirmoIdentidad = false;
    this.confirmoEntrega = false;
  }
}