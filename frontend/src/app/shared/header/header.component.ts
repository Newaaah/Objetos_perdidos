import { Component, inject, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PersonaService } from '../../core/services/persona.service';
import { TemaService } from '../../core/services/tema.service';
import { PersonaResumen } from '../../core/interfaces/persona.interface';
import { CampanaComponent } from '../campana/campana.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CampanaComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  authService = inject(AuthService);
  tema = inject(TemaService);           // modo claro / oscuro
  private personaService = inject(PersonaService);

  rol = '';
  userName = '';                         // lo que escribio en el login (codigo o DNI)
  perfil: PersonaResumen | null = null;  // nombre real (viene del backend)

  ngOnInit(): void {
    // El header solo existe con sesion iniciada (dentro del layout)
    this.rol = this.authService.getRol() || '';
    this.userName = localStorage.getItem('user_name') || '';
    this.personaService.miPerfil().subscribe({
      next: (p) => this.perfil = p,
      error: () => this.perfil = null   // si falla, se muestra lo de siempre
    });
  }

  get etiquetaRol(): string {
    switch (this.rol) {
      case 'ADMIN': return 'Administrador';
      case 'PERSONAL': return 'Personal';
      case 'ESTUDIANTE': return 'Estudiante';
      default: return '';
    }
  }

  // "Diego Alonso Shupingahua Garate"
  get nombreCompleto(): string {
    return this.perfil ? `${this.perfil.nombres} ${this.perfil.apellidos}` : this.userName;
  }

  // "Diego" (para el boton de arriba)
  get primerNombre(): string {
    return this.perfil ? this.perfil.nombres.split(' ')[0] : this.etiquetaRol;
  }

  // "Codigo 202511992" para estudiantes, "DNI 70000001" para el resto
  get identificador(): string {
    if (!this.perfil) return this.userName;
    return this.perfil.codigoUni
      ? `Código ${this.perfil.codigoUni}`
      : `DNI ${this.perfil.documentoIdentidad}`;
  }

  logout() {
    this.authService.logout();
  }
} 