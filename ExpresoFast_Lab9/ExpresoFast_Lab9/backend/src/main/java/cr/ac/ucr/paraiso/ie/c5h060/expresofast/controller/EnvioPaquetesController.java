package cr.ac.ucr.paraiso.ie.c5h060.expresofast.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.business.EnvioPaquetesService;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioConPaquetesResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.EnvioRegistroDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.TrackingCheckDTO;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "http://localhost:4200")
public class EnvioPaquetesController {

    private final EnvioPaquetesService envioPaquetesService;

    public EnvioPaquetesController(EnvioPaquetesService envioPaquetesService) {
        this.envioPaquetesService = envioPaquetesService;
    }

  
    @PostMapping("/con-paquetes")
    public ResponseEntity<EnvioConPaquetesResponseDTO> registrarConPaquetes(
            @Valid @RequestBody EnvioRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(envioPaquetesService.registrarConPaquetes(dto));
    }

    
    @GetMapping("/check-tracking/{trackingNumber}")
    public ResponseEntity<TrackingCheckDTO> checkTracking(
            @PathVariable("trackingNumber") String trackingNumber) {
        return ResponseEntity.ok(new TrackingCheckDTO(
                envioPaquetesService.existeCodigoRastreo(trackingNumber)));
    }
}