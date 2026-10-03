import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CrearEnvioPayload, EnvioRespuesta, TrackingCheck } from '../models/envio.model';

@Injectable({ providedIn: 'root' })
export class EnvioService {
  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.API_URL}envios`;

 
  crearEnvio(payload: CrearEnvioPayload): Observable<EnvioRespuesta> {
    return this.http.post<EnvioRespuesta>(`${this.baseUrl}/con-paquetes`, payload);
  }

 
  checkTracking(numero: string): Observable<TrackingCheck> {
    return this.http.get<TrackingCheck>(
      `${this.baseUrl}/check-tracking/${encodeURIComponent(numero)}`
    );
  }
}