package cr.ac.ucr.paraiso.ie.c5h060.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Envio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    @Query("SELECT e FROM Envio e " +
            "JOIN FETCH e.vehiculo v " +
            "JOIN FETCH v.empresa " +
            "JOIN FETCH e.conductor " +
            "ORDER BY e.id DESC")
    List<Envio> findAllOptimizado();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :estado WHERE e.vehiculo.id = :vehiculoId")
    int actualizarEstadoPorVehiculo(@Param("vehiculoId") Integer vehiculoId,
            @Param("estado") String estado);

    @Procedure(name = "Envio.obtenerPorEstadoSP")
    List<Envio> obtenerPorEstadoSP(@Param("pEstado") String pEstado);

    Page<Envio> findByEstadoEnvio(String estado, Pageable pageable);

    @Query("""
            SELECT e FROM Envio e
            WHERE (:estado = '' OR e.estadoEnvio = :estado)
              AND (:busqueda = ''
                   OR LOWER(e.codigoRastreo)    LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(e.destinatario)     LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(e.direccionDestino) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<Envio> buscar(@Param("estado") String estado,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
