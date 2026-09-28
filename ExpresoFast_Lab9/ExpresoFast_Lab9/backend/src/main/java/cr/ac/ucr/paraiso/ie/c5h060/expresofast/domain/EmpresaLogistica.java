package cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EmpresaLogistica")
public class EmpresaLogistica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empresa_id")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "cedula_juridica", nullable = false, unique = true, length = 20)
    private String cedulaJuridica;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Vehiculo> vehiculos = new ArrayList<>();
}