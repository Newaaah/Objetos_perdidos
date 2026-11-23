import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { LowerCasePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AlertService } from '../../core/services/alert.service';
import { PersonaService } from '../../core/services/persona.service';
import { PersonaResumen } from '../../core/interfaces/persona.interface';
import { MisReportesComponent } from '../estudiante/mis-reportes/mis-reportes.component';
import { MisHallazgosComponent } from '../estudiante/mis-hallazgos/mis-hallazgos.component';
import { MisReclamosComponent } from '../estudiante/mis-reclamos/mis-reclamos.component';

type Pestana = 'reportes' | 'hallazgos' | 'reclamos' | 'clave';

@Component({
  selector: 'app-cuenta',
  standalone: true,
  imports: [ReactiveFormsModule, LowerCasePipe, MisReportesComponent, MisHallazgosComponent, MisReclamosComponent],
  templateUrl: './cuenta.component.html',
  styleUrl: './cuenta.component.css'
})
export class CuentaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private alertService = inject(AlertService);
  private personaService = inject(PersonaService);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);

  rol = this.authService.getRol() || '';
  ingreso = localStorage.getItem('user_name') || '';
  esEstudiante = this.rol === 'ESTUDIANTE';
  perfil: PersonaResumen | null = null;

  // Estudiante abre en "Mis reportes"; personal y admin solo tienen "Contraseña"
  pestana: Pestana = this.esEstudiante ? 'reportes' : 'clave';
  private orden: Pestana[] = ['reportes', 'hallazgos', 'reclamos', 'clave'];

  ver = { actual: false, nueva: false, confirmar: false };
  guardando = false;

  form: FormGroup = this.fb.group({
    claveActual: ['', Validators.required],
    nuevaClave: ['', [Validators.required, Validators.minLength(12)]],
    confirmar: ['', Validators.required]
  });

  ngOnInit(): void {
    this.personaService.miPerfil().subscribe({
      next: (p) => this.perfil = p,
      error: () => this.perfil = null   // si falla, se muestra el codigo/DNI de siempre
    });

    // /mi-cuenta?tab=reportes | hallazgos | reclamos | clave  (opcional)
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const tab = params.get('tab') as Pestana | null;
        if (!this.esEstudiante) this.pestana = 'clave';
        else if (tab && this.orden.includes(tab)) this.pestana = tab;
      });
  }

  // Posicion del fondo azul que se desliza entre pestañas
  get indicePestana(): number {
    return this.orden.indexOf(this.pestana);
  }

  // ===== Datos del perfil =====
  get nombreCompleto(): string {
    return this.perfil ? `${this.perfil.nombres} ${this.perfil.apellidos}` : this.ingreso;
  }

  get identificador(): string {
    if (!this.perfil) return '';
    return this.perfil.codigoUni ? `Código ${this.perfil.codigoUni}` : `DNI ${this.perfil.documentoIdentidad}`;
  }

  get etiquetaRol(): string {
    switch (this.rol) {
      case 'ADMIN': return 'Administrador';
      case 'PERSONAL': return 'Personal';
      case 'ESTUDIANTE': return 'Estudiante';
      default: return this.rol;
    }
  }

  get iconoRol(): string {
    switch (this.rol) {
      case 'ADMIN': return 'bi-shield-check';
      case 'PERSONAL': return 'bi-person-badge';
      default: return 'bi-mortarboard';
    }
  }

  // ===== Guia visual de la nueva contrasena (no bloquea nada) =====
  get nueva(): string {
    return this.form.value.nuevaClave || '';
  }

  get requisitos() {
    const c = this.nueva;
    return [
      { texto: 'Al menos 12 caracteres', ok: c.length >= 12 },
      { texto: 'Una letra mayúscula', ok: /[A-ZÁÉÍÓÚÑ]/.test(c) },
      { texto: 'Un número', ok: /\d/.test(c) },
      { texto: 'Un símbolo (!, @, #...)', ok: /[^A-Za-z0-9ÁÉÍÓÚÑáéíóúñ]/.test(c) }
    ];
  }

  get puntos(): number {
    return this.requisitos.filter(r => r.ok).length;
  }

  get nivel(): { texto: string; clase: string } {
    if (!this.nueva) return { texto: '', clase: '' };
    if (this.puntos <= 1) return { texto: 'Débil', clase: 'debil' };
    if (this.puntos <= 3) return { texto: 'Aceptable', clase: 'media' };
    return { texto: 'Fuerte', clase: 'fuerte' };
  }

  get noCoinciden(): boolean {
    const conf = this.form.value.confirmar || '';
    return conf.length > 0 && conf !== this.nueva;
  }

  // ===== Guardar (misma logica de siempre) =====
  guardar() {
    if (this.form.invalid || this.noCoinciden) {
      this.form.markAllAsTouched();
      if (this.noCoinciden) this.alertService.error('Las contraseñas no coinciden');
      return;
    }
    const { claveActual, nuevaClave } = this.form.value;

    this.guardando = true;
    this.authService.cambiarClave(claveActual, nuevaClave)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: () => {
          this.alertService.success('Usa tu nueva contraseña la próxima vez que ingreses.', 'Contraseña actualizada');
          this.form.reset();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo cambiar la contraseña. Revisa tu contraseña actual.')
      });
  }
}