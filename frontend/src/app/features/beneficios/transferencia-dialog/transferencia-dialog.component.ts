import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BeneficioService } from '../../../services/beneficio.service';
import { Beneficio } from '../../../models/beneficio.model';

@Component({
  selector: 'app-transferencia-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
  ],
  templateUrl: './transferencia-dialog.component.html',
})
export class TransferenciaDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(BeneficioService);
  private snack = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<TransferenciaDialogComponent>);
  beneficios: Beneficio[] = inject(MAT_DIALOG_DATA);

  form!: FormGroup;
  saving = false;

  ngOnInit(): void {
    this.form = this.fb.group({
      fromId: [null, Validators.required],
      toId: [null, Validators.required],
      valor: [null, [Validators.required, Validators.min(0.01)]],
    }, { validators: this.diferentesValidator });
  }

  private diferentesValidator(group: FormGroup) {
    const from = group.get('fromId')?.value;
    const to = group.get('toId')?.value;
    return from && to && from === to ? { mesmaConta: true } : null;
  }

  transferir(): void {
    if (this.form.invalid) return;
    this.saving = true;
    this.service.transferir(this.form.value).subscribe({
      next: () => {
        this.snack.open('Transferência realizada com sucesso', 'Fechar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        let detail = 'Erro ao realizar transferência';
        try {
          const body = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          if (body?.detail) detail = body.detail;
        } catch { /* resposta não é JSON — mantém mensagem padrão */ }
        this.snack.open(detail, 'Fechar', { duration: 4000 });
        this.saving = false;
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
