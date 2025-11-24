# web-testing-ia

Backend de referencia para gestionar proyectos de automatización web sin base de datos. La aplicación descubre proyectos por carpeta, analiza calidad de casos Gherkin, expone APIs REST para locators y grabaciones multiusuario, y ejecuta steps genéricos compatibles con Cucumber.

## Formato de respuestas
- **Éxito**: todo se devuelve dentro de `data`.
- **Error**: todo se devuelve dentro de `notifications` (códigos y tipos incluidos) evitando errores HTTP 500.

```json
// Ejemplo de respuesta exitosa
{
  "data": {
    "sessionId": "6f4f9f5e-72cf-4af3-8e55-09a4c9ae5e2a"
  }
}

// Ejemplo de respuesta con error controlado
{
  "notifications": [
    {
      "message": "Validación fallida",
      "detail": [
        { "field": "ruta", "message": "no debe estar vacío" }
      ],
      "code": 400,
      "type": "ValidationError"
    }
  ]
}
```

### Cuerpos de ejemplo listos para usar
```json
// Crear o actualizar metadata de proyecto
{
  "id": "demo-web",
  "name": "Demo web",
  "jiraCode": "WEB-10",
  "type": "web",
  "author": "QA",
  "editor": "QA",
  "cases": []
}

// Crear un caso .feature
{
  "path": "login/new.feature",
  "content": "Feature: Login\n  Scenario: sign in\n    Given ..."
}

// Enviar evento al recorder
{
  "sessionId": "6f4f9f5e-72cf-4af3-8e55-09a4c9ae5e2a",
  "action": "click",
  "selector": "css=#login",
  "text": "",
  "value": ""
}
```

## Arquitectura
- **Spring Boot** para APIs REST y configuración.
- **Cucumber + Selenium** para pasos web reutilizables con esperas automáticas.
- **qa-core** para precondiciones de base de datos o servicios.
- **Analizador de calidad** configurable mediante `config/quality-rules.yml`.
- **Grabador multiusuario** con `RecorderSessionManager` y `RecorderService` que construyen pasos Gherkin a partir de eventos.

### Estructura de carpetas
```
src/main/java/com/example/webtestingia/
    config/RestExceptionHandler.java
    controller/ (ProyectoController, CasoWebController, LocatorController, RecorderController)
    drivers/WebDriverFactory.java
    hooks/LoggingHooks.java
    model/ (ProjectMetadata, QualityRule, QualityResult, TestCaseDetail, TestCaseSummary, excepciones)
    quality/QualityAnalyzer.java
    recorder/ (RecorderSessionManager, RecorderService, StepMapper)
    service/ (ProjectDiscoveryService, CaseFileService, LocatorService)
    steps/ (WebGenericSteps, PreconditionSteps)
```

## Detección de proyectos
- Cada carpeta inmediata dentro de `src/test/resources/features` es un proyecto.
- Cada proyecto posee un `project.json`. Si falta, `ProjectDiscoveryService` crea uno por defecto con campos mínimos (`id`, `name`, `jiraCode`, `type`, `author`, `editor`, `cases`). Los alias en español siguen siendo aceptados al leer archivos existentes.
- `ProyectoController` expone **únicamente** rutas en inglés:
  - `GET /api/projects` para listar.
  - `GET /api/projects/{project}` para metadata y calidad promedio.
  - `PUT /api/projects/{project}` para actualizar `project.json` con validación básica.

### Contratos de endpoints de proyectos
- **GET /api/projects** → `200 OK` con lista de `ProjectMetadata`.
- **GET /api/projects/{project}** → `200 OK` con `ProjectMetadata` + `calidadPromedio`; `404` si la carpeta no existe; `422` si `project.json` está corrupto.
- **PUT /api/projects/{project}** → body JSON con los mismos campos de `project.json`; `200` al guardar, `400` si faltan campos obligatorios, `500` por error de escritura.

Ejemplos:

- **GET /api/projects/{project}**

```json
{
  "data": {
    "id": "demo-web",
    "name": "Demo web",
    "jiraCode": "WEB-10",
    "type": "web",
    "author": "QA",
    "editor": "QA",
    "cases": [],
    "averageQuality": 0.87
  }
}
```

- **PUT /api/projects/{project}** request body

```json
{
  "id": "demo-web",
  "name": "Demo web",
  "jiraCode": "WEB-10",
  "type": "web",
  "author": "QA",
  "editor": "QA",
  "cases": []
}
```

Respuesta exitosa:

```json
{
  "data": {
    "message": "Project saved"
  }
}
```

## Casos de prueba (.feature)
- Ubicados en `src/test/resources/features/<proyecto>/<funcionalidad>/*.feature`.
- `CaseFileService` permite listar, leer, crear, actualizar y eliminar archivos, extrayendo escenarios y tags mediante expresiones regulares.
- Calidad por caso calculada con `QualityAnalyzer`.
- APIs en `CasoWebController` (paths en inglés):
  - `GET /api/projects/{project}/web-cases` → lista `TestCaseSummary` (ruta, tags, calidad) (`200`).
  - `GET /api/projects/{project}/web-cases/{ruta}` → `TestCaseDetail` con contenido y análisis (`200`); `404` si falta archivo; `422` si Gherkin es inválido.
  - `POST /api/projects/{project}/web-cases` → body `{ "path": "feature/new.feature", "content": "Feature: ..." }`; `201` creado, `400` ruta inválida, `422` parsing Gherkin.
  - `PUT /api/projects/{project}/web-cases/{ruta}` → body `{ "content": "Feature: ..." }`; `200` al sobrescribir, `404` si no existe, `422` por parsing.
  - `DELETE /api/projects/{project}/web-cases/{ruta}` → `204` al borrar, `404` si no existe.

Ejemplos de entrada/salida:

- **GET /api/projects/{project}/web-cases**

```json
{
  "data": [
    {
      "ruta": "login/login.feature",
      "escenario": "Scenario: successful login",
      "tags": ["@smoke"],
      "calidad": {
        "score": 0.9,
        "passedRules": ["R2"],
        "failedRules": ["R1"],
        "details": []
      }
    }
  ]
}
```

- **POST /api/projects/{project}/web-cases** request body

```json
{
  "path": "feature/new.feature",
  "content": "Feature: ..."
}
```

Respuesta exitosa:

```json
{
  "data": {
    "message": "Case created"
  }
}
```

- **GET /api/projects/{project}/web-cases/{ruta}** respuesta

```json
{
  "data": {
    "ruta": "login/login.feature",
    "contenido": "Feature: ...",
    "escenarios": ["Scenario: successful login"],
    "calidad": {
      "score": 0.9,
      "passedRules": ["R2"],
      "failedRules": ["R1"],
      "details": []
    }
  }
}
```

## Locators YAML
- Se cargan desde `src/main/resources/locators/<proyecto>/*.yml`.
- `LocatorService` cachea los YAML, valida grupos y locators, y permite resolver selectores.
- APIs (paths en inglés):
  - `GET /api/projects/{project}/locators` → mapa completo de grupos (`200`); `404` si falta proyecto o YAML.
  - `GET /api/projects/{project}/locators/{group}` → mapa de locators del grupo (`200`); `404` si el grupo no existe; `422` si el YAML está corrupto.

## Steps genéricos
- `WebGenericSteps` soporta dos formas de selección:
  1. Locator por nombre + grupo YAML (`Given establezco el grupo "login"`, `When hago clic en "btn_ingresar"`).
  2. Selector directo (`When hago clic en "//button[@id='login']"` o `css=#login`).
- Utiliza `WebDriverWait` para esperas automáticas y loguea cada acción.
- `PreconditionSteps` usa `qa-core` para ejecutar queries (`DBHelper`) y servicios (`RestServiceClient`).

## Reglas de calidad
- Definidas en `src/main/resources/config/quality-rules.yml` con campos `id`, `nombre`, `descripcion`, `activo`, `peso`.
- `QualityAnalyzer` sólo ejecuta reglas activas y calcula puntaje normalizado, reglas cumplidas/falladas, motivos y sugerencias.
- Reglas actuales y su buena práctica QA:
  - **R1 - Debe tener al menos un Then**: obliga a validar resultados observables; evita escenarios sin aserciones.
  - **R2 - Nombre descriptivo**: títulos claros explican la intención y aceleran la trazabilidad de cobertura.
  - **R3 - No más de 20 pasos**: escenarios cortos son más estables y fáciles de mantener.
  - **R4 - Debe tener al menos una validación Then**: refuerza la presencia de aserciones útiles para detectar regresiones.
  - **R5 - Uso consistente de Given/When/Then**: mantiene la narrativa Gherkin, facilita lectura para QA y negocio.

## Grabador multiusuario
- `RecorderController` crea sesiones (`/api/recorder/start`), recibe eventos (`/api/recorder/event`), lista pasos (`/api/recorder/steps`), publica el catálogo de steps (`/api/recorder/available-steps`) y cierra sesiones (`/api/recorder/stop`).
- `RecorderSessionManager` mantiene sesiones aisladas con su propio `WebDriver` y pasos acumulados.
- `RecorderService` convierte eventos a steps mediante `StepMapper` y ejecuta `QualityAnalyzer` al finalizar para devolver sugerencias de mejora.
- Contratos clave (paths en inglés):
  - **POST /api/recorder/start** → `201` con `{ "sessionId": "uuid" }`; `500` si el navegador no arranca.
  - **POST /api/recorder/event** → body `{ sessionId, action, selector, text, value }`; `202` al aceptar; `404` si la sesión no existe; `400` por evento inválido.
  - **GET /api/recorder/steps?sessionId=...** → `200` con lista de pasos generados.
  - **GET /api/recorder/available-steps** → `200` con los steps disponibles en orden Given/When/Then para que el frontend los despliegue. Ejemplo de respuesta:
    ```json
    {
      "data": {
        "steps": [
          { "type": "GIVEN", "template": "Given navego a \"<url>\"" },
          { "type": "WHEN", "template": "When hago clic en \"<target>\"" },
          { "type": "WHEN", "template": "When escribo \"<text>\" en \"<target>\"" },
          { "type": "WHEN", "template": "When ejecuto accion \"<action>\" sobre \"<target>\"" },
          { "type": "THEN", "template": "Then debería ver el texto \"<text>\"" }
        ]
      }
    }
    ```
  - **POST /api/recorder/stop?sessionId=...** → `200` con pasos finales y resumen de calidad; `404` si la sesión no existe.

## Catálogo de steps reutilizables
- **GET /api/steps** → Lista todas las definiciones de steps encontradas en las clases `WebGenericSteps` y `ServiceSteps`, ordenadas primero los Given, luego los When y al final los Then.

Ejemplo de respuesta:

```json
{
  "data": {
    "steps": [
      { "type": "GIVEN", "pattern": "establezco el grupo \"{string}\"", "source": "WebGenericSteps" },
      { "type": "WHEN", "pattern": "hago clic en \"{string}\"", "source": "WebGenericSteps" },
      { "type": "WHEN", "pattern": "escribo \"{string}\" en \"{string}\"", "source": "WebGenericSteps" },
      { "type": "THEN", "pattern": "debería ver el texto \"{string}\"", "source": "WebGenericSteps" }
    ]
  }
}
```

## Configuración de navegador
- Archivo `src/main/resources/config/navegador.yml` permite `tipo` (chrome/firefox/edge), `modo` (local/grid), auto-descarga y URL de grid.
- `WebDriverFactory` aplica la configuración, usa WebDriverManager y soporta `RemoteWebDriver` para grid.

## Cómo levantar el backend y habilitar las APIs
1. Requisitos: JDK 17+ y acceso a internet si `auto_download_drivers` está en `true`.
2. Ejecutar `./gradlew bootRun` desde la raíz del proyecto.
3. Las APIs quedarán disponibles en `http://localhost:8080` y el descubrimiento de proyectos se realiza al vuelo sobre `src/test/resources/features`.

## Ejecución de Cucumber
Ejemplo de ejecución con tags específicos:
```
./gradlew runCucumber -Pcucumber.tags="@smoke"
```

