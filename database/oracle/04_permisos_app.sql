-- =====================================================================
--  OBJETOS PERDIDOS - UPeU Tarapoto
--  Script 04: permisos del usuario de la aplicacion (APP_UPEU)
--
--  Ejecutar conectado como: UPEU_DATOS
--  Requisito previo:        01, 02 y 03
--
--  APP_UPEU recibe SOLO lo que la aplicacion necesita en cada tabla.
--  Ejemplos de minimo privilegio:
--    - Nunca puede borrar reportes, entregas ni custodias (sin DELETE):
--      la trazabilidad queda intacta.
--    - Solo los catalogos admiten DELETE (el administrador los mantiene).
--    - PERSONA, ESTUDIANTE y PERSONAL son de solo lectura para la app.
-- =====================================================================


-- ---------------------------------------------------------------------
-- Catalogos: el administrador los mantiene desde la app
-- ---------------------------------------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE ON AREA      TO APP_UPEU;
GRANT SELECT, INSERT, UPDATE, DELETE ON CARRERA   TO APP_UPEU;
GRANT SELECT, INSERT, UPDATE, DELETE ON CATEGORIA TO APP_UPEU;
GRANT SELECT, INSERT, UPDATE, DELETE ON UBICACION TO APP_UPEU;

-- ---------------------------------------------------------------------
-- Personas: solo lectura (se registran por la universidad)
-- ---------------------------------------------------------------------
GRANT SELECT ON PERSONA            TO APP_UPEU;
GRANT SELECT ON ESTUDIANTE         TO APP_UPEU;
GRANT SELECT ON PERSONAL           TO APP_UPEU;
GRANT SELECT ON PERSONAL_UBICACION TO APP_UPEU;

-- Cuentas: el admin habilita claves y cada usuario cambia la suya
GRANT SELECT, INSERT, UPDATE ON USUARIO TO APP_UPEU;

-- ---------------------------------------------------------------------
-- Flujo del objeto: se registra, nunca se borra
-- ---------------------------------------------------------------------
GRANT SELECT, INSERT         ON OBJETO              TO APP_UPEU;
GRANT SELECT, INSERT         ON REPORTE_PERDIDA     TO APP_UPEU;
GRANT SELECT, INSERT, UPDATE ON REPORTE_HALLAZGOS   TO APP_UPEU;
GRANT SELECT, INSERT         ON OBJETOS_CUSTODIADOS TO APP_UPEU;
GRANT SELECT, INSERT         ON COINCIDENCIA        TO APP_UPEU;
GRANT SELECT, INSERT         ON ENTREGA             TO APP_UPEU;
GRANT SELECT, INSERT, UPDATE ON RECLAMOS            TO APP_UPEU;
GRANT SELECT, INSERT, UPDATE ON NOTIFICACIONES      TO APP_UPEU;

-- ---------------------------------------------------------------------
-- Secuencias (para generar los ID)
-- ---------------------------------------------------------------------
GRANT SELECT ON SEQ_AREA                TO APP_UPEU;
GRANT SELECT ON SEQ_CARRERA             TO APP_UPEU;
GRANT SELECT ON SEQ_CATEGORIA           TO APP_UPEU;
GRANT SELECT ON SEQ_UBICACION           TO APP_UPEU;
GRANT SELECT ON SEQ_PERSONA             TO APP_UPEU;
GRANT SELECT ON SEQ_PERSONAL_UBICACION  TO APP_UPEU;
GRANT SELECT ON SEQ_OBJETO              TO APP_UPEU;
GRANT SELECT ON SEQ_REPORTE_PERDIDA     TO APP_UPEU;
GRANT SELECT ON SEQ_REPORTE_HALLAZGOS   TO APP_UPEU;
GRANT SELECT ON SEQ_OBJETOS_CUSTODIADOS TO APP_UPEU;
GRANT SELECT ON SEQ_COINCIDENCIA        TO APP_UPEU;
GRANT SELECT ON SEQ_ENTREGA             TO APP_UPEU;
GRANT SELECT ON SEQ_RECLAMOS            TO APP_UPEU;
GRANT SELECT ON SEQ_NOTIFICACIONES      TO APP_UPEU;

-- ---------------------------------------------------------------------
-- Reportes: la vista y el procedimiento
-- ---------------------------------------------------------------------
GRANT SELECT  ON VW_SEGUIMIENTO_RECLAMOS TO APP_UPEU;
GRANT EXECUTE ON SP_RESUMEN_RECLAMOS     TO APP_UPEU;


-- Fin del script 04
