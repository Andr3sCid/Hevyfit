# HevyFit — Guía del workspace

HevyFit es una aplicación de seguimiento fitness. El código vive como **monorepo** en un único repositorio público: [github.com/Andr3sCid/Hevyfit](https://github.com/Andr3sCid/Hevyfit).

## Estructura del repositorio

| Carpeta | Rol |
|---|---|
| `hevyfit-api-measurements` | API de mediciones corporales + calorías |
| `hevyfit-api-routines` | API de rutinas de ejercicio |
| `hevyfit-common` | Librería compartida entre las APIs |
| `hevyfit-web` | Frontend web |
| `hevyfit-mobile` | Frontend mobile |

Cada carpeta mantiene su propio `pom.xml`/`package.json`/`pubspec.yaml` y `.gitignore`, pero todas comparten un único historial de git (sin submódulos ni `.git` propios). Este `.claude` en la raíz documenta las decisiones que aplican a todo el proyecto.

> Nota histórica: hasta el 2026-09-23 cada carpeta fue un repositorio independiente bajo una organización de GitHub (naming `hevyfit-{repositorio}`). Esos repos (y un Project/milestones de prueba en `hevyfit-docs`) se dejaron intactos como respaldo, sin usarse activamente.

## Documentación del proyecto (`.claude/docs`)

`.claude/docs` guarda documentación de referencia para el desarrollo (decisiones de diseño, notas técnicas, especificaciones, etc.) que por su tamaño o naturaleza no tiene sentido meter directamente en este `CLAUDE.md`. El roadmap de HU/tareas sigue viviendo en GitHub Issues/Project (ver sección "Roadmap"), no acá.

## Stack tecnológico

| Componente | Tecnología | Notas |
|---|---|---|
| Web | React + TypeScript + Vite | Gestor de paquetes: **pnpm** (fijado con `packageManager` en `package.json`). Linter: **oxlint** (`pnpm lint`). Formateo: **Prettier** (`pnpm format` / `pnpm format:check`; sin `;`, comillas simples, `printWidth` 100) |
| APIs | Quarkus (Java) | Build tool: **Maven**. Extensión `quarkus-smallrye-openapi` estándar en todas las APIs (documentación OpenAPI + Swagger UI en `/q/openapi` y `/q/swagger-ui`), para que el frontend pueda consultar/generar clientes de los endpoints rápido |
| Mobile | Flutter | Codebase único Dart para Android/iOS (solo esas plataformas; org `com.hevyfit`, paquete Dart `hevyfit_mobile`). Lint con `flutter analyze` (`flutter_lints`), formateo con `dart format` |
| Librería compartida | Java/Maven (`hevyfit-common`) | Instalación local (`mvn install`); las APIs la consumen como dependencia Maven normal desde el `.m2` local |
| Base de datos | PostgreSQL | Una instancia local (Docker), con **una base de datos por servicio** (`hevyfit_measurements`, `hevyfit_routines`) |
| Autenticación | JWT propio | Access token + refresh token, servicio de auth a medida (sin IDP externo) |
| CI/CD | Ninguno por ahora | Solo desarrollo local; se evaluará más adelante |

## Módulos funcionales

### 1. Mediciones corporales (`hevyfit-api-measurements`)
- IMC, % de grasa corporal, % de masa magra, FFMI (índice de masa libre de grasa).
- Historial de mediciones; estimaciones de progreso a futuro en base a la tendencia.
- **Cálculos persistidos, no al vuelo:** cada medición guarda sus derivados (IMC, % y kg de masa magra, masa grasa, FFMI, % de grasa), la altura usada en ese momento y una `formula_version` (para no invalidar mediciones viejas si cambia una fórmula).
- **Editar/borrar una medición antigua** recalcula esa fila y **regenera los snapshots posteriores** (tendencia, proyección y calorías), todo en la misma transacción.
- **% de grasa corporal automático** (no manual, al menos en el MVP): método US Navy, usa cuello, cintura y cadera (esta última solo en mujeres).
- **Circunferencias registradas en cada medición:** cuello, pecho, cintura, cadera, brazos, antebrazos, muslos y pantorrillas (las que no entran en la fórmula igual se guardan para seguimiento de evolución).
- Tendencia y proyección también se guardan como snapshot en cada medición nueva (no se recalculan on-the-fly al consultar).

### 2. Calorías (`hevyfit-api-measurements`)
- Estimación de gasto calórico (TDEE) en base a las mediciones (peso, % grasa/masa magra).
- Proyección de cuánto peso/grasa se ganaría o perdería según el objetivo calórico definido.
- Igual que mediciones: se persiste un snapshot (TDEE, objetivo, proyección) en cada cambio relevante (medición nueva o cambio de objetivo), no se calcula al vuelo.

> Este módulo vive dentro de `hevyfit-api-measurements` porque sus cálculos dependen directamente de los datos de mediciones corporales (peso, % grasa, % masa magra), evitando así una llamada entre servicios para cada estimación.

### 3. Rutinas de ejercicio (`hevyfit-api-routines`)
- Distintos tipos de rutina; bloques (ej. bloque de fuerza, bloque de hipertrofia); series y repeticiones dentro de cada bloque.
- **Catálogo de ejercicios:** precargado (con categorías) más ejercicios propios del usuario. El diseño debe dejar abierta la posibilidad de un **maestro de ejercicios administrable** más adelante (no se implementa todavía, solo se diseña para no cerrar esa puerta).

### Multiusuario e idioma
- **Multiusuario** con registro y login (no es una app single-user). `hevyfit-api-routines` guarda solo el `userId` del claim `sub` del JWT, **sin llave foránea** hacia la base de `measurements` (son bases separadas).
- **Tokens:** access token + refresh token (no solo access token). Ambos los emite `hevyfit-api-measurements`; el detalle de expiración/rotación se define al abrir H1.
- **Unidades e idioma:** sistema métrico; UI en español por defecto, con el diseño abierto a multidioma (ver H0.3 en el roadmap de GitHub).

## Entorno local de PostgreSQL

Instancia de PostgreSQL corriendo en Docker (imagen `postgres:latest`, actualmente v18+), sin `docker-compose` por ahora: se levantó a mano con `docker run` porque solo hace falta evitar instalar Postgres nativo en la máquina de desarrollo. Credenciales de desarrollo local únicamente (no se exponen a internet, sin impacto de seguridad real para este proyecto):

- **Contenedor**: `hevyfit-postgres`
- **Usuario**: `hevyfit`
- **Password**: ver `.env` local de cada API (no se documenta en texto plano aquí porque este archivo se versiona en un repo público; ver sección "Credenciales y secretos")
- **Puerto**: `5432` (expuesto al host)
- **Volumen**: `hevyfit_pgdata` (persiste los datos)
- **Bases de datos**: `hevyfit_measurements`, `hevyfit_routines`

Connection strings:
- `jdbc:postgresql://localhost:5432/hevyfit_measurements`
- `jdbc:postgresql://localhost:5432/hevyfit_routines`

Comando para volver a levantarlo si el contenedor se borra (sustituir `<password>` por el valor real, tomado del `.env` local):

```
docker run -d --name hevyfit-postgres -e POSTGRES_USER=hevyfit -e POSTGRES_PASSWORD=<password> -p 5432:5432 -v hevyfit_pgdata:/var/lib/postgresql postgres:latest
```

> Nota: en Postgres 18+ el volumen se monta en `/var/lib/postgresql` (no en `/var/lib/postgresql/data` como en versiones anteriores); montarlo en la ruta vieja hace que el contenedor falle al iniciar.

## `hevyfit-common`

Librería Java/Maven consumida únicamente por las dos APIs (no por los frontends). Por ahora se instala localmente (`mvn install`, queda en el `.m2` de cada desarrollador). No se publica en un registro remoto (GitHub Packages u otro) mientras el proyecto sea de desarrollo local/portfolio; se reevaluará si en el futuro hace falta CI o colaboración externa.

**Versionado (decisión 2026-09-30):** se mantiene fijo en `0.x.x-SNAPSHOT` a propósito, sin bumpear SemVer manualmente — en un monorepo de un solo historial no hay "drift" entre consumidores que versionar, y nada lo lee todavía; se formaliza recién si algún día hay registro remoto o CI que lo consuma.

## Autenticación

Auth propia basada en JWT (sin IDP externo tipo Keycloak/Auth0), con **access token + refresh token**, repartida así:

- **Emisión de tokens** (`/login`, registro, credenciales, gestión de usuario, refresh/rotación): vive dentro de `hevyfit-api-measurements`.
- **Generación/validación de JWT** (código compartido: firmar, leer claims, validar expiración/firma): vive en `hevyfit-common`.
- **`hevyfit-api-routines`** no emite tokens; solo los valida usando el código compartido de `hevyfit-common`, confiando en los tokens emitidos por `hevyfit-api-measurements`, y guarda solo el `userId` del claim `sub` (ver "Multiusuario e idioma" arriba).
- El detalle fino (expiración de cada token, estrategia de rotación/revocación del refresh token, dónde se guarda en la web) se define al abrir H1 (issue #7 en GitHub).

## Roadmap (plano general)

El roadmap vive en GitHub: **Project "HevyFit Roadmap"** (https://github.com/users/Andr3sCid/projects/2) vinculado a `Andr3sCid/Hevyfit`, con un **milestone por hito (H0–H5)** y las HU/tareas como issues. No se mantiene un `.md` de roadmap en el repo para evitar tener dos fuentes de verdad desincronizadas.

**Regla de compuerta:** cada hito abre con un issue "Definir HU del hito" (etiqueta `definicion-hu`); el resto de issues del hito no puede iniciarse hasta cerrarlo.

**Cada hito es un incremento de punta a punta** (backend → web → mobile): mobile no es un hito aparte, sino la última tarea dentro de cada hito.

**Hitos:** H0 Fundaciones · H1 Usuarios y auth · H2 Mediciones corporales · H3 Calorías (depende de H2) · H4 Rutinas · H5 Endurecimiento. Orden: H0 → H1 → (H2 → H3) en paralelo con H4 → H5.

Etiquetas usadas: `definicion-hu`, `historia-usuario`, `tarea`.

**Estado actual (2026-09-30):** H0 está completamente detallado y cargado como issues #1–#6 (con criterios de aceptación). H1–H5 solo tienen su issue "Definir HU del hito" (#7–#11 respectivamente) creado; su detalle (HU, criterios de aceptación) todavía no se ha escrito y debe hacerse al abrir cada hito, usando como base las decisiones ya tomadas que están documentadas en este archivo (secciones "Módulos funcionales", "Multiusuario e idioma" y "Autenticación"). Próximo paso de trabajo: H0.1 (issue #2, formato de errores común).

## Credenciales y secretos

**Regla general:** ningún archivo con credenciales reales se sube a un repositorio, ni siquiera de desarrollo — y desde el 2026-09-23 esta regla también aplica a **este mismo `.claude/CLAUDE.md`**, porque quedó versionado dentro del monorepo público `Andr3sCid/Hevyfit`. Solo pueden subirse archivos de ejemplo (`.env.example` o un `application.properties` que lea variables de entorno) sin secretos. Los valores reales viven en un `.env` local ignorado por git (uno por API, ver `.env.example` en cada carpeta).

- En las APIs Quarkus, `application.properties` usa `${HEVYFIT_DB_USER}` y `${HEVYFIT_DB_PASSWORD}`; Quarkus lee el `.env` de la raíz de cada API (en dev y tests).
- Historial (repos antiguos, ya no activos): la contraseña de desarrollo se subió por error en el commit inicial de `hevyfit-api-measurements` y `hevyfit-api-routines` en la organización GitHub anterior; se reescribió el historial y se hizo force push (2026-09-18) antes de migrar al monorepo.

## Entorno local / herramientas

Instaladas en esta máquina (Windows) durante el setup; el PATH de usuario ya las incluye, pero **una terminal abierta antes de instalar algo no lo ve hasta que se abre una nueva**:

- **JDK 21+**: `C:\Program Files\Java\jdk-26.0.1` (`JAVA_HOME` seteado). El `pom.xml` compila con `maven.compiler.release=21` aunque el JDK instalado sea más nuevo.
- **Maven 3.9.16**: `C:\Tools\apache-maven-3.9.16\bin`.
- **Node 24 + pnpm 12**: ya en PATH.
- **Flutter 3.47.4**: `C:\Users\andrew\develop\flutter\bin`.
- **Docker Desktop**: `C:\Users\andrew\AppData\Local\Programs\DockerDesktop`. Se ha detenido solo más de una vez (ej. tras reiniciar Windows); si una API falla al conectar a Postgres, lo primero a revisar es si Docker Desktop sigue corriendo y el contenedor `hevyfit-postgres` está `Up` (`docker start hevyfit-postgres` si no).
- **GitHub CLI (`gh`) 2.101.0**: `C:\Program Files\GitHub CLI\gh.exe`, autenticado como `Andr3sCid` con scopes `repo`, `project`, `workflow`, `read:org`, `gist`.

## Convenciones de git

- Identidad de commits configurada globalmente en esta máquina: `user.name=Andr3sCid`, `user.email=a.cid04@ufromail.cl`.
- **No** incluir la línea `Co-Authored-By: Claude...` (u otra atribución a Claude/IA) en mensajes de commit ni descripciones de PR de este workspace.
- **Ramas:** `dev` es donde se trabaja día a día (issues, commits). `main` es la rama estable y está **protegida en GitHub desde el 2026-09-30**: no admite push directo (ni siquiera del dueño del repo, `enforce_admins` activado), solo se actualiza mediante Pull Request `dev → main` (sin aprobaciones obligatorias, pero debe pasar por PR). Se actualiza al cerrar un hito completo, no en cada commit.

## Decisiones abiertas / pendientes

- **CI/CD y estrategia de despliegue**: descartado por ahora, solo desarrollo local. Revisar si el proyecto avanza hacia algo desplegado.
- **Publicación de `hevyfit-common`**: reevaluar GitHub Packages si el proyecto crece o requiere CI.
