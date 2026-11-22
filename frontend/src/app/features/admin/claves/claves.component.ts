import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { CuentaService } from '../../../core/services/cuenta.service';
import { PersonaService } from '../../../core/services/persona.service';
import { AlertService } from '../../../core/services/alert.service';
import { PersonaResumen } from '../../../core/interfaces/persona.interface';

@Component({
  selector: 'app-claves',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './claves.component.html',
  styleUrl: './claves.component.css'
})
export class ClavesComponent {
  private fb = inject(FormBuilder);
  private cuentaService = inject(CuentaService);
  private personaService = inject(PersonaService);
  private alertService = inject(AlertService);

  // Paso 1: buscar al estudiante
  codigo = '';
  buscando = false;
  estudiante: PersonaResumen | null = null;

  // Paso 2: su nueva contraseña
  form: FormGroup;
  guardando = false;

  constructor() {
    this.form = this.fb.group({
      nuevaClave: ['', [Validators.required, Validators.minLength(12)]],
      confirmar: ['', Validators.required]
    });
  }

  buscar() {
    if (this.codigo.trim().length < 8) return;
    this.buscando = true;
    this.estudiante = null;
    this.personaService.buscar(this.codigo.trim())
      .pipe(finalize(() => this.buscando = false))
      .subscribe({
        next: (persona) => {
          if (!persona.codigoUni) {
            this.alertService.error('Esa persona no es estudiante');
            return;
          }
          this.estudiante = persona;
        },
        error: () => this.alertService.error('No se encontró ningún estudiante con ese código o DNI')
      });
  }

  cambiarEstudiante() {
    this.estudiante = null;
    this.codigo = '';
    this.form.reset();
  }

  async guardar() {
    if (this.form.invalid || !this.estudiante) return;
    const { nuevaClave, confirmar } = this.form.value;
    if (nuevaClave !== confirmar) {
      this.alertService.error('Las contraseñas no coinciden');
      return;
    }
    const nombre = `${this.estudiante.nombres} ${this.estudiante.apellidos}`;
    const ok = await this.alertService.confirm(`Desde ahora ${nombre} deberá usar esta contraseña para ingresar.`);
    if (!ok) return;

    this.guardando = true;
    this.cuentaService.definirClave(this.estudiante.idPersona, nuevaClave)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: () => {
          this.alertService.success(`Comunícale la contraseña a ${nombre} de forma privada.`, 'Contraseña habilitada');
          this.cambiarEstudiante();
        },
        error: (err) => this.alertService.error(
          err.status === 404
            ? 'No se encontró ningún estudiante con ese código o DNI'
            : `Error al buscar (código ${err.status}). Vuelve a iniciar sesión.`)
      });
  }
}