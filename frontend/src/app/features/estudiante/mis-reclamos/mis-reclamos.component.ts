import { Component, inject, Input, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReclamoService } from '../../../core/services/reclamo.service';
import { ObjetoService } from '../../../core/services/objeto.service';
import { AlertService } from '../../../core/services/alert.service';
import { Reclamo } from '../../../core/interfaces/reclamo.interface';

type Filtro = 'todos' | 'proceso' | 'cerrados';

@Component({
  selector: 'app-mis-reclamos',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './mis-reclamos.component.html',
  styleUrl: './mis-reclamos.component.css'
})
export class MisReclamosComponent implements OnInit {
  private reclamoService = inject(ReclamoService);
  private objetoService = inject(ObjetoService);
  private alertService = inject(AlertService);

  // true = se muestra dentro de "Mi cuenta" (sin el titulo de pagina)
  @Input() embebido = false;

  reclamos: Reclamo[] = [];
  cargando = true;
  filtro: Filtro = 'todos';
  private nombres = new Map<number, string>();   // idEntrega -> nombre del objeto

  ngOnInit(): void {
    this.reclamoService.mios().subscribe({
      next: (data) => {
        this.reclamos = data.sort((a, b) => b.idReclamo - a.idReclamo);
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar tus reclamos');
      }
    });

    this.objetoService.getEntregados(365).subscribe({
      next: (data) => data.forEach(o => this.nombres.set(o.idEntrega, o.nombre))
    });
  }

  // ===== Contadores y filtro (solo visual) =====
  private abierto(r: Reclamo): boolean {
    return r.estado === 'PENDIENTE' || r.estado === 'EN_REVISION';
  }

  get enProceso(): number { return this.reclamos.filter(r => this.abierto(r)).length; }
  get aprobados(): number { return this.reclamos.filter(r => r.estado === 'APROBADO' || r.estado === 'RESUELTO').length; }
  get rechazados(): number { return this.reclamos.filter(r => r.estado === 'RECHAZADO').length; }

  get filtrados(): Reclamo[] {
    if (this.filtro === 'proceso') return this.reclamos.filter(r => this.abierto(r));
    if (this.filtro === 'cerrados') return this.reclamos.filter(r => !this.abierto(r));
    return this.reclamos;
  }

  // ===== Datos de cada tarjeta =====
  nombreReclamo(r: Reclamo): string {
    if (r.idEntrega) return this.nombres.get(r.idEntrega) || `Entrega #${r.idEntrega}`;
    if (r.idCustodia) return `Custodia #${r.idCustodia}`;
    if (r.idHallazgo) return `Hallazgo #${r.idHallazgo}`;
    return 'Consulta';
  }

  // 1 = enviado, 2 = en revision, 3 = cerrado
  paso(r: Reclamo): number {
    return r.estado === 'PENDIENTE' ? 1 : r.estado === 'EN_REVISION' ? 2 : 3;
  }

  etapas(r: Reclamo): string[] {
    const final = r.estado === 'RECHAZADO' ? 'Rechazado' : r.estado === 'APROBADO' ? 'Aprobado' : 'Resuelto';
    return ['Enviado', 'En revisión', final];
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'e-pendiente';
      case 'EN_REVISION': return 'e-revision';
      case 'APROBADO':
      case 'RESUELTO': return 'e-aprobado';
      case 'RECHAZADO': return 'e-rechazado';
      default: return 'e-pendiente';
    }
  }

  iconoEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'bi-hourglass-split';
      case 'EN_REVISION': return 'bi-search';
      case 'APROBADO':
      case 'RESUELTO': return 'bi-patch-check-fill';
      case 'RECHAZADO': return 'bi-x-octagon-fill';
      default: return 'bi-chat-left-text';
    }
  }

  textoEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'Pendiente';
      case 'EN_REVISION': return 'En revisión';
      case 'APROBADO': return 'Aprobado';
      case 'RESUELTO': return 'Resuelto';
      case 'RECHAZADO': return 'Rechazado';
      default: return estado;
    }
  }
}