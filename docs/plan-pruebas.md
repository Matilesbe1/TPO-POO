# Plan de pruebas – Barrio Los Aromos

Las pruebas funcionales salen de los casos de uso del diagrama de la Etapa 1. Cada caso se ejecuta desde la consola (`Main`) y se anota el resultado obtenido.

## 1. Pruebas unitarias (JUnit 5)

| Clase de test | Qué verifica | Resultado |
|---|---|---|
| `ExpensaTest` (18 tests) | Importe = presupuesto × coeficiente, vencimiento el día 10, interés a 0/15/45 días, imputación a intereses y después a capital, pagos inválidos, orden natural por período, ajustes (positivos, negativos y rechazados), copia defensiva de la lista de pagos | ✅ 18/18 |
| `LoteExpensasTest` (10 tests) | Emisión con el coeficiente del lote, período duplicado, pago que salda primero el período más antiguo, pago mayor a la deuda, morosidad con 3 y 4 períodos impagos, una sola notificación, regularización y nueva notificación | ✅ 10/10 |
| `ReporteMorosidadTest` (2 tests) | Solo lista morosos, ordenados por deuda descendente (`Comparator`) | ✅ 2/2 |
| `RepositorioArchivoTest` (3 tests) | Archivo inexistente arranca vacío; lo guardado se recupera desde otro repositorio; `actualizar` persiste los cambios | ✅ 3/3 |
| `VecinoLoteTest` (2 tests) | `equals`/`hashCode` de `Vecino` y `Lote` | ✅ 2/2 |
| `RepositorioMemoriaTest` (1 test) | Guardar, buscar, listar y actualizar | ✅ 1/1 |

**Corrida del 06/11/2026:** 35 tests encontrados, 35 exitosos, 0 fallidos.

## 2. Pruebas funcionales por caso de uso

**Datos de partida para expensas:** vecino `30111222` (Ana Perez), lote `10` con superficie 500 y coeficiente 0.05. Presupuesto mensual de $ 100.000, así que cada expensa es de $ 5.000.

### Módulo Expensas (Persona B)

| ID | Caso de uso | Precondición | Pasos | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|---|
| PF-E01 | Emitir expensa mensual | Lote 10 registrado | Expensas → 1 → período `2026-06` → presupuesto `100000` | Se emite $ 5.000 con vencimiento 2026-06-10 | `Lote 10: $ 5000.0 (vence el 2026-06-10)` | ✅ OK |
| PF-E02 | Emitir dos veces el mismo período | Expensa 2026-09 emitida | Expensas → 1 → `2026-09` → `100000` | Se rechaza (una expensa por período) | `El lote 10 ya tiene la expensa de 2026-09.` | ✅ OK |
| PF-E03 | Consultar estado de cuenta con mora | Expensas de 06, 07, 08 y 09/2026 sin pagar | Expensas → 4 → lote `10` → fecha `2026-09-15` | Junio: 97 días de mora → interés $ 485. Deuda total $ 21.025. 4 períodos impagos → MOROSO | Interés de junio $ 485.0, deuda total $ 21025.0, `Períodos impagos: 4 \| MOROSO` | ✅ OK |
| PF-E04 | Notificar al titular moroso | PF-E03 | Expensas → 6 → lote `10` | Una notificación formal para Ana Perez | `2026-09-15 \| Para: Ana Perez \| El lote 10 registra 4 períodos impagos y pasa a ser moroso...` | ✅ OK |
| PF-E05 | Reporte de morosidad | PF-E03 | Expensas → 5 → fecha `2026-09-15` | El lote 10 figura con 4 períodos y deuda $ 21.025 | `Lote 10 \| Titular: Ana Perez (30111222) \| Períodos impagos: 4 \| Deuda: $ 21025.0` | ✅ OK |
| PF-E06 | Registrar pago parcial | PF-E03 | Expensas → 2 → lote `10` → `6000` → transferencia → `2026-09-15` | Primero se salda junio (intereses $ 485 + capital $ 5.000). El resto ($ 515) va a julio: $ 335 de intereses y $ 180 de capital | `2026-06: $ 5485.0 (intereses $ 485.0, capital $ 5000.0)` y `2026-07: $ 515.0 (intereses $ 335.0, capital $ 180.0)` | ✅ OK |
| PF-E07 | Dejar de ser moroso al regularizar | PF-E06 | Expensas → 4 → lote `10` → `2026-09-15` | Quedan 3 períodos impagos y el lote ya no es moroso | `Deuda total: $ 15025.0 \| Períodos impagos: 3` (sin MOROSO) | ✅ OK |
| PF-E08 | Pago mayor a la deuda | PF-E06 | Expensas → 2 → lote `10` → `999999` | Se rechaza sin registrar nada | `El pago supera la deuda total del lote ($ 15039.82).` | ✅ OK |
| PF-E09 | Ajuste de un pago | PF-E06 | Expensas → 3 → lote `10` → `2026-07` → pago `1` → `-200` → "Error de carga" → `2026-09-16` | El pago original sigue en $ 515 con efectivo $ 315. Vuelven $ 180 al capital y $ 20 a intereses | Pago `$ 515.0 (efectivo $ 315.0)`, `Ajuste ... $ -200.0: Error de carga`, capital de julio $ 5000.0 | ✅ OK |
| PF-E10 | Ajuste inválido | PF-E06 | Expensas → 3 → lote `10` → `2026-06` → pago `1` → `-6000` | Se rechaza porque el pago quedaría negativo | `El ajuste deja el pago con un importe negativo.` | ✅ OK |
| PF-E11 | Dato numérico inválido | — | Expensas → 2 → lote `10` → importe `abc` | Mensaje claro y el programa sigue | `Tenés que ingresar un número válido. Volvé a intentarlo.` | ✅ OK |
| PF-E12 | Persistencia entre ejecuciones | PF-E06 | Salir, volver a abrir la app → Expensas → 4 → lote `10` | Los datos y pagos siguen cargados (`data/lotes.dat`) | Se ven las 4 expensas y los pagos del 2026-09-15 | ✅ OK |

### Módulo Reclamos (Persona A)

| ID | Caso de uso | Precondición | Pasos | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|---|
| PF-R01 | Presentar reclamo | | | | | ⏳ Pendiente |
| PF-R02 | Asignar reclamo | | | | | ⏳ Pendiente |
| PF-R03 | Reabrir reclamo | | | | | ⏳ Pendiente |

### Módulo Equipos y Accesos (Persona C)

| ID | Caso de uso | Precondición | Pasos | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|---|
| PF-C01 | Registrar falla de cámara | | | | | ⏳ Pendiente |
| PF-C02 | Cerrar orden de mantenimiento | | | | | ⏳ Pendiente |
| PF-C03 | Autorizar visita | | | | | ⏳ Pendiente |
| PF-C04 | Registrar ingreso | | | | | ⏳ Pendiente |
| PF-C05 | Registrar egreso | | | | | ⏳ Pendiente |

## 3. Criterios de cálculo usados en las pruebas

- **Interés:** 3 % mensual con prorrateo diario (3 % / 30 por día), sobre el capital impago, desde el día siguiente al vencimiento. Es un supuesto nuestro (supuesto 7 de la Etapa 1).
- **Ajustes:** un ajuste positivo reconoce un pago mayor y se imputa como un pago (intereses y después capital). Uno negativo anula parte de un pago: vuelve a sumar capital y, si no alcanza, intereses. El pago original nunca se modifica.
