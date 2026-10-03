package cr.ac.ucr.paraiso.ie.c5h060.expresofast.business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.EnvioMetricasRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.ResumenMetricasDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception.ResourceNotFoundException;

@Service
public class EnvioService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "EN_TRANSITO", "ENTREGADO", "CANCELADO");
    private static final Set<String> ESTADOS_FINALES = Set.of("ENTREGADO", "CANCELADO");
    private static final Set<String> ESTADOS_NO_REGRESABLES = Set.of("PENDIENTE", "EN_TRANSITO");

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final BitacoraEnvioRepository bitacoraEnvioRepository;
    private final UsuarioRepository usuarioRepository;
    private final EnvioMetricasRepository envioMetricasRepository;

    public EnvioService(EnvioRepository envioRepository,
            VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository,
            BitacoraEnvioRepository bitacoraEnvioRepository,
            UsuarioRepository usuarioRepository,
            EnvioMetricasRepository envioMetricasRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.bitacoraEnvioRepository = bitacoraEnvioRepository;
        this.usuarioRepository = usuarioRepository;
        this.envioMetricasRepository = envioMetricasRepository;
    }

    @Transactional(readOnly = true)
    public List<Envio> obtenerEnviosOptimizados() {
        return envioRepository.findAllOptimizado();
    }

    @Transactional
    public Envio registrarEnvio(EnvioRequestDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findById(dto.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el vehiculo con ID " + dto.vehiculoId()));

        Conductor conductor = conductorRepository.findById(dto.conductorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el conductor con ID " + dto.conductorId()));

        if (dto.pesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new InvalidStateTransitionException(
                    "El peso del envio (" + dto.pesoKg() + " kg) supera la capacidad maxima del vehiculo "
                            + vehiculo.getPlaca() + " (" + vehiculo.getCapacidadKg() + " kg).");
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(dto.codigoRastreo());
        envio.setDireccionDestino(dto.direccionDestino());
        envio.setPesoKg(dto.pesoKg());
        envio.setCosto(dto.costo());
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        envio.setEstadoEnvio("PENDIENTE");

        return envioRepository.save(envio);
    }

    @Transactional
    public Envio actualizarEstado(Integer envioId, String nuevoEstado, String observaciones) {
        String estadoNormalizado = nuevoEstado == null ? null : nuevoEstado.trim().toUpperCase();

        if (estadoNormalizado == null || !ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new InvalidStateTransitionException(
                    "Estado invalido: " + nuevoEstado + ". Valores permitidos: " + ESTADOS_VALIDOS);
        }

        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el envio con ID " + envioId));

        String estadoAnterior = envio.getEstadoEnvio();

        // Reto autonomo: bloquear transicion invalida (ENTREGADO/CANCELADO -> PENDIENTE/EN_TRANSITO)
        if (ESTADOS_FINALES.contains(estadoAnterior) && ESTADOS_NO_REGRESABLES.contains(estadoNormalizado)) {
            throw new InvalidStateTransitionException(
                    "Transición de estado no permitida para el envío " + envio.getCodigoRastreo());
        }

        envio.setEstadoEnvio(estadoNormalizado);
        envioRepository.save(envio);

        Usuario usuarioActual = obtenerUsuarioAutenticado();

        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setEnvio(envio);
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNormalizado);
        bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setUsuario(usuarioActual);
        bitacora.setObservaciones(observaciones);
        bitacoraEnvioRepository.save(bitacora);

        return envio;
    }

    @Transactional(readOnly = true)
    public List<BitacoraEnvio> obtenerBitacora(Integer envioId) {
        if (!envioRepository.existsById(envioId)) {
            throw new ResourceNotFoundException("No existe el envio con ID " + envioId);
        }
        return bitacoraEnvioRepository.findByEnvioId(envioId);
    }

    private static final Map<String, String> CAMPOS_ORDENABLES = Map.of(
            "id", "id",
            "codigoRastreo", "codigoRastreo",
            "destinatario", "destinatario",
            "direccionDestino", "direccionDestino",
            "montoFlete", "costo",
            "estado", "estadoEnvio",
            "fechaCreacion", "fechaCreacion");

    @Transactional(readOnly = true)
    public Page<EnvioDTO> listarPaginado(int page, int size, String sortBy, String dir,
            String busqueda, String estado) {

        int pagina = Math.max(page, 0);
        int tamano = Math.min(Math.max(size, 1), 100);

        String campo = CAMPOS_ORDENABLES.getOrDefault(sortBy == null ? "" : sortBy, "fechaCreacion");
        Sort.Direction direccion = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direccion, campo);
        if (!campo.equals("id")) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }

        Pageable pageable = PageRequest.of(pagina, tamano, sort);

        String termino = busqueda == null ? "" : busqueda.trim();
        String estadoNormalizado = "";
        if (estado != null && !estado.isBlank()) {
            estadoNormalizado = estado.trim().toUpperCase();
            if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
                throw new InvalidStateTransitionException(
                        "Estado invalido: " + estado + ". Valores permitidos: " + ESTADOS_VALIDOS);
            }
        }

        return envioRepository.buscar(estadoNormalizado, termino, pageable).map(EnvioDTO::from);
    }

    @Transactional(readOnly = true)
    public List<EnvioDTO> listarViaStoredProcedure(String estado) {
        String estadoNormalizado = estado == null ? "" : estado.trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new InvalidStateTransitionException(
                    "Estado invalido: " + estado + ". Valores permitidos: " + ESTADOS_VALIDOS);
        }
        return envioRepository.obtenerPorEstadoSP(estadoNormalizado).stream()
                .map(EnvioDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResumenMetricasDTO> obtenerResumenMetricas() {
        return envioMetricasRepository.obtenerResumen();
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        String username = autenticacion.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }
}