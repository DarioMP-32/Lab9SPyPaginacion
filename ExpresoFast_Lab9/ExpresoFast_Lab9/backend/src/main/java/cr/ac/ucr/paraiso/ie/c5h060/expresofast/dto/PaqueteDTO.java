package cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaqueteDTO(
        Integer id,

        @NotBlank(message = "La descripción del paquete es obligatoria")
        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion,

        @NotNull(message = "El peso del paquete es obligatorio")
        @DecimalMin(value = "0.01", message = "El peso mínimo por paquete es 0.01 kg")
        @DecimalMax(value = "999.99", message = "El peso máximo por paquete es 999.99 kg")
        BigDecimal pesoKg) {
}
