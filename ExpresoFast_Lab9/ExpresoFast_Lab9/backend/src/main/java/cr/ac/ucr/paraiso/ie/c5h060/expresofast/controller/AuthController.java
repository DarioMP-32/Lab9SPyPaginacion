package cr.ac.ucr.paraiso.ie.c5h060.expresofast.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.business.AuthService;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.AuthResponseDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO respuesta = authService.login(request);
        return ResponseEntity.ok(respuesta);
    }
}