ExpresoFast Laboratorio 11
Desarrollo de Software 4.

En este laboratorio se mejoró ExpresoFast para que un mismo envío pueda tener varios paquetes. El formulario permite registrar el envío completo con todos sus paquetes de una sola vez.

## Qué hay en el repositorio

- `expresofast-backend/`: el servidor hecho con Spring Boot y SQL Server.
- `expresofast-frontend/`: la página hecha con Angular 19.
- `database/`: los scripts de la base de datos.

## Qué se hizo

**Backend**
- Se creó la tabla `Paquete`, que se relaciona con `Envio` (un envío tiene muchos paquetes).
- Se creó la entidad `Paquete` y se relacionó con `Envio`.
- El envío y sus paquetes se guardan juntos con `@Transactional`. Si algo falla, no se guarda nada.
- Se agregó el endpoint `GET /api/envios/check-tracking/{trackingNumber}`, que dice si un número de rastreo ya existe.

**Frontend**
- Formulario reactivo con tipos (Typed Forms), sin usar `ngModel`.
- Botón "+ Añadir Paquete" y botón "X" para quitar paquetes (siempre queda al menos uno).
- Validación de fechas: la entrega debe ser después del despacho.
- Validación del número de rastreo contra el servidor para que no se repita.

## Cómo ejecutarlo

1. Ejecutar el script de la carpeta `database/` en SQL Server.
2. Backend:
   ```
   cd expresofast-backend
   ./mvnw spring-boot:run
   ```
   Queda en `http://localhost:8080`.
3. Frontend:
   ```
   cd expresofast-frontend
   npm install
   ng serve
   ```
   Se abre en `http://localhost:4200`.

---

## Preguntas teóricas

### 1. ¿Por qué FormArray y los formularios reactivos son mejores que 10 campos fijos y ocultos en HTML?

**Para el usuario (UX):**
- Con los campos ocultos, el formulario siempre tiene un límite. Si el operador necesita 11 paquetes, no puede.
- Con `FormArray` solo se muestran los paquetes que se necesitan. Se agregan con un botón y se quitan con otro, así que el formulario se ve más limpio.
- Las validaciones se ven al momento, en el paquete que tiene el error.

**Para el código (mantenimiento):**
- Con 10 campos fijos hay que repetir el mismo HTML 10 veces. Si hay que cambiar algo, se cambia en 10 lugares y es fácil equivocarse.
- Con `FormArray` el bloque se escribe una sola vez dentro de un `@for`, y Angular lo repite por cada paquete.
- El formulario reactivo tiene tipos. Si se intenta poner un texto donde va el peso (que es número), TypeScript da error antes de ejecutar el programa.
- El valor del formulario ya viene como una lista de paquetes, lista para enviar al backend. No hay que recorrer 10 campos para armarla ni descartar los vacíos.
- Las reglas de validación están en el código TypeScript y no mezcladas con el HTML, por lo que son más fáciles de probar y de reutilizar.

### 2. Diferencia entre el validador de fechas (síncrono) y el de tracking (asíncrono)

JavaScript trabaja con un solo hilo. Ejecuta una cosa a la vez y usa el **Event Loop** para manejar las tareas que tardan, para que la página no se congele.

**Validador de fechas (síncrono):**
- Solo compara dos fechas que ya están en memoria, así que es instantáneo.
- Se ejecuta completo, de principio a fin, en el mismo momento y devuelve el resultado directamente (el error o `null`).
- No tiene que esperar nada, por eso no usa el Event Loop.

**Validador de tracking (asíncrono):**
- Tiene que preguntarle al servidor si el número ya existe, y eso tarda un tiempo que no se sabe.
- Si JavaScript se quedara esperando la respuesta, toda la página se quedaría congelada.
- Por eso la petición HTTP se envía y el programa sigue funcionando. Cuando llega la respuesta, el Event Loop ejecuta el código que estaba pendiente.
- Mientras espera, el campo queda en estado `PENDING` (pendiente).

**¿Por qué Angular pide devolver un Observable o una Promise?**
- Porque el validador no puede devolver el resultado de inmediato, ya que todavía no lo tiene.
- Un Observable o una Promise es una forma de decir "el resultado va a llegar después". Angular se queda esperando y, cuando llega, actualiza si el campo es válido o no.
- Si el validador devolviera un valor normal, Angular lo tomaría como respuesta inmediata y no podría esperar al servidor.
- En este proyecto se usa un Observable con `timer` y `switchMap`: se espera un momento después de que el usuario deja de escribir, y se cancela la consulta anterior si el usuario sigue escribiendo. Así no se hacen consultas de más al servidor.
