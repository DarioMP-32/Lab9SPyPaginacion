import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import {
    FormArray,
    FormControl,
    FormGroup,
    NonNullableFormBuilder,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';

import { ApiError, CrearEnvioPayload, EnvioRespuesta } from '../../models/envio.model';
import { EnvioService } from '../../services/envio.service';
import { fechasCruzadasValidator } from '../../validators/fechas.validator';
import { trackingUnicoValidator } from '../../validators/tracking.validator';


type PaqueteFormGroup = FormGroup<{
  descripcion: FormControl<string>;
  pesoKg: FormControl<number | null>; // null = campo vacío (type=number lo permite)
}>;

@Component({
  selector: 'app-envio-avanzado-form',
  // ReactiveFormsModule habilita [formGroup], formControlName, formArrayName...
  imports: [ReactiveFormsModule],
  templateUrl: './envio-avanzado-form.component.html',
  styleUrl: './envio-avanzado-form.component.css',
})
export class EnvioAvanzadoFormComponent {
  private readonly fb = inject(NonNullableFormBuilder); // controles que nunca son null
  private readonly envioService = inject(EnvioService);

  // Estado de la interfaz
  enviando = false;
  envioCreado: EnvioRespuesta | null = null;   // se llena al guardar con éxito
  errorServidor = '';                 // mensaje si el backend responde error


  readonly form = this.fb.group(
    {
      numeroTracking: this.fb.control('', {
        // Validadores SÍNCRONOS: se ejecutan primero y al instante.
        validators: [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{5,30}$/)],
        // Validador ASÍNCRONO: solo corre cuando los síncronos pasan.
        asyncValidators: [trackingUnicoValidator(this.envioService)],
      }),
      destinatario: this.fb.control('', [Validators.required, Validators.maxLength(100)]),
      direccionDestino: this.fb.control('', [Validators.required, Validators.maxLength(200)]),

      // Número o null: <input type="number"> vacío entrega null, por eso el tipo honesto es number | null.
      costo: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
      // IDs de vehículo y conductor (como en tu EnvioRequestDTO del Lab 9).
      vehiculoId: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
      conductorId: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
      fechaDespacho: this.fb.control('', Validators.required),
      fechaEntregaEstimada: this.fb.control('', Validators.required),

      // FormArray: lista dinámica de grupos, uno por paquete.
      // Arranca con 1 paquete porque el negocio exige mínimo 1.
      paquetes: this.fb.array<PaqueteFormGroup>([this.crearPaqueteGroup()]),
    },
    {
      // Validador CRUZADO a nivel de FormGroup (compara las dos fechas).
      validators: [fechasCruzadasValidator],
    }
  );

  // Atajos para usar en la plantilla y simplificar el código.
  get paquetes(): FormArray<PaqueteFormGroup> {
    return this.form.controls.paquetes;
  }
  get tracking() {
    return this.form.controls.numeroTracking;
  }

  /** Suma del peso de todos los paquetes (solo informativa en pantalla; el backend la recalcula). */
  get pesoTotal(): number {
    return this.paquetes.controls.reduce((acc, g) => acc + (g.controls.pesoKg.value ?? 0), 0);
  }

  /** Fábrica de un bloque de campos para un paquete. */
  private crearPaqueteGroup(): PaqueteFormGroup {
    return this.fb.group({
      descripcion: this.fb.control('', [Validators.required, Validators.maxLength(255)]),
      // Mínimo 0.01 y máximo 999.99 porque la columna es DECIMAL(5,2).
      pesoKg: new FormControl<number | null>(null, [
        Validators.required,
        Validators.min(0.01),
        Validators.max(999.99),
      ]),
    });
  }

  agregarPaquete(): void {
    this.paquetes.push(this.crearPaqueteGroup());
  }

  eliminarPaquete(indice: number): void {
    if (this.paquetes.length > 1) {
      this.paquetes.removeAt(indice);
    }
  }

  guardar(): void {
    if (this.form.invalid || this.form.pending) {
      this.form.markAllAsTouched(); // muestra todos los errores pendientes
      return;
    }

    this.enviando = true;
    this.errorServidor = '';
    this.envioCreado = null;

    // getRawValue() devuelve el valor TIPADO de todo el formulario.
    const v = this.form.getRawValue();
    const payload: CrearEnvioPayload = {
      numeroTracking: v.numeroTracking.trim(),
      destinatario: v.destinatario.trim(),
      direccionDestino: v.direccionDestino.trim(),
      costo: v.costo ?? 0,            
      vehiculoId: v.vehiculoId ?? 0,
      conductorId: v.conductorId ?? 0,
      fechaDespacho: v.fechaDespacho,
      fechaEntregaEstimada: v.fechaEntregaEstimada,
      paquetes: v.paquetes.map((p) => ({
        descripcion: p.descripcion.trim(),
        pesoKg: p.pesoKg ?? 0, 
      })),
    };

    this.envioService.crearEnvio(payload).subscribe({
      next: (envio) => {
        this.envioCreado = envio;
        this.enviando = false;
        this.reiniciarFormulario();
      },
      error: (err: HttpErrorResponse) => {
        this.enviando = false;
        const api = err.error as ApiError | null;
        if (err.status === 409) {
          this.tracking.setErrors({ trackingTomado: true });
          this.errorServidor = api?.message ?? 'Ese número de rastreo ya está en uso.';
        } else if (err.status === 400 || err.status === 404) {
          const detalle = api?.errores?.map((e) => e.mensaje).join(' · ');
          this.errorServidor = detalle || api?.message || 'Datos inválidos según el servidor.';
        } else {
          this.errorServidor = 'No se pudo conectar con el servidor. ¿Está corriendo el backend?';
        }
      },
    });
  }

  /** Deja el formulario limpio con un solo paquete vacío. */
  private reiniciarFormulario(): void {
    this.paquetes.clear();
    this.agregarPaquete();
    this.form.reset();
  }
}