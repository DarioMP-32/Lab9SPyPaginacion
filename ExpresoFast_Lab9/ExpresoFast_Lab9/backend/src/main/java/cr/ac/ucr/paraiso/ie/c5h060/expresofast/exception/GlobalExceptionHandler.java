package cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
 

@RestControllerAdvice
public class GlobalExceptionHandler {
 
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
 
  
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
 
        List<Map<String, String>> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();
 
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Uno o mas campos no cumplen las reglas de validacion.",
                request.getRequestURI(),
                errores);
 
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
 
    private Map<String, String> mapFieldError(FieldError fieldError) {
        Map<String, String> detalle = new HashMap<>();
        detalle.put("campo", fieldError.getField());
        detalle.put("mensaje", fieldError.getDefaultMessage());
        return detalle;
    }
 

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
 
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
 
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStateTransition(
            InvalidStateTransitionException ex, HttpServletRequest request) {
 
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
 
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
 
 
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
 
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Usuario o contrasena incorrectos.",
                request.getRequestURI());
 
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
 
  
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
 
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "No cuenta con los permisos necesarios para realizar esta accion.",
                request.getRequestURI());
 
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
 
   
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
 
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getReason(),
                request.getRequestURI());
 
        return ResponseEntity.status(status).body(body);
    }
 
   
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
 
        log.error("Error inesperado procesando {} {}", request.getMethod(), request.getRequestURI(), ex);
 
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrio un error inesperado en el servidor. Contacte al administrador.",
                request.getRequestURI());
 
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}