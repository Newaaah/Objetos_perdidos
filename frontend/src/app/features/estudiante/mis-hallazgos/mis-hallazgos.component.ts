import { Component, inject, Input, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AlertService } from '../../../core/services/alert.service';

// Lo que devuelve GET /api/v1/hallazgos/mios (MiHallazgoDTO)
interface MiHallazgo {
  idHallazgo: number;
  idObjeto: number;
  nombreObjeto: string;
  categoria: string;
  estado: string;            // PENDIENTE, APROBADO, RECHAZADO, CANCELADO
  fechaReporte: string;
  ubicacionDestino: string;
}

@Component({
  selector: 'app-mis-hallazgos',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './mis-hallazgos.component.html',
  styleUrl: './mis-hallazgos.component.css'
})
export class MisHallazgosComponent implements OnInit {
  private http = inject(HttpClient);
  private alertService = inject(AlertService);

  // true = se muestra dentro de "Mi cuenta" (sin el titulo de pagina)
  @Input() embebido = false;

  hallazgos: MiHallazgo[] = [];
  cargando = true;

  ngOnInit(): void {
    this.http.get<MiHallazgo[]>('/api/v1/hallazgos/mios').subscribe({
      next: (data) => {
        // los mas recientes primero
        this.hallazgos = data.sort((a, b) => b.idHallazgo - a.idHallazgo);
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar tus hallazgos');
      }
    });
  }

  contar(estado: string): number {
    return this.hallazgos.filter(h => h.estado === estado).length;
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'e-pendiente';
      case 'APROBADO': return 'e-aprobado';
      case 'RECHAZADO': return 'e-rechazado';
      default: return 'e-cancelado';
    }
  }

  textoEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'En revisión';
      case 'APROBADO': return 'Aprobado';
      case 'RECHAZADO': return 'Rechazado';
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
}