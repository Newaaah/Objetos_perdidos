import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HallazgoService } from '../../../core/services/hallazgo.service';
import { AlertService } from '../../../core/services/alert.service';
import { HallazgoPendiente } from '../../../core/interfaces/hallazgo.interface';

@Component({
  selector: 'app-revision-hallazgos',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './revision-hallazgos.component.html',
  styleUrl: './revision-hallazgos.component.css'
})
export class RevisionHallazgosComponent implements OnInit, OnDestroy {
  private hallazgoService = inject(HallazgoService);
  private alertService = inject(AlertService);

  pendientes: HallazgoPendiente[] = [];
  cargando = true;
  procesando: number | null = null;              // id del que se esta resolviendo
  fotos: Record<number, string> = {};            // idHallazgo -> URL local de la foto
  observaciones: Record<number, string> = {};    // idHallazgo -> texto escrito

  ngOnInit(): void {
    this.loadingPendientes();
  }

  ngOnDestroy(): void {
    Object.values(this.fotos).forEach(url => URL.revokeObjectURL(url));
  }

  loadingPendientes() {
    this.cargando = true;
    this.hallazgoService.pendientes().subscribe({
      next: (data) => {
        // los mas antiguos primero (los que llevan mas tiempo esperando)
        this.pendientes = data.sort((a, b) => a.idHallazgo - b.idHallazgo);
        this.cargando = false;
        this.pendientes.forEach(h => this.cargarFoto(h.idHallazgo));
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar los hallazgos pendientes');
      }
    });
  }

  private cargarFoto(id: number) {
    if (this.fotos[id]) return;
    this.hallazgoService.imagenRevision(id).subscribe({
      next: (blob) => this.fotos[id] = URL.createObjectURL(blob),
      error: () => { /* sin foto: se muestra el icono */ }
    });
  }

  async resolver(h: HallazgoPendiente, decision: 'APROBADO' | 'RECHAZADO') {
    const observacion = (this.observaciones[h.idHallazgo] || '').trim();

    if (decision === 'RECHAZADO' && !observacion) {
      this.alertService.error('Para rechazar debes escribir el motivo en la observación');
      return;
    }
    const texto = decision === 'APROBADO'
      ? `Aprobar "${h.nombreObjeto}". Quedará listo para recibirse en ${h.ubicacionDestino || 'el punto de recepción'}.`
      : `Rechazar "${h.nombreObjeto}". El estudiante será notificado.`;
    const ok = await this.alertService.confirm(texto);
    if (!ok) return;

    this.procesando = h.idHallazgo;
    this.hallazgoService.resolver(h.idHallazgo, decision, observacion || undefined).subscribe({
      next: () => {
        this.procesando = null;
        this.alertService.toast(decision === 'APROBADO' ? 'Hallazgo aprobado' : 'Hallazgo rechazado');
        // lo quitamos de la lista y liberamos su foto
        this.pendientes = this.pendientes.filter(x => x.idHallazgo !== h.idHallazgo);
        if (this.fotos[h.idHallazgo]) URL.revokeObjectURL(this.fotos[h.idHallazgo]);
        delete this.fotos[h.idHallazgo];
      },
      error: (err) => {
        this.procesando = null;
        this.alertService.error(err.error?.mensaje || err.error?.message || 'No se pudo registrar la decisión');
      }
    });
  }
}