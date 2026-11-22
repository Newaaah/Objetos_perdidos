import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ReclamoService } from '../../../core/services/reclamo.service';
import { AlertService } from '../../../core/services/alert.service';
import { Reclamo } from '../../../core/interfaces/reclamo.interface';

@Component({
  selector: 'app-reclamos',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './reclamos.component.html',
  styleUrl: './reclamos.component.css'
})
export class ReclamosComponent implements OnInit, OnDestroy {
  private reclamoService = inject(ReclamoService);
  private alertService = inject(AlertService);

  filtro = 'PENDIENTE';
  reclamos: Reclamo[] = [];
  cargando = true;

  seleccionado: Reclamo | null = null;
  foto: string | null = null;
  resolucion = '';
  guardando = false;

  ngOnInit(): void {
    this.loadingReclamos();
  }

  ngOnDestroy(): void {
    this.liberarFoto();
  }

  loadingReclamos() {
    this.cargando = true;
    this.reclamoService.listar(this.filtro || undefined).subscribe({
      next: (data) => {
        // los mas antiguos primero (los que llevan mas tiempo esperando)
        this.reclamos = data.sort((a, b) => a.idReclamo - b.idReclamo);
        this.cargando = false;
        // si el seleccionado sigue en la lista, lo actualizamos
        if (this.seleccionado) {
          this.seleccionado = this.reclamos.find(r => r.idReclamo === this.seleccionado!.idReclamo) || null;
        }
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar los reclamos');
      }
    });
  }

  elegir(r: Reclamo) {
    this.seleccionado = r;
    this.resolucion = '';
    this.liberarFoto();
    if (r.tieneEvidencia) {
      this.reclamoService.evidencia(r.idReclamo).subscribe({
        next: (blob) => this.foto = URL.createObjectURL(blob),
        error: () => this.foto = null
      });
    }
  }

  cerrarPanel() {
    this.seleccionado = null;
    this.liberarFoto();
  }

  // ¿Se puede seguir trabajando en el?
  abierto(r: Reclamo): boolean {
    return r.estado === 'PENDIENTE' || r.estado === 'EN_REVISION';
  }

  tomar() {
    if (!this.seleccionado) return;
    this.enviar('EN_REVISION');
  }

  async cerrar(estado: 'APROBADO' | 'RECHAZADO') {
    if (!this.seleccionado) return;
    const texto = this.resolucion.trim();
    if (texto.length < 10) {
      this.alertService.error('La resolución debe tener al menos 10 caracteres');
      return;
    }
    const ok = await this.alertService.confirm(
      estado === 'APROBADO'
        ? `Aprobar el reclamo #${this.seleccionado.idReclamo}. Se notificará al estudiante; la entrega no se revierte automáticamente.`
        : `Rechazar el reclamo #${this.seleccionado.idReclamo}. Se notificará al estudiante.`);
    if (!ok) return;
    this.enviar(estado, texto);
  }

  private enviar(estado: string, resolucion?: string) {
    if (!this.seleccionado) return;
    this.guardando = true;
    this.reclamoService.revisar(this.seleccionado.idReclamo, estado, resolucion)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: (r) => {
          this.alertService.toast(`Reclamo #${r.idReclamo}: ${r.estado}`);
          this.seleccionado = r;
          this.resolucion = '';
          this.loadingReclamos();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo actualizar el reclamo')
      });
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'bg-warning text-dark';
      case 'EN_REVISION': return 'bg-info text-dark';
      case 'APROBADO':
      case 'RESUELTO': return 'bg-success';
      case 'RECHAZADO': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  private liberarFoto() {
    if (this.foto) URL.revokeObjectURL(this.foto);
    this.foto = null;
  }
}