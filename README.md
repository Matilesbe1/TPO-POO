# Barrio Cerrado Los Aromos – Sistema de gestión

Trabajo práctico obligatorio de **Programación Orientada a Objetos** (Propuesta A).

## Integrantes

| Integrante | GitHub | Módulo |
|---|---|---|
| Matías Lesbegueris | [Matilesbe1](https://github.com/Matilesbe1) | Persona A – Vecinos, lotes y reclamos |
| Juani Amalvy | [juaniAmalvy](https://github.com/juaniAmalvy) | Persona B – Expensas, pagos y persistencia |
| Máximo Silva | [maxisilvaa](https://github.com/maxisilvaa) | Persona C – Equipos, mantenimiento y accesos |

## Descripción del sistema

Aplicación de consola en Java que centraliza la gestión del barrio cerrado Los Aromos:

| Módulo | Qué permite hacer |
|---|---|
| **Vecinos, lotes y reclamos** | Alta de vecinos, lotes y residentes. Los vecinos presentan reclamos (seguridad, mantenimiento, convivencia o administrativo) y la prioridad sale del tipo. El administrador los asigna a un empleado (máximo 5 activos por empleado) y el reclamo avanza por los estados ABIERTO → ASIGNADO → EN_CURSO → RESUELTO → CERRADO, con historial y comentarios. Se puede reabrir dentro de los 5 días, se cierra solo pasado ese plazo y los de prioridad alta sin atender en 24 h se escalan al administrador. |
| **Expensas y pagos** | Emisión mensual de la expensa de cada lote (presupuesto × coeficiente, vence el día 10). Interés por mora del 3 % mensual. Pagos parciales imputados primero a intereses y después a capital, desde el período más antiguo. Ajustes de pagos sin borrar el original. Detección de lotes morosos (más de 3 períodos impagos) con notificación al titular. |
| **Equipos y mantenimiento** | Alta de cámaras y luminarias. El guardia registra fallas y se genera una orden de mantenimiento: las cámaras siempre con prioridad alta y las luminarias con prioridad normal, salvo que fallen más de 3 en el mismo sector. El administrador asigna la orden y el empleado registra la reparación. Historial por equipo y aviso de cámaras con más de 48 h sin funcionar. |
| **Visitas y proveedores** | El vecino autoriza visitas con fecha y franja horaria. Los proveedores pueden tener autorizaciones recurrentes de hasta 30 días. El guardia valida la autorización al registrar el ingreso y no deja ingresar a quien tiene un ingreso sin egreso. |
| **Reportes** | Reclamos pendientes, morosidad por lote y equipos fuera de servicio. |

Los datos se guardan en archivos (`data/*.dat`) con serialización de objetos, así que **se conservan entre ejecuciones**.

### Diseño

- **Arquitectura MVC:**
  - `vista/`: menús de consola.
  - `controlador/`: coordinan los casos de uso.
  - `modelo/`: datos y reglas de negocio.
  - `persistencia/`: repositorios.
- **Patrones GRASP:** Experto, Creador, Controlador, Bajo acoplamiento, Alta cohesión, Polimorfismo, Fabricación pura, Indirección y Variaciones protegidas.
- **Principios SOLID:**
  - **SRP:** reportes y persistencia en clases propias.
  - **OCP:** interfaz `Reporte`.
  - **LSP:** `Camara`/`Luminaria` y `Visitante`/`Proveedor`.
  - **ISP:** interfaces chicas `Reporte` e `Identificable`.
  - **DIP:** los controladores dependen de `Repositorio<T>`.
- **Justificación completa:** [`docs/solid-grasp.txt`](docs/solid-grasp.txt), [`docs/patrones-grasp.txt`](docs/patrones-grasp.txt) e [`docs/informe-final.md`](docs/informe-final.md).

## Requisitos

| Requisito | Versión |
|---|---|
| **Java JDK** | 17 o superior (probado con JDK 21) |
| **JUnit** | 5 (solo para correr los tests) |
| **IDE** (opcional) | IntelliJ IDEA o VS Code con la extensión de Java |

No usa librerías externas para la aplicación: solo la biblioteca estándar de Java.

## Instrucciones de ejecución

### Opción 1: IntelliJ IDEA

1. **File → Open** → elegir la carpeta del repositorio.
2. **File → Project Structure → Project** → SDK **17 o superior**.
3. Clic derecho en `src` → **Mark Directory as → Sources Root**.
4. Clic derecho en `test` → **Mark Directory as → Test Sources Root**.
5. Abrir `src/Main.java` y ejecutar con el botón ▶.
6. **Para los tests:** abrir cualquier test, poner el cursor sobre `@Test` → `Alt + Enter` → **Add 'JUnit5' to classpath**. Después, clic derecho en `test` → **Run 'All Tests'**.

### Opción 2: terminal

Desde la carpeta raíz del repositorio.

**Compilar y ejecutar (Git Bash / Linux / macOS):**

```bash
javac -d out -encoding UTF-8 $(find src -name "*.java")
java -cp out Main
```

**Compilar y ejecutar (PowerShell en Windows):**

```powershell
javac -d out -encoding UTF-8 (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

**Correr los tests.** Hace falta el jar `junit-platform-console-standalone` (1.11 o superior), que se descarga de [Maven Central](https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar). Con el jar guardado como `junit.jar` en la raíz (en Windows, cambiar `:` por `;` en el classpath):

```bash
javac -d out-test -cp "out:junit.jar" -encoding UTF-8 $(find test -name "*.java")
java -jar junit.jar execute -cp "out:out-test" --scan-classpath
```

Resultado esperado: **70 tests, 70 exitosos**.

### Cómo usar la aplicación

Al ejecutar `Main` aparece el menú principal:

```
=== BARRIO LOS AROMOS ===
1. Vecinos, lotes y reclamos
2. Expensas y pagos
3. Equipos y mantenimiento
4. Visitas y proveedores
0. Salir
```

- **Por dónde empezar:** primero hay que dar de alta un vecino y un lote (opción 1 → 1 y 1 → 2). Casi todo lo demás depende de eso.
- **Fechas:** en cada operación se pide la fecha (`AAAA-MM-DD`) o la fecha y hora (`AAAA-MM-DDTHH:MM`). Con **Enter** se usa la actual. Escribir fechas a mano permite probar plazos (mora, 24 h, 48 h, 5 días) sin esperar.
- **Reiniciar los datos:** borrar la carpeta `data/`.
- **Casos de prueba paso a paso:** están en [`docs/plan-pruebas.md`](docs/plan-pruebas.md).

## Estructura del proyecto

```
src/
├── Main.java                 Arranque: crea los repositorios, controladores y vistas
├── vista/                    VistaPrincipal, VistaReclamos, VistaExpensas, VistaEquipos, VistaAccesos
├── controlador/              ControladorReclamos, ControladorExpensas, ControladorEquipos, ControladorAccesos
├── Persistencia/             Identificable, Repositorio<T>, RepositorioMemoria<T>, RepositorioArchivo<T>
└── modelo/
    ├── Personas/             Vecino, Administrador, Guardia, EmpleadoMantenimiento
    ├── Lotes/                Lote
    ├── Reclamos/             Reclamo, HistorialEstado, Comentario, TipoReclamo, EstadoReclamo, Prioridad
    ├── Expensas/             Expensa, Pago, AjustePago, Notificacion, EstadoExpensa, MedioPago
    ├── Equipos/              Equipo (abstracta), Camara, Luminaria, OrdenMantenimiento, EstadoEquipo
    ├── Accesos/              Autorizacion, RegistroAcceso, PersonaExterna (abstracta), Visitante, Proveedor
    ├── Reportes/             Reporte, ReporteReclamosPendientes, ReporteMorosidad, ReporteEquiposFueraServicio
    └── Excepciones/          ReglaNegocioException y una excepción por regla de negocio
test/                         Tests JUnit 5 por módulo
docs/                         Documentación de análisis, diseño y pruebas
```

## Documentación

| Documento | Contenido |
|---|---|
| [`analisis del enunciado.md`](analisis%20del%20enunciado.md) | Actores, requerimientos funcionales, reglas de negocio y supuestos (Etapa 1) |
| [`docs/UML/`](docs/UML) | Diagrama de casos de uso, diagrama de clases final y diagramas de secuencia (Etapa 1) |
| [`docs/solid-grasp.txt`](docs/solid-grasp.txt) / [`docs/patrones-grasp.txt`](docs/patrones-grasp.txt) | Justificación de principios SOLID y patrones GRASP (Etapa 1) |
| [`docs/Cambios-diagrama-final.txt`](docs/Cambios-diagrama-final.txt) | Cambios del diagrama inicial al final (Etapa 1) |
| [`docs/plan-implementacion.md`](docs/plan-implementacion.md) | Plan de la Etapa 2: tareas por integrante y criterios de aceptación |
| [`docs/plan-pruebas.md`](docs/plan-pruebas.md) | Pruebas unitarias y plan de pruebas funcionales con resultados |
| [`docs/explicacion-persona-B.md`](docs/explicacion-persona-B.md) | Explicación detallada del módulo de expensas |
| [`docs/informe-final.md`](docs/informe-final.md) | Informe final: cambios respecto del diseño, dificultades, distribución de tareas y reflexión |
