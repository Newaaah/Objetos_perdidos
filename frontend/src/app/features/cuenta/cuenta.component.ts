import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AlertService } from '../../core/services/alert.service';

@Component({
  selector: 'app-cuenta',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cuenta.component.html',
  styleUrl: './cuenta.component.css'
})
export class CuentaComponent {
  private fb = inject(FormBuilder);
  public authService = inject(AuthService);
  private alertService = inject(AlertService);

  form: FormGroup;
  guardando = false;

  constructor() {
    this.form = this.fb.group({
      claveActual: ['', Validators.required],
      nuevaClave: ['', [Validators.required, Validators.minLength(12)]],
      confirmar: ['', Validators.required]
    });
  }

  guardar() {
    if (this.form.invalid) return;
    const { claveActual, nuevaClave, confirmar } = this.form.value;
    if (nuevaClave !== confirmar) {
      this.alertService.error('Las contraseñas nuevas no coinciden');
      return;
    }
    this.guardando = true;
    this.authService.cambiarClave(claveActual, nuevaClave)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: () => {
          this.alertService.success('Tu contraseña se actualizó correctamente');
          this.form.reset();
        },
        error: () => this.alertService.error('La contraseña actual no es correcta')
      });
  }
}