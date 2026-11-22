-- =====================================================================
--  OBJETOS PERDIDOS - UPeU Tarapoto
--  Script 05: cuentas de prueba (OPCIONAL, solo para desarrollo)
--
--  Ejecutar conectado como: UPEU_DATOS
--  Requisito previo:        01, 02 y 03
--
--  Crea 1 administrador, 1 personal y 2 estudiantes para poder
--  ingresar a la aplicacion recien instalada.
--
--  | Rol        | Ingreso    | Contrasena          |
--  |------------|------------|---------------------|
--  | ADMIN      | 70000001   | UpeuAdmin2026!      |
--  | PERSONAL   | 70000002   | UpeuPersonal2026!   |
--  | ESTUDIANTE | 209900001  | (sin contrasena)    |
--  | ESTUDIANTE | 209900002  | (sin contrasena)    |
--
--  Las contrasenas se guardan como hash BCrypt (no en texto plano).
--  Son cuentas de PRUEBA: cambialas desde "Mi cuenta" al ingresar.
--  NO usar estos datos en un servidor real.
-- =====================================================================


-- ---------------------------------------------------------------------
-- ADMINISTRADOR (tambien es personal: revisa, recibe y entrega)
-- ---------------------------------------------------------------------
INSERT INTO PERSONA (ID_PERSONA, NOMBRES, APELLIDOS, TIPO_DOCUMENTO, DOCUMENTO_IDENTIDAD, TELEFONO)
VALUES (SEQ_PERSONA.NEXTVAL, 'Admin', 'Sistema UPeU', 'DNI', '70000001', NULL);

INSERT INTO PERSONAL (ID_PERSONA, CARGO, ESTADO, ID_AREA)
VALUES (SEQ_PERSONA.CURRVAL, 'Administrador del sistema', 'ACTIVO',
        (SELECT ID_AREA FROM AREA WHERE NOMBRE = 'Seguridad'));

INSERT INTO USUARIO (ID_PERSONA, ROL, ESTADO, PASSWORD)
VALUES (SEQ_PERSONA.CURRVAL, 'ADMIN', 'ACTIVO',
        '$2a$10$enfQlm.XnQV9WLfua0DwquYpI1UjTWOPSNkeNqP0oo7hFGIUqlOJW');

INSERT INTO PERSONAL_UBICACION (ID_ASIGNACION, ID_PERSONA, ID_UBICACION, FECHA_INICIO, FECHA_FIN)
VALUES (SEQ_PERSONAL_UBICACION.NEXTVAL, SEQ_PERSONA.CURRVAL,
        (SELECT ID_UBICACION FROM UBICACION WHERE NOMBRE = 'Garita'), TRUNC(SYSDATE), NULL);


-- ---------------------------------------------------------------------
-- PERSONAL (trabaja en Garita)
-- ---------------------------------------------------------------------
INSERT INTO PERSONA (ID_PERSONA, NOMBRES, APELLIDOS, TIPO_DOCUMENTO, DOCUMENTO_IDENTIDAD, TELEFONO)
VALUES (SEQ_PERSONA.NEXTVAL, 'Personal', 'Garita Prueba', 'DNI', '70000002', NULL);

INSERT INTO PERSONAL (ID_PERSONA, CARGO, ESTADO, ID_AREA)
VALUES (SEQ_PERSONA.CURRVAL, 'Agente de seguridad', 'ACTIVO',
        (SELECT ID_AREA FROM AREA WHERE NOMBRE = 'Seguridad'));

INSERT INTO USUARIO (ID_PERSONA, ROL, ESTADO, PASSWORD)
VALUES (SEQ_PERSONA.CURRVAL, 'PERSONAL', 'ACTIVO',
        '$2a$10$lOY711t48DSeOnBVtGkneOtSi7isA7C9aeJbXPbOhtmUk9MNFTqHO');

INSERT INTO PERSONAL_UBICACION (ID_ASIGNACION, ID_PERSONA, ID_UBICACION, FECHA_INICIO, FECHA_FIN)
VALUES (SEQ_PERSONAL_UBICACION.NEXTVAL, SEQ_PERSONA.CURRVAL,
        (SELECT ID_UBICACION FROM UBICACION WHERE NOMBRE = 'Garita'), TRUNC(SYSDATE), NULL);


-- ---------------------------------------------------------------------
-- ESTUDIANTES (ingresan solo con su codigo; PASSWORD vacio)
-- ---------------------------------------------------------------------
INSERT INTO PERSONA (ID_PERSONA, NOMBRES, APELLIDOS, TIPO_DOCUMENTO, DOCUMENTO_IDENTIDAD, TELEFONO)
VALUES (SEQ_PERSONA.NEXTVAL, 'Ana', 'Estudiante Prueba', 'DNI', '71000001', NULL);

INSERT INTO ESTUDIANTE (ID_PERSONA, CODIGO_UNI, CICLO, ESTADO, ID_CARRERA)
VALUES (SEQ_PERSONA.CURRVAL, '209900001', 5, 'ACTIVO',
        (SELECT ID_CARRERA FROM CARRERA WHERE NOMBRE = 'Ingenieria de Sistemas'));

INSERT INTO USUARIO (ID_PERSONA, ROL, ESTADO, PASSWORD)
VALUES (SEQ_PERSONA.CURRVAL, 'ESTUDIANTE', 'ACTIVO', NULL);


INSERT INTO PERSONA (ID_PERSONA, NOMBRES, APELLIDOS, TIPO_DOCUMENTO, DOCUMENTO_IDENTIDAD, TELEFONO)
VALUES (SEQ_PERSONA.NEXTVAL, 'Luis', 'Estudiante Prueba', 'DNI', '71000002', NULL);

INSERT INTO ESTUDIANTE (ID_PERSONA, CODIGO_UNI, CICLO, ESTADO, ID_CARRERA)
VALUES (SEQ_PERSONA.CURRVAL, '209900002', 3, 'ACTIVO',
        (SELECT ID_CARRERA FROM CARRERA WHERE NOMBRE = 'Ingenieria de Sistemas'));

INSERT INTO USUARIO (ID_PERSONA, ROL, ESTADO, PASSWORD)
VALUES (SEQ_PERSONA.CURRVAL, 'ESTUDIANTE', 'ACTIVO', NULL);


COMMIT;

-- Fin del script 05
