import { Component, ElementRef, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CustodiaService } from '../../../core/services/custodia.service';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { PersonaService } from '../../../core/services/persona.service';
import { HallazgoService } from '../../../core/services/hallazgo.service';
import { AlertService } from '../../../core/services/alert.service';
import { PendienteRecepcion } from '../../../core/interfaces/custodia.interface';
import { Categoria, Ubicacion } from '../../../core/interfaces/catalogo.interface';
import { PersonaResumen } from '../../../core/interfaces/persona.interface';

@Component({
  selector: 'app-recepcion',
  standalone: true,
  imports: [DatePipe, FormsModule, NgTemplateOutlet],
  templateUrl: './recepcion.component.html',
  styleUrl: './recepcion.component.css'
})
export class RecepcionComponent implements OnInit, OnDestroy {
  private custodiaService = inject(CustodiaService);
  private catalogoService = inject(CatalogoService);
  private personaService = inject(PersonaService);
  private hallazgoService = inject(HallazgoService);
  private alertService = inject(AlertService);

  @ViewChild('archivo') archivo?: ElementRef<HTMLInputElement>;

  modo: 'hallazgo' | 'directa' = 'hallazgo';

  // Pestaña "con hallazgo"
  pendientes: PendienteRecepcion[] = [];
  cargando = true;
  seleccionado: PendienteRecepcion | null = null;
  fotoPrevia: string | null = null;

  // Catalogos
  categorias: Categoria[] = [];
  ubicaciones: Ubicacion[] = [];   // donde se encontro (sin puntos de recepcion)
  puntos: Ubicacion[] = [];        // donde queda en custodia (solo puntos de recepcion)

  // Pestaña "directa"
  directa = this.directaVacia();

  // Datos comunes a las dos formas
  idUbicacionActual: number | null = null;
  observacion = '';
  confirmo = false;
  imagen: File | null = null;
  preview: string | null = null;
  codigoEntrega = '';
  personaEntrega: PersonaResumen | null = null;
  buscandoPersona = false;
  guardando = false;

  ngOnInit(): void {
    this.catalogoService.getCategorias().subscribe({ next: (d) => this.categorias = d });
    this.catalogoService.getUbicaciones().subscribe({
      next: (d) => {
        const esPunto = (u: Ubicacion) => (u.detalle || '').toLowerCase().includes('recepcion');
        this.ubicaciones = d.filter(u => !esPunto(u));
        const p = d.filter(u => esPunto(u));
        this.puntos = p.length > 0 ? p : d;
      }
    });
    this.loadingPendientes();
  }

  ngOnDestroy(): void {
    this.liberar(this.preview);
    this.liberar(this.fotoPrevia);
  }

  loadingPendientes() {
    this.cargando = true;
    this.custodiaService.pendientes().subscribe({
      next: (data) => {
        this.pendientes = data.sort((a, b) => a.idHallazgo - b.idHallazgo);
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar los pendientes de recepción');
      }
    });
  }

  cambiarModo(m: 'hallazgo' | 'directa') {
    this.modo = m;
    this.seleccionado = null;
    this.limpiarComun();
    this.directa = this.directaVacia();
    this.idUbicacionActual = m === 'directa' ? (this.puntos[0]?.idUbicacion ?? null) : null;
  }

  elegir(p: PendienteRecepcion) {
    this.seleccionado = p;
    this.limpiarComun();
    this.idUbicacionActual = p.idUbicacionDestino ?? null;
    // Mostramos la foto del reporte para comparar con el objeto real
    this.hallazgoService.imagenRevision(p.idHallazgo).subscribe({
      next: (blob) => this.fotoPrevia = URL.createObjectURL(blob),
      error: () => this.fotoPrevia = null
    });
  }

  cancelar() {
    this.seleccionado = null;
    this.limpiarComun();
  }

  // ---- Quien lo trajo (opcional) ----
  buscarPersona() {
    const codigo = this.codigoEntrega.trim();
    if (codigo.length < 8) return;
    this.buscandoPersona = true;
    this.personaService.buscar(codigo)
      .pipe(finalize(() => this.buscandoPersona = false))
      .subscribe({
        next: (p) => this.personaEntrega = p,
        error: () => this.alertService.error('No se encontró a nadie con ese código o DNI')
      });
  }

  quitarPersona() {
    this.personaEntrega = null;
    this.codigoEntrega = '';
  }

  // ---- Foto ----
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
    this.liberar(this.preview);
    this.imagen = archivo;
    this.preview = URL.createObjectURL(archivo);
  }

  quitarImagen() {
    this.liberar(this.preview);
    this.preview = null;
    this.imagen = null;
    if (this.archivo) this.archivo.nativeElement.value = '';
  }

  // ---- Registrar ----
  registrar() {
    if (!this.idUbicacionActual) {
      this.alertService.error('Elige dónde quedará en custodia');
      return;
    }
    if (!this.confirmo) {
      this.alertService.error('Confirma que recibiste el objeto físicamente');
      return;
    }

    const datos = new FormData();
    if (this.modo === 'hallazgo') {
      if (!this.seleccionado) return;
      datos.append('idHallazgo', String(this.seleccionado.idHallazgo));
    } else {
      const d = this.directa;
      if (!d.idCategoria || !d.nombre.trim() || !d.descripcion.trim()) {
        this.alertService.error('Completa categoría, nombre y descripción');
        return;
      }
      if (!this.imagen) {
        this.alertService.error('En la recepción directa la foto es obligatoria');
        return;
      }
      datos.append('idCategoria', String(d.idCategoria));
      datos.append('nombre', d.nombre.trim());
      if (d.marca.trim()) datos.append('marca', d.marca.trim());
      if (d.color.trim()) datos.append('color', d.color.trim());
      datos.append('descripcion', d.descripcion.trim());
      if (d.idUbicacionEncontrado) datos.append('idUbicacionEncontrado', String(d.idUbicacionEncontrado));
    }

    // Campos comunes (mismos nombres que los DTO de Java)
    datos.append('idUbicacionActual', String(this.idUbicacionActual));
    if (this.personaEntrega) datos.append('idPersonaEntrega', String(this.personaEntrega.idPersona));
    if (this.observacion.trim()) datos.append('observacionRecepcion', this.observacion.trim());
    datos.append('confirmoRecepcionFisica', 'true');
    if (this.imagen) datos.append('imagen', this.imagen);

    const peticion = this.modo === 'hallazgo'
      ? this.custodiaService.recibirConHallazgo(datos)
      : this.custodiaService.recibirDirecta(datos);

    this.guardando = true;
    peticion.pipe(finalize(() => this.guardando = false)).subscribe({
      next: (c) => {
        this.alertService.success(
          `"${c.nombreObjeto}" quedó en ${c.ubicacionActual} con la custodia #${c.idCustodia}. Ya aparece en el catálogo.`,
          'Objeto en custodia');
        if (this.modo === 'hallazgo') {
          this.cancelar();
          this.loadingPendientes();
        } else {
          this.cambiarModo('directa');
        }
      },
      error: (err) => this.alertService.error(
        err.error?.mensaje || err.error?.message || 'No se pudo registrar la recepción')
    });
  }

  private limpiarComun() {
    this.observacion = '';
    this.confirmo = false;
    this.quitarPersona();
    this.quitarImagen();
    this.liberar(this.fotoPrevia);
    this.fotoPrevia = null;
  }

  private liberar(url: string | null) {
    if (url) URL.revokeObjectURL(url);
  }

  private directaVacia() {
    return {
      idCategoria: null as number | null,
      nombre: '',
      marca: '',
      color: '',
      descripcion: '',
      idUbicacionEncontrado: null as number | null
    };
  }
}