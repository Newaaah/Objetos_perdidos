# Base de datos Oracle - Objetos Perdidos UPeU

Scripts para crear desde cero la base de datos del proyecto, con las mismas tablas,
restricciones, vista, procedimiento, datos y permisos que usa la aplicación.

**Probado con:** Oracle Database 21c Express Edition (PDB `XEPDB1`) y SQL Developer.

---

## Diseño: dos usuarios

| Usuario | Rol | Qué puede hacer |
|---|---|---|
| `UPEU_DATOS` | **Dueño** de las tablas | Crea y modifica la estructura. La aplicación **no** se conecta con él. |
| `APP_UPEU` | Usuario de la **aplicación** (Spring Boot) | Solo los permisos que necesita en cada tabla (ver `04_permisos_app.sql`). |

Así se aplica el **principio de mínimo privilegio**: aunque alguien obtuviera la contraseña de la
aplicación, no podría borrar tablas ni eliminar reportes, custodias o entregas.

---

## Orden de ejecución

Ejecutar cada archivo en SQL Developer como **script** (tecla **F5**), en este orden:

| # | Archivo | Conectado como | Qué hace |
|---|---|---|---|
| 0 | `00_usuarios.sql` | `SYSTEM` en `XEPDB1` | Crea `UPEU_DATOS` y `APP_UPEU` |
| 1 | `01_tablas.sql` | `UPEU_DATOS` | 14 secuencias, 17 tablas, llaves y restricciones |
| 2 | `02_vistas_y_procedimientos.sql` | `UPEU_DATOS` | Vista `VW_SEGUIMIENTO_RECLAMOS` y procedimiento `SP_RESUMEN_RECLAMOS` |
| 3 | `03_datos_iniciales.sql` | `UPEU_DATOS` | Áreas, carreras, categorías y ubicaciones del campus |
| 4 | `04_permisos_app.sql` | `UPEU_DATOS` | Permisos de `APP_UPEU` sobre cada tabla |
| 5 | `05_datos_prueba.sql` *(opcional)* | `UPEU_DATOS` | Cuentas de prueba (admin, personal y 2 estudiantes) |

### Paso a paso

1. **Conexión como SYSTEM a la PDB.** En SQL Developer crea una conexión con usuario `SYSTEM`,
   host `localhost`, puerto `1521` y **Nombre del servicio** `XEPDB1` (no SID).
2. Abre `00_usuarios.sql`, **cambia las dos contraseñas de ejemplo** y ejecútalo (F5).
3. Crea una conexión nueva con el usuario **`UPEU_DATOS`** (mismo host, puerto y servicio).
4. Con esa conexión ejecuta, en orden, `01`, `02`, `03`, `04` y, si quieres cuentas de prueba, `05`.
5. Configura el backend con el usuario `APP_UPEU` (ver abajo) y arráncalo.

### Comprobar que todo quedó bien

Conectado como `UPEU_DATOS`:

```sql
SELECT object_type, COUNT(*) AS cantidad
FROM user_objects
GROUP BY object_type
ORDER BY object_type;
```

Debe mostrar **17 TABLE**, **14 SEQUENCE**, **1 VIEW** y **1 PROCEDURE**, todos `VALID`.

---

## Conexión del backend

El archivo `backend/src/main/resources/application.yaml` ya apunta a:

```yaml
url: jdbc:oracle:thin:@//localhost:1521/XEPDB1
username: APP_UPEU
password: ${DB_PASSWORD}
default_schema: UPEU_DATOS
```

La contraseña **no se escribe en el código**: se lee de la variable de entorno `DB_PASSWORD`.
También se necesita `JWT_SECRET` (una clave larga y aleatoria para firmar los tokens).

En IntelliJ: **Run → Edit Configurations → ObjetosPerdidosApplication → Environment variables**:

```
DB_PASSWORD=la_contraseña_de_APP_UPEU;JWT_SECRET=una_clave_larga_y_aleatoria_de_al_menos_32_caracteres
```

---

## Cuentas de prueba (`05_datos_prueba.sql`)

| Rol | Ingreso | Contraseña |
|---|---|---|
| Administrador | `70000001` | `UpeuAdmin2026!` |
| Personal | `70000002` | `UpeuPersonal2026!` |
| Estudiante | `209900001` | *(vacía)* |
| Estudiante | `209900002` | *(vacía)* |

Son solo para desarrollo. Cambia las contraseñas desde **Mi cuenta** después de ingresar.

---

## Modelo de datos (resumen)

| Grupo | Tablas |
|---|---|
| Catálogos | `AREA`, `CARRERA`, `CATEGORIA`, `UBICACION` |
| Personas y acceso | `PERSONA`, `ESTUDIANTE`, `PERSONAL`, `PERSONAL_UBICACION`, `USUARIO` |
| Reportes | `OBJETO`, `REPORTE_PERDIDA`, `REPORTE_HALLAZGOS` |
| Custodia y entrega | `OBJETOS_CUSTODIADOS`, `COINCIDENCIA`, `ENTREGA` |
| Seguimiento | `RECLAMOS` (reclamos e incidencias), `NOTIFICACIONES` |

Reglas importantes garantizadas por la base de datos:

- Cada estado válido se controla con `CHECK` (por ejemplo, un reporte de pérdida solo puede estar
  `REPORTADO`, `RECUPERADO` o `CANCELADO`).
- Un **reclamo** se cierra como `APROBADO`/`RECHAZADO`; una **incidencia**, como `RESUELTO`
  (`CK_RECLAMOS_ESTADO_TIPO`).
- Un objeto en custodia solo se puede **entregar una vez** (`UK_ENTREGA_CUSTODIA`).
- Un hallazgo y un reporte de pérdida solo pueden tener **una coincidencia confirmada**
  (índices únicos basados en función `UX_COINC_CONF_HALL` y `UX_COINC_CONF_PERD`).

---

## Volver a empezar (borrar todo)

Conectado como `SYSTEM` en `XEPDB1`:

```sql
DROP USER APP_UPEU CASCADE;
DROP USER UPEU_DATOS CASCADE;
```

Luego vuelve a ejecutar desde `00_usuarios.sql`.
