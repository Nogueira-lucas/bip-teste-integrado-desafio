import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BeneficioService } from '../../../services/beneficio.service';
import { Beneficio } from '../../../models/beneficio.model';
import { BeneficioFormDialogComponent } from '../beneficio-form-dialog/beneficio-form-dialog.component';
import { TransferenciaDialogComponent } from '../transferencia-dialog/transferencia-dialog.component';

@Component({
  selector: 'app-beneficio-list',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './beneficio-list.component.html',
  styleUrl: './beneficio-list.component.scss',
})
export class BeneficioListComponent implements OnInit {
  private service = inject(BeneficioService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns = ['id', 'nome', 'descricao', 'valor', 'ativo', 'acoes'];
  beneficios: Beneficio[] = [];
  loading = false;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => {
        this.beneficios = data;
        this.loading = false;
      },
      error: () => {
        this.snack.open('Erro ao carregar benefícios', 'Fechar', { duration: 3000 });
        this.loading = false;
      },
    });
  }

  abrirCriar(): void {
    const ref = this.dialog.open(BeneficioFormDialogComponent, {
      width: '480px',
      data: null,
    });
    ref.afterClosed().subscribe((salvo) => { if (salvo) this.carregar(); });
  }

  abrirEditar(beneficio: Beneficio): void {
    const ref = this.dialog.open(BeneficioFormDialogComponent, {
      width: '480px',
      data: { ...beneficio },
    });
    ref.afterClosed().subscribe((salvo) => { if (salvo) this.carregar(); });
  }

  inativar(beneficio: Beneficio): void {
    if (!confirm(`Inativar "${beneficio.nome}"?`)) return;
    this.service.deletar(beneficio.id!).subscribe({
      next: () => {
        this.snack.open('Benefício inativado', 'Fechar', { duration: 3000 });
        this.carregar();
      },
      error: () => this.snack.open('Erro ao inativar', 'Fechar', { duration: 3000 }),
    });
  }

  abrirTransferencia(): void {
    const ref = this.dialog.open(TransferenciaDialogComponent, {
      width: '480px',
      data: this.beneficios,
    });
    ref.afterClosed().subscribe((feito) => { if (feito) this.carregar(); });
  }
}
