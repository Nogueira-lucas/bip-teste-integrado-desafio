import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { TransferenciaDialogComponent } from './transferencia-dialog.component';
import { BeneficioService } from '../../../services/beneficio.service';
import { Beneficio } from '../../../models/beneficio.model';

const mockBeneficios: Beneficio[] = [
  { id: 1, nome: 'Plano de Saúde', valor: 500, ativo: true },
  { id: 2, nome: 'Vale Refeição', valor: 300, ativo: true },
];

function buildSut() {
  const serviceSpy = { transferir: jest.fn() };
  const dialogRefSpy = { close: jest.fn() };
  const snackSpy = { open: jest.fn() };

  TestBed.configureTestingModule({
    imports: [TransferenciaDialogComponent, ReactiveFormsModule, NoopAnimationsModule],
    providers: [
      { provide: BeneficioService, useValue: serviceSpy },
      { provide: MatDialogRef, useValue: dialogRefSpy },
      { provide: MAT_DIALOG_DATA, useValue: mockBeneficios },
    ],
  });
  TestBed.overrideProvider(MatSnackBar, { useValue: snackSpy });

  const fixture = TestBed.createComponent(TransferenciaDialogComponent);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance, serviceSpy, dialogRefSpy, snackSpy };
}

describe('TransferenciaDialogComponent — inicialização', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('deve instanciar o componente', () => {
    const { component } = buildSut();
    expect(component).toBeTruthy();
  });

  it('deve receber a lista de benefícios via MAT_DIALOG_DATA', () => {
    const { component } = buildSut();
    expect(component.beneficios).toEqual(mockBeneficios);
  });

  it('form deve iniciar inválido (campos em branco)', () => {
    const { component } = buildSut();
    expect(component.form.invalid).toBe(true);
  });
});

describe('TransferenciaDialogComponent — validadores', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('fromId required — erro quando fromId está vazio', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: null, toId: 2, valor: 100 });
    expect(component.form.get('fromId')!.hasError('required')).toBe(true);
  });

  it('toId required — erro quando toId está vazio', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: null, valor: 100 });
    expect(component.form.get('toId')!.hasError('required')).toBe(true);
  });

  it('valor required — erro quando valor está vazio', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 2, valor: null });
    expect(component.form.get('valor')!.hasError('required')).toBe(true);
  });

  it('valor min(0.01) — erro quando valor é zero', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 2, valor: 0 });
    expect(component.form.get('valor')!.hasError('min')).toBe(true);
  });

  it('valor min(0.01) — válido com 0.01', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 2, valor: 0.01 });
    expect(component.form.get('valor')!.hasError('min')).toBe(false);
  });

  it('valor negativo — erro min', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 2, valor: -1 });
    expect(component.form.get('valor')!.hasError('min')).toBe(true);
  });
});

describe('TransferenciaDialogComponent — diferentesValidator', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('mesmaConta — erro quando fromId === toId', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 1, valor: 100 });
    expect(component.form.hasError('mesmaConta')).toBe(true);
    expect(component.form.invalid).toBe(true);
  });

  it('mesmaConta — sem erro quando fromId !== toId', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 2, valor: 100 });
    expect(component.form.hasError('mesmaConta')).toBe(false);
  });

  it('mesmaConta — sem erro quando fromId é null (ainda inválido por required)', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: null, toId: 1, valor: 100 });
    expect(component.form.hasError('mesmaConta')).toBe(false);
  });

  it('form válido somente quando fromId, toId e valor corretos e diferentes', () => {
    const { component } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 2, valor: 200 });
    expect(component.form.valid).toBe(true);
  });
});

describe('TransferenciaDialogComponent — transferir()', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('não chama service quando form é inválido', () => {
    const { component, serviceSpy } = buildSut();
    component.form.patchValue({ fromId: null, toId: null, valor: null });
    component.transferir();
    expect(serviceSpy.transferir).not.toHaveBeenCalled();
  });

  it('não chama service quando fromId === toId (mesmaConta)', () => {
    const { component, serviceSpy } = buildSut();
    component.form.patchValue({ fromId: 1, toId: 1, valor: 100 });
    component.transferir();
    expect(serviceSpy.transferir).not.toHaveBeenCalled();
  });

  it('chama service.transferir() com os valores do form', () => {
    const { component, serviceSpy, dialogRefSpy, snackSpy } = buildSut();
    serviceSpy.transferir.mockReturnValue(of('Transferência realizada com sucesso'));

    component.form.patchValue({ fromId: 1, toId: 2, valor: 150 });
    component.transferir();

    expect(serviceSpy.transferir).toHaveBeenCalledWith({ fromId: 1, toId: 2, valor: 150 });
    expect(snackSpy.open).toHaveBeenCalledWith(
      'Transferência realizada com sucesso',
      'Fechar',
      expect.any(Object),
    );
    expect(dialogRefSpy.close).toHaveBeenCalledWith(true);
  });

  it('erro com body JSON como string — exibe apenas o campo detail no snack', () => {
    const { component, serviceSpy, snackSpy } = buildSut();
    const problemDetail = JSON.stringify({
      type: 'about:blank',
      title: 'Erro interno',
      status: 500,
      detail: 'Saldo insuficiente para transferência',
      instance: '/api/v1/beneficios/transferencia',
    });
    serviceSpy.transferir.mockReturnValue(throwError(() => ({ error: problemDetail })));

    component.form.patchValue({ fromId: 1, toId: 2, valor: 999 });
    component.transferir();

    expect(snackSpy.open).toHaveBeenCalledWith(
      'Saldo insuficiente para transferência',
      'Fechar',
      expect.any(Object),
    );
    expect(component.saving).toBe(false);
  });

  it('erro com body já como objeto — lê detail diretamente', () => {
    const { component, serviceSpy, snackSpy } = buildSut();
    serviceSpy.transferir.mockReturnValue(
      throwError(() => ({ error: { detail: 'Benefício de origem não encontrado' } })),
    );

    component.form.patchValue({ fromId: 1, toId: 2, valor: 100 });
    component.transferir();

    expect(snackSpy.open).toHaveBeenCalledWith(
      'Benefício de origem não encontrado',
      'Fechar',
      expect.any(Object),
    );
  });

  it('erro com body JSON sem campo detail — exibe mensagem padrão', () => {
    const { component, serviceSpy, snackSpy } = buildSut();
    serviceSpy.transferir.mockReturnValue(
      throwError(() => ({ error: JSON.stringify({ title: 'Erro interno', status: 500 }) })),
    );

    component.form.patchValue({ fromId: 1, toId: 2, valor: 100 });
    component.transferir();

    expect(snackSpy.open).toHaveBeenCalledWith(
      'Erro ao realizar transferência',
      'Fechar',
      expect.any(Object),
    );
  });

  it('erro com body não-JSON — exibe mensagem padrão sem lançar exceção', () => {
    const { component, serviceSpy, snackSpy } = buildSut();
    serviceSpy.transferir.mockReturnValue(
      throwError(() => ({ error: 'Internal Server Error' })),
    );

    component.form.patchValue({ fromId: 1, toId: 2, valor: 100 });
    expect(() => component.transferir()).not.toThrow();
    expect(snackSpy.open).toHaveBeenCalledWith(
      'Erro ao realizar transferência',
      'Fechar',
      expect.any(Object),
    );
  });

  it('erro sem body — exibe mensagem padrão', () => {
    const { component, serviceSpy, snackSpy } = buildSut();
    serviceSpy.transferir.mockReturnValue(throwError(() => ({})));

    component.form.patchValue({ fromId: 1, toId: 2, valor: 100 });
    component.transferir();

    expect(snackSpy.open).toHaveBeenCalledWith(
      'Erro ao realizar transferência',
      'Fechar',
      expect.any(Object),
    );
  });
});

describe('TransferenciaDialogComponent — cancelar()', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('fecha o dialog com null', () => {
    const { component, dialogRefSpy } = buildSut();
    component.cancelar();
    expect(dialogRefSpy.close).toHaveBeenCalledWith(null);
  });
});
