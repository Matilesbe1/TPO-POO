# Plan de implementación – Etapa 2

**Entrega:** 6/11/2026 23:59 (cierre 8/11)
**Lenguaje:** Java, con app de consola organizada en **MVC**, persistencia en archivos con **serialización** y colecciones del **Java Collections Framework**.

> Todo el plan está armado con la teoría de la materia (`Paradigma.md`). Lo que **no** sale de esa teoría está marcado con **⚠️ FUERA DE LA TEORÍA**, y además está todo listado en la sección 8.

---

## 1. Teoría que aplica el código

| Tema de la teoría | Dónde se aplica en el TPO |
|---|---|
| **MVC** (Modelo, Vista, Controlador y una clase Principal que arranca) | Paquetes `modelo/`, `vista/` y `controlador/`. `Principal` crea y conecta los objetos. La vista **no** tiene reglas de negocio y el modelo **no** usa `Scanner` ni `System.out` |
| **Relaciones UML → Java** (los "muchos" se vuelven una colección, la composición es que el todo crea y contiene la parte) | `Reclamo` ◆ `HistorialEstado` y `Comentario`, `Expensa` ◆ `Pago`, `Pago` ◆ `AjustePago`. Las asociaciones con multiplicidad "muchos" se implementan como `List<>` |
| **Clase abstracta** | `Equipo` (abstracta, con `calcularPrioridadFalla`) y `PersonaExterna` |
| **Interfaz** (programar a la interfaz) | `Reporte`, `Repositorio<T>` e `Identificable` |
| **Enum con atributos y constructor** | `TipoReclamo(Prioridad)`, `EstadoReclamo` con `siguiente()`, `EstadoEquipo`, `EstadoExpensa` y `MedioPago` |
| **Clases inmutables** (`final`, campos `private final`, sin setters, copia defensiva) | `HistorialEstado`, `Comentario`, `Pago` y `AjustePago`. Encaja con RN02.8: los pagos no se modifican, se corrigen con un ajuste |
| **Tipos primitivos y wrappers** | `double` para importes, `int` para cantidades. Los wrappers se usan solo dentro de colecciones |
| **Colecciones** | `List`/`ArrayList` para las relaciones "muchos" y `Map`/`HashMap` para buscar por clave (documento, número de lote, código). Siempre se declara la variable con la interfaz (`List<>` o `Map<>`) |
| **`Comparable`** | `Expensa implements Comparable<Expensa>`, ordenada por período. Así se resuelve "imputar primero al período más antiguo" (RN02.4) |
| **`Comparator`** | Ordenar los reportes: reclamos por prioridad y fecha, morosos por deuda y equipos por sector |
| **`equals()` y `hashCode()`** | `Vecino` (por documento), `Lote` (por número) y `Equipo` (por código) |
| **Excepciones personalizadas, `throw` y `throws`** | `ReglaNegocioException extends Exception` y una subclase por cada regla. El modelo hace `throw`, el controlador las captura y la vista muestra el mensaje |
| **Archivos y serialización** | `RepositorioArchivo<T>` con `ObjectOutputStream` / `ObjectInputStream`, `try-with-resources` y manejo de `IOException` y `ClassNotFoundException` |
| **GRASP (los 9)** | Ver la sección 2 |
| **SOLID (los 5)** | Ver la sección 2 |
| **Casos de uso como base de las pruebas funcionales** | El plan de pruebas se arma a partir de los casos de uso del diagrama de la Etapa 1 |

---

## 2. GRASP y SOLID en el código

En la Etapa 1 justificamos 4 patrones GRASP y 3 principios SOLID. En el código además aparecen los demás que da la teoría, y conviene mencionarlos en el informe final.

| Patrón / principio | Clases | Regla para el código |
|---|---|---|
| **Experto** *(Etapa 1)* | `Autorizacion.validarVigencia()`, `Expensa.calcularInteres()` y `Lote.esMoroso()` | El cálculo se hace en la clase que tiene los datos. Nadie calcula vigencias ni intereses desde afuera |
| **Creador** *(Etapa 1)* | `Reclamo` crea `HistorialEstado` y `Comentario`. `Expensa` crea `Pago`. `Pago` crea `AjustePago` | Solo el "todo" hace `new` de sus partes |
| **Polimorfismo** *(Etapa 1)* | `Equipo` → `Camara` y `Luminaria` | Prohibido usar `instanceof Camara` o `instanceof Luminaria` |
| **Bajo acoplamiento** *(Etapa 1)* | `Reclamo`, controladores y repositorios | Atributos `private`. Las listas se devuelven como `new ArrayList<>(lista)` (copia defensiva). Los controladores dependen de `Repositorio<T>` |
| **Controlador** | `ControladorReclamos`, `ControladorExpensas`, `ControladorEquipos` y `ControladorAccesos` | Reciben el evento desde la vista y coordinan el modelo **sin** contener las reglas de negocio |
| **Alta cohesión** | Todo el modelo | La vista solo muestra y captura, el controlador solo coordina y el modelo tiene datos y reglas |
| **Fabricación pura** | `RepositorioArchivo<T>` y `RepositorioMemoria<T>` | No representan nada del barrio. Existen para separar la persistencia del dominio |
| **Indirección / Variaciones protegidas** | `Repositorio<T>` y `Reporte` | Cambiar cómo se guarda, o agregar un reporte, no obliga a tocar los controladores |
| **SRP** *(Etapa 1)* | Reportes separados de `Administrador`. Vista, controlador y modelo separados | Cada clase cambia por un solo motivo |
| **OCP** *(Etapa 1)* | `Reporte` y sus 3 implementaciones | Ningún `switch` ni `if` para elegir el reporte |
| **LSP** | `Camara` y `Luminaria` respecto de `Equipo` | Las subclases no tiran excepciones nuevas ni cambian el contrato de `Equipo` |
| **ISP** | `Reporte` (solo `generar()`), `Identificable` (solo `getId()`) | Interfaces chicas, sin métodos que alguna clase no pueda cumplir |
| **DIP** *(Etapa 1)* | Los controladores reciben un `Repositorio<T>` por constructor | Solo `Principal` hace `new RepositorioArchivo<>(...)` |

---

## 3. Estructura del proyecto (MVC)

```
src/ar/aromos/
├── Principal.java                      arranque: crea repositorios, controladores y vistas, y los conecta
├── modelo/
│   ├── personas/   Vecino, Administrador, Guardia, EmpleadoMantenimiento,
│   │               PersonaExterna (abstract), Visitante, Proveedor
│   ├── lotes/      Lote
│   ├── reclamos/   Reclamo, HistorialEstado, Comentario, TipoReclamo, EstadoReclamo, Prioridad
│   ├── expensas/   Expensa, Pago, AjustePago, MedioPago, EstadoExpensa, Notificacion
│   ├── equipos/    Equipo (abstract), Camara, Luminaria, OrdenMantenimiento, EstadoEquipo
│   ├── accesos/    Autorizacion, RegistroAcceso
│   ├── reportes/   Reporte, ReporteReclamosPendientes, ReporteMorosidad, ReporteEquiposFueraServicio
│   └── excepciones/ ReglaNegocioException y una subclase por regla
├── persistencia/   Identificable, Repositorio<T>, RepositorioArchivo<T>, RepositorioMemoria<T>
├── controlador/    ControladorReclamos, ControladorExpensas, ControladorEquipos, ControladorAccesos
└── vista/          VistaPrincipal (menú), VistaReclamos, VistaExpensas, VistaEquipos, VistaAccesos
test/ar/aromos/...                      mismo árbol
```

**Flujo de un caso de uso (igual que el ejemplo de la teoría):** el usuario escribe → la `Vista` le envía la solicitud al `Controlador` → el controlador busca los objetos en el `Repositorio` y le pide al `Modelo` que haga la operación → el modelo aplica la regla, o tira `throw new XxxException(...)` → el controlador guarda y devuelve un mensaje → la vista lo muestra.

**Convenciones obligatorias:**

- Las fechas se reciben **por parámetro** (`LocalDate` o `LocalDateTime`). El modelo nunca pide la hora actual: la pide la vista o el controlador y la pasa. Así los tests de 5 días, 24 h y 48 h son posibles.
- Reglas de negocio: excepción **verificada**, que hereda de `ReglaNegocioException extends Exception` y se declara con `throws`. Datos inválidos (importe negativo, `null`): `IllegalArgumentException`, como en el ejemplo de `CuentaBancaria`.
- Nunca un `catch` vacío ni un `catch (Exception e)` general. Primero se capturan las excepciones específicas.
- No se crean getters y setters "por las dudas", solo los que se usan. Se prefieren métodos con intención (`cambiarEstado()`, `registrarPago()`).

---

## 4. Cómo se trabaja

> ⚠️ **FUERA DE LA TEORÍA:** las ramas, los PR y la revisión entre compañeros son flujo de trabajo de Git. No son contenido de la materia, pero la consigna pide el código en el repositorio.

- **Una rama por iteración**, creada desde `main` actualizado, con el formato `<persona>/<iteracion>-<tema>`.
- Al terminar, se abre un PR a `main` y otro integrante lo revisa contra los criterios de aceptación.
- Archivos compartidos (`Lote`, `Principal`, `VistaPrincipal`): cada uno toca solo su bloque y avisa por el grupo.

| Iteración | Cuándo | Objetivo |
|---|---|---|
| **I0** | 4/11, primeras 3–4 h | Base compartida en `main` (la hace A). Mientras tanto, B y C escriben sus tests |
| **I1** | 4/11, tarde | Modelo de cada módulo |
| **I2** | 5/11 | Reglas de negocio completas y reporte de cada módulo |
| **I3** | 5/11 noche – 6/11 | Controlador y vista de cada módulo, persistencia real y documentación |

---

## 5. Definición de Hecho (vale para todos los PR)

| # | Criterio | Teoría |
|---|---|---|
| DH1 | Todos los tests pasan después de actualizar desde `main` | ⚠️ JUnit (ver sección 8) |
| DH2 | Atributos `private`. Las listas se devuelven como `new ArrayList<>(lista)`, nunca la lista interna. Las variables se declaran con la interfaz (`List`, `Map`) | Bajo acoplamiento, Colecciones |
| DH3 | Ningún `instanceof` sobre equipos ni `switch` para elegir reportes | Polimorfismo, OCP |
| DH4 | Ningún controlador conoce `RepositorioArchivo`, solo `Repositorio<T>`. El único `new RepositorioArchivo` está en `Principal` | DIP, Variaciones protegidas |
| DH5 | El modelo no usa `Scanner` ni `System.out`, y la vista no tiene reglas de negocio | MVC, Alta cohesión |
| DH6 | Cada regla de negocio violada tira una excepción propia (checked) con un mensaje claro. No hay `catch` vacíos | Excepciones |
| DH7 | Las clases inmutables son `final`, con campos `private final` y sin setters | Clases inmutables |
| DH8 | Cada regla de negocio implementada tiene al menos un test del caso válido y uno del inválido | ⚠️ JUnit |

---

## 6. Tareas por persona

### Persona A: Base, vecinos, lotes y reclamos (RF01, RF02, RN01)

| Iter. | Rama | Qué hay que hacer | Criterios de aceptación |
|---|---|---|---|
| **I0** | `a/i0-base` | • Crear el proyecto en el IDE con la carpeta `test/` y JUnit 5 ⚠️<br>• `persistencia/`: `Identificable` (interfaz con `getId()`), `Repositorio<T>` (`guardar`, `buscarPorId`, `listar`, `actualizar`) y `RepositorioMemoria<T>` (con un `Map<String, T>` adentro)<br>• `excepciones/ReglaNegocioException extends Exception`<br>• Enum `Prioridad { BAJA, NORMAL, MEDIA, ALTA }`<br>• `Vecino` (documento, nombre; `equals`/`hashCode` por documento)<br>• `Lote` (numero, superficie, coeficiente, titular, `List` de residentes, expensas, autorizaciones y reclamos; `equals`/`hashCode` por número)<br>• `Principal` y `VistaPrincipal` con el menú vacío, una opción por módulo | ✅ El proyecto compila y corre un test de `RepositorioMemoria` (guardar, buscar, listar, actualizar)<br>✅ Dos `Vecino` con el mismo documento son `equals` y en un `HashSet` cuentan una sola vez<br>✅ `Lote` ya tiene todas las listas, así B y C no necesitan modificarlo<br>✅ Mergeado a `main` antes de que B y C arranquen I1 |
| **I1** | `a/i1-reclamos-modelo` | • `TipoReclamo` como **enum con atributo**: `SEGURIDAD(Prioridad.ALTA)`, `MANTENIMIENTO(Prioridad.MEDIA)`, `CONVIVENCIA(Prioridad.BAJA)` y `ADMINISTRATIVO(Prioridad.BAJA)`<br>• `EstadoReclamo` (enum) con `siguiente()`<br>• `Reclamo`, `HistorialEstado` (**inmutable**) y `Comentario` (**inmutable**)<br>• `Vecino.presentarReclamo(lote, tipo, descripcion, fecha)`<br>• `Reclamo.cambiarEstado(nuevo, observacion, fecha)` y `agregarComentario(autor, texto, fecha)` | ✅ La prioridad sale de `tipo.getPrioridad()`, sin `if` (RN01.2)<br>✅ Si no es titular ni residente autorizado → `ReclamoNoAutorizadoException` (RN01.10)<br>✅ Solo se avanza a `estado.siguiente()`; si no → `TransicionInvalidaException` (RN01.4)<br>✅ **Composición y Creador:** cada cambio de estado crea su `HistorialEstado` dentro de `Reclamo`, y no hay `new HistorialEstado` en otra clase<br>✅ `HistorialEstado` y `Comentario` son `final`, con campos `private final` y sin setters<br>✅ `getHistorial()` devuelve una copia: agregarle algo desde afuera no cambia el reclamo |
| **I2** | `a/i2-reclamos-reglas` | • `Reclamo.reabrir(fecha)` según el diagrama de secuencia "Reabrir reclamo"<br>• `Reclamo.cerrarSiVencido(fecha)`<br>• `Administrador.asignarReclamo(reclamo, empleado, fecha)`<br>• `Reclamo.escalar()` y `debeEscalarse(fechaHora)`<br>• Interfaz `Reporte` y `ReporteReclamosPendientes` | ✅ Reabrir a los 4 días → ABIERTO, con un historial "Reabierto por el vecino"<br>✅ Reabrir a los 6 días → CERRADO y `PlazoVencidoException`<br>✅ Reabrir uno que no está RESUELTO → `ReclamoNoReabribleException`<br>✅ Un único responsable por reclamo; con 5 reclamos en curso → `LimiteReclamosException` (RN01.7–8)<br>✅ Un reclamo ALTA sin atender a las 25 h se escala; a las 23 h, no (RN01.9)<br>✅ El reporte lista los no CERRADOS, ordenados con un **`Comparator`** por prioridad y fecha |
| **I3** | `a/i3-mvc-readme` | • `ControladorReclamos`: recibe `Repositorio<Vecino>`, `Repositorio<Lote>` y `Repositorio<Reclamo>` por constructor<br>• `VistaReclamos` con `Scanner`<br>• Conectarlos en `Principal`<br>• `README.md` | ✅ Desde la consola se puede dar de alta un vecino y un lote, y presentar, asignar, avanzar y reabrir un reclamo<br>✅ El controlador captura cada `ReglaNegocioException` y la vista muestra `e.getMessage()`, sin que el programa se corte<br>✅ Si se ingresa texto donde va un número, se captura `InputMismatchException` o `NumberFormatException` y se vuelve a pedir el dato<br>✅ El README tiene descripción, integrantes, requisitos, cómo ejecutar y cómo correr los tests, la estructura MVC y links a `docs/` |

### Persona B: Expensas, pagos y persistencia (RF03, RN02)

| Iter. | Rama | Qué hay que hacer | Criterios de aceptación |
|---|---|---|---|
| **I0** | *(sin rama)* | • Escribir los tests de I1 e I2 | ✅ Tests listos para la rama de I1 |
| **I1** | `b/i1-expensas-modelo` | • Enums `EstadoExpensa` y `MedioPago`<br>• `Expensa implements Comparable<Expensa>` (orden natural por período)<br>• `Pago` (**inmutable**) y `AjustePago` (**inmutable**)<br>• `Expensa.emitir(lote, periodo, presupuesto)`<br>• `Expensa.calcularInteres(fecha)` (Experto)<br>• `Expensa.registrarPago(fecha, importe, medio)` (Creador de `Pago`) | ✅ Importe = presupuesto × coeficiente (RN02.1), en `double`<br>✅ Vence el día 10 del mes (RN02.2)<br>✅ Interés de 0 antes del vencimiento y de 3 % mensual prorrateado por día ⚠️ sobre el saldo impago (RN02.3). Testeado a 0, 15 y 45 días<br>✅ Un pago parcial cubre primero los intereses y después el capital (RN02.4)<br>✅ Importe ≤ 0 → `IllegalArgumentException`<br>✅ `Pago` es `final`, sin setters y sin método para borrarlo (RN02.8)<br>✅ `Collections.sort(expensas)` las deja ordenadas de la más antigua a la más nueva |
| **I2** | `b/i2-mora-ajustes` | • `Lote.imputarPago(...)`: ordena sus expensas con `Comparable` y reparte el pago desde la más antigua<br>• `Pago.registrarAjuste(motivo, importe, fecha)`. Como `Pago` es inmutable, el ajuste se guarda en su lista interna de ajustes (composición) y los datos originales del pago no cambian<br>• `Lote.esMoroso()` (Experto) y `Notificacion`<br>• `ReporteMorosidad` | ✅ Un pago que alcanza para dos períodos salda primero el más antiguo (RN02.4)<br>✅ Después de un ajuste, la fecha, el importe y el medio del pago original no cambiaron, y el ajuste queda registrado aparte (RN02.8)<br>✅ Con 4 períodos impagos el lote es moroso y se genera **una** notificación al titular; con 3, no (RN02.5–6)<br>✅ Al saldar la deuda deja de ser moroso (RN02.7)<br>✅ El reporte lista los morosos ordenados por deuda con un **`Comparator`** |
| **I3** | `b/i3-persistencia-mvc-pruebas` | • `RepositorioArchivo<T>`: guarda el `Map` completo con `ObjectOutputStream` en `data/<nombre>.dat` y lo lee con `ObjectInputStream`, siempre con **try-with-resources**<br>• `Serializable` en todo el modelo<br>• `Principal` carga al iniciar y guarda al salir<br>• `ControladorExpensas` y `VistaExpensas`<br>• `docs/plan-pruebas.md` | ✅ `RepositorioArchivo` es la **única** clase que usa `java.io` (Fabricación pura, DIP)<br>✅ Maneja `IOException` y `ClassNotFoundException` con un mensaje claro, sin `catch` vacío. Si el archivo no existe la primera vez, arranca vacío<br>✅ Test: guardar → crear otro `RepositorioArchivo` sobre el mismo archivo → leer → mismos datos. Al final se borra el archivo con `File.delete()`<br>✅ Cerrar y volver a abrir la app conserva los datos<br>✅ El plan de pruebas tiene, por cada caso de uso, ID, precondición, pasos, resultado esperado, resultado obtenido y OK/Falla |

### Persona C: Equipos, mantenimiento y accesos (RF04, RF05, RN03, RN04)

| Iter. | Rama | Qué hay que hacer | Criterios de aceptación |
|---|---|---|---|
| **I0** | *(sin rama)* | • Escribir los tests de I1 e I2 | ✅ Tests listos para la rama de I1 |
| **I1** | `c/i1-equipos-modelo` | • `Equipo` **abstracta** (código, sector, fecha de instalación, estado; método concreto `registrarFalla()` y método abstracto `calcularPrioridadFalla(int)`). `equals`/`hashCode` por código<br>• `Camara` y `Luminaria` con `@Override`<br>• Enum `EstadoEquipo`<br>• `OrdenMantenimiento` y `cerrar(fecha, tecnico, descripcion)`<br>• `Guardia.registrarFalla(equipo, fallasEnSector, fecha)`<br>• `Administrador.asignarOrden(...)` y `EmpleadoMantenimiento.registrarReparacion(...)` | ✅ No se puede hacer `new Equipo(...)` (no compila)<br>✅ Cámara → ALTA (RN03.2). Luminaria → NORMAL, o ALTA con más de 3 fallas en el sector (RN03.3–4)<br>✅ **Ningún `instanceof`**: la orden toma `equipo.calcularPrioridadFalla(n)` (Polimorfismo)<br>✅ **LSP:** un test recorre una `List<Equipo>` con cámaras y luminarias y todas se comportan según el contrato<br>✅ Cerrar una orden sin fecha, técnico o descripción → `OrdenIncompletaException` (RN03.6)<br>✅ Falla → CON_FALLA; orden cerrada → OPERATIVO |
| **I2** | `c/i2-accesos` | • `PersonaExterna` **abstracta**, con `Visitante` y `Proveedor`<br>• `Autorizacion` con `validarVigencia(fechaHora)` (Experto)<br>• `Vecino.autorizarVisita(...)`<br>• `RegistroAcceso`<br>• `Guardia.registrarIngreso(documento, lote, fechaHora)` y `registrarEgreso(...)`<br>• Alerta de cámara caída más de 48 h y `ReporteEquiposFueraServicio` | ✅ El ingreso sigue **exactamente** el diagrama de secuencia "Registrar ingreso" (recorre `lote.getAutorizaciones()`, valida la vigencia y revisa los egresos)<br>✅ Sin autorización, o fuera de fecha o de franja → `AutorizacionInvalidaException` (RN04.1–3)<br>✅ Si tiene un ingreso sin egreso → `IngresoDuplicadoException` (RN04.6)<br>✅ Una autorización recurrente de más de 30 días se rechaza (RN04.4)<br>✅ Una cámara caída a las 49 h aparece en la alerta; a las 47 h, no (RN03.5)<br>✅ El reporte lista los equipos no OPERATIVOS, ordenados por sector con un **`Comparator`** |
| **I3** | `c/i3-mvc-informe` | • `ControladorEquipos`, `ControladorAccesos`, `VistaEquipos` y `VistaAccesos`<br>• Conectarlos en `Principal`<br>• `docs/informe-final.md` | ✅ Desde la consola se puede registrar una falla, asignar y cerrar una orden, autorizar una visita y registrar su ingreso y egreso<br>✅ Las excepciones de negocio se muestran como mensaje, sin cortar el programa<br>✅ El informe tiene: cambios respecto del diseño (sección 7), dificultades (un párrafo de cada integrante), distribución de tareas y reflexión del grupo |

> Si C se atrasa en I2, A puede tomar la parte de accesos cuando termine su I2.

### Cierre (los tres, 6/11)

| Tarea | Criterio de aceptación |
|---|---|
| Merge final | `main` compila y todos los tests pasan |
| Recorrido manual | Los casos de uso del plan de pruebas se ejecutan desde la consola: presentar, asignar y reabrir reclamo; emitir expensa; pago parcial; falla de cámara; cerrar orden; autorizar visita; ingreso y egreso |
| Revisión de diseño | Se cumplen DH2–DH7. Buscar en el código `instanceof`, `new HistorialEstado`, `new RepositorioArchivo`, `catch (Exception`, y `Scanner` o `System.out` dentro de `modelo/` |
| Entrega | README, plan de pruebas e informe están en `main` y el link al repo está subido al campus |

---

## 7. Cambios respecto del diagrama de clases final (para el informe)

| Cambio | Motivo (teoría) |
|---|---|
| Se agregan los paquetes `vista/` y `controlador/`, más la clase `Principal` | MVC y GRASP Controlador. El diagrama de la Etapa 1 solo tenía el modelo |
| Se agrega `RepositorioMemoria<T>` | DIP: "pruebas unitarias con implementaciones simuladas (stubs)". Así los tests no dependen de archivos |
| Se agrega la interfaz `Identificable` | ISP: un contrato mínimo para que el repositorio pueda buscar por id |
| Se agregan las excepciones personalizadas y `ReglaNegocioException` | Excepciones personalizadas para reglas del dominio |
| `HistorialEstado`, `Comentario`, `Pago` y `AjustePago` pasan a ser inmutables | Clases inmutables y RN02.8 |
| `TipoReclamo` es un enum que conoce su `Prioridad` | Enum con atributos: se elimina el `if` para calcular la prioridad |
| `Expensa implements Comparable<Expensa>` | Orden natural por período para la imputación (RN02.4) |
| `Lote.imputarPago()` y `Lote.esMoroso()` | Experto: el lote es el que conoce todas sus expensas |

---

## 8. ⚠️ Lo que NO sale de la teoría

Esto **no** está en `Paradigma.md`. Va igual en el plan porque lo pide la consigna o porque no hay alternativa dentro de la teoría. Conviene que lo sepan por si el docente pregunta.

| # | Qué es | Por qué va igual | Alternativa / qué hacer |
|---|---|---|---|
| 1 | **JUnit 5 (pruebas unitarias)**: `@Test`, `assertEquals`, `assertThrows` | La consigna pide "pruebas unitarias" y la teoría no da ninguna herramienta de testing | Es lo estándar en Java. Hay que aprender solo `@Test`, `assertEquals`, `assertTrue` y `assertThrows`. Se agrega desde el IDE (IntelliJ: *Add JUnit 5 to classpath*) |
| 2 | **Git: ramas, pull requests y revisión de código** | La consigna pide el código en el repositorio de GitHub | Si les complica, cada uno puede trabajar en su rama de siempre (`juani`, `maxi`, `matii`) sin una rama por iteración |
| 3 | **Clases genéricas propias** (`Repositorio<T>`, `RepositorioArchivo<T>`) | Ya está en el diagrama final de la Etapa 1. La teoría solo **usa** genéricos en colecciones (`List<Alumno>`), no enseña a **definir** clases genéricas | Si les resulta difícil: un repositorio por entidad (`RepositorioReclamos`, `RepositorioLotes`...), como `RepositorioFactura` en la teoría. Pero entonces hay que cambiar el diagrama |
| 4 | **API de fechas `java.time`** (`LocalDate`, `LocalDateTime`, `ChronoUnit.DAYS.between`, `plusDays`) | Las reglas dependen de plazos (5 días, 24 h, 48 h, día 10, 30 días). La teoría menciona `LocalDate` solo como ejemplo de clase inmutable | No hay alternativa razonable. Usen solo `of()`, `plusDays()`, `isBefore()`, `isAfter()` y `ChronoUnit.DAYS/HOURS.between()` |
| 5 | **Prorrateo diario del interés** (3 % / 30 por día) | El enunciado no lo define (supuesto 7 de la Etapa 1). Es una regla de negocio nuestra, no un tema de la materia | Dejarlo escrito como supuesto en el informe |

**Cosas que saqué del plan anterior porque no estaban en la teoría:**

| Antes | Ahora (según la teoría) |
|---|---|
| Maven y `pom.xml` | Proyecto común del IDE, con JUnit agregado desde el IDE |
| `java.time.Clock` para inyectar la fecha | La fecha se pasa como `LocalDate`/`LocalDateTime` por parámetro |
| `List.copyOf(...)` | Copia defensiva con `new ArrayList<>(lista)`, como enseña la teoría de clases inmutables |
| `@TempDir` de JUnit | Archivo de prueba que se borra con `File.delete()` |
| `ReglaNegocioException extends RuntimeException` | `extends Exception` con `throws`, como el ejemplo `SaldoInsuficienteException` |
| Clase `SistemaBarrio` | `Principal` + controladores, como en el ejemplo de MVC de la teoría |

**Opcional, sí está en la teoría:** la teoría da **Java Swing**. Como el proyecto está en MVC, si sobra tiempo se puede reemplazar alguna `VistaXxx` de consola por un `JFrame` sin tocar ni el modelo ni los controladores. Es una buena forma de mostrar que la separación funciona. **No lo encaren antes de tener todo lo demás terminado.**
