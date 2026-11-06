# Persona B – Expensas, pagos y persistencia
### Explicación paso a paso y justificada

**Responsabilidad de B:** RF03 (gestión de expensas y pagos), las reglas de negocio RN02.1 a RN02.8 y la persistencia del sistema en archivos.

**Rama en GitHub:** `b/expensas-pagos` (6 commits de juaniAmalvy)

**Tamaño:** ~670 líneas de código, 331 de tests (30 tests) y 60 del plan de pruebas.

---

## Índice

1. [Arquitectura: dónde encaja B](#paso-1--arquitectura-dónde-encaja-b-mvc)
2. [Enums: estados y medios de pago](#paso-2--enums-estadoexpensa-y-mediopago)
3. [Excepciones de negocio](#paso-3--excepciones-de-negocio)
4. [La clase Expensa](#paso-4--la-clase-expensa)
5. [La clase Pago](#paso-5--la-clase-pago)
6. [La clase AjustePago](#paso-6--la-clase-ajustepago)
7. [La clase Notificacion](#paso-7--la-clase-notificacion)
8. [Lo que B agrega en Lote](#paso-8--lo-que-b-agrega-en-lote)
9. [ReporteMorosidad](#paso-9--reportemorosidad)
10. [RepositorioArchivo (persistencia)](#paso-10--repositorioarchivo-persistencia)
11. [ControladorExpensas](#paso-11--controladorexpensas)
12. [VistaExpensas](#paso-12--vistaexpensas)
13. [Integración en Main y VistaPrincipal](#paso-13--integración-en-main-y-vistaprincipal)
14. [Pruebas unitarias](#paso-14--pruebas-unitarias-junit-5)
15. [Pruebas funcionales](#paso-15--pruebas-funcionales)
16. [Resumen de GRASP y SOLID](#resumen-grasp-y-solid-aplicados)
17. [Lo que no sale de la teoría](#️-lo-que-no-sale-de-la-teoría)
18. [Decisiones de diseño para el informe](#decisiones-de-diseño-para-el-informe)

---

## Paso 1 – Arquitectura: dónde encaja B (MVC)

**Qué se hizo:** el módulo se dividió en las tres partes de MVC, siguiendo el ejemplo de la teoría (`VistaConsola` → `ControladorTareas` → `Tarea`).

```
Usuario → VistaExpensas → ControladorExpensas → Lote / Expensa / Pago   (modelo)
                                   │
                                   └──→ Repositorio<Lote>  →  RepositorioArchivo  (data/lotes.dat)
```

| Capa | Clases de B | Responsabilidad |
|---|---|---|
| Vista | `VistaExpensas` | Mostrar el menú, leer datos con `Scanner`, mostrar resultados |
| Controlador | `ControladorExpensas` | Recibir el pedido, buscar el lote, delegar en el modelo, guardar |
| Modelo | `Expensa`, `Pago`, `AjustePago`, `Notificacion`, enums, excepciones, lo agregado a `Lote` | Datos y reglas de negocio |
| Persistencia | `RepositorioArchivo<T>` | Guardar y leer archivos |
| Reportes | `ReporteMorosidad` | Armar el reporte de morosos |

**Por qué (teoría):**
- **MVC:** "mezclar interfaz, datos y lógica en una clase genera problemas". Cada parte tiene una responsabilidad propia, el modelo no depende de `Scanner` y la vista no contiene reglas de negocio.
- **Alta cohesión (GRASP):** la vista solo muestra, el controlador solo coordina y el modelo solo tiene datos y reglas.

**Regla de convivencia:** el modelo **nunca** pide la fecha actual. La fecha la pide la vista y viaja por parámetro, y así los plazos (vencimiento, días de mora) se pueden probar con fechas inventadas.

---

## Paso 2 – Enums: `EstadoExpensa` y `MedioPago`

```java
public enum EstadoExpensa { PENDIENTE, PAGADA_PARCIALMENTE, PAGADA }
public enum MedioPago     { EFECTIVO, TRANSFERENCIA, TARJETA_CREDITO }
```

**Qué se hizo:** dos listas cerradas de valores posibles.

**Por qué:**
- **Regla de negocio:** RF03 pide registrar "el estado de cada expensa" y "el medio de pago".
- **Teoría (enums):** "piensen en los enums como una lista cerrada de opciones". Evitan números mágicos (`1` = efectivo) y que alguien cargue un valor inexistente.
- **Decisión:** "vencida" **no** es un estado guardado, porque depende de la fecha en que se consulta. Se calcula con `estaVencida(fecha)`.

---

## Paso 3 – Excepciones de negocio

| Excepción | Cuándo se lanza |
|---|---|
| `ExpensaDuplicadaException` | Se intenta emitir dos veces la expensa del mismo mes para un lote |
| `PagoInvalidoException` | El pago supera la deuda, la expensa ya está pagada o el lote no tiene deuda |
| `AjusteInvalidoException` | El ajuste dejaría un pago negativo o la expensa con saldo a favor |

```java
public class PagoInvalidoException extends ReglaNegocioException {
    public PagoInvalidoException(String mensaje) { super(mensaje); }
}
```

**Por qué:**
- **Teoría (excepciones personalizadas):** "se crean para representar problemas propios del dominio" y "permiten comunicar reglas de negocio de forma más clara". Es el mismo esquema que `SaldoInsuficienteException` de la teoría.
- Heredan de `ReglaNegocioException extends Exception`, así que son **checked**: el compilador obliga a manejarlas (`throws` en el método y `try-catch` en la vista).
- Para **datos mal ingresados** (un importe negativo, un `null`) se usa `IllegalArgumentException`, igual que en el ejemplo `CuentaBancaria` de la teoría. Se distingue un **dato inválido** de una **regla de negocio violada**.

---

## Paso 4 – La clase `Expensa`

Es la clase central del módulo: representa **la expensa de un lote en un mes**.

### 4.1 Atributos

| Atributo | Tipo | Para qué |
|---|---|---|
| `periodo` | `YearMonth` | Mes de la expensa (ej. 2026-09) |
| `importe` | `double` | Monto original |
| `fechaVencimiento` | `LocalDate` | Día 10 del mes |
| `capitalPendiente` | `double` | Lo que falta pagar del importe |
| `interesPendiente` | `double` | Intereses generados y todavía no pagados |
| `interesCalculadoHasta` | `LocalDate` | Hasta qué día ya se calcularon intereses |
| `pagos` | `List<Pago>` | Pagos recibidos |

Tiene además tres constantes: `TASA_MENSUAL = 0.03`, `DIAS_POR_MES = 30` y `DIA_VENCIMIENTO = 10`.

**Por qué:**
- **Teoría (tipos):** se usa `double` (un primitivo) para los importes, porque es "más rápido y ligero" y alcanza para el caso.
- **Teoría (colecciones):** la relación "muchos" con `Pago` se implementa como una colección y la variable se declara con la interfaz (`List<Pago> pagos = new ArrayList<>()`), que es "menor acoplamiento y mayor flexibilidad".
- Los atributos son `private` (**encapsulamiento**, visibilidad `-` en UML).
- Las constantes con nombre evitan números mágicos y dejan las reglas a la vista.

### 4.2 Constructor: cálculo del importe

```java
this.importe = redondear(presupuesto * coeficiente);
this.fechaVencimiento = periodo.atDay(DIA_VENCIMIENTO);
```

- **RN02.1:** la expensa es el presupuesto del mes multiplicado por el coeficiente del lote. Con un presupuesto de $ 100.000 y un coeficiente de 0,05, queda en **$ 5.000**.
- **RN02.2:** vence el día 10 de cada mes.
- Valida que el presupuesto y el coeficiente sean mayores que cero (`IllegalArgumentException`).

### 4.3 `calcularInteres(fecha)`: interés por mora

```java
long dias = ChronoUnit.DAYS.between(interesCalculadoHasta, fecha);
return capitalPendiente * TASA_MENSUAL / DIAS_POR_MES * dias;
```

- **RN02.3:** se aplica un interés del 3 % mensual sobre el saldo impago desde el vencimiento.
- **Supuesto 7 de la Etapa 1:** prorrateo diario, 3 % / 30 por día.
- **Ejemplo:** $ 1.000 con 15 días de atraso → 1000 × 0,03 / 30 × 15 = **$ 15**. Con 45 días → **$ 45**.
- Antes del vencimiento, el interés es 0.

**Por qué acá (GRASP Experto en información):** "la responsabilidad debe asignarse a la clase que posee toda la información necesaria". La expensa conoce su capital, su vencimiento y sus pagos, y nadie de afuera calcula intereses. Esto ya estaba justificado en la Etapa 1.

### 4.4 `registrarPago(fecha, monto, medio)`: imputación

```java
double aIntereses = Math.min(monto, interesPendiente);
double aCapital   = monto - aIntereses;
Pago pago = new Pago(fecha, monto, medio, aIntereses, aCapital);
pagos.add(pago);
```

- **RN02.4:** los pagos se imputan **primero a los intereses** y después al capital.
- **RF03:** se permiten pagos parciales.
- **Ejemplo:** la deuda tiene $ 15 de interés y se paga $ 100. Van $ 15 a intereses y $ 85 a capital, y el capital pendiente queda en $ 915.
- Si el pago supera la deuda o la expensa ya está pagada, tira `PagoInvalidoException`.

**Por qué (GRASP Creador):** "B debería crear A si B contiene o agrega instancias de A". La expensa **contiene** sus pagos, entonces es ella la que hace `new Pago(...)`. Es el mismo caso que el ejemplo de la teoría, donde `Pedido` crea sus `ItemPedido`.

**Por qué (relación UML):** es una **composición** (rombo negro), porque un pago no tiene sentido sin su expensa.

### 4.5 `registrarAjuste(numeroPago, fecha, motivo, monto)`

Se explica en el Paso 6.

### 4.6 `compareTo`: orden natural por período

```java
public class Expensa implements Comparable<Expensa> {
    public int compareTo(Expensa otra) { return this.periodo.compareTo(otra.periodo); }
}
```

**Por qué:**
- **RN02.4:** los pagos se imputan "empezando por el período más antiguo".
- **Teoría (Comparable):** "una clase implementa `Comparable<T>` para definir su orden natural". El orden natural de una expensa es su período, así que `Collections.sort(expensas)` las deja de la más vieja a la más nueva.

### 4.7 Consultas

| Método | Devuelve |
|---|---|
| `getEstado()` | `PENDIENTE`, `PAGADA_PARCIALMENTE` o `PAGADA` |
| `estaPagada()` | `true` si el capital pendiente es 0 |
| `estaVencida(fecha)` | `true` si la fecha es posterior al vencimiento |
| `estaImpaga(fecha)` | Vencida y no pagada (se usa para la morosidad) |
| `getDeuda(fecha)` | Capital + intereses a esa fecha |
| `getPagos()` | **Copia** de la lista de pagos |

**Por qué `getPagos()` devuelve una copia:** es **bajo acoplamiento y copia defensiva** (teoría de clases inmutables). Si alguien hace `expensa.getPagos().remove(...)`, la expensa no se modifica. Así se cumple también RN02.8: los pagos no se eliminan.

### 4.8 `redondear(valor)`

```java
return Math.round(valor * 100) / 100.0;
```

Redondea a centavos para evitar errores de precisión de `double`, por ejemplo que 0,1 + 0,2 dé 0,30000000000000004.

---

## Paso 5 – La clase `Pago`

```java
public final class Pago implements Serializable {
    private final LocalDate fecha;
    private final double importe;
    private final MedioPago medio;
    private final double imputadoAIntereses;
    private final double imputadoACapital;
    private final List<AjustePago> ajustes = new ArrayList<>();

    Pago(...)  // constructor de paquete, sin "public"
```

**Qué se hizo:** un pago guarda fecha, importe, medio, y cuánto fue a intereses y cuánto a capital.

**Por qué:**
- **RF03:** "registrar pagos indicando fecha, importe y medio de pago".
- **RN02.8:** "los pagos registrados no se eliminan".
- **Teoría (clases inmutables)**, aplicando sus reglas:
  1. La clase es `final`.
  2. Los campos son `private final`.
  3. No hay setters.
  4. Copia defensiva: `getAjustes()` devuelve `new ArrayList<>(ajustes)`.
- **Constructor de paquete** (visibilidad `~`): solo las clases del paquete `modelo.expensas` pueden crear un `Pago`. En la práctica, solo `Expensa` (**Creador**), y nadie puede "inventar" un pago desde afuera.
- `getImporteEfectivo()` devuelve el importe original más la suma de los ajustes.

**Aclaración honesta:** los **datos originales** del pago son inmutables, pero la lista de ajustes acepta que se agreguen nuevos. Así se puede corregir un pago sin modificarlo.

---

## Paso 6 – La clase `AjustePago`

```java
public final class AjustePago implements Serializable {
    private final LocalDate fecha;
    private final String motivo;
    private final double importe;   // positivo o negativo
```

**Qué se hizo:** un ajuste corrige un pago **sin tocarlo**.

**Por qué:**
- **RN02.8:** "los errores deben corregirse mediante asientos de ajuste".
- **RF03:** "registrar ajustes [...] conservando el registro original".
- Es **inmutable**, por las mismas reglas que `Pago`.
- **Creador:** lo crea `Pago` (que contiene sus ajustes), pero solo cuando se lo pide `Expensa`, que es la que actualiza la deuda.

**Cómo funciona `Expensa.registrarAjuste`:**

| Ajuste | Significado | Efecto en la deuda |
|---|---|---|
| **Positivo** (+50) | Se había registrado de menos | La deuda baja: primero intereses, después capital (como un pago, RN02.4) |
| **Negativo** (−200) | Se anula parte del pago (ej. una transferencia rebotada) | La deuda sube: vuelve capital y, si no alcanza, intereses |

**Ejemplo real (probado):** un pago de $ 515 se había repartido en $ 335 de intereses y $ 180 de capital. Se registra un ajuste de −200 por "Error de carga":
- El pago **sigue mostrando $ 515** y su importe efectivo pasa a ser $ 315.
- Al capital vuelven $ 180 y a intereses $ 20.
- El ajuste queda visible en el estado de cuenta.

**Validaciones (`AjusteInvalidoException`):** el pago no puede quedar negativo y la expensa no puede quedar con saldo a favor.

---

## Paso 7 – La clase `Notificacion`

```java
public final class Notificacion implements Serializable {
    private final Vecino destinatario;
    private final LocalDate fecha;
    private final String mensaje;
```

**Por qué:**
- **RN02.6:** "cuando un lote pasa a ser moroso, se genera una notificación formal dirigida a su titular".
- Es **inmutable**: una notificación enviada no cambia.
- La crea `Lote` (**Creador**), porque el lote la registra y tiene los datos necesarios: el titular y la deuda.

---

## Paso 8 – Lo que B agrega en `Lote`

`Lote` es una clase de A. B solo hizo dos cosas: cambió `List<Object> expensas` por `List<Expensa> expensas` y agregó los métodos de abajo.

| Método | Qué hace | Regla | Teoría |
|---|---|---|---|
| `emitirExpensa(periodo, presupuesto)` | Crea la expensa del mes con **su** coeficiente. Si ya existe ese período, tira `ExpensaDuplicadaException` | RF03, RN02.1 | **Creador**: el lote "genera" sus expensas (así está en el diagrama) |
| `buscarExpensa(periodo)` | Devuelve la expensa de un mes | — | — |
| `getDeudaTotal(fecha)` | Suma la deuda de todas sus expensas | — | **Experto** |
| `imputarPago(fecha, importe, medio)` | Ordena las expensas y reparte el pago desde la más antigua | **RN02.4** | **Comparable**, `Collections.sort` |
| `registrarAjuste(...)` | Delega en la expensa y actualiza la morosidad | RN02.8 | Bajo acoplamiento |
| `contarPeriodosImpagos(fecha)` | Cuenta las expensas vencidas y no pagadas | RN02.5 | **Experto** |
| `esMoroso(fecha)` | Devuelve si tiene **más de 3** períodos impagos | RN02.5 | **Experto** |
| `actualizarMorosidad(fecha)` | Si **pasa** a moroso, crea **una** notificación; si regulariza, deja de serlo | RN02.6, RN02.7 | **Creador** de `Notificacion` |
| `getNotificaciones()` | Devuelve una **copia** de la lista | — | Copia defensiva |

### `imputarPago` paso a paso

```java
List<Expensa> ordenadas = new ArrayList<>(expensas);
Collections.sort(ordenadas);                       // orden natural: la más vieja primero
for (Expensa expensa : ordenadas) {
    if (expensa.estaPagada()) continue;
    double aplicar = Math.min(restante, expensa.getDeuda(fecha));
    pagosGenerados.add(expensa.registrarPago(fecha, aplicar, medio));
    restante = restante - aplicar;
}
actualizarMorosidad(fecha);
```

1. Valida que el lote tenga deuda y que el pago no la supere (`PagoInvalidoException`).
2. Copia la lista y la ordena por período (**Comparable**).
3. Recorre con **for-each** (teoría de colecciones) y a cada expensa le aplica lo que alcance.
4. Cada expensa imputa primero a intereses y después a capital.
5. Al final revisa la morosidad.

**Ejemplo real (probado):** junio, julio, agosto y septiembre impagos y un pago de $ 6.000 el 15/09:
- Junio: $ 5.485 (intereses $ 485 y capital $ 5.000), queda **PAGADA**.
- Julio: $ 515 (intereses $ 335 y capital $ 180), queda **PAGADA_PARCIALMENTE**.
- El lote pasa de 4 a 3 períodos impagos, así que **deja de ser moroso**.

### Por qué `esMoroso` está en `Lote` (Experto)
La morosidad depende de **todas** las expensas del lote, y el que las conoce es el lote. Si la calculara otra clase, tendría que recorrer las expensas desde afuera, lo que aumenta el acoplamiento.

### Por qué la notificación se genera una sola vez
`Lote` guarda un `boolean moroso`. Solo se notifica cuando pasa de **no moroso → moroso**. Si se consulta varias veces el mismo día, no se repite. Si regulariza y vuelve a caer en mora más adelante, sí se genera una notificación nueva (eso también está testeado).

---

## Paso 9 – `ReporteMorosidad`

```java
public class ReporteMorosidad implements Reporte {
    public List<String> generar() {
        // 1. filtra los lotes morosos
        // 2. los ordena por deuda, de mayor a menor
        morosos.sort(Comparator.comparingDouble((Lote lote) -> lote.getDeudaTotal(fecha)).reversed());
        // 3. arma una línea por lote: número, titular, períodos impagos, deuda
    }
}
```

**Por qué:**
- **RF06:** "generar reportes de morosidad por lote".
- **OCP (Abierto/Cerrado):** es una clase nueva que implementa la interfaz `Reporte`, sin modificar nada de lo que ya existía. Esto ya estaba justificado en la Etapa 1.
- **SRP:** el armado del reporte no es tarea del `Administrador` ni del `Lote`.
- **Polimorfismo:** quien use un `Reporte` llama a `generar()` sin saber de qué tipo es.
- **Teoría (Comparator):** "permite establecer órdenes alternativos sin modificar la clase original". El orden natural del lote es su número, y acá se lo ordena por deuda.

---

## Paso 10 – `RepositorioArchivo` (persistencia)

```java
public class RepositorioArchivo<T extends Identificable> implements Repositorio<T> {
    private final File archivo;
    private final Map<String, T> datos;

    private Map<String, T> cargar() {
        if (!archivo.exists()) return new HashMap<>();
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            return (Map<String, T>) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) { ... }
    }
    private void escribir() {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(archivo))) {
            salida.writeObject(new HashMap<>(datos));
        } catch (IOException e) { ... }
    }
```

**Qué se hizo:** guarda los objetos en `data/lotes.dat` (y también vecinos y reclamos) para que no se pierdan al cerrar el programa.

**Por qué (teoría de archivos):**
- "Las variables viven en la memoria RAM: al cerrar el programa, todos los datos se pierden."
- **Serialización de objetos:** se usan `ObjectOutputStream` y `ObjectInputStream`. Por eso todas las clases del modelo implementan `Serializable` ("si un atributo es un objeto, esa clase también debe ser Serializable").
- **Try-with-resources:** el archivo se cierra solo, aunque haya un error.
- **Cast obligatorio:** `readObject()` devuelve `Object` y hay que convertirlo.
- Se manejan `IOException` y `ClassNotFoundException`, que son **checked**.
- Con `File.exists()` se resuelve el primer uso: si el archivo no existe, el repositorio arranca vacío.

**Por qué (teoría de colecciones):** los datos se guardan en un `Map<String, T>` (`HashMap`), porque se buscan por clave (el número de lote o el documento): "búsqueda rápida por clave".

**Por qué (GRASP y SOLID):**
- **Fabricación pura:** "cuando ninguna clase del dominio es candidata natural para una responsabilidad técnica (persistencia...) se crea una clase de diseño". El ejemplo de la teoría es justamente `RepositorioDeUsuarios`.
- **DIP:** los controladores dependen de la interfaz `Repositorio<T>`, no de esta clase. El único `new RepositorioArchivo` está en `Main`.
- **Variaciones protegidas / Indirección:** si mañana se guarda en una base de datos, se cambia solo esta clase.
- **SRP:** es la **única** clase del sistema que usa `java.io`.

---

## Paso 11 – `ControladorExpensas`

```java
public class ControladorExpensas {
    private final Repositorio<Lote> lotes;
    public ControladorExpensas(Repositorio<Lote> lotes) { this.lotes = lotes; }
```

| Método | Qué hace |
|---|---|
| `emitirExpensas(periodo, presupuesto)` | Emite la expensa del mes **para cada lote** (RF03). Si un lote ya la tenía, lo informa y sigue con los demás |
| `registrarPago(lote, fecha, importe, medio)` | Imputa el pago y devuelve el detalle de cómo se repartió |
| `registrarAjuste(lote, periodo, nroPago, fecha, motivo, importe)` | Registra el ajuste y devuelve la nueva deuda |
| `estadoDeCuenta(lote, fecha)` | Lista las expensas, sus pagos y ajustes, la deuda total y si es moroso |
| `reporteMorosidad(fecha)` | Actualiza la morosidad de todos los lotes y genera el reporte |
| `notificaciones(lote)` | Lista las notificaciones del lote |

**Por qué:**
- **GRASP Controlador:** "el primer objeto que recibe un evento del sistema y coordina la respuesta **sin ejecutar la lógica de negocio directamente**". El controlador no calcula intereses ni imputa pagos: busca el lote, le delega al modelo y guarda con `lotes.actualizar(lote)`.
- **DIP:** recibe el `Repositorio<Lote>` **por constructor** (como `ServicioPago` recibe `MetodoPago` en la teoría). Para los tests se le puede pasar un `RepositorioMemoria`.
- **Bajo acoplamiento:** el controlador no sabe si los datos van a un archivo o a memoria.

---

## Paso 12 – `VistaExpensas`

Es el menú de consola con 6 opciones: emitir, pagar, ajustar, estado de cuenta, reporte y notificaciones.

**Por qué (MVC – Vista):** "muestra información al usuario, captura sus acciones y **no contiene reglas de negocio**". La vista solo lee con `Scanner`, llama al controlador y muestra el resultado.

**Manejo de errores (teoría de excepciones):**

```java
} catch (NumberFormatException e) {                       // específica primero
    System.out.println("Tenés que ingresar un número válido.");
} catch (ReglaNegocioException | IllegalArgumentException e) {   // captura múltiple
    System.out.println(e.getMessage());
} catch (DateTimeParseException e) { ... }
  catch (IllegalStateException e) { ... }
```

- "Las excepciones específicas deben capturarse antes que las generales." `NumberFormatException` es hija de `IllegalArgumentException`, por eso va primero.
- Hay **captura múltiple** con `|`.
- No hay ningún `catch` vacío y se informa al usuario con un mensaje claro.
- El programa **no se corta** ante un error y vuelve al menú.

**La fecha la pide la vista** ("Fecha (AAAA-MM-DD, Enter = hoy)"). Así el modelo no depende del reloj y en las pruebas se pueden simular atrasos de varios meses.

---

## Paso 13 – Integración en `Main` y `VistaPrincipal`

- **`Main`:** crea los repositorios con `RepositorioArchivo` (`data/vecinos.dat`, `data/lotes.dat`, `data/reclamos.dat`), el `ControladorExpensas` y la `VistaExpensas`, y los conecta.
  - Así cumple el rol de la clase `Principal` del ejemplo MVC de la teoría ("arranque de la aplicación: crea y conecta los objetos").
  - Es el **único punto de configuración** del DIP ("la selección del proveedor ocurre en la configuración, no en el negocio").
- **`VistaPrincipal`:** se habilitó la opción **2. Expensas**.
- **`.gitignore`:** excluye `data/` para que los datos de prueba de cada uno no se suban al repo.

---

## Paso 14 – Pruebas unitarias (JUnit 5)

**30 tests, todos en verde.** Cada regla tiene su caso válido y su caso inválido.

| Clase de test | Tests | Qué verifica |
|---|---|---|
| `ExpensaTest` | 18 | Importe = presupuesto × coeficiente, vencimiento el día 10, datos inválidos, interés a 0/15/45 días, pago parcial (intereses primero), pago que solo cubre intereses, pago total, pago mayor a la deuda, orden por período, ajuste que no modifica el pago original, ajustes negativos y positivos, ajustes rechazados, copia defensiva de la lista de pagos |
| `LoteExpensasTest` | 10 | Emisión con el coeficiente del lote, período duplicado, **pago que salda primero el período más antiguo**, pago mayor a la deuda total, lote sin deuda, **3 períodos → no moroso**, **4 períodos → moroso con una sola notificación**, regularización, nueva notificación al volver a caer en mora |
| `ReporteMorosidadTest` | 2 | Solo lista morosos, ordenados por deuda descendente |
| `RepositorioArchivoTest` | 3 | Archivo inexistente arranca vacío; lo guardado se recupera desde otro repositorio; `actualizar` persiste los cambios. El archivo de prueba se borra con `File.delete()` |

**Ejemplo de test:**

```java
@Test void pagoSaldaPrimeroElPeriodoMasAntiguo() throws Exception {
    Lote lote = loteConExpensas(9, 8);   // se emite septiembre antes que agosto, a propósito
    lote.imputarPago(LocalDate.of(2026, 8, 5), 1500, MedioPago.EFECTIVO);
    assertTrue(lote.buscarExpensa(YearMonth.of(2026, 8)).estaPagada());
    assertEquals(500, lote.buscarExpensa(YearMonth.of(2026, 9)).getCapitalPendiente());
}
```

**Por qué las fechas son fijas en los tests:** como el modelo recibe la fecha por parámetro, se puede probar "45 días de atraso" sin esperar 45 días.

---

## Paso 15 – Pruebas funcionales

**Archivo:** `docs/plan-pruebas.md`.

**Por qué (teoría de casos de uso):** los casos de uso sirven "como base para elaborar pruebas funcionales".

Se ejecutaron **12 casos (PF-E01 a PF-E12)** desde la consola, todos OK:

| ID | Caso |
|---|---|
| PF-E01 | Emitir expensa mensual |
| PF-E02 | Emitir dos veces el mismo período (se rechaza) |
| PF-E03 | Estado de cuenta con mora (97 días → interés de $ 485) |
| PF-E04 | Notificación al titular moroso |
| PF-E05 | Reporte de morosidad |
| PF-E06 | Pago parcial imputado desde el período más antiguo |
| PF-E07 | Deja de ser moroso al regularizar |
| PF-E08 | Pago mayor a la deuda (se rechaza) |
| PF-E09 | Ajuste de un pago (el original no cambia) |
| PF-E10 | Ajuste inválido (se rechaza) |
| PF-E11 | Dato numérico inválido (mensaje claro, el programa sigue) |
| PF-E12 | Persistencia: los datos siguen al cerrar y volver a abrir |

Cada caso tiene precondición, pasos, resultado esperado, resultado obtenido y estado.

---

## Resumen: GRASP y SOLID aplicados

| Patrón / principio | Dónde lo aplica B |
|---|---|
| **Experto** | `Expensa.calcularInteres()`, `Lote.esMoroso()`, `Lote.getDeudaTotal()` |
| **Creador** | `Lote` → `Expensa` → `Pago` → `AjustePago`; `Lote` → `Notificacion` |
| **Controlador** | `ControladorExpensas` |
| **Bajo acoplamiento** | Atributos `private`, copias defensivas, el controlador depende de `Repositorio<T>` |
| **Alta cohesión** | Vista, controlador, modelo y repositorio con una responsabilidad cada uno |
| **Polimorfismo** | `ReporteMorosidad` usado como `Reporte` |
| **Fabricación pura** | `RepositorioArchivo` |
| **Indirección / Variaciones protegidas** | La interfaz `Repositorio<T>` entre el controlador y el archivo |
| **SRP** | El reporte en su propia clase; la persistencia en su propia clase |
| **OCP** | Reporte nuevo sin modificar código existente |
| **DIP** | Repositorio inyectado por constructor; `new RepositorioArchivo` solo en `Main` |

## Resumen: reglas de negocio cubiertas

| Regla | Dónde | Test |
|---|---|---|
| RN02.1 Importe = presupuesto × coeficiente | Constructor de `Expensa` | `importeEsPresupuestoPorCoeficiente` |
| RN02.2 Vence el día 10 | Constructor de `Expensa` | `venceElDiaDiezDelMes` |
| RN02.3 3 % mensual sobre el saldo | `Expensa.calcularInteres` | `interesA15Dias...`, `interesA45Dias...` |
| RN02.4 Intereses primero, el período más antiguo primero | `Expensa.registrarPago`, `Lote.imputarPago` | `pagoParcialCubrePrimeroLosIntereses`, `pagoSaldaPrimeroElPeriodoMasAntiguo` |
| RN02.5 Más de 3 períodos → moroso | `Lote.esMoroso` | `conTres...NoEsMoroso`, `conCuatro...EsMoroso` |
| RN02.6 Notificación al titular | `Lote.actualizarMorosidad` | `...SeNotificaUnaVez` |
| RN02.7 Regularizar quita la mora | `Lote.actualizarMorosidad` | `alRegularizarLaDeudaDejaDeSerMoroso` |
| RN02.8 Pagos no se borran, se ajustan | `Pago` (inmutable), `AjustePago` | `ajusteNoModificaElPagoOriginal` |

---

## ⚠️ Lo que no sale de la teoría

| Qué | Dónde | Por qué se usó |
|---|---|---|
| **JUnit 5** (`@Test`, `assertEquals`, `assertThrows`, `@AfterEach`) | Tests | La consigna pide pruebas unitarias y la teoría no da ninguna herramienta de testing |
| **`YearMonth`, `LocalDate`, `ChronoUnit`** (API `java.time`) | `Expensa`, `Lote`, vista | Hacen falta para los meses, el día 10 y los días de mora. La teoría solo nombra `LocalDate` como ejemplo de clase inmutable |
| **`Optional`** | Interfaz `Repositorio` (la definió A) | El controlador usa `buscarPorId(...).orElseThrow(...)` |
| **Lambda en el `Comparator`** | `ReporteMorosidad` | La teoría usa `Comparator.comparingDouble(Alumno::getPromedio)`. Acá hace falta pasar la fecha, así que se escribe `(Lote l) -> l.getDeudaTotal(fecha)`. Las lambdas aparecen en la teoría solo en Swing |
| **`Math.round` para centavos** | `Expensa.redondear` | Para evitar errores de precisión de `double` |
| **`IllegalStateException` envolviendo la `IOException`** | `RepositorioArchivo` | La interfaz `Repositorio` no declara `throws`, así que el error de archivo se informa con una excepción no verificada y un mensaje claro |
| **Prorrateo diario del interés** | `Expensa` | No es un tema de la materia: es un supuesto de negocio (supuesto 7 de la Etapa 1) |

---

## Decisiones de diseño para el informe

1. **La expensa la emite `Lote.emitirExpensa()`** y no `Expensa.emitir()`, como decía el plan. Es por el patrón **Creador**: el lote contiene y "genera" sus expensas (así figura en el diagrama de clases).
2. **"Vencida" no es un estado guardado**, porque depende de la fecha de consulta. Se calcula con `estaVencida(fecha)`.
3. **`Pago` tiene los datos originales inmutables**, pero su lista de ajustes acepta agregados. Así se corrige sin modificar ni borrar (RN02.8).
4. **Interés simple con prorrateo diario:** 3 % / 30 por día, sobre el capital impago, desde el día siguiente al vencimiento.
5. **Ajustes:** uno positivo se imputa como un pago (intereses y después capital); uno negativo devuelve capital y, si no alcanza, intereses.
6. **Las expensas se guardan dentro del lote** (en `lotes.dat`), no en un repositorio aparte. Así un pago y su lote nunca quedan desincronizados.
7. **Un pago mayor a la deuda se rechaza.** No se manejan saldos a favor.
8. **El repositorio escribe el archivo en cada `guardar`/`actualizar`**, en vez de solo al salir, para no perder datos si el programa se cierra de golpe.
