-- =====================================================================
--  OBJETOS PERDIDOS - UPeU Tarapoto
--  Script 02: vista y procedimiento almacenado para reportes
--
--  Ejecutar conectado como: UPEU_DATOS
--  Requisito previo:        01_tablas.sql
--  Ejecutar como SCRIPT (F5): el procedimiento termina en "/".
-- =====================================================================


-- =====================================================================
-- VISTA: VW_SEGUIMIENTO_RECLAMOS
--   Seguimiento de los reclamos de estudiantes (sin incidencias).
--   Un reclamo puede venir de una custodia, de una entrega o de un
--   hallazgo; la vista busca el objeto en cualquiera de los tres
--   caminos (COALESCE) y muestra su categoria.
--   La usa: GET /api/v1/reportes/reclamos/seguimiento
-- =====================================================================
CREATE OR REPLACE VIEW VW_SEGUIMIENTO_RECLAMOS (
    ID_RECLAMACION,
    ID_OBJETO,
    CATEGORIA,
    DESCRIPCION,
    ESTADO_RECLAMACION,
    FECHA_SOLICITUD,
    FECHA_RESOLUCION,
    SITUACION_REVISION
) AS
SELECT
    R.ID_RECLAMO          AS ID_RECLAMACION,
    O.ID_OBJETO,
    C.NOMBRE              AS CATEGORIA,
    O.DESCRIPCION,
    R.ESTADO              AS ESTADO_RECLAMACION,
    R.FECHA_REGISTRO      AS FECHA_SOLICITUD,
    R.FECHA_RESOLUCION,
    CASE
        WHEN R.ESTADO = 'PENDIENTE' THEN 'SIN_REVISAR'
        ELSE 'REVISADA'
    END                   AS SITUACION_REVISION
FROM RECLAMOS R
    -- Camino 1: reclamo sobre una custodia
    LEFT JOIN OBJETOS_CUSTODIADOS OC ON R.ID_CUSTODIA = OC.ID_CUSTODIA
    -- Camino 2: reclamo sobre un hallazgo
    LEFT JOIN REPORTE_HALLAZGOS H    ON R.ID_HALLAZGO = H.ID_HALLAZGO
    -- Camino 3: reclamo sobre una entrega (entrega -> custodia -> objeto)
    LEFT JOIN ENTREGA E              ON R.ID_ENTREGA  = E.ID_ENTREGA
    LEFT JOIN OBJETOS_CUSTODIADOS OE ON E.ID_CUSTODIA = OE.ID_CUSTODIA
    -- El objeto sale del primer camino que tenga dato
    LEFT JOIN OBJETO O               ON O.ID_OBJETO = COALESCE(OC.ID_OBJETO, OE.ID_OBJETO, H.ID_OBJETO)
    LEFT JOIN CATEGORIA C            ON O.ID_CATEGORIA = C.ID_CATEGORIA
WHERE R.TIPO_RECLAMO = 'RECLAMO';


-- =====================================================================
-- PROCEDIMIENTO: SP_RESUMEN_RECLAMOS
--   Devuelve, en parametros de salida, cuantos reclamos hay en total,
--   cuantos siguen sin revisar (PENDIENTE) y cuantos ya se revisaron.
--   La usa: GET /api/v1/reportes/reclamos/resumen
-- =====================================================================
CREATE OR REPLACE PROCEDURE SP_RESUMEN_RECLAMOS (
    P_TOTAL        OUT NUMBER,
    P_SIN_REVISAR  OUT NUMBER,
    P_REVISADOS    OUT NUMBER
)
AUTHID DEFINER
AS
BEGIN
    SELECT
        COUNT(*),
        COUNT(CASE WHEN R.ESTADO =  'PENDIENTE' THEN 1 END),
        COUNT(CASE WHEN R.ESTADO <> 'PENDIENTE' THEN 1 END)
    INTO
        P_TOTAL,
        P_SIN_REVISAR,
        P_REVISADOS
    FROM RECLAMOS R
    WHERE R.TIPO_RECLAMO = 'RECLAMO';
END SP_RESUMEN_RECLAMOS;
/


-- =====================================================================
-- PRUEBAS (opcional): quitar los "--" para ejecutarlas
-- =====================================================================
-- SELECT * FROM VW_SEGUIMIENTO_RECLAMOS ORDER BY FECHA_SOLICITUD DESC;
--
-- SET SERVEROUTPUT ON
-- DECLARE
--     v_total NUMBER; v_sin NUMBER; v_rev NUMBER;
-- BEGIN
--     SP_RESUMEN_RECLAMOS(v_total, v_sin, v_rev);
--     DBMS_OUTPUT.PUT_LINE('Total: ' || v_total || ' | Sin revisar: ' || v_sin || ' | Revisados: ' || v_rev);
-- END;
-- /


-- Fin del script 02
