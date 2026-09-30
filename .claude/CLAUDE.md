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

## Stack tecnológico

| Componente | Tecnología | Notas |
|---|---|---|
| Web | React + TypeScript + Vite | Gestor de paquetes: **pnpm** (fijado con `packageManager` en `package.json`). Linter: **oxlint** (`pnpm lint`). Formateo: **Prettier** (`pnpm format` / `pnpm format:check`; sin `;`, comillas simples, `printWidth` 100) |
| APIs | Quarkus (Java) | Build tool: **Maven**. Extensión `quarkus-smallrye-openapi` estándar en todas las APIs (documentación OpenAPI + Swagger UI en `/q/openapi` y `/q/swagger-ui`), para que el frontend pueda consultar/generar clientes de los endpoints rápido |
| Mobile | Flutter | Codebase único Dart para Android/iOS (solo esas plataformas; org `com.hevyfit`, paquete Dart `hevyfit_mobile`). Lint con `flutter analyze` (`flutter_lints`), formateo con `dart format` |
| Librería compartida | Java/Maven (`hevyfit-common`) | Instalación local (`mvn install`); las APIs la consumen como dependencia Maven normal desde el `.m2` local |
| Base de datos | PostgreSQL | Una instancia local (Docker), con **una base de datos por servicio** (`hevyfit_measurements`, `hevyfit_routines`) |
| Autenticación | JWT propio | Servicio de auth a medida (sin IDP externo) |
| CI/CD | Ninguno por ahora | Solo desarrollo local; se evaluará más adelante |

## Módulos funcionales

### 1. Mediciones corporales (`hevyfit-api-measurements`)
- IMC
- % de grasa corporal
- % de masa magra
- FFMI (índice de masa libre de grasa)
- Historial de mediciones
- Estimaciones de progreso a futuro en base a la tendencia

### 2. Calorías (`hevyfit-api-measurements`)
- Estimación de gasto calórico en base a las mediciones (peso, % grasa/masa magra)
- Proyección de cuánto peso/grasa se ganaría o perdería según el objetivo calórico definido

> Este módulo vive dentro de `hevyfit-api-measurements` porque sus cálculos dependen directamente de los datos de mediciones corporales (peso, % grasa, % masa magra), evitando así una llamada entre servicios para cada estimación.

### 3. Rutinas de ejercicio (`hevyfit-api-routines`)
- Distintos tipos de rutina
- Bloques (ej. bloque de fuerza, bloque de hipertrofia)
- Series y repeticiones dentro de cada bloque

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

Librería Java/Maven consumida únicamente por las dos APIs (no por los frontends). Por ahora se instala localmente (`mvn install`, queda en el `.m2` de cada desarrollador) y se versiona con SemVer manual (MAJOR.MINOR.PATCH). No se publica en un registro remoto (GitHub Packages u otro) mientras el proyecto sea de desarrollo local/portfolio; se reevaluará si en el futuro hace falta CI o colaboración externa.

## Autenticación

Auth propia basada en JWT (sin IDP externo tipo Keycloak/Auth0), repartida así:

- **Emisión de tokens** (`/login`, credenciales, gestión de usuario): vive dentro de `hevyfit-api-measurements`.
- **Generación/validación de JWT** (código compartido: firmar, leer claims, validar expiración/firma): vive en `hevyfit-common`.
- **`hevyfit-api-routines`** no emite tokens; solo los valida usando el código compartido de `hevyfit-common`, confiando en los tokens emitidos por `hevyfit-api-measurements`.

## Roadmap (plano general)

El roadmap vive en GitHub: **Project "HevyFit Roadmap"** (https://github.com/users/Andr3sCid/projects/2) vinculado a `Andr3sCid/Hevyfit`, con un **milestone por hito (H0–H5)** y las HU/tareas como issues. No se mantiene un `.md` de roadmap en el repo para evitar tener dos fuentes de verdad desincronizadas.

**Regla de compuerta:** cada hito abre con un issue "Definir HU del hito" (etiqueta `definicion-hu`); el resto de issues del hito no puede iniciarse hasta cerrarlo.

**Cada hito es un incremento de punta a punta** (backend → web → mobile): mobile no es un hito aparte, sino la última tarea dentro de cada hito.

**Hitos:** H0 Fundaciones · H1 Usuarios y auth · H2 Mediciones corporales · H3 Calorías (depende de H2) · H4 Rutinas · H5 Endurecimiento. Orden: H0 → H1 → (H2 → H3) en paralelo con H4 → H5.

Etiquetas usadas: `definicion-hu`, `historia-usuario`, `tarea`.

## Credenciales y secretos

**Regla general:** ningún archivo con credenciales reales se sube a un repositorio, ni siquiera de desarrollo — y desde el 2026-09-23 esta regla también aplica a **este mismo `.claude/CLAUDE.md`**, porque quedó versionado dentro del monorepo público `Andr3sCid/Hevyfit`. Solo pueden subirse archivos de ejemplo (`.env.example` o un `application.properties` que lea variables de entorno) sin secretos. Los valores reales viven en un `.env` local ignorado por git (uno por API, ver `.env.example` en cada carpeta).

- En las APIs Quarkus, `application.properties` usa `${HEVYFIT_DB_USER}` y `${HEVYFIT_DB_PASSWORD}`; Quarkus lee el `.env` de la raíz de cada API (en dev y tests).
- Historial (repos antiguos, ya no activos): la contraseña de desarrollo se subió por error en el commit inicial de `hevyfit-api-measurements` y `hevyfit-api-routines` en la organización GitHub anterior; se reescribió el historial y se hizo force push (2026-09-18) antes de migrar al monorepo.

## Convenciones de git

- Identidad de commits configurada globalmente en esta máquina: `user.name=Andr3sCid`, `user.email=a.cid04@ufromail.cl`.
- **No** incluir la línea `Co-Authored-By: Claude...` (u otra atribución a Claude/IA) en mensajes de commit ni descripciones de PR de este workspace.

## Decisiones abiertas / pendientes

- **CI/CD y estrategia de despliegue**: descartado por ahora, solo desarrollo local. Revisar si el proyecto avanza hacia algo desplegado.
- **Publicación de `hevyfit-common`**: reevaluar GitHub Packages si el proyecto crece o requiere CI.
