import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Un registro de cualquier catalogo: { idCategoria: 1, nombre: '...' }, etc.
export type Registro = Record<string, any>;

// Sirve para categorias, ubicaciones y carreras (mismo patron en el backend)
@Injectable({
  providedIn: 'root'
})
export class CatalogoAdminService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1';

  listar(recurso: string): Observable<Registro[]> {
    return this.http.get<Registro[]>(`${this.apiUrl}/${recurso}`);
  }

  crear(recurso: string, datos: Registro): Observable<Registro> {
    return this.http.post<Registro>(`${this.apiUrl}/${recurso}`, datos);
  }

  actualizar(recurso: string, id: number, datos: Registro): Observable<Registro> {
    return this.http.put<Registro>(`${this.apiUrl}/${recurso}/${id}`, datos);
  }

  eliminar(recurso: string, id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${recurso}/${id}`);
  }
}