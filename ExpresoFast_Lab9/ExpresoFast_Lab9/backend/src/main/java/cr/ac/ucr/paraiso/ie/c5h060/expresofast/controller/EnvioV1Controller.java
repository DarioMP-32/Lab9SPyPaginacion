package cr.ac.ucr.paraiso.ie.c5h060.expresofast.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.PaginaResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.ResumenMetricasDTO;

@RestController
@RequestMapping("/api/v1/envios")
@CrossOrigin(origins = "*")
public class EnvioV1Controller {

    private final EnvioService envioService;

    public EnvioV1Controller(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public ResponseEntity<PaginaResponseDTO<EnvioDTO>> listarPaginado(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortBy", defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(name = "busqueda", required = false) String busqueda,
            @RequestParam(name = "estado", required = false) String estado) {

        return ResponseEntity.ok(PaginaResponseDTO.from(
                envioService.listarPaginado(page, size, sortBy, direction, busqueda, estado)));
    }

    @GetMapping("/procedimiento/{estado}")
    public ResponseEntity<List<EnvioDTO>> listarViaProcedimiento(@PathVariable("estado") String estado) {
        return ResponseEntity.ok(envioService.listarViaStoredProcedure(estado));
    }

    @GetMapping("/metricas")
    public ResponseEntity<List<ResumenMetricasDTO>> obtenerMetricas() {
        return ResponseEntity.ok(envioService.obtenerResumenMetricas());
    }
}
