import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { HallazgoService } from '../../../core/services/hallazgo.service';
import { AlertService } from '../../../core/services/alert.service';
import { Categoria, Ubicacion } from '../../../core/interfaces/catalogo.interface';
import { MiHallazgo } from '../../../core/interfaces/hallazgo.interface';

@Component({
  selector: 'app-reportar-hallazgo',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './reportar-hallazgo.component.html',
  styleUrl: './reportar-hallazgo.component.css'
})
export class ReportarHallazgoComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private catalogoService = inject(CatalogoService);
  private hallazgoService = inject(HallazgoService);
  private alertService = inject(AlertService);

  categorias: Categoria[] = [];
  ubicaciones: Ubicacion[] = [];   // donde lo encontro (sin puntos de recepcion)
  destinos: Ubicacion[] = [];      // donde lo va a dejar (solo puntos de recepcion)
  misHallazgos: MiHallazgo[] = [];

  imagen: File | null = null;
  preview: string | null = null;
  guardando = false;

  form: FormGroup = this.fb.group({
    idCategoria: [null, Validators.required],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    marca: ['', Validators.maxLength(100)],
    color: ['', Validators.maxLength(50)],
    descripcion: ['', [Validators.required, Validators.maxLength(255)]],
    idUbicacionEncontrado: [null, Validators.required],
    idUbicacionDestino: [null, Validators.required]
  });

  ngOnInit(): void {
    this.catalogoService.getCategorias().subscribe({
      next: (data) => this.categorias = data,
      error: () => this.alertService.error('No se pudieron cargar las categorías')
    });
    this.catalogoService.getUbicaciones().subscribe({
      next: (data) => {
        const esPunto = (u: Ubicacion) => (u.detalle || '').toLowerCase().includes('recepcion');
        // Donde lo encontro: todo MENOS Garita, Mesa de Partes y Limpieza
        this.ubicaciones = data.filter(u => !esPunto(u));
        // Donde lo va a dejar: SOLO los puntos de recepcion
        const puntos = data.filter(u => esPunto(u));
        this.destinos = puntos.length > 0 ? puntos : data;
      },
      error: () => this.alertService.error('No se pudieron cargar las ubicaciones')
    });
    this.loadingHallazgos();
  }

  ngOnDestroy(): void {
    this.limpiarPreview();
  }

  loadingHallazgos() {
    this.hallazgoService.misHallazgos().subscribe({
      next: (data) => this.misHallazgos = data.sort((a, b) => b.idHallazgo - a.idHallazgo),
      error: () => this.misHallazgos = []
    });
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && c.touched;
  }

  // Cuando elige una foto
  seleccionarImagen(event: Event) {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;

    if (!['image/jpeg', 'image/png', 'image/webp'].includes(archivo.type)) {
      this.alertService.error('Solo se permiten imágenes JPG, PNG o WebP');
      input.value = '';
      return;
    }
    if (archivo.size > 5 * 1024 * 1024) {
      this.alertService.error('La imagen no puede pesar más de 5 MB');
      input.value = '';
      return;
    }
    this.limpiarPreview();
    this.imagen = archivo;
    this.preview = URL.createObjectURL(archivo);   // vista previa local
  }

  quitarImagen(input: HTMLInputElement) {
    this.limpiarPreview();
    this.imagen = null;
    input.value = '';
  }

  guardar(input: HTMLInputElement) {
    if (this.form.invalid || !this.imagen) {
      this.form.markAllAsTouched();
      if (!this.imagen) this.alertService.error('La foto del objeto es obligatoria');
      return;
    }
    const v = this.form.value;

    // Armamos el formulario multipart (mismos nombres que CrearReporteHallazgoRequest)
    const datos = new FormData();
    datos.append('idCategoria', String(v.idCategoria));
    datos.append('nombre', v.nombre.trim());
    if (v.marca?.trim()) datos.append('marca', v.marca.trim());
    if (v.color?.trim()) datos.append('color', v.color.trim());
    datos.append('descripcion', v.descripcion.trim());
    datos.append('idUbicacionEncontrado', String(v.idUbicacionEncontrado));
    datos.append('idUbicacionDestino', String(v.idUbicacionDestino));
    datos.append('imagen', this.imagen);   // @RequestPart("imagen")

    this.guardando = true;
    this.hallazgoService.crear(datos)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: () => {
          this.alertService.success('Gracias. Lleva el objeto al punto de entrega que elegiste.', 'Hallazgo registrado');
          this.form.reset({ idCategoria: null, idUbicacionEncontrado: null, idUbicacionDestino: null });
          this.quitarImagen(input);
          this.loadingHallazgos();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo registrar el hallazgo')
      });
  }

  claseEstado(estado: string): string {
    const e = (estado || '').toUpperCase();
    if (e.includes('RECHAZ')) return 'bg-danger';
    if (e.includes('PEND') || e.includes('REVISION')) return 'bg-warning text-dark';
    if (e.includes('APROB') || e.includes('RECIB') || e.includes('CUSTOD')) return 'bg-success';
    return 'bg-secondary';
  }

  private limpiarPreview() {
    if (this.preview) URL.revokeObjectURL(this.preview);
    this.preview = null;
  }
}