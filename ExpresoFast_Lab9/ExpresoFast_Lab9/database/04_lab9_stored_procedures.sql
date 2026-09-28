IF COL_LENGTH('Envio', 'destinatario') IS NULL
    ALTER TABLE Envio ADD destinatario VARCHAR(100) NULL;
GO

CREATE OR ALTER PROCEDURE SP_OBTENER_ENVIOS_POR_ESTADO
    @pEstado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT envio_id, codigo_rastreo, destinatario, direccion_destino,
           peso_kg, costo, estado_envio, vehiculo_id, conductor_id,
           fecha_creacion, fecha_modificacion
    FROM Envio
    WHERE estado_envio = @pEstado
    ORDER BY fecha_creacion DESC;
END
GO

CREATE OR ALTER PROCEDURE SP_RESUMEN_METRICAS_ENVIOS
AS
BEGIN
    SET NOCOUNT ON;

    SELECT estado_envio,
           COUNT(*)   AS total_envios,
           SUM(costo) AS total_flete
    FROM Envio
    GROUP BY estado_envio
    ORDER BY estado_envio;
END
GO

IF NOT EXISTS (SELECT 1 FROM EmpresaLogistica WHERE cedula_juridica = '3-101-900001')
    INSERT INTO EmpresaLogistica (nombre, cedula_juridica, telefono, fecha_registro)
    VALUES ('ExpresoFast Lab9', '3-101-900001', '2222-0009', GETDATE());

IF NOT EXISTS (SELECT 1 FROM Vehiculo WHERE placa = 'LAB9-001')
    INSERT INTO Vehiculo (placa, capacidad_kg, estado, empresa_id)
    VALUES ('LAB9-001', 1500.00, 'DISPONIBLE',
            (SELECT TOP 1 empresa_id FROM EmpresaLogistica WHERE cedula_juridica = '3-101-900001'));

IF NOT EXISTS (SELECT 1 FROM Conductor WHERE licencia = 'LAB9-LIC-01')
    INSERT INTO Conductor (nombre, apellidos, licencia, telefono)
    VALUES ('Luis', 'Mora Vargas', 'LAB9-LIC-01', '8888-0009');

DECLARE @veh INT = (SELECT vehiculo_id  FROM Vehiculo  WHERE placa    = 'LAB9-001');
DECLARE @con INT = (SELECT conductor_id FROM Conductor WHERE licencia = 'LAB9-LIC-01');

INSERT INTO Envio (codigo_rastreo, destinatario, direccion_destino, peso_kg, costo,
                   estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
SELECT v.codigo, v.dest, v.dir, v.peso, v.costo, v.estado, @veh, @con, v.fecha, v.fecha
FROM (VALUES
  ('EXP-9001', 'Ana Solano',       'Cartago centro',        12.50,  4500.00, 'PENDIENTE',   '20260901 08:15:00'),
  ('EXP-9002', 'Bruno Jiménez',    'Paraíso, Cartago',       3.20,  2800.00, 'PENDIENTE',   '20260902 09:30:00'),
  ('EXP-9003', 'Carla Vega',       'Turrialba',             45.00, 12500.00, 'PENDIENTE',   '20260903 10:00:00'),
  ('EXP-9004', 'Diego Araya',      'Orosi',                  8.75,  3900.00, 'PENDIENTE',   '20260904 11:45:00'),
  ('EXP-9005', 'Elena Chacón',     'Tres Ríos',             20.00,  6200.00, 'PENDIENTE',   '20260905 13:20:00'),
  ('EXP-9006', 'Fabián Rojas',     'San José centro',       15.30,  5100.00, 'EN_TRANSITO', '20260906 07:50:00'),
  ('EXP-9007', 'Gabriela Mena',    'Heredia',               60.00, 15800.00, 'EN_TRANSITO', '20260907 08:40:00'),
  ('EXP-9008', 'Héctor Salas',     'Alajuela',               5.40,  3300.00, 'EN_TRANSITO', '20260908 09:10:00'),
  ('EXP-9009', 'Irene Campos',     'Limón centro',          33.10,  9800.00, 'EN_TRANSITO', '20260909 14:00:00'),
  ('EXP-9010', 'Jorge Quesada',    'Puntarenas',            27.80,  8700.00, 'EN_TRANSITO', '20260910 15:25:00'),
  ('EXP-9011', 'Karla Umaña',      'Cartago, Guadalupe',     2.10,  2500.00, 'ENTREGADO',   '20260911 08:05:00'),
  ('EXP-9012', 'Luis Barrantes',   'Paraíso centro',        18.60,  5600.00, 'ENTREGADO',   '20260912 10:35:00'),
  ('EXP-9013', 'Marta Pineda',     'Cot, Oreamuno',          9.90,  4100.00, 'ENTREGADO',   '20260913 11:10:00'),
  ('EXP-9014', 'Nelson Brenes',    'Juan Viñas',            41.00, 11900.00, 'ENTREGADO',   '20260914 12:55:00'),
  ('EXP-9015', 'Olga Sandoval',    'Cervantes',              6.30,  3000.00, 'ENTREGADO',   '20260915 16:30:00'),
  ('EXP-9016', 'Pablo Zamora',     'Liberia',               52.00, 14300.00, 'CANCELADO',   '20260916 09:00:00'),
  ('EXP-9017', 'Quetzal Ruiz',     'San Carlos',            11.40,  4700.00, 'CANCELADO',   '20260917 10:20:00'),
  ('EXP-9018', 'Rosa Trejos',      'Sarapiquí',              7.80,  3600.00, 'CANCELADO',   '20260918 13:45:00')
) AS v(codigo, dest, dir, peso, costo, estado, fecha)
WHERE NOT EXISTS (SELECT 1 FROM Envio e WHERE e.codigo_rastreo = v.codigo);
GO
