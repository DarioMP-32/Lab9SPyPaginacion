package cr.ac.ucr.paraiso.ie.c5h060.expresofast.business;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.VehiculoRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.VehiculoResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception.ResourceNotFoundException;


@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final EmpresaLogisticaRepository empresaLogisticaRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
            EmpresaLogisticaRepository empresaLogisticaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.empresaLogisticaRepository = empresaLogisticaRepository;
    }

    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> obtenerTodos() {
        return vehiculoRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtenerPorId(Integer id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el vehiculo con ID " + id));
        return toResponseDTO(vehiculo);
    }

    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO request) {
        EmpresaLogistica empresa = empresaLogisticaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la empresa logistica con ID " + request.empresaId()));

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(request.placa());
        vehiculo.setCapacidadKg(request.capacidadKg());
        vehiculo.setEstado(request.estado());
        vehiculo.setEmpresa(empresa);

        return toResponseDTO(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoResponseDTO actualizar(Integer id, VehiculoRequestDTO request) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el vehiculo con ID " + id));

        EmpresaLogistica empresa = empresaLogisticaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la empresa logistica con ID " + request.empresaId()));

        vehiculo.setPlaca(request.placa());
        vehiculo.setCapacidadKg(request.capacidadKg());
        vehiculo.setEstado(request.estado());
        vehiculo.setEmpresa(empresa);

        return toResponseDTO(vehiculo);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe el vehiculo con ID " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    private VehiculoResponseDTO toResponseDTO(Vehiculo vehiculo) {
        Integer empresaId = vehiculo.getEmpresa() != null ? vehiculo.getEmpresa().getId() : null;
        String nombreEmpresa = vehiculo.getEmpresa() != null ? vehiculo.getEmpresa().getNombre() : null;

        return new VehiculoResponseDTO(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getCapacidadKg(),
                vehiculo.getEstado(),
                empresaId,
                nombreEmpresa);
    }
}