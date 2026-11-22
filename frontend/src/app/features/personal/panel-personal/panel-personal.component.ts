import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import { HallazgoService } from '../../../core/services/hallazgo.service';
import { CustodiaService } from '../../../core/services/custodia.service';
import { ObjetoService } from '../../../core/services/objeto.service';
import { ReclamoService } from '../../../core/services/reclamo.service';
import { IncidenciaService } from '../../../core/services/incidencia.service';

interface Contadores {
  hallazgos: number | null;     // por revisar
  recepcion: number | null;     // aprobados que aun no llegan
  custodia: number | null;      // en custodia (para entregar)
  reclamos: number | null;      // reclamos pendientes
  incidencias: number | null;   // incidencias pendientes
}

@Component({
  selector: 'app-panel-personal',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './panel-personal.component.html',
  styleUrl: './panel-personal.component.css'
})
export class PanelPersonalComponent implements OnInit {
  private hallazgoService = inject(HallazgoService);
  private custodiaService = inject(CustodiaService);
  private objetoService = inject(ObjetoService);
  private reclamoService = inject(ReclamoService);
  private incidenciaService = inject(IncidenciaService);

  cargando = true;
  c: Contadores = { hallazgos: null, recepcion: null, custodia: null, reclamos: null, incidencias: null };

  // clave = que contador mostrar en el globo de la tarjeta
  modulos: { icono: string; color: string; titulo: string; descripcion: string; ruta: string; clave?: keyof Contadores }[] = [
    { icono: 'bi-clipboard-check', color: 'azul', titulo: 'Revisión de hallazgos',
      descripcion: 'Aprueba o rechaza los objetos reportados por los estudiantes.', ruta: '/hallazgos', clave: 'hallazgos' },
    { icono: 'bi-box-arrow-in-down', color: 'verde', titulo: 'Recepción en custodia',
      descripcion: 'Registra el ingreso físico de un objeto aprobado o directo.', ruta: '/custodias', clave: 'recepcion' },
    { icono: 'bi-hand-thumbs-up', color: 'amarillo', titulo: 'Registro de entrega',
      descripcion: 'Entrega el objeto a su dueño con verificación y foto.', ruta: '/entregas', clave: 'custodia' },
    { icono: 'bi-link-45deg', color: 'morado', titulo: 'Coincidencias',
      descripcion: 'Compara pérdidas con hallazgos y decide si son el mismo objeto.', ruta: '/coincidencias' },
    { icono: 'bi-flag', color: 'rojo', titulo: 'Reclamos',
      descripcion: 'Atiende a quienes creen que su objeto se entregó a otra persona.', ruta: '/reclamos', clave: 'reclamos' },
    { icono: 'bi-exclamation-triangle', color: 'naranja', titulo: 'Incidencias',
      descripcion: 'Registra problemas ocurridos durante la custodia.', ruta: '/incidencias', clave: 'incidencias' }
  ];

  ngOnInit(): void {
    this.cargarContadores();
  }

  cargarContadores() {
    this.cargando = true;
    // Todas a la vez; si una falla, ese contador queda en null ("—")
    forkJoin({
      hallazgos: this.hallazgoService.pendientes().pipe(map(l => l.length), catchError(() => of(null))),
      recepcion: this.custodiaService.pendientes().pipe(map(l => l.length), catchError(() => of(null))),
      custodia: this.objetoService.getObjetos().pipe(map(l => l.length), catchError(() => of(null))),
      reclamos: this.reclamoService.listar('PENDIENTE').pipe(map(l => l.length), catchError(() => of(null))),
      incidencias: this.incidenciaService.listar('PENDIENTE').pipe(map(l => l.length), catchError(() => of(null)))
    }).subscribe(res => {
      this.c = res;
      this.cargando = false;
    });
  }

  valor(clave?: keyof Contadores): number | null {
    return clave ? this.c[clave] : null;
  }

  // Saludo segun la hora
  get saludo(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Buenos días';
    if (h < 19) return 'Buenas tardes';
    return 'Buenas noches';
  }
}