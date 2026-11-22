import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PersonaResumen } from '../interfaces/persona.interface';

@Injectable({
  providedIn: 'root'
})
export class PersonaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/personas';

  buscar(ingreso: string): Observable<PersonaResumen> {
    return this.http.get<PersonaResumen>(`${this.apiUrl}/buscar`, { params: { ingreso } });
  }
}