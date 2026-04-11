import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Beneficio, TransferenciaRequest } from '../models/beneficio.model';

const BASE_URL = '/api/v1/beneficios';

@Injectable({ providedIn: 'root' })
export class BeneficioService {
  private http = inject(HttpClient);

  listar(): Observable<Beneficio[]> {
    return this.http.get<Beneficio[]>(BASE_URL);
  }

  buscar(id: number): Observable<Beneficio> {
    return this.http.get<Beneficio>(`${BASE_URL}/${id}`);
  }

  criar(beneficio: Beneficio): Observable<Beneficio> {
    return this.http.post<Beneficio>(BASE_URL, beneficio);
  }

  atualizar(id: number, beneficio: Beneficio): Observable<Beneficio> {
    return this.http.put<Beneficio>(`${BASE_URL}/${id}`, beneficio);
  }

  deletar(id: number): Observable<Beneficio> {
    return this.http.delete<Beneficio>(`${BASE_URL}/${id}`);
  }

  transferir(req: TransferenciaRequest): Observable<string> {
    return this.http.post(`${BASE_URL}/transferencia`, req, { responseType: 'text' });
  }
}
