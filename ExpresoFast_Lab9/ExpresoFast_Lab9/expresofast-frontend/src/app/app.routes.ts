
import { Routes } from '@angular/router';
import { EnvioAvanzadoFormComponent } from './components/envio-avanzado-form/envio-avanzado-form.component';

export const routes: Routes = [

  { path: '', redirectTo: 'nuevo-envio', pathMatch: 'full' },

  { path: 'nuevo-envio', component: EnvioAvanzadoFormComponent },

  { path: '**', redirectTo: 'nuevo-envio' },
];