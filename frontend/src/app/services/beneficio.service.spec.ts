import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { BeneficioService } from './beneficio.service';
import { Beneficio, TransferenciaRequest } from '../models/beneficio.model';

const BASE = '/api/v1/beneficios';

const mockBeneficio: Beneficio = {
  id: 1,
  nome: 'Plano de Saúde',
  descricao: 'Cobertura completa',
  valor: 250,
  ativo: true,
  version: 0,
};

describe('BeneficioService', () => {
  let service: BeneficioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BeneficioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  describe('listar()', () => {
    it('deve fazer GET em /api/v1/beneficios e retornar lista', () => {
      let result: Beneficio[] | undefined;
      service.listar().subscribe((data) => (result = data));

      const req = http.expectOne(BASE);
      expect(req.request.method).toBe('GET');
      req.flush([mockBeneficio]);

      expect(result).toEqual([mockBeneficio]);
    });
  });

  describe('buscar()', () => {
    it('deve fazer GET em /api/v1/beneficios/:id e retornar o benefício', () => {
      let result: Beneficio | undefined;
      service.buscar(1).subscribe((data) => (result = data));

      const req = http.expectOne(`${BASE}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBeneficio);

      expect(result).toEqual(mockBeneficio);
    });
  });

  describe('criar()', () => {
    it('deve fazer POST em /api/v1/beneficios com o payload e retornar o benefício criado', () => {
      const payload: Beneficio = { nome: 'Vale Refeição', valor: 500, ativo: true };
      let result: Beneficio | undefined;
      service.criar(payload).subscribe((data) => (result = data));

      const req = http.expectOne(BASE);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(payload);
      req.flush({ ...payload, id: 2, version: 0 });

      expect(result?.id).toBe(2);
    });
  });

  describe('atualizar()', () => {
    it('deve fazer PUT em /api/v1/beneficios/:id com o payload atualizado', () => {
      const payload: Beneficio = { ...mockBeneficio, nome: 'Plano Odonto' };
      let result: Beneficio | undefined;
      service.atualizar(1, payload).subscribe((data) => (result = data));

      const req = http.expectOne(`${BASE}/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(payload);
      req.flush(payload);

      expect(result?.nome).toBe('Plano Odonto');
    });
  });

  describe('deletar()', () => {
    it('deve fazer DELETE em /api/v1/beneficios/:id', () => {
      let result: Beneficio | undefined;
      service.deletar(1).subscribe((data) => (result = data));

      const req = http.expectOne(`${BASE}/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush({ ...mockBeneficio, ativo: false });

      expect(result?.ativo).toBe(false);
    });
  });

  describe('transferir()', () => {
    it('deve fazer POST em /api/v1/beneficios/transferencia com fromId, toId e valor', () => {
      const req_body: TransferenciaRequest = { fromId: 1, toId: 2, valor: 100 };
      let result: string | undefined;
      service.transferir(req_body).subscribe((msg) => (result = msg));

      const req = http.expectOne(`${BASE}/transferencia`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(req_body);
      req.flush('Transferência realizada com sucesso');

      expect(result).toBe('Transferência realizada com sucesso');
    });

    it('deve usar responseType text para receber a resposta como string', () => {
      service.transferir({ fromId: 1, toId: 2, valor: 50 }).subscribe();

      const req = http.expectOne(`${BASE}/transferencia`);
      expect(req.request.responseType).toBe('text');
      req.flush('ok');
    });
  });
});
