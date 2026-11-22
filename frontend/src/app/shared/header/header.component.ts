import { Component, inject, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CampanaComponent } from '../campana/campana.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CampanaComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  public authService = inject(AuthService);
  userName: string = '';
  rol: string | null = null;

  ngOnInit(): void {
    this.authService.isLoggedIn$.subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.userName = localStorage.getItem('user_name') || 'Usuario';
        this.rol = this.authService.getRol();
      } else {
        this.userName = '';
        this.rol = null;
      }
    });
  }

  get etiquetaRol(): string {
    return this.rol === 'ADMIN' ? 'Administrador' : this.rol === 'PERSONAL' ? 'Personal' : 'Estudiante';
  }

  logout(): void {
    this.authService.logout();
  }
}