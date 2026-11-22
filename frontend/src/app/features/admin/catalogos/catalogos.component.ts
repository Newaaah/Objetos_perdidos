import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, concatMap, finalize, from, map, of } from 'rxjs';
import { CatalogoAdminService, Registro } from '../../../core/services/catalogo-admin.service';
import { AlertService } from '../../../core/services/alert.service';

interface Campo {
  clave: string;        // nombre del campo en el DTO de Java
  etiqueta: string;
  max: number;
  obligatorio: boolean;
  ayuda?: string;
}

interface CatalogoConfig {
  recurso: string;      // parte de la URL: /api/v1/{recurso}
  titulo: string;
  singular: string;
  icono: string;
  idCampo: string;      // nombre del ID en el DTO
  campos: Campo[];
}

@Component({
  selector: 'app-catalogos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './catalogos.component.html',
  styleUrl: './catalogos.component.css'
})
export class CatalogosComponent implements OnInit {
  private catalogoAdminService = inject(CatalogoAdminService);
  private alertService = inject(AlertService);

  // Configuracion: mismos campos que CategoriaDTO, UbicacionDTO y CarreraDTO
  catalogos: CatalogoConfig[] = [
    {
      recurso: 'categorias', titulo: 'Categorías', singular: 'categoría', icono: 'bi-tags', idCampo: 'idCategoria',
      campos: [{ clave: 'nombre', etiqueta: 'Nombre', max: 50, obligatorio: true }]
    },
    {
      recurso: 'ubicaciones', titulo: 'Ubicaciones', singular: 'ubicación', icono: 'bi-geo-alt', idCampo: 'idUbicacion',
      campos: [
        { clave: 'nombre', etiqueta: 'Nombre', max: 100, obligatorio: true },
        { clave: 'detalle', etiqueta: 'Detalle', max: 255, obligatorio: false,
          ayuda: 'Si es un punto donde se reciben objetos, incluye la palabra "recepcion" (así aparecerá como destino).' }
      ]
    },
    {
      recurso: 'carreras', titulo: 'Carreras', singular: 'carrera', icono: 'bi-mortarboard', idCampo: 'idCarrera',
      campos: [{ clave: 'nombre', etiqueta: 'Nombre', max: 50, obligatorio: true }]
    }
  ];

  activo: CatalogoConfig = this.catalogos[0];
  registros: Registro[] = [];
  cargando = true;
  filtro = '';

  // Formulario (sirve para crear y para editar)
  form: Record<string, string> = {};
  editandoId: number | null = null;
  guardando = false;

  // Agregar varios
  modoVarios = false;
  textoVarios = '';
  importando = false;
  hechos = 0;
  totalImportar = 0;

  ngOnInit(): void {
    this.seleccionar(this.activo);
  }

  seleccionar(c: CatalogoConfig) {
    this.activo = c;
    this.filtro = '';
    this.modoVarios = false;
    this.textoVarios = '';
    this.nuevo();
    this.cargar();
  }

  cargar() {
    this.cargando = true;
    this.catalogoAdminService.listar(this.activo.recurso).subscribe({
      next: (data) => {
        this.registros = data.sort((a, b) => this.idDe(a) - this.idDe(b));
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.alertService.error(`No se pudieron cargar las ${this.activo.titulo.toLowerCase()}`);
      }
    });
  }

  // Lista filtrada por el buscador
  get registrosFiltrados(): Registro[] {
    const f = this.normalizar(this.filtro);
    if (!f) return this.registros;
    return this.registros.filter(r =>
      this.activo.campos.some(c => this.normalizar(r[c.clave] || '').includes(f)));
  }

  idDe(r: Registro): number {
    return r[this.activo.idCampo];
  }

  // ===== UNO POR UNO =====
  nuevo() {
    this.editandoId = null;
    this.form = {};
    this.activo.campos.forEach(c => this.form[c.clave] = '');
  }

  editar(r: Registro) {
    this.modoVarios = false;
    this.editandoId = this.idDe(r);
    this.form = {};
    this.activo.campos.forEach(c => this.form[c.clave] = r[c.clave] ?? '');
  }

  // El formulario es valido si los obligatorios no estan vacios
  get valido(): boolean {
    return this.activo.campos.every(c => !c.obligatorio || (this.form[c.clave] || '').trim().length > 0);
  }

  guardar() {
    if (!this.valido) return;
    // Armamos el cuerpo: texto recortado; los opcionales vacios van como null
    const datos: Registro = {};
    this.activo.campos.forEach(c => {
      const v = (this.form[c.clave] || '').trim();
      datos[c.clave] = v || null;
    });

    const peticion = this.editandoId
      ? this.catalogoAdminService.actualizar(this.activo.recurso, this.editandoId, datos)
      : this.catalogoAdminService.crear(this.activo.recurso, datos);

    this.guardando = true;
    peticion.pipe(finalize(() => this.guardando = false)).subscribe({
      next: () => {
        this.alertService.toast(this.editandoId ? 'Cambios guardados' : 'Registro creado');
        this.nuevo();
        this.cargar();
      },
      error: (err) => this.alertService.error(
        err.error?.mensaje || err.error?.message || 'No se pudo guardar')
    });
  }

  async eliminar(r: Registro) {
    const ok = await this.alertService.confirm(
      `Eliminar la ${this.activo.singular} "${r['nombre']}". Solo se puede si no está en uso.`);
    if (!ok) return;
    this.catalogoAdminService.eliminar(this.activo.recurso, this.idDe(r)).subscribe({
      next: () => {
        this.alertService.toast('Eliminado');
        if (this.editandoId === this.idDe(r)) this.nuevo();
        this.cargar();
      },
      error: (err) => this.alertService.error(
        err.error?.mensaje || err.error?.message ||
        `No se puede eliminar: la ${this.activo.singular} está siendo usada.`)
    });
  }

  // ===== AGREGAR VARIOS =====
  abrirVarios() {
    this.modoVarios = !this.modoVarios;
    this.textoVarios = '';
    this.nuevo();
  }

  // Convierte el texto pegado en registros: una linea = un registro; "nombre | detalle"
  get lineas(): Registro[] {
    const vistos = new Set<string>();
    const lista: Registro[] = [];
    this.textoVarios.split('\n').forEach(linea => {
      const partes = linea.split('|').map(p => p.trim());
      if (!partes[0]) return;
      const datos: Registro = {};
      this.activo.campos.forEach((c, i) => {
        const v = (partes[i] || '').substring(0, c.max);
        datos[c.clave] = v || null;
      });
      const clave = this.normalizar(datos['nombre']);
      if (vistos.has(clave)) return;   // repetido dentro del mismo texto
      vistos.add(clave);
      lista.push(datos);
    });
    return lista;
  }

  // Solo los que NO existen todavia (no se cambia lo existente)
  get nuevos(): Registro[] {
    const existentes = new Set(this.registros.map(r => this.normalizar(r['nombre'])));
    return this.lineas.filter(d => !existentes.has(this.normalizar(d['nombre'])));
  }

  get repetidos(): number {
    return this.lineas.length - this.nuevos.length;
  }

  importar() {
    const lista = this.nuevos;
    if (lista.length === 0) return;

    this.importando = true;
    this.hechos = 0;
    this.totalImportar = lista.length;
    let creados = 0;
    let fallidos = 0;

    // Uno tras otro (concatMap) para no saturar el backend
    from(lista).pipe(
      concatMap(datos => this.catalogoAdminService.crear(this.activo.recurso, datos).pipe(
        map(() => true),
        catchError(() => of(false))
      )),
      finalize(() => {
        this.importando = false;
        this.textoVarios = '';
        this.modoVarios = false;
        this.cargar();
        if (fallidos === 0) {
          this.alertService.success(`Se agregaron ${creados} registros.`, 'Listo');
        } else {
          this.alertService.error(`Se agregaron ${creados}; ${fallidos} no se pudieron guardar.`);
        }
      })
    ).subscribe(ok => {
      this.hechos++;
      ok ? creados++ : fallidos++;
    });
  }

  // Para comparar sin importar mayusculas, tildes ni espacios de mas
  private normalizar(texto: string): string {
    return (texto || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
      .toLowerCase().replace(/\s+/g, ' ').trim();
  }
}