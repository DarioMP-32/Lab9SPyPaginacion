package cr.ac.ucr.paraiso.ie.c5h060.expresofast.data;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.dto.ResumenMetricasDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

@Repository
public class EnvioMetricasRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<ResumenMetricasDTO> obtenerResumen() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SP_RESUMEN_METRICAS_ENVIOS");
        List<Object[]> filas = query.getResultList();

        return filas.stream()
                .map(f -> new ResumenMetricasDTO(
                        (String) f[0],
                        ((Number) f[1]).longValue(),
                        f[2] instanceof BigDecimal b ? b : BigDecimal.ZERO))
                .toList();
    }
}
