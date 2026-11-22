import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { PerdidaService } from '../../../core/services/perdida.service';
import { AlertService } from '../../../core/services/alert.service';
import { Categoria, Ubicacion } from '../../../core/interfaces/catalogo.interface';
import { CrearPerdidaRequest } from '../../../core/interfaces/perdida.interface';

@Component({
  selector: 'app-reportar-perdida',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './reportar-perdida.component.html',
  styleUrl: './reportar-perdida.component.css'
})
export class ReportarPerdidaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private catalogoService = inject(CatalogoService);
  private perdidaService = inject(PerdidaService);
  private alertService = inject(AlertService);

  categorias: Categoria[] = [];
  ubicaciones: Ubicacion[] = [];
  guardando = false;
  hoy = this.fechaHoy();

  form: FormGroup = this.fb.group({
    idCategoria: [null, Validators.required],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    marca: ['', Validators.maxLength(100)],
    color: ['', Validators.maxLength(50)],
    descripcion: ['', [Validators.required, Validators.maxLength(255)]],
    idUbicacionPerdida: [null],
    fechaPerdida: ['']
  });

  ngOnInit(): void {
    this.catalogoService.getCategorias().subscribe({
      next: (data) => this.categorias = data,
      error: () => this.alertService.error('No se pudieron cargar las categorías')
    });
    this.catalogoService.getUbicaciones().subscribe({
      // Sin puntos de recepcion (Garita, Mesa de Partes, Limpieza): ahi no se pierden cosas, se llevan
      next: (data) => this.ubicaciones = data.filter(u => !(u.detalle || '').toLowerCase().includes('recepcion')),
      error: () => this.alertService.error('No se pudieron cargar las ubicaciones')
    });
  }

  // true si el campo esta mal y el usuario ya lo toco
  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && c.touched;
  }

  guardar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.value;
    // Los campos vacios no se mandan (el backend los toma como "no indicado")
    const datos: CrearPerdidaRequest = {
      idCategoria: v.idCategoria,
      nombre: v.nombre.trim(),
      marca: v.marca?.trim() || undefined,
      color: v.color?.trim() || undefined,
      descripcion: v.descripcion.trim(),
      idUbicacionPerdida: v.idUbicacionPerdida ?? undefined,
      fechaPerdida: v.fechaPerdida || undefined
    };

    this.guardando = true;
    this.perdidaService.crear(datos)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: () => {
          this.alertService.success('Te avisaremos si aparece algo parecido.', 'Reporte registrado');
          this.form.reset({ idCategoria: null, idUbicacionPerdida: null });
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo registrar el reporte')
      });
  }

  // Fecha local en formato AAAA-MM-DD (para no permitir fechas futuras)
  private fechaHoy(): string {
    const d = new Date();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mm}-${dd}`;
  }
}