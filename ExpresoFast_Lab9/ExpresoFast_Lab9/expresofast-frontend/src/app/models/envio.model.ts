
export interface Paquete {
  id?: number;          
  descripcion: string;
  pesoKg: number;
}

export interface CrearEnvioPayload {
  numeroTracking: string;
  destinatario: string;
  direccionDestino: string;
  costo: number;
  vehiculoId: number;
  conductorId: number;
  fechaDespacho: string;          // formato ISO "YYYY-MM-DD" (lo que entrega <input type="date">)
  fechaEntregaEstimada: string;
  paquetes: Paquete[];
}

export interface EnvioRespuesta {
  id: number;
  numeroTracking: string;
  destinatario: string;
  direccionDestino: string;
  pesoKg: number;               
  costo: number;
  estadoEnvio: string;
  fechaDespacho: string;
  fechaEntregaEstimada: string;
  placaVehiculo: string;
  nombreConductor: string;
  paquetes: Paquete[];
}

export interface TrackingCheck {
  existe: boolean;
}

export interface ApiError {
  status: number;
  message: string;
  errores?: { campo: string; mensaje: string }[];
}