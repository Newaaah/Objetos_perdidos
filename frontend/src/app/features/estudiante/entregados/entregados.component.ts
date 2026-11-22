import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ObjetoService } from '../../../core/services/objeto.service';
import { ReclamoService } from '../../../core/services/reclamo.service';
import { AlertService } from '../../../core/services/alert.service';
import { ObjetoEntregado } from '../../../core/interfaces/objeto.interface';
import { Reclamo } from '../../../core/interfaces/reclamo.interface';
import { EntregaService } from '../../../core/services/entrega.service';

@Component({
  selector: 'app-entregados',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './entregados.component.html',
  styleUrl: './entregados.component.css'
})
export class EntregadosComponent implements OnInit, OnDestroy {
  private objetoService = inject(ObjetoService);
  private reclamoService = inject(ReclamoService);
  private alertService = inject(AlertService);
  private entregaService = inject(EntregaService);
  private recibidas = new Set<number>();   // idEntrega de lo que YO recibi

  objetos: ObjetoEntregado[] = [];
  reclamos: Reclamo[] = [];
  cargando = true;
  dias = 90;

  seleccionado: ObjetoEntregado | null = null;
  descripcion = '';
  evidencia: File | null = null;
  preview: string | null = null;
  enviando = false;

  ngOnInit(): void {
    this.loadingEntregados();
    this.loadingReclamos();
        this.entregaService.mias().subscribe({
      next: (data) => this.recibidas = new Set(data.map(e => e.idEntrega))
    });
  }

  ngOnDestroy(): void {
    this.limpiarPreview();
  }

  loadingEntregados() {
    this.cargando = true;
    this.objetoService.getEntregados(this.dias).subscribe({
      next: (data) => {
        this.objetos = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.alertService.error('No se pudieron cargar los objetos entregados');
      }
    });
  }

  loadingReclamos() {
    this.reclamoService.mios().subscribe({
      next: (data) => this.reclamos = data.sort((a, b) => b.idReclamo - a.idReclamo),
      error: () => this.reclamos = []
    });
  }

  yaReclamado(idEntrega: number): boolean {
    return this.reclamos.some(r => r.idEntrega === idEntrega && r.estado !== 'RECHAZADO');
  }

  loRecibi(idEntrega: number): boolean {
    return this.recibidas.has(idEntrega);
  }

  nombreEntrega(idEntrega?: number): string {
    const o = this.objetos.find(x => x.idEntrega === idEntrega);
    return o ? o.nombre : `Entrega #${idEntrega}`;
  }

  abrirReclamo(o: ObjetoEntregado) {
    this.seleccionado = o;
    this.descripcion = '';
    this.evidencia = null;
    this.limpiarPreview();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cerrarReclamo() {
    this.seleccionado = null;
    this.descripcion = '';
    this.evidencia = null;
    this.limpiarPreview();
  }

  seleccionarEvidencia(event: Event) {
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
    this.evidencia = archivo;
    this.preview = URL.createObjectURL(archivo);
  }

  quitarEvidencia(input: HTMLInputElement) {
    this.limpiarPreview();
    this.evidencia = null;
    input.value = '';
  }

  async enviar() {
    if (!this.seleccionado) return;
    const texto = this.descripcion.trim();
    if (texto.length < 15) {
      this.alertService.error('Explica tu reclamo con al menos 15 caracteres');
      return;
    }
    const ok = await this.alertService.confirm(
      `Vas a reclamar la entrega de "${this.seleccionado.nombre}". El personal revisará tu caso.`);
    if (!ok) return;

    const datos = new FormData();
    datos.append('idEntrega', String(this.seleccionado.idEntrega));
    datos.append('descripcion', texto);
    if (this.evidencia) datos.append('evidencia', this.evidencia);

    this.enviando = true;
    this.reclamoService.crear(datos)
      .pipe(finalize(() => this.enviando = false))
      .subscribe({
        next: () => {
          this.alertService.success('Te notificaremos cuando el personal lo revise.', 'Reclamo enviado');
          this.cerrarReclamo();
          this.loadingReclamos();
        },
        error: (err) => this.alertService.error(
          err.error?.mensaje || err.error?.message || 'No se pudo enviar el reclamo')
      });
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'bg-warning text-dark';
      case 'EN_REVISION': return 'bg-info text-dark';
      case 'APROBADO': return 'bg-success';
      case 'RECHAZADO': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  private limpiarPreview() {
    if (this.preview) URL.revokeObjectURL(this.preview);
    this.preview = null;
  }
}