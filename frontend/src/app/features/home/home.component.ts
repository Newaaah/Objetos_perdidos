import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ObjetoService } from '../../core/services/objeto.service';
import { AuthService } from '../../core/services/auth.service';
import { Objeto } from '../../core/interfaces/objeto.interface';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private objetoService = inject(ObjetoService);
  private authService = inject(AuthService);

  objetos: Objeto[] = [];
  cargando = true;
  error = false;

  // Filtros (solo en pantalla)
  busqueda = '';
  categoria = '';

  // Ventana de detalle
  seleccionado: Objeto | null = null;

  esEstudiante = this.authService.getRol() === 'ESTUDIANTE';

  ngOnInit(): void {
    this.loadingObjetos();
  }

  loadingObjetos() {
    this.cargando = true;
    this.error = false;
    this.objetoService.getObjetos().subscribe({
      next: (data) => {
        // los mas recientes primero (sirve si fechaRegistro es texto o Date)
        this.objetos = data.sort((a, b) => this.tiempo(b.fechaRegistro) - this.tiempo(a.fechaRegistro));
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.error = true;
      }
    });
  }

  // Categorias que realmente tienen objetos, ordenadas
  get categorias(): string[] {
    return [...new Set(this.objetos.map(o => o.categoria))].sort();
  }

  contar(categoria: string): number {
    return this.objetos.filter(o => o.categoria === categoria).length;
  }

  // Lista visible segun buscador y categoria
  get filtrados(): Objeto[] {
    const texto = this.normalizar(this.busqueda);
    return this.objetos.filter(o => {
      const okCategoria = !this.categoria || o.categoria === this.categoria;
      const okTexto = !texto || this.normalizar(`${o.nombre} ${o.marca || ''} ${o.color || ''}`).includes(texto);
      return okCategoria && okTexto;
    });
  }

  limpiarFiltros() {
    this.busqueda = '';
    this.categoria = '';
  }

  diasEnCustodia(o: Objeto): number {
    const ms = Date.now() - this.tiempo(o.fechaRegistro);
    return Math.max(0, Math.floor(ms / 86400000));
  }

  abrir(o: Objeto) {
    this.seleccionado = o;
  }

  cerrar() {
    this.seleccionado = null;
  }

  // Convierte la fecha (texto o Date) a milisegundos para poder compararla
  private tiempo(fecha: unknown): number {
    const t = new Date(fecha as string | Date).getTime();
    return isNaN(t) ? 0 : t;
  }

  private normalizar(t: string): string {
    return (t || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }
}