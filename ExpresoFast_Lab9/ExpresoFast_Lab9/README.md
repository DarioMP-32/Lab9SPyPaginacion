# ExpresoFast — Laboratorio 9: Procedimientos Almacenados y Paginación Relacional

**Curso:** IF0009 - Desarrollo de Software IV
**Ciclo:** II-2026
**Profesor:** Mag. Jonathan Granados C.
**Estudiante:** Josué Méndez Sanabria — Carné c5h060

Consola web de operación logística que consume la API REST de Spring Boot construida
en el Laboratorio 6 (autenticación JWT, RBAC, envíos, vehículos, bitácora de auditoría).

## Estructura del repositorio

```
/backend     Proyecto Spring Boot (API REST, sin cambios funcionales respecto al Lab 6)
/frontend    Cliente web: index.html (login), dashboard.html (consola), styles.css, app.js, auth.js
/database    Scripts SQL (esquema + seeds). Ejecutar 04_lab9_stored_procedures.sql antes de iniciar el backend
```

## Requisitos

- Java 21 + Maven
- Microsoft SQL Server (o acceso a la instancia ya configurada en `application.properties`)
- Extensión **Live Server** de VS Code (o cualquier servidor estático) para el frontend

## Cómo ejecutar

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`. El CORS ya está habilitado de forma
global en `SecurityConfig` (`corsConfigurationSource`) con `allowedOriginPatterns("*")` y
`allowCredentials(true)`, por lo que acepta peticiones desde cualquier origen local
(por ejemplo `http://127.0.0.1:5500`, el puerto por defecto de Live Server).

### 2. Frontend

Abra la carpeta `frontend/` con Live Server (clic derecho sobre `index.html` →
"Open with Live Server") o cualquier servidor estático de desarrollo. **No** abra los
archivos con doble clic (`file://`), ya que algunas peticiones `fetch` lo bloquean.

Si su backend corre en otra URL, ajuste la constante `API_BASE_URL` en `frontend/auth.js`.

## Usuarios de prueba

| Usuario     | Contraseña     | Rol            |
|-------------|----------------|----------------|
| admin       | Password123!   | ROLE_ADMIN     |
| operador1   | Password123!   | ROLE_OPERADOR  |
| conductor1  | Password123!   | ROLE_CONDUCTOR |

## Notas sobre el alcance

Este laboratorio se entrega **sin haber completado el Laboratorio 7** (suite de pruebas
unitarias con JUnit 5 y Mockito). Esto no afecta al Laboratorio 8: el Lab 8 consume la
API REST tal como quedó construida y asegurada en el Laboratorio 6 (persistencia, JWT,
RBAC y manejo de errores RFC 7807), que es todo lo que el cliente necesita para funcionar.
La suite de pruebas del backend queda pendiente como trabajo del Lab 7.


## Laboratorio 9

- Ejecute `database/04_lab9_stored_procedures.sql` en SQL Server (agrega `destinatario`, los SP y 18 envíos semilla).
- Endpoints nuevos (requieren JWT):
  - `GET /api/v1/envios?page=0&size=5&sortBy=fechaCreacion&direction=desc&busqueda=&estado=`
  - `GET /api/v1/envios/procedimiento/{estado}`
  - `GET /api/v1/envios/metricas`
- Vista cliente: `frontend/dashboard_paginado.html` (Live Server).
