import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BeneficioService } from '../../../services/beneficio.service';
import { Beneficio } from '../../../models/beneficio.model';

@Component({
  selector: 'app-beneficio-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatSnackBarModule,
  ],
  templateUrl: './beneficio-form-dialog.component.html',
})
export class BeneficioFormDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(BeneficioService);
  private snack = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<BeneficioFormDialogComponent>);
  data: Beneficio | null = inject(MAT_DIALOG_DATA);

  form!: FormGroup;
  isEdit = false;
  saving = false;

  ngOnInit(): void {
    this.isEdit = !!this.data?.id;
    this.form = this.fb.group({
      nome: [this.data?.nome ?? '', [Validators.required, Validators.maxLength(100)]],
      descricao: [this.data?.descricao ?? ''],
      valor: [this.data?.valor ?? '', [Validators.required, Validators.min(0.01)]],
      ativo: [this.data?.ativo ?? true],
    });
  }

  salvar(): void {
    if (this.form.invalid) return;
    this.saving = true;

    const payload: Beneficio = { ...this.data, ...this.form.value };

    const request$ = this.isEdit
      ? this.service.atualizar(this.data!.id!, payload)
      : this.service.criar(payload);

    request$.subscribe({
      next: (result) => {
        this.snack.open(
          this.isEdit ? 'Benefício atualizado' : 'Benefício criado',
          'Fechar',
          { duration: 3000 }
        );
        this.dialogRef.close(result);
      },
      error: (err) => {
        const msg = err?.error?.detail ?? 'Erro ao salvar benefício';
        this.snack.open(msg, 'Fechar', { duration: 4000 });
        this.saving = false;
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
