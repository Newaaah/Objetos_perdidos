import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-panel-admin',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './panel-admin.component.html',
  styleUrl: './panel-admin.component.css'
})
export class PanelAdminComponent {
  secciones = [
    {
      icono: 'bi-person-badge', titulo: 'Gestión de objetos',
      descripcion: 'Hallazgos, custodias, entregas, coincidencias, reclamos e incidencias.', ruta: '/personal'
    },
    {
      icono: 'bi-journal-bookmark', titulo: 'Catálogos',
      descripcion: 'Categorías, ubicaciones y carreras que usa todo el sistema.', ruta: '/catalogos'
    },
    {
      icono: 'bi-key', titulo: 'Claves de estudiantes',
      descripcion: 'Habilita o restablece la contraseña de un estudiante.', ruta: '/claves'
    },
    {
      icono: 'bi-bar-chart', titulo: 'Estadísticas',
      descripcion: 'Reportes de objetos perdidos, encontrados y entregados.', ruta: '/estadisticas'
    }
  ];
}