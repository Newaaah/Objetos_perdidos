import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private alertService = inject(AlertService);
  private authService = inject(AuthService);

  ingreso = '';
  password = '';
  verPassword = false;
  isLoading = false;

  login(): void {
    if (!this.ingreso.trim()) return;
    this.isLoading = true;
    this.authService.login({ ingreso: this.ingreso.trim(), password: this.password || undefined })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (success) => {
          if (success) {
            this.alertService.toast('Bienvenido');
            this.password = '';
          } else {
            this.alertService.error('Código, DNI o contraseña incorrectos');
          }
        }
      });
  }
}