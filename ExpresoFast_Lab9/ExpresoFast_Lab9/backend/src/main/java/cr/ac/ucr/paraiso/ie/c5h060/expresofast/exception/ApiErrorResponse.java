package cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
 
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<Map<String, String>> errores) {
 
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }
 
    public ApiErrorResponse(int status, String error, String message, String path,
            List<Map<String, String>> errores) {
        this(LocalDateTime.now(), status, error, message, path, errores);
    }
}