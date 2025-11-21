# web-testing-ia

Backend de referencia para gestionar proyectos de automatización web sin base de datos. La aplicación descubre proyectos por carpeta, analiza calidad de casos Gherkin, expone APIs REST para locators y grabaciones multiusuario, y ejecuta steps genéricos compatibles con Cucumber.

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
- Cada proyecto posee un `project.json`. Si falta, `ProjectDiscoveryService` crea uno por defecto con campos mínimos (`id`, `nombre`, `codigoJira`, `tipo`, `autor`, `editor`, `casos`).
- `ProyectoController` expone:
  - `GET /api/proyectos` para listar.
  - `GET /api/proyectos/{proyecto}` para metadata y calidad promedio.
  - `PUT /api/proyectos/{proyecto}` para actualizar `project.json` con validación básica.

## Casos de prueba (.feature)
- Ubicados en `src/test/resources/features/<proyecto>/<funcionalidad>/*.feature`.
- `CaseFileService` permite listar, leer, crear, actualizar y eliminar archivos, extrayendo escenarios y tags mediante expresiones regulares.
- Calidad por caso calculada con `QualityAnalyzer`.
- APIs en `CasoWebController`:
  - `GET /api/proyectos/{proyecto}/casos-web`
  - `GET /api/proyectos/{proyecto}/casos-web/{ruta}`
  - `POST /api/proyectos/{proyecto}/casos-web`
  - `PUT /api/proyectos/{proyecto}/casos-web/{ruta}`
  - `DELETE /api/proyectos/{proyecto}/casos-web/{ruta}`

## Locators YAML
- Se cargan desde `src/main/resources/locators/<proyecto>/*.yml`.
- `LocatorService` cachea los YAML, valida grupos y locators, y permite resolver selectores.
- APIs:
  - `GET /api/proyectos/{proyecto}/locators`
  - `GET /api/proyectos/{proyecto}/locators/{grupo}`

## Steps genéricos
- `WebGenericSteps` soporta dos formas de selección:
  1. Locator por nombre + grupo YAML (`Given establezco el grupo "login"`, `When hago clic en "btn_ingresar"`).
  2. Selector directo (`When hago clic en "//button[@id='login']"` o `css=#login`).
- Utiliza `WebDriverWait` para esperas automáticas y loguea cada acción.
- `PreconditionSteps` usa `qa-core` para ejecutar queries (`DBHelper`) y servicios (`RestServiceClient`).

## Reglas de calidad
- Definidas en `src/main/resources/config/quality-rules.yml` con campos `id`, `nombre`, `descripcion`, `activo`, `peso`.
- `QualityAnalyzer` sólo ejecuta reglas activas y calcula puntaje normalizado, reglas cumplidas/falladas y sugerencias.

## Grabador multiusuario
- `RecorderController` crea sesiones (`/api/recorder/start`), recibe eventos (`/api/recorder/event`), lista pasos (`/api/recorder/steps`) y cierra sesiones (`/api/recorder/stop`).
- `RecorderSessionManager` mantiene sesiones aisladas con su propio `WebDriver` y pasos acumulados.
- `RecorderService` convierte eventos a steps mediante `StepMapper` y ejecuta `QualityAnalyzer` al finalizar para devolver sugerencias de mejora.

## Configuración de navegador
- Archivo `src/main/resources/config/navegador.yml` permite `tipo` (chrome/firefox/edge), `modo` (local/grid), auto-descarga y URL de grid.
- `WebDriverFactory` aplica la configuración, usa WebDriverManager y soporta `RemoteWebDriver` para grid.

## Ejecución de Cucumber
Ejemplo de ejecución con tags específicos:
```
./gradlew runCucumber -Pcucumber.tags="@smoke"
```

