import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PersonaResumen } from '../interfaces/persona.interface';

@Injectable({
  providedIn: 'root'
})
export class PersonaService {
  private http = inject(HttpClient);

  // Buscar por codigo universitario o DNI (solo PERSONAL y ADMIN)
  buscar(ingreso: string): Observable<PersonaResumen> {
    return this.http.get<PersonaResumen>('/api/v1/personas/buscar', { params: { ingreso } });
  }

  // Mis propios datos (cualquier usuario conectado; el backend usa el token)
  miPerfil(): Observable<PersonaResumen> {
    return this.http.get<PersonaResumen>('/api/v1/cuenta/perfil');
  }
}