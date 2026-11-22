import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Categoria, Ubicacion } from '../interfaces/catalogo.interface';

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1';

  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.apiUrl}/categorias`);
  }

  getUbicaciones(): Observable<Ubicacion[]> {
    return this.http.get<Ubicacion[]>(`${this.apiUrl}/ubicaciones`);
  }
}