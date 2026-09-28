package cr.ac.ucr.paraiso.ie.c5h060.expresofast.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioMapper;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioResponseDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> obtenerEnviosOptimizados() {
        List<Envio> envios = envioService.obtenerEnviosOptimizados();
        return ResponseEntity.ok(EnvioMapper.toResponseDTOList(envios));
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> registrarEnvio(@Valid @RequestBody EnvioRequestDTO dto) {
        Envio envioCreado = envioService.registrarEnvio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(EnvioMapper.toResponseDTO(envioCreado));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstado(@PathVariable("id") Integer id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        Envio envioActualizado = envioService.actualizarEstado(id, dto.nuevoEstado(), dto.observaciones());
        return ResponseEntity.ok(EnvioMapper.toResponseDTO(envioActualizado));
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable("id") Integer id) {
        List<BitacoraResponseDTO> bitacora =
                EnvioMapper.toBitacoraResponseDTOList(envioService.obtenerBitacora(id));
        return ResponseEntity.ok(bitacora);
    }
}
