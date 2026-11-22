import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ObjetoService } from '../../core/services/objeto.service';
import { Objeto } from '../../core/interfaces/objeto.interface';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private objetoService = inject(ObjetoService);

  objetos: Objeto[] = [];

  ngOnInit(): void {
    this.loadingObjetos();
  }

  loadingObjetos() {
    this.objetoService.getObjetos().subscribe({
      next: (data) => this.objetos = data,
      error: (err) => console.error('Error al cargar objetos', err)
    });
  }
}