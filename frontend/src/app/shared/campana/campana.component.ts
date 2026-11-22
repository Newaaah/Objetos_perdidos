import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, forkJoin, of, Subscription, switchMap, timer } from 'rxjs';
import { NotificacionService } from '../../core/services/notificacion.service';
import { AuthService } from '../../core/services/auth.service';
import { Notificacion } from '../../core/interfaces/notificacion.interface';

@Component({
  selector: 'app-campana',
  standalone: true,
  imports: [DatePipe],
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
    const ruta = this.rutaDe(n);
    if (ruta) this.router.navigate([ruta]);
  }

  marcarTodas(event: Event) {
    event.stopPropagation();   // para que el menu no se cierre
    const pendientes = this.notificaciones.filter(n => !n.leida);
    if (pendientes.length === 0) return;
    forkJoin(pendientes.map(n => this.notificacionService.marcarLeida(n.idNotificacion))).subscribe({
      next: () => pendientes.forEach(n => n.leida = true)
    });
  }

  private rutaDe(n: Notificacion): string | null {
    if (this.authService.getRol() !== 'ESTUDIANTE') return null;
    if (n.idReportePerdida) return '/mis-reportes';
    if (n.idHallazgo) return '/reportar-hallazgo';
    return null;
  }

  icono(tipo: string): string {
    const t = (tipo || '').toUpperCase();
    if (t.includes('COINCID') || t.includes('SUGER')) return 'bi-stars';
    if (t.includes('RECLAMO')) return 'bi-chat-left-text';
    if (t.includes('ENTREG')) return 'bi-box-arrow-right';
    if (t.includes('HALLAZGO') || t.includes('CUSTOD')) return 'bi-box-seam';
    return 'bi-bell';
  }
}