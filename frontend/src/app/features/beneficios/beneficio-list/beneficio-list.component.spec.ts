import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { BeneficioListComponent } from './beneficio-list.component';
import { BeneficioService } from '../../../services/beneficio.service';
import { Beneficio } from '../../../models/beneficio.model';
import { BeneficioFormDialogComponent } from '../beneficio-form-dialog/beneficio-form-dialog.component';
import { TransferenciaDialogComponent } from '../transferencia-dialog/transferencia-dialog.component';

const mockBeneficios: Beneficio[] = [
  { id: 1, nome: 'Plano de Saúde', descricao: 'Cobertura completa', valor: 250, ativo: true },
  { id: 2, nome: 'Vale Refeição', descricao: 'Alimentação', valor: 500, ativo: true },
];

function buildSut() {
  const serviceSpy = {
    listar: jest.fn().mockReturnValue(of(mockBeneficios)),
    deletar: jest.fn(),
  };
  const dialogRefSpy = { afterClosed: jest.fn().mockReturnValue(of(null)) };
  const dialogSpy = { open: jest.fn().mockReturnValue(dialogRefSpy) };
  const snackSpy = { open: jest.fn() };

  TestBed.configureTestingModule({
    imports: [BeneficioListComponent, NoopAnimationsModule],
    providers: [
      { provide: BeneficioService, useValue: serviceSpy },
    ],
  });
  TestBed.overrideProvider(MatDialog, { useValue: dialogSpy });
  TestBed.overrideProvider(MatSnackBar, { useValue: snackSpy });

  const fixture = TestBed.createComponent(BeneficioListComponent);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance, serviceSpy, dialogSpy, dialogRefSpy, snackSpy };
}

describe('BeneficioListComponent — inicialização', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('deve instanciar o componente', () => {
    const { component } = buildSut();
    expect(component).toBeTruthy();
  });

  it('ngOnInit chama service.listar() e popula beneficios', () => {
    const { component, serviceSpy } = buildSut();
    expect(serviceSpy.listar).toHaveBeenCalledTimes(1);
    expect(component.beneficios).toEqual(mockBeneficios);
  });

  it('loading fica false após carregar com sucesso', () => {
    const { component } = buildSut();
    expect(component.loading).toBe(false);
  });

  it('exibe snack de erro e loading=false quando listar() falha', () => {
    TestBed.resetTestingModule();
    const serviceSpy = { listar: jest.fn().mockReturnValue(throwError(() => new Error('fail'))) };
    const snackSpy = { open: jest.fn() };
    TestBed.configureTestingModule({
      imports: [BeneficioListComponent, NoopAnimationsModule],
      providers: [
        { provide: BeneficioService, useValue: serviceSpy },
      ],
    });
    TestBed.overrideProvider(MatDialog, { useValue: { open: jest.fn() } });
    TestBed.overrideProvider(MatSnackBar, { useValue: snackSpy });
    const fixture = TestBed.createComponent(BeneficioListComponent);
    fixture.detectChanges();

    expect(snackSpy.open).toHaveBeenCalledWith(
      'Erro ao carregar benefícios',
      'Fechar',
      expect.any(Object),
    );
    expect(fixture.componentInstance.loading).toBe(false);
  });

  it('define as colunas corretas na tabela', () => {
    const { component } = buildSut();
    expect(component.displayedColumns).toEqual(['id', 'nome', 'descricao', 'valor', 'ativo', 'acoes']);
  });
});

describe('BeneficioListComponent — abrirCriar()', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('abre BeneficioFormDialogComponent com data=null', () => {
    const { component, dialogSpy } = buildSut();
    component.abrirCriar();
    expect(dialogSpy.open).toHaveBeenCalledWith(
      BeneficioFormDialogComponent,
      expect.objectContaining({ data: null }),
    );
  });

  it('recarrega benefícios quando dialog fecha com valor', () => {
    const { component, dialogSpy, dialogRefSpy, serviceSpy } = buildSut();
    dialogRefSpy.afterClosed.mockReturnValue(of({ id: 3, nome: 'Novo' }));
    serviceSpy.listar.mockClear();

    component.abrirCriar();

    expect(serviceSpy.listar).toHaveBeenCalledTimes(1);
  });

  it('não recarrega quando dialog fecha com null', () => {
    const { component, dialogRefSpy, serviceSpy } = buildSut();
    dialogRefSpy.afterClosed.mockReturnValue(of(null));
    serviceSpy.listar.mockClear();

    component.abrirCriar();

    expect(serviceSpy.listar).not.toHaveBeenCalled();
  });
});

describe('BeneficioListComponent — abrirEditar()', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('abre BeneficioFormDialogComponent com uma cópia do benefício', () => {
    const { component, dialogSpy } = buildSut();
    const b = mockBeneficios[0];
    component.abrirEditar(b);
    expect(dialogSpy.open).toHaveBeenCalledWith(
      BeneficioFormDialogComponent,
      expect.objectContaining({ data: { ...b } }),
    );
  });

  it('recarrega benefícios quando dialog fecha com valor atualizado', () => {
    const { component, dialogRefSpy, serviceSpy } = buildSut();
    dialogRefSpy.afterClosed.mockReturnValue(of({ ...mockBeneficios[0], nome: 'Alterado' }));
    serviceSpy.listar.mockClear();

    component.abrirEditar(mockBeneficios[0]);

    expect(serviceSpy.listar).toHaveBeenCalledTimes(1);
  });
});

describe('BeneficioListComponent — inativar()', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('chama service.deletar() quando confirm retorna true', () => {
    const { component, serviceSpy, snackSpy } = buildSut();
    jest.spyOn(window, 'confirm').mockReturnValue(true);
    serviceSpy.deletar.mockReturnValue(of({ ...mockBeneficios[0], ativo: false }));
    serviceSpy.listar.mockClear();

    component.inativar(mockBeneficios[0]);

    expect(serviceSpy.deletar).toHaveBeenCalledWith(1);
    expect(snackSpy.open).toHaveBeenCalledWith('Benefício inativado', 'Fechar', expect.any(Object));
    expect(serviceSpy.listar).toHaveBeenCalledTimes(1);
  });

  it('não chama service.deletar() quando confirm retorna false', () => {
    const { component, serviceSpy } = buildSut();
    jest.spyOn(window, 'confirm').mockReturnValue(false);

    component.inativar(mockBeneficios[0]);

    expect(serviceSpy.deletar).not.toHaveBeenCalled();
  });

  it('exibe snack de erro quando deletar() falha', () => {
    const { component, serviceSpy, snackSpy } = buildSut();
    jest.spyOn(window, 'confirm').mockReturnValue(true);
    serviceSpy.deletar.mockReturnValue(throwError(() => new Error('fail')));

    component.inativar(mockBeneficios[0]);

    expect(snackSpy.open).toHaveBeenCalledWith('Erro ao inativar', 'Fechar', expect.any(Object));
  });
});

describe('BeneficioListComponent — abrirTransferencia()', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('abre TransferenciaDialogComponent com a lista atual de benefícios', () => {
    const { component, dialogSpy } = buildSut();
    component.abrirTransferencia();
    expect(dialogSpy.open).toHaveBeenCalledWith(
      TransferenciaDialogComponent,
      expect.objectContaining({ data: mockBeneficios }),
    );
  });

  it('recarrega benefícios quando dialog fecha com true', () => {
    const { component, dialogRefSpy, serviceSpy } = buildSut();
    dialogRefSpy.afterClosed.mockReturnValue(of(true));
    serviceSpy.listar.mockClear();

    component.abrirTransferencia();

    expect(serviceSpy.listar).toHaveBeenCalledTimes(1);
  });

  it('não recarrega quando dialog fecha com null', () => {
    const { component, dialogRefSpy, serviceSpy } = buildSut();
    dialogRefSpy.afterClosed.mockReturnValue(of(null));
    serviceSpy.listar.mockClear();

    component.abrirTransferencia();

    expect(serviceSpy.listar).not.toHaveBeenCalled();
  });
});
