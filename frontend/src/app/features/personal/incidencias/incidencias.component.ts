import { Component, ElementRef, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { IncidenciaService } from '../../../core/services/incidencia.service';
import { ObjetoService } from '../../../core/services/objeto.service';
import { AlertService } from '../../../core/services/alert.service';
import { Reclamo } from '../../../core/interfaces/reclamo.interface';
import { Objeto } from '../../../core/interfaces/objeto.interface';

@Component({
  selector: 'app-incidencias',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './incidencias.component.html',
  styleUrl: './incidencias.component.css'
})
export class IncidenciasComponent implements OnInit, OnDestroy {
  private incidenciaService = inject(IncidenciaService);
  private objetoService = inject(ObjetoService);
  private alertService = inject(AlertService);

  @ViewChild('archivo') archivo?: ElementRef<HTMLInputElement>;

  // Bandeja
  filtro = 'PENDIENTE';
  incidencias: Reclamo[] = [];
  cargando = true;
  seleccionado: Reclamo | null = null;
  foto: string | null = null;
  resolucion = '';
  guardando = false;

  // Nueva incidencia
  mostrarFormulario = false;
  custodias: Objeto[] = [];
  nueva = { idCustodia: null as number | null, descripcion: '' };
  evidencia: File | null = null;
  preview: string | null = null;
  registrando = false;

  ngOnInit(): void {
    this.loadingIncidencias();
    // objetos que siguen en custodia (para elegir sobre cual es la incidencia)
    this.objetoService.getObjetos().subscribe({ next: (d) => this.custodias = d });
  }

  ngOnDestroy(): void {
    this.liberar(this.foto);
    this.liberar(this.preview);
  }

  // ===== BANDEJA =====
  loadingIncidencias() {
    this.cargando = true;
    this.incidenciaService.listar(this.filtro || undefined).subscribe({
      next: (data) => {
        this.incidencias = data.sort((a, b) => a.idReclamo - b.idReclamo);
        this.cargando = false;
        if (this.seleccionado) {
          this.seleccionado = this.incidencias.find(i => i.idReclamo === this.seleccionado!.idReclamo) || null;
        }
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar las incidencias');
      }
    });
  }

  elegir(i: Reclamo) {
    this.seleccionado = i;
    this.resolucion = '';
    this.liberar(this.foto);
    this.foto = null;
    if (i.tieneEvidencia) {
      this.incidenciaService.evidencia(i.idReclamo).subscribe({
        next: (blob) => this.foto = URL.createObjectURL(blob),
        error: () => this.foto = null
      });
    }
  }

  cerrarPanel() {
    this.seleccionado = null;
    this.liberar(this.foto);
    this.foto = null;
  }

  abierta(i: Reclamo): boolean {
    return i.estado === 'PENDIENTE' || i.estado === 'EN_REVISION';
  }

  // Nombre del objeto de esa custodia (si sigue en custodia)
  nombreCustodia(idCustodia?: number): string {
    const o = this.custodias.find(c => c.idCustodia === idCustodia);
    return o ? `${o.nombre} (custodia #${idCustodia})` : `Custodia #${idCustodia}`;
  }

  tomar() {
    this.enviar('EN_REVISION');
  }

  async resolver() {
    if (!this.seleccionado) return;
    const texto = this.resolucion.trim();
    if (texto.length < 10) {
      this.alertService.error('La resolución debe tener al menos 10 caracteres');
      return;
    }
    const ok = await this.alertService.confirm(`Cerrar la incidencia #${this.seleccionado.idReclamo} como RESUELTA.`);
    if (!ok) return;
    this.enviar('RESUELTO', texto);
  }

  private enviar(estado: string, resolucion?: string) {
    if (!this.seleccionado) return;
    this.guardando = true;
    this.incidenciaService.revisar(this.seleccionado.idReclamo, estado, resolucion)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: (r) => {
          this.alertService.toast(`Incidencia #${r.idReclamo}: ${r.estado}`);
          this.seleccionado = r;
          this.resolucion = '';
          this.loadingIncidencias();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo actualizar la incidencia')
      });
  }

  // ===== NUEVA INCIDENCIA =====
  abrirFormulario() {
    this.mostrarFormulario = !this.mostrarFormulario;
    this.limpiarNueva();
  }

  seleccionarEvidencia(event: Event) {
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
    this.liberar(this.preview);
    this.evidencia = archivo;
    this.preview = URL.createObjectURL(archivo);
  }

  quitarEvidencia() {
    this.liberar(this.preview);
    this.preview = null;
    this.evidencia = null;
    if (this.archivo) this.archivo.nativeElement.value = '';
  }

  registrar() {
    if (!this.nueva.idCustodia) {
      this.alertService.error('Elige el objeto en custodia');
      return;
    }
    const texto = this.nueva.descripcion.trim();
    if (texto.length < 15) {
      this.alertService.error('Describe el problema con al menos 15 caracteres');
      return;
    }
    // Mismos nombres que RegistrarIncidenciaRequest + @RequestPart("evidencia")
    const datos = new FormData();
    datos.append('idCustodia', String(this.nueva.idCustodia));
    datos.append('descripcion', texto);
    if (this.evidencia) datos.append('evidencia', this.evidencia);

    this.registrando = true;
    this.incidenciaService.crear(datos)
      .pipe(finalize(() => this.registrando = false))
      .subscribe({
        next: (r) => {
          this.alertService.success(`Incidencia #${r.idReclamo} registrada.`, 'Registrada');
          this.mostrarFormulario = false;
          this.limpiarNueva();
          this.filtro = 'PENDIENTE';
          this.loadingIncidencias();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo registrar la incidencia')
      });
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'bg-warning text-dark';
      case 'EN_REVISION': return 'bg-info text-dark';
      case 'RESUELTO': return 'bg-success';
      default: return 'bg-secondary';
    }
  }

  private limpiarNueva() {
    this.nueva = { idCustodia: null, descripcion: '' };
    this.quitarEvidencia();
  }

  private liberar(url: string | null) {
    if (url) URL.revokeObjectURL(url);
  }
}