package cr.ac.ucr.paraiso.ie.c5h060.expresofast.security;
 
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
 
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
 
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
 
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "No cuenta con los permisos necesarios (rol) para realizar esta accion.",
                request.getRequestURI());
 
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}