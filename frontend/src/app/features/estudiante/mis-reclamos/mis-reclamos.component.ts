import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReclamoService } from '../../../core/services/reclamo.service';
import { ObjetoService } from '../../../core/services/objeto.service';
import { AlertService } from '../../../core/services/alert.service';
import { Reclamo } from '../../../core/interfaces/reclamo.interface';

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

  reclamos: Reclamo[] = [];
  cargando = true;
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

  nombreReclamo(r: Reclamo): string {
    if (r.idEntrega) return this.nombres.get(r.idEntrega) || `Entrega #${r.idEntrega}`;
    if (r.idCustodia) return `Custodia #${r.idCustodia}`;
    if (r.idHallazgo) return `Hallazgo #${r.idHallazgo}`;
    return 'Consulta';
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
}