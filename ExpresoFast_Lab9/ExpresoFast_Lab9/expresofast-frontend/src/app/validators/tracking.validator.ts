import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, catchError, map, of, switchMap, timer } from 'rxjs';

import { EnvioService } from '../services/envio.service';

export function trackingUnicoValidator(envioService: EnvioService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const numero = (control.value as string)?.trim();

    if (!numero) {
      return of(null);
    }

    return timer(400).pipe(
      switchMap(() => envioService.checkTracking(numero)),
      map((resp) => (resp.existe ? { trackingTomado: true } : null)),
      catchError(() => of(null))
    );
  };
}