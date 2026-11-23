import { Injectable } from '@angular/core';

export type Tema = 'claro' | 'oscuro';

@Injectable({
  providedIn: 'root'
})
export class TemaService {
  private clave = 'tema';
  tema: Tema = this.inicial();

  constructor() {
    this.aplicar(false);

    // Si el usuario nunca eligio, el tema sigue al de su computadora
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem(this.clave)) {
        this.tema = e.matches ? 'oscuro' : 'claro';
        this.aplicar(true);
      }
    });
  }

  get oscuro(): boolean {
    return this.tema === 'oscuro';
  }

  alternar(): void {
    this.tema = this.oscuro ? 'claro' : 'oscuro';
    localStorage.setItem(this.clave, this.tema);
    this.aplicar(true);
  }

  // Lo guardado gana; si no hay nada, se usa el tema del sistema
  private inicial(): Tema {
    const guardado = localStorage.getItem(this.clave);
    if (guardado === 'claro' || guardado === 'oscuro') return guardado;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'oscuro' : 'claro';
  }

  private aplicar(animar: boolean): void {
    const html = document.documentElement;
    if (animar) {
      // transicion suave de colores solo durante el cambio
      html.classList.add('tema-cambiando');
      setTimeout(() => html.classList.remove('tema-cambiando'), 450);
    }
    html.setAttribute('data-bs-theme', this.oscuro ? 'dark' : 'light');
  }
}