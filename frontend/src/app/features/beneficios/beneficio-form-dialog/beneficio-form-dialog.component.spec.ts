import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { BeneficioFormDialogComponent } from './beneficio-form-dialog.component';
import { BeneficioService } from '../../../services/beneficio.service';
import { Beneficio } from '../../../models/beneficio.model';

const mockBeneficio: Beneficio = {
  id: 1,
  nome: 'Plano de Saúde',
  descricao: 'Cobertura completa',
  valor: 250,
  ativo: true,
  version: 0,
};

function buildSut(data: Beneficio | null) {
  const serviceSpy = { criar: jest.fn(), atualizar: jest.fn() };
  const dialogRefSpy = { close: jest.fn() };
  const snackSpy = { open: jest.fn() };

  TestBed.configureTestingModule({
    imports: [BeneficioFormDialogComponent, ReactiveFormsModule, NoopAnimationsModule],
    providers: [
      { provide: BeneficioService, useValue: serviceSpy },
      { provide: MatDialogRef, useValue: dialogRefSpy },
      { provide: MAT_DIALOG_DATA, useValue: data },
    ],
  });
  TestBed.overrideProvider(MatSnackBar, { useValue: snackSpy });

  const fixture = TestBed.createComponent(BeneficioFormDialogComponent);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance, serviceSpy, dialogRefSpy, snackSpy };
}

describe('BeneficioFormDialogComponent — modo criar', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('deve instanciar o componente', () => {
    const { component } = buildSut(null);
    expect(component).toBeTruthy();
  });

  it('isEdit deve ser false quando data não tem id', () => {
    const { component } = buildSut(null);
    expect(component.isEdit).toBe(false);
  });

  it('form deve iniciar com campos vazios e ativo=true', () => {
    const { component } = buildSut(null);
    expect(component.form.value).toMatchObject({ nome: '', descricao: '', ativo: true });
  });

  it('nome required — form inválido quando nome está vazio', () => {
    const { component } = buildSut(null);
    component.form.patchValue({ nome: '', valor: 100 });
    expect(component.form.get('nome')!.hasError('required')).toBe(true);
    expect(component.form.invalid).toBe(true);
  });

  it('nome maxLength(100) — erro quando nome excede 100 caracteres', () => {
    const { component } = buildSut(null);
    component.form.patchValue({ nome: 'a'.repeat(101), valor: 100 });
    expect(component.form.get('nome')!.hasError('maxlength')).toBe(true);
  });

  it('nome maxLength(100) — válido com exatamente 100 caracteres', () => {
    const { component } = buildSut(null);
    component.form.patchValue({ nome: 'a'.repeat(100), valor: 100 });
    expect(component.form.get('nome')!.hasError('maxlength')).toBe(false);
  });

  it('valor required — form inválido quando valor está vazio', () => {
    const { component } = buildSut(null);
    component.form.patchValue({ nome: 'Teste', valor: '' });
    expect(component.form.get('valor')!.hasError('required')).toBe(true);
  });

  it('valor min(0.01) — erro quando valor é zero', () => {
    const { component } = buildSut(null);
    component.form.patchValue({ nome: 'Teste', valor: 0 });
    expect(component.form.get('valor')!.hasError('min')).toBe(true);
  });

  it('valor min(0.01) — válido com 0.01', () => {
    const { component } = buildSut(null);
    component.form.patchValue({ nome: 'Teste', valor: 0.01 });
    expect(component.form.get('valor')!.hasError('min')).toBe(false);
  });

  it('form válido quando nome e valor preenchidos corretamente', () => {
    const { component } = buildSut(null);
    component.form.patchValue({ nome: 'Vale Refeição', valor: 500 });
    expect(component.form.valid).toBe(true);
  });

  it('salvar() não chama service quando form é inválido', () => {
    const { component, serviceSpy } = buildSut(null);
    component.form.patchValue({ nome: '', valor: '' });
    component.salvar();
    expect(serviceSpy.criar).not.toHaveBeenCalled();
  });

  it('salvar() chama service.criar() com o payload do form', () => {
    const { component, serviceSpy, dialogRefSpy, snackSpy } = buildSut(null);
    const novo: Beneficio = { id: 99, nome: 'Vale Alimentação', valor: 600, ativo: true };
    serviceSpy.criar.mockReturnValue(of(novo));

    component.form.patchValue({ nome: 'Vale Alimentação', valor: 600, ativo: true, descricao: '' });
    component.salvar();

    expect(serviceSpy.criar).toHaveBeenCalledWith(
      expect.objectContaining({ nome: 'Vale Alimentação', valor: 600 }),
    );
    expect(snackSpy.open).toHaveBeenCalledWith('Benefício criado', 'Fechar', expect.any(Object));
    expect(dialogRefSpy.close).toHaveBeenCalledWith(novo);
  });

  it('salvar() exibe snack com detail do erro e não fecha o dialog', () => {
    const { component, serviceSpy, dialogRefSpy, snackSpy } = buildSut(null);
    serviceSpy.criar.mockReturnValue(
      throwError(() => ({ error: { detail: 'Valor inválido' } })),
    );

    component.form.patchValue({ nome: 'Teste', valor: 100 });
    component.salvar();

    expect(snackSpy.open).toHaveBeenCalledWith('Valor inválido', 'Fechar', expect.any(Object));
    expect(dialogRefSpy.close).not.toHaveBeenCalled();
    expect(component.saving).toBe(false);
  });

  it('salvar() exibe mensagem padrão quando erro não tem detail', () => {
    const { component, serviceSpy, snackSpy } = buildSut(null);
    serviceSpy.criar.mockReturnValue(throwError(() => ({ error: {} })));

    component.form.patchValue({ nome: 'Teste', valor: 100 });
    component.salvar();

    expect(snackSpy.open).toHaveBeenCalledWith('Erro ao salvar benefício', 'Fechar', expect.any(Object));
  });

  it('cancelar() fecha o dialog com null', () => {
    const { component, dialogRefSpy } = buildSut(null);
    component.cancelar();
    expect(dialogRefSpy.close).toHaveBeenCalledWith(null);
  });
});

describe('BeneficioFormDialogComponent — modo editar', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('isEdit deve ser true quando data tem id', () => {
    const { component } = buildSut(mockBeneficio);
    expect(component.isEdit).toBe(true);
  });

  it('form deve ser pré-preenchido com os dados do benefício', () => {
    const { component } = buildSut(mockBeneficio);
    expect(component.form.value).toMatchObject({
      nome: 'Plano de Saúde',
      descricao: 'Cobertura completa',
      valor: 250,
      ativo: true,
    });
  });

  it('salvar() chama service.atualizar() com o id correto', () => {
    const { component, serviceSpy, snackSpy, dialogRefSpy } = buildSut(mockBeneficio);
    const atualizado = { ...mockBeneficio, nome: 'Plano Odonto' };
    serviceSpy.atualizar.mockReturnValue(of(atualizado));

    component.form.patchValue({ nome: 'Plano Odonto' });
    component.salvar();

    expect(serviceSpy.atualizar).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ nome: 'Plano Odonto' }),
    );
    expect(snackSpy.open).toHaveBeenCalledWith('Benefício atualizado', 'Fechar', expect.any(Object));
    expect(dialogRefSpy.close).toHaveBeenCalledWith(atualizado);
  });
});
