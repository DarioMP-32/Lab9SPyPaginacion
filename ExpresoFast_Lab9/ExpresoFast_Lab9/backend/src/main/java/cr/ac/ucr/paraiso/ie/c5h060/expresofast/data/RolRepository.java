package cr.ac.ucr.paraiso.ie.c5h060.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5h060.expresofast.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombreRol(String nombreRol);
}