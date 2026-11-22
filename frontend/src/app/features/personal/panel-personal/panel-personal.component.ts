import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-panel-personal',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './panel-personal.component.html',
  styleUrl: './panel-personal.component.css'
})
export class PanelPersonalComponent {
  modulos = [
    {
      icono: 'bi-clipboard-check', titulo: 'Revisión de hallazgos',
      descripcion: 'Aprueba o rechaza los objetos reportados por los estudiantes.', ruta: '/hallazgos'
    },
    {
      icono: 'bi-box-arrow-in-down', titulo: 'Recepción en custodia',
      descripcion: 'Registra el ingreso físico de un objeto aprobado o directo.', ruta: '/custodias'
    },
    {
      icono: 'bi-hand-thumbs-up', titulo: 'Registro de entrega',
      descripcion: 'Entrega el objeto a su dueño con verificación y foto.', ruta: '/entregas'
    },
    {
      icono: 'bi-link-45deg', titulo: 'Coincidencias',
      descripcion: 'Compara pérdidas con hallazgos y decide si son el mismo objeto.', ruta: '/coincidencias'
    },
    {
      icono: 'bi-flag', titulo: 'Reclamos',
      descripcion: 'Atiende a quienes creen que su objeto se entregó a otra persona.', ruta: '/reclamos'
    },
    {
      icono: 'bi-exclamation-triangle', titulo: 'Incidencias',
      descripcion: 'Registra problemas ocurridos durante la custodia.', ruta: '/incidencias'
    }
  ];
}