import { Routes } from '@angular/router';
import { BeneficioListComponent } from './features/beneficios/beneficio-list/beneficio-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'beneficios', pathMatch: 'full' },
  { path: 'beneficios', component: BeneficioListComponent },
];
