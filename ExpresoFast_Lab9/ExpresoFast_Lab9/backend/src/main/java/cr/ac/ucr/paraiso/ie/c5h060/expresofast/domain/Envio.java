package cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedStoredProcedureQuery(
        name = "Envio.obtenerPorEstadoSP",
        procedureName = "SP_OBTENER_ENVIOS_POR_ESTADO",
        resultClasses = Envio.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "pEstado", type = String.class)
        })
@Entity
@Table(name = "Envio")
public class Envio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "envio_id")
    private Integer id;

    @Column(name = "codigo_rastreo", nullable = false, unique = true, length = 30)
    private String codigoRastreo;

    @Column(name = "destinatario", length = 100)
    private String destinatario;

    @Column(name = "direccion_destino", nullable = false, length = 200)
    private String direccionDestino;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "estado_envio", nullable = false, length = 20)
    private String estadoEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;

}
