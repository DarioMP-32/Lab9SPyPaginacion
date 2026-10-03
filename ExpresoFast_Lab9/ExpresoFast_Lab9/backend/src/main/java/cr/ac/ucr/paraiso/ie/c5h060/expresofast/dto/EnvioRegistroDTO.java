package cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EnvioRegistroDTO(
      
        @NotBlank(message = "El número de rastreo es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9-]{5,30}$",
                 message = "El rastreo debe tener de 5 a 30 caracteres (letras, números y guiones)")
        String numeroTracking,

        @NotBlank(message = "El destinatario es obligatorio")
        @Size(max = 100, message = "El destinatario no puede superar 100 caracteres")
        String destinatario,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccionDestino,

        @NotNull(message = "El costo es obligatorio")
        @Positive(message = "El costo debe ser mayor a cero")
        BigDecimal costo,

        @NotNull(message = "Debe indicar el ID del vehículo")
        Integer vehiculoId,

        @NotNull(message = "Debe indicar el ID del conductor")
        Integer conductorId,

        @NotNull(message = "La fecha de despacho es obligatoria")
        LocalDate fechaDespacho,

        @NotNull(message = "La fecha de entrega estimada es obligatoria")
        LocalDate fechaEntregaEstimada,

        @NotEmpty(message = "Debe incluir al menos un paquete")
        List<@Valid PaqueteDTO> paquetes) {
}