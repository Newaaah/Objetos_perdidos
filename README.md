# Objetos Perdidos - UPeU

![Java](https://img.shields.io/badge/Java-Spring%20Boot%203-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-18-DD0031?logo=angular&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-21c%20XE-F80000?logo=oracle&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?logo=bootstrap&logoColor=white)

Plataforma web para **reportar, encontrar y recuperar objetos perdidos** dentro del campus de la
Universidad Peruana Unión, Filial Tarapoto. Los estudiantes reportan lo que perdieron o encontraron;
el personal recibe los objetos, los mantiene en custodia y los entrega a su dueño con verificación presencial.

---

## Datos del proyecto

| | |
|---|---|
| **Universidad** | Universidad Peruana Unión (UPeU) |
| **Campus** | Filial Tarapoto |
| **Semestre** | 2026-2 |
| **Grupo** | G2 |
| **Título** | Objetos Perdidos - UPeU |

### Cursos y docentes

| Curso | Docente |
|---|---|
| Análisis y Diseño de Sistemas | Joseph Ibrahim Cruz Rodriguez |
| Administración de Base de Datos 2 | Sergio Omar Valladares Castillo |
| Lenguaje de Programación 2 | Jhan Arly Sanchez Tarrillo |

### Integrantes

- Diego Alonso Shupingahua Garate
- Carlos Elias Vasquez Ikeda
- Jose Angel Valderrama Isuiza
- Adriano Sebastian Manrique Camacho

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java, Spring Boot 3, Spring Security con JWT, Spring Data JPA |
| Base de datos | Oracle 21c XE: 17 tablas, vista `VW_SEGUIMIENTO_RECLAMOS`, procedimiento `SP_RESUMEN_RECLAMOS` |
| Frontend | Angular 18 (componentes standalone), Bootstrap 5.3, Bootstrap Icons, SweetAlert2 |
| Documentación de la API | Swagger / OpenAPI |

---

## Estructura del repositorio

```
objetos_perdidos/
├── backend/            API REST en Spring Boot
├── frontend/           Aplicación web en Angular
├── database/oracle/    Scripts de la base de datos y guía de instalación
└── README.md
```

---

## Funcionalidades por rol

### Estudiante
- Inicia sesión con su **código universitario o DNI** (la contraseña es opcional; la habilita el administrador).
- **Reporta pérdidas** y ve **sugerencias** de objetos parecidos que están en custodia.
- **Reporta hallazgos** con foto e indica a qué punto de recepción lo llevará.
- Ve los **objetos entregados** y puede presentar un **reclamo** si cree que su objeto se entregó a otra persona.
- Recibe **notificaciones** de cada cambio en sus reportes.

### Personal
- **Revisa hallazgos** (aprueba o rechaza, viendo la foto).
- Registra la **recepción física** en custodia (con hallazgo previo o directa).
- Registra la **entrega** al dueño con verificación presencial y foto de evidencia.
- Evalúa **coincidencias** entre pérdidas y hallazgos.
- Atiende **reclamos** e **incidencias** de custodia.

### Administrador
- Todo lo del personal.
- Administra **catálogos**: categorías, ubicaciones (aulas, pabellones, puntos de recepción) y carreras.
- Habilita o restablece **contraseñas de estudiantes**.
- Consulta **estadísticas** generadas desde Oracle (consultas, vista y procedimiento almacenado).

### Recorrido de un objeto

```
Estudiante reporta hallazgo → Personal aprueba → Objeto llega a Garita / Mesa de Partes / Limpieza
→ Queda EN CUSTODIA (aparece en el catálogo) → Personal verifica al dueño → ENTREGADO
```

---

## Cómo ejecutar el proyecto

### Requisitos
- JDK 17 o superior
- Node.js 20 o superior y npm
- Oracle Database 21c XE (PDB `XEPDB1`) y SQL Developer
- IntelliJ IDEA (backend) y Visual Studio Code (frontend), recomendados

### 1. Base de datos
Seguir la guía de [`database/oracle`](database/oracle): se ejecutan los scripts `00` a `05` en orden.
Se crean dos usuarios: **`UPEU_DATOS`** (dueño de las tablas) y **`APP_UPEU`** (el que usa la aplicación,
con permisos mínimos).

### 2. Backend
1. Abrir la carpeta `backend` en IntelliJ IDEA.
2. Configurar las **variables de entorno** (las contraseñas **no** están en el código):
   **Run → Edit Configurations → ObjetosPerdidosApplication → Environment variables**
   ```
   DB_PASSWORD=contraseña_de_APP_UPEU;JWT_SECRET=una_clave_larga_y_aleatoria_de_al_menos_32_caracteres
   ```
3. Ejecutar `ObjetosPerdidosApplication`.
4. La API queda en `http://localhost:8082/objetos_perdidos`.
5. Documentación Swagger: `http://localhost:8082/objetos_perdidos/swagger-ui.html`

### 3. Frontend
```bash
cd frontend
npm install
npm start
```
Abrir `http://localhost:4200`. El proxy de Angular (`proxy.config.json`) envía las llamadas `/api` al backend.

> `node_modules` no se sube al repositorio: `npm install` lo descarga la primera vez.

---

## Cuentas de prueba

Se crean con el script opcional [`database/oracle/05_datos_prueba.sql`](database/oracle/05_datos_prueba.sql):

| Rol | Ingreso | Contraseña |
|---|---|---|
| Administrador | `70000001` | `UpeuAdmin2026!` |
| Personal | `70000002` | `UpeuPersonal2026!` |
| Estudiante | `209900001` | *(vacía)* |
| Estudiante | `209900002` | *(vacía)* |

Son **solo para desarrollo**: cambia las contraseñas desde **Mi cuenta** al ingresar.

---

## Seguridad

- Autenticación con **JWT**; cada petición protegida envía el token en la cabecera `Authorization`.
- Permisos por rol en el backend (`ESTUDIANTE`, `PERSONAL`, `ADMIN`) y rutas protegidas con *guards* en el frontend.
- Las fotos de revisión, entrega y evidencia **no son públicas**: solo las ve el personal autorizado.
- Contraseñas guardadas con **BCrypt**; credenciales de la base y del JWT por **variables de entorno**.
- En Oracle, la aplicación usa un usuario con **mínimo privilegio** (`APP_UPEU`): no puede borrar
  reportes, custodias ni entregas.

---

Universidad Peruana Unión · Filial Tarapoto · 2026