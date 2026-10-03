
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const fechasCruzadasValidator: ValidatorFn = (
  grupo: AbstractControl
): ValidationErrors | null => {
  const despacho = grupo.get('fechaDespacho')?.value as string;
  const entrega = grupo.get('fechaEntregaEstimada')?.value as string;

  if (!despacho || !entrega) {
    return null;
  }

  const tDespacho = new Date(despacho).getTime();
  const tEntrega = new Date(entrega).getTime();

  return tEntrega > tDespacho ? null : { fechasInvalidas: true };
};