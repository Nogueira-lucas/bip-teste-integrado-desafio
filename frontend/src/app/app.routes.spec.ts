import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Location } from '@angular/common';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';

import { routes } from './app.routes';
import { BeneficioService } from './services/beneficio.service';
import { BeneficioListComponent } from './features/beneficios/beneficio-list/beneficio-list.component';

function buildRouter() {
  const serviceSpy = { listar: jest.fn().mockReturnValue(of([])) };

  TestBed.configureTestingModule({
    imports: [NoopAnimationsModule],
    providers: [
      provideRouter(routes),
      provideHttpClient(),
      { provide: BeneficioService, useValue: serviceSpy },
      { provide: MatDialog, useValue: { open: jest.fn() } },
      { provide: MatSnackBar, useValue: { open: jest.fn() } },
    ],
  });

  return {
    router: TestBed.inject(Router),
    location: TestBed.inject(Location),
  };
}

describe('app.routes — navegação', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it("'' redireciona para /beneficios", fakeAsync(() => {
    const { router, location } = buildRouter();
    router.initialNavigation();
    router.navigate(['']);
    tick();
    expect(location.path()).toBe('/beneficios');
  }));

  it("'/beneficios' resolve para BeneficioListComponent", fakeAsync(() => {
    const { router } = buildRouter();
    router.initialNavigation();
    router.navigate(['/beneficios']);
    tick();

    const config = router.config;
    const beneficiosRoute = config.find((r) => r.path === 'beneficios');
    expect(beneficiosRoute?.component).toBe(BeneficioListComponent);
  }));

  it("a configuração tem exatamente 2 entradas de rota", () => {
    const { router } = buildRouter();
    expect(router.config).toHaveLength(2);
  });
});
