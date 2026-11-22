-- =====================================================================
--  OBJETOS PERDIDOS - UPeU Tarapoto
--  Script 00: usuarios de base de datos
--
--  Ejecutar conectado como: SYSTEM, dentro de la PDB XEPDB1
--    (conexion con "Nombre del servicio" = XEPDB1, o primero:
--     ALTER SESSION SET CONTAINER = XEPDB1;)
--
--  Se crean DOS usuarios (principio de minimo privilegio):
--    UPEU_DATOS -> DUENO de las tablas. Nadie se conecta con el desde la app.
--    APP_UPEU   -> el que usa Spring Boot. Solo puede conectarse; los
--                  permisos sobre cada tabla se dan en 04_permisos_app.sql.
--
--  IMPORTANTE: cambia las contrasenas de ejemplo antes de ejecutar.
--  La de APP_UPEU es la que va en la variable de entorno DB_PASSWORD.
-- =====================================================================


-- ---------------------------------------------------------------------
-- Dueno de los datos
-- ---------------------------------------------------------------------
CREATE USER UPEU_DATOS IDENTIFIED BY "Cambiar_Datos_2026"
    DEFAULT TABLESPACE USERS
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION   TO UPEU_DATOS;
GRANT CREATE TABLE     TO UPEU_DATOS;
GRANT CREATE SEQUENCE  TO UPEU_DATOS;
GRANT CREATE VIEW      TO UPEU_DATOS;
GRANT CREATE PROCEDURE TO UPEU_DATOS;


-- ---------------------------------------------------------------------
-- Usuario de la aplicacion (Spring Boot)
-- ---------------------------------------------------------------------
CREATE USER APP_UPEU IDENTIFIED BY "Cambiar_App_2026"
    DEFAULT TABLESPACE USERS;

GRANT CREATE SESSION TO APP_UPEU;


-- Fin del script 00
