# Informe final – Etapa 2

**Propuesta A:** Sistema de gestión del Barrio Cerrado Los Aromos
**Integrantes:** Matías Lesbegueris, Juani Amalvy y Máximo Silva

---

## 1. Cambios respecto del diseño original

Como punto de partida tomamos el **diagrama de clases final de la Etapa 1** (`docs/UML/Diagrama_de_clases_final.png`). Al programarlo, el diseño se mantuvo en lo esencial: las mismas clases de dominio, las mismas relaciones y los patrones que habíamos justificado. Sí aparecieron cosas que en el diagrama no estaban o que hubo que resolver distinto.

### 1.1 Clases y capas que se agregaron

| Cambio | Motivo |
|---|---|
| **Paquetes `vista/` y `controlador/`**, más la clase `Main` | El diagrama solo tenía el modelo. Para separar la interfaz de las reglas de negocio usamos **MVC**: las vistas muestran y leen datos, y los controladores (`ControladorReclamos`, `ControladorExpensas`, `ControladorEquipos`, `ControladorAccesos`) coordinan cada caso de uso. Es el patrón **Controlador** de GRASP. `Main` es el único lugar donde se crean los repositorios concretos. |
| **`RepositorioMemoria<T>`** | Una segunda implementación de `Repositorio<T>`, para que los tests no dependan de archivos. Es la ventaja del **DIP** que habíamos anotado en la Etapa 1. |
| **Interfaz `Identificable`** | `Repositorio<T>` necesita saber el id de cada objeto para guardarlo y buscarlo. Es una interfaz de un solo método (**ISP**). |
| **Enums `TipoReclamo`, `EstadoReclamo`, `Prioridad`, `EstadoExpensa`, `MedioPago` y `EstadoEquipo`** | En el diagrama, `tipo`, `estado` y `prioridad` eran atributos sueltos. Con enums solo se pueden usar los valores permitidos. `TipoReclamo` además conoce su prioridad (`SEGURIDAD(Prioridad.ALTA)`), así que la prioridad sale del tipo sin ningún `if` (RN01.2). |
| **Excepciones propias** (`ReglaNegocioException` y una por regla) | Cada regla de negocio violada lanza su propia excepción: `PlazoVencidoException`, `LimiteReclamosException`, `PagoInvalidoException`, `AutorizacionInvalidaException`, `IngresoDuplicadoException`, `OrdenIncompletaException`, etc. Son verificadas, y la vista las captura y muestra el mensaje sin cortar el programa. |
| **`Notificacion`** | RN02.6 pide una notificación formal al titular cuando el lote pasa a ser moroso. No estaba en el diagrama. |
| **`ReporteEquiposFueraServicio`, `ReporteMorosidad` y `ReporteReclamosPendientes`** | Estaban en el diagrama y se implementaron sobre la interfaz `Reporte`. Cada uno ordena con un `Comparator`. |

### 1.2 Responsabilidades que cambiaron de lugar

| En el diseño | En la implementación | Motivo |
|---|---|---|
| `Administrador.gestionarPrioridad()` | Se eliminó | La prioridad la define el tipo de reclamo (enum) o el tipo de equipo (polimorfismo). Ya no hace falta que alguien la "gestione". |
| `Reclamo.actualizarEstado()` | `Reclamo.cambiarEstado(nuevo, observacion, fecha)` | Además de cambiar el estado, valida que sea el siguiente (RN01.4) y crea el `HistorialEstado` (**Creador**). |
| La expensa "se generaba" desde el lote | `Lote.emitirExpensa()` | **Creador**: el lote contiene sus expensas y tiene el coeficiente para calcularlas. |
| Imputación de pagos sin dueño claro | `Lote.imputarPago()` reparte el pago y `Expensa.registrarPago()` lo imputa | El lote conoce todas sus expensas y las ordena por período (`Expensa implements Comparable`) para pagar primero la más antigua (RN02.4). Cada expensa imputa primero intereses y después capital. |
| Morosidad sin dueño claro | `Lote.esMoroso()` y `Lote.actualizarMorosidad()` | **Experto**: el lote es el que conoce todas sus expensas. |
| `Guardia.registrarFalla()` creaba la orden | El guardia informa, y `Equipo.registrarFalla()` crea la `OrdenMantenimiento` con su propia prioridad | **Creador + Polimorfismo**: el equipo guarda su historial de órdenes (RF04) y cada subclase calcula su prioridad. |
| `EmpleadoMantenimiento.actualizarEstado()` | Se eliminó | El estado del equipo cambia solo con el ciclo de la orden: falla → `CON_FALLA`, asignada → `EN_REPARACION`, cerrada → `OPERATIVO`. |

### 1.3 Atributos y métodos que se agregaron

- **`Reclamo`:**
  - Se agregó el estado **ASIGNADO** (RN01.3).
  - `asignarResponsable()` garantiza un único responsable.
  - `escalar(fecha)` y `debeEscalarse(fecha)` resuelven RN01.9.
  - `cerrarSiVencido(fecha)` hace el cierre automático a los 5 días (RN01.6).
  - `estaActivo()` sirve para el límite de 5 reclamos (RN01.8).
- **`Expensa`:**
  - `capitalPendiente`, `interesPendiente`, `registrarPago()` y `registrarAjuste()`.
  - Implementa `Comparable<Expensa>`.
- **`Pago` y `AjustePago`:** son **clases inmutables** (`final`, campos `private final`, sin setters). Un pago no se modifica ni se borra: se corrige con un ajuste (RN02.8).
- **`Equipo`:**
  - Ahora guarda su `historial` de órdenes.
  - `getTipo()` permite contar las luminarias del sector sin `instanceof` (RN03.4).
  - `requiereAvisoAdministrador()` devuelve `false` por defecto y `Camara` lo redefine para el aviso de 48 h (RN03.5).
- **`Autorizacion`:**
  - Pasó a tener fecha desde/hasta y **franja horaria** (`horaDesde`/`horaHasta`), porque RN04.2 lo pide.
  - Guarda sus `RegistroAcceso`, que es lo que recorre el diagrama de secuencia "Registrar ingreso".
- **`PersonaExterna.puedeTenerAutorizacionRecurrente()`:** `Visitante` devuelve `false` y `Proveedor` `true`. Así solo los proveedores pueden tener autorizaciones recurrentes (RN04.4), sin preguntar el tipo (**polimorfismo / LSP**).
- **Copias defensivas:** todos los `getXxx()` que devuelven listas devuelven una copia, para que nadie modifique el estado interno desde afuera (**bajo acoplamiento**).

### 1.4 Supuestos que se definieron al programar

1. **Interés por mora:** 3 % mensual prorrateado por día (3 % / 30), sobre el capital impago, desde el día siguiente al vencimiento.
2. **Pagos:** un pago mayor a la deuda se rechaza. No se manejan saldos a favor.
3. **Ajustes:** un ajuste positivo se imputa como un pago (intereses y después capital). Uno negativo devuelve capital y, si no alcanza, intereses.
4. **Límite de reclamos:** se cuentan los reclamos ASIGNADOS y EN_CURSO del empleado.
5. **Escalado:** un reclamo de prioridad alta se escala una sola vez, si pasan más de 24 h y sigue ABIERTO o ASIGNADO. El escalamiento queda en el historial.
6. **Autorizaciones:** una visita común es para un solo día. La recurrente solo para proveedores, de hasta 30 días, con la misma franja horaria todos los días.
7. **Ingreso duplicado:** se controla en todo el barrio, no solo en el lote de destino.
8. **Fechas:** el modelo nunca consulta la fecha actual. La pide la vista y llega por parámetro, y así los plazos (5 días, 24 h, 48 h, mora) se pueden probar.

---

## 2. Dificultades encontradas

**Integración del código.** Fue la dificultad más grande. Cada uno programó su parte por separado, y al juntarlas el proyecto no compilaba:
- Algunos archivos tenían el paquete `ar.aromos.modelo...` y otros `modelo...`.
- La interfaz `Repositorio` no era genérica.
- Dos clases de excepciones tenían adentro el código de otra.
- Los archivos de equipos y accesos se subieron por la web de GitHub en carpetas fuera de `modelo`, y quedaron duplicados con los archivos vacíos de la estructura inicial.

Hubo que unificar paquetes y carpetas antes de seguir. Aprendimos que conviene acordar la estructura y compilar todo junto **antes** de que cada uno avance con su parte.

**Uso de Git y GitHub.** Tuvimos problemas para sincronizar el repositorio:
- Algunos no veían los cambios de los demás porque tenían la copia local desactualizada (faltaba hacer `pull`).
- Al intentar subir, Git lo rechazaba por no estar al día con `main`.
- También tuvimos que cuidar el orden de los Pull Requests: si se mergeaba uno sin el otro, `main` no compilaba.

**Probar reglas que dependen del tiempo.** Muchas reglas dependen de plazos (5 días, 24 h, 48 h, días de mora). Si el modelo usaba la fecha actual, no había forma de probarlas. Lo resolvimos pasando siempre la fecha por parámetro.

**Persistencia con serialización.** Cada repositorio guarda su propio archivo. Si un objeto está en dos archivos (como los reclamos, que están en `reclamos.dat` y también dentro de cada lote), al volver a cargar quedan dos copias separadas. Para expensas, autorizaciones y órdenes de mantenimiento lo evitamos guardándolas **dentro de su contenedor**: el lote o el equipo. En reclamos queda como una mejora pendiente.

**Precisión de los importes.** Con `double` aparecían errores de redondeo en los cálculos de intereses. Los resolvimos redondeando a centavos.

**Tiempo.** La entrega se juntó con otras materias, y parte del trabajo se terminó sobre el final del plazo.

---

## 3. Distribución de tareas

### Etapa 1 – Modelado y diseño

| Integrante | Tareas |
|---|---|
| **Matías Lesbegueris** | Repositorio inicial, diagrama de casos de uso y diagrama de clases final |
| **Juani Amalvy** | Diagramas de secuencia (Registrar ingreso y Reabrir reclamo), justificación de los principios SOLID y de los patrones GRASP, y cambios del diagrama final |
| **Máximo Silva** | Análisis de la Propuesta A (actores, requerimientos, reglas de negocio y supuestos) y diagrama de clases inicial |

### Etapa 2 – Implementación

El plan de trabajo (`docs/plan-implementacion.md`) dividió el sistema en tres módulos, con tareas y criterios de aceptación por iteración.

| Integrante | Módulo asignado | Lo que hizo |
|---|---|---|
| **Matías Lesbegueris** (A) | Vecinos, lotes y reclamos | Estructura de carpetas del proyecto. `Vecino`, `Lote`, `Reclamo`, `HistorialEstado`, `Comentario`, `TipoReclamo`, `EstadoReclamo` y `Prioridad`. `Administrador`, `RepositorioMemoria`, `ControladorReclamos`, `VistaReclamos`, `VistaPrincipal`, `ReporteReclamosPendientes` y `Main`. Primeros tests (`VecinoLoteTest` y `RepositorioMemoriaTest`). |
| **Juani Amalvy** (B) | Expensas, pagos y persistencia | Módulo completo de expensas: `Expensa`, `Pago`, `AjustePago`, `Notificacion`, la imputación y la morosidad en `Lote`, `ReporteMorosidad`, `ControladorExpensas` y `VistaExpensas`. `RepositorioArchivo`. 30 tests y el plan de pruebas. Además: arreglos de compilación e integración de los tres módulos; lo que faltaba en reclamos (estado ASIGNADO, escalado, cierre automático, residentes, comentarios y `ReclamoTest`); controladores, vistas y tests de equipos y accesos; README e informe final. |
| **Máximo Silva** (C) | Equipos, mantenimiento y accesos | Modelo de equipos y accesos: `Equipo`, `Camara`, `Luminaria`, `OrdenMantenimiento`, `EstadoEquipo`, `Autorizacion`, `RegistroAcceso`, `PersonaExterna`, `Visitante` y `Proveedor`, y sus excepciones (`AutorizacionInvalidaException`, `IngresoDuplicadoException`, `IngresoNoEncontradoException` y `OrdenIncompletaException`). |

**Resultado final:** 4 módulos integrados en un menú, **70 tests unitarios** (todos exitosos) y **36 casos de prueba funcionales** ejecutados desde la consola (ver `docs/plan-pruebas.md`).

---

## 4. Reflexión del grupo

Lo que más nos sirvió de la Etapa 1 fue haber pensado el diseño antes de programar. Con el diagrama de clases, los diagramas de secuencia y los patrones ya justificados, al programar no hubo que inventar la estructura: sabíamos qué clase hacía cada cosa. Por ejemplo:
- `Autorizacion` valida su vigencia.
- `Expensa` calcula su interés.
- `Reclamo` crea su historial.
- Cada equipo calcula su prioridad sin preguntar el tipo.

Ahí vimos que los patrones no son solo teoría: cuando algo había que cambiarlo, por ejemplo agregar la franja horaria o el estado ASIGNADO, el cambio quedaba dentro de una sola clase.

Donde más nos costó no fue en programar sino en **trabajar en equipo sobre el mismo código**. Cada parte andaba por separado, pero juntarlas llevó más tiempo del esperado, por diferencias de paquetes, carpetas y versiones del repositorio. Si lo volviéramos a hacer:
- Acordaríamos desde el primer día la estructura de paquetes.
- Compilaríamos todo junto seguido.
- Trabajaríamos con ramas y Pull Requests, haciendo siempre `pull` antes de empezar.

También aprendimos a valorar los tests. Las pruebas unitarias nos avisaron de errores que a simple vista no se veían, como un ajuste de pago que dejaba el capital por encima del importe de la expensa. Además nos dieron confianza para tocar código de otro integrante sin romper lo que ya funcionaba.

En resumen, el trabajo nos sirvió para aplicar en un sistema real los temas de la materia: MVC, GRASP, SOLID, herencia y polimorfismo, interfaces, enums, clases inmutables, colecciones con `Comparable`/`Comparator`, excepciones propias y persistencia con archivos. Y también para aprender a coordinar y organizar el trabajo entre los tres.
