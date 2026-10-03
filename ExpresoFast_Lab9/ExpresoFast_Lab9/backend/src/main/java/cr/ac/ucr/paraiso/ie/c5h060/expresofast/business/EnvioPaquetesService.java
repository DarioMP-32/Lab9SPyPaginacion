package cr.ac.ucr.paraiso.ie.c5h060.expresofast.business;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Paquete;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioConPaquetesResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioRegistroDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.PaqueteDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception.ResourceNotFoundException;


@Service
public class EnvioPaquetesService {

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public EnvioPaquetesService(EnvioRepository envioRepository,
            VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

  
    @Transactional
    public EnvioConPaquetesResponseDTO registrarConPaquetes(EnvioRegistroDTO dto) {

    
        if (!dto.fechaEntregaEstimada().isAfter(dto.fechaDespacho())) {
            throw new InvalidStateTransitionException(
                    "La fecha de entrega estimada debe ser posterior a la fecha de despacho.");
        }


        String codigo = dto.numeroTracking().trim();
        if (envioRepository.existsByCodigoRastreo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El número de rastreo '" + codigo + "' ya está en uso.");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(dto.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el vehiculo con ID " + dto.vehiculoId()));
        Conductor conductor = conductorRepository.findById(dto.conductorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el conductor con ID " + dto.conductorId()));


        BigDecimal pesoTotal = dto.paquetes().stream()
                .map(PaqueteDTO::pesoKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (pesoTotal.compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new InvalidStateTransitionException(
                    "El peso total de los paquetes (" + pesoTotal + " kg) supera la capacidad del vehiculo "
                            + vehiculo.getPlaca() + " (" + vehiculo.getCapacidadKg() + " kg).");
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(codigo);
        envio.setDestinatario(dto.destinatario().trim());
        envio.setDireccionDestino(dto.direccionDestino().trim());
        envio.setPesoKg(pesoTotal);
        envio.setCosto(dto.costo());
        envio.setEstadoEnvio("PENDIENTE");
        envio.setFechaDespacho(dto.fechaDespacho());
        envio.setFechaEntregaEstimada(dto.fechaEntregaEstimada());
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);

        for (PaqueteDTO p : dto.paquetes()) {
            Paquete paquete = new Paquete();
            paquete.setDescripcion(p.descripcion().trim());
            paquete.setPesoKg(p.pesoKg());
            envio.addPaquete(paquete);
        }

        Envio guardado;
        try {
            guardado = envioRepository.save(envio);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El número de rastreo '" + codigo + "' ya está en uso.");
        }

        return EnvioConPaquetesResponseDTO.from(guardado);
    }

    @Transactional(readOnly = true) 
    public boolean existeCodigoRastreo(String codigo) {
        return envioRepository.existsByCodigoRastreo(codigo.trim());
    }
}