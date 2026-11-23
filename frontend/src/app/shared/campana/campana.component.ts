import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, forkJoin, of, Subscription, switchMap, timer } from 'rxjs';
import { NotificacionService } from '../../core/services/notificacion.service';
import { AuthService } from '../../core/services/auth.service';
import { Notificacion } from '../../core/interfaces/notificacion.interface';

@Component({
  selector: 'app-campana',
  standalone: true,
  imports: [],
  templateUrl: './campana.component.html',
  styleUrl: './campana.component.css'
})
export class CampanaComponent implements OnInit, OnDestroy {
  private notificacionService = inject(NotificacionService);
  private authService = inject(AuthService);
  private router = inject(Router);

  notificaciones: Notificacion[] = [];
  private sub?: Subscription;

  get noLeidas(): number {
    return this.notificaciones.filter(n => !n.leida).length;
  }

  ngOnInit(): void {
    this.sub = timer(0, 60000).pipe(
      switchMap(() => this.notificacionService.mias().pipe(catchError(() => of(null))))
    ).subscribe(data => {
      if (data) this.notificaciones = data;
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  abrir(n: Notificacion) {
    if (!n.leida) {
      this.notificacionService.marcarLeida(n.idNotificacion).subscribe({
        next: () => n.leida = true
      });
    }
    const tab = this.pestanaDe(n);
    if (tab) this.router.navigate(['/mi-cuenta'], { queryParams: { tab } });
  }

  marcarTodas(event: Event) {
    event.stopPropagation();   // para que el menu no se cierre
    const pendientes = this.notificaciones.filter(n => !n.leida);
    if (pendientes.length === 0) return;
    forkJoin(pendientes.map(n => this.notificacionService.marcarLeida(n.idNotificacion))).subscribe({
      next: () => pendientes.forEach(n => n.leida = true)
    });
  }

  // A que pestaña de "Mi cuenta" lleva cada notificacion (solo estudiante)
  private pestanaDe(n: Notificacion): string | null {
    if (this.authService.getRol() !== 'ESTUDIANTE') return null;
    if ((n.tipo || '').toUpperCase().includes('RECLAMO')) return 'reclamos';
    if (n.idReportePerdida) return 'reportes';
    if (n.idHallazgo) return 'hallazgos';
    return null;
  }

  // ===== Diseño =====
  icono(tipo: string): string {
    const t = (tipo || '').toUpperCase();
    if (t.includes('COINCID') || t.includes('SUGER')) return 'bi-stars';
    if (t.includes('RECLAMO')) return 'bi-chat-left-text';
    if (t.includes('ENTREG')) return 'bi-box-arrow-right';
    if (t.includes('HALLAZGO') || t.includes('CUSTOD')) return 'bi-box-seam';
    return 'bi-bell';
  }

  color(tipo: string): string {
    const t = (tipo || '').toUpperCase();
    if (t.includes('COINCID') || t.includes('SUGER')) return 't-morado';
    if (t.includes('RECLAMO')) return 't-naranja';
    if (t.includes('ENTREG')) return 't-verde';
    if (t.includes('HALLAZGO') || t.includes('CUSTOD')) return 't-azul';
    return 't-gris';
  }

  // "hace 5 min", "hace 2 h", "hace 3 días"
  tiempo(fecha: string | Date): string {
    const ms = Date.now() - new Date(fecha).getTime();
    const min = Math.floor(ms / 60000);
    if (isNaN(min)) return '';
    if (min < 1) return 'justo ahora';
    if (min < 60) return `hace ${min} min`;
    const h = Math.floor(min / 60);
    if (h < 24) return `hace ${h} h`;
    const d = Math.floor(h / 24);
    if (d < 7) return d === 1 ? 'ayer' : `hace ${d} días`;
    return new Date(fecha).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' });
  }
}