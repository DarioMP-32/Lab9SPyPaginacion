package cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Envio;


public record EnvioConPaquetesResponseDTO(
        Integer id,
        String numeroTracking,
        String destinatario,
        String direccionDestino,
        BigDecimal pesoKg,          
        BigDecimal costo,
        String estadoEnvio,
        LocalDate fechaDespacho,
        LocalDate fechaEntregaEstimada,
        String placaVehiculo,
        String nombreConductor,
        List<PaqueteDTO> paquetes) {

    public static EnvioConPaquetesResponseDTO from(Envio e) {
        List<PaqueteDTO> paquetes = e.getPaquetes().stream()
                .map(p -> new PaqueteDTO(p.getId(), p.getDescripcion(), p.getPesoKg()))
                .toList();

        String placa = e.getVehiculo() != null ? e.getVehiculo().getPlaca() : null;
        String conductor = e.getConductor() != null
                ? e.getConductor().getNombre() + " " + e.getConductor().getApellidos()
                : null;

        return new EnvioConPaquetesResponseDTO(
                e.getId(), e.getCodigoRastreo(), e.getDestinatario(), e.getDireccionDestino(),
                e.getPesoKg(), e.getCosto(), e.getEstadoEnvio(),
                e.getFechaDespacho(), e.getFechaEntregaEstimada(),
                placa, conductor, paquetes);
    }
}