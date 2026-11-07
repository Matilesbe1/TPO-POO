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
| `ReclamoTest` (13 tests) | Prioridad según el tipo, solo titular o residente presenta, estado inicial e historial, asignación (pasa a ASIGNADO), solo se avanza al estado siguiente, único responsable, límite de 5 reclamos activos, reapertura a los 4 y a los 6 días, no reabrir si no está resuelto, cierre automático, escalado a las 23 h y a las 25 h | ✅ 13/13 |
| `EquipoTest` (11 tests) | Cámara → prioridad alta, luminaria → normal, más de 3 luminarias con falla en el sector → alta, polimorfismo sin `instanceof`, no se registra otra falla con una orden abierta, ciclo completo de la orden, cierre incompleto rechazado, aviso de cámara a las 47 h y a las 49 h, la luminaria no avisa, reporte de fuera de servicio | ✅ 11/11 |
| `AccesoTest` (11 tests) | Ingreso válido, sin autorización, fuera de la franja o de la fecha, ingreso duplicado, nuevo ingreso después del egreso, egreso sin ingreso, recurrente de 30 días aceptada y de 31 rechazada, visitante recurrente rechazado, solo titular o residente autoriza, vigencia por fecha y franja | ✅ 11/11 |

**Corrida del 07/11/2026:** 70 tests encontrados, 70 exitosos, 0 fallidos.

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
| PF-R01 | Presentar reclamo sin estar autorizado | Vecino `999` dado de alta, sin relación con el lote 10 | Reclamos → 3 → `999` → lote `10` → convivencia | Se rechaza (RN01.10) | `El vecino no es titular ni residente autorizado para ese lote.` | ✅ OK |
| PF-R02 | Presentar reclamo | Lote 10 con titular `30111222` | Reclamos → 3 → `30111222` → `10` → 1 (seguridad) → "Camara rota en la entrada" → `2026-11-01T10:00` | Se crea en estado ABIERTO con prioridad ALTA | `Reclamo creado. ID: 10-2026-11-01T10:00`; en el reporte: `ALTA \| ABIERTO` | ✅ OK |
| PF-R03 | Escalar reclamo de prioridad alta | PF-R02 sin atender | Reclamos → 10 → `2026-11-02T11:00` (25 h después) | Se escala al administrador | `Escalado al administrador: 10-2026-11-01T10:00`; en el reporte aparece `ESCALADO` | ✅ OK |
| PF-R04 | Saltear un estado | PF-R02 en ABIERTO | Reclamos → 5 → id → 1 (EN_CURSO) | Se rechaza: solo puede pasar a ASIGNADO (RN01.4) | `El reclamo solo puede avanzar al estado siguiente.` | ✅ OK |
| PF-R05 | Asignar reclamo | PF-R02 | Reclamos → 4 → id → `E1` Carlos | Pasa a ASIGNADO con responsable | `Reclamo asignado.` | ✅ OK |
| PF-R06 | Avanzar hasta resuelto | PF-R05 | Reclamos → 5 → 1 (EN_CURSO), después 5 → 2 (RESUELTO) el `2026-11-03T10:00` | Avanza en orden | `Estado actualizado.` (dos veces) | ✅ OK |
| PF-R07 | Reabrir reclamo | PF-R06 | Reclamos → 6 → id → `2026-11-05T10:00` (2 días después) | Vuelve a ABIERTO (RN01.5) | `Reclamo reabierto.` | ✅ OK |
| PF-R08 | Agregar residente y comentar | Vecino `999` dado de alta | Reclamos → 8 → `10` → `999`; Reclamos → 9 → id → "Gracias" | Se agregan el residente y el comentario | `Residente agregado.` y `Comentario agregado.` | ✅ OK |

### Módulo Equipos y Accesos (Persona C)

| ID | Caso de uso | Precondición | Pasos | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|---|
| PF-C01 | Registrar falla de cámara | Cámara `C1` en sector Norte | Equipos → 3 → `C1` → "Sin imagen" → guardia `G1` → `2026-11-01T10:00` | Orden de prioridad ALTA (RN03.2) | `Orden C1-1 generada con prioridad ALTA.` | ✅ OK |
| PF-C02 | Falla de luminarias en el mismo sector | Luminarias `L1` a `L4` en sector Norte | Equipos → 3 sobre L1, L2, L3 y L4 | L1 a L3: NORMAL; L4 (más de 3 en el sector): ALTA (RN03.3 y RN03.4) | `L1-1/L2-1/L3-1 ... NORMAL` y `Orden L4-1 generada con prioridad ALTA.` | ✅ OK |
| PF-C03 | Aviso de cámara más de 48 h sin funcionar | PF-C01 | Equipos → 8 → `2026-11-02T09:00`, y después `2026-11-03T11:00` | A las 23 h no avisa; a las 49 h sí (RN03.5) | `No hay cámaras para informar.` / `AVISO: Cámara C1 (sector Norte) sin funcionar desde 2026-11-01T10:00` | ✅ OK |
| PF-C04 | Reporte de equipos fuera de servicio | PF-C01 y PF-C02 | Equipos → 9 | Lista los 5 equipos con falla, por sector | 5 líneas `Sector Norte \| ... \| CON_FALLA` | ✅ OK |
| PF-C05 | Asignar orden | PF-C01 | Equipos → 4 → `C1` → empleado `E1` Carlos | Orden asignada, el equipo pasa a EN_REPARACION | `Orden C1-1 asignada a Carlos.` | ✅ OK |
| PF-C06 | Cerrar orden sin descripción | PF-C05 | Equipos → 5 → `C1` → descripción vacía | Se rechaza (RN03.6) | `Para cerrar: fecha, técnico y descripción son obligatorios.` | ✅ OK |
| PF-C07 | Cerrar orden de mantenimiento | PF-C05 | Equipos → 5 → `C1` → "Cambio de fuente" → `2026-11-03` | Orden cerrada y el equipo vuelve a OPERATIVO | `Orden C1-1 cerrada. El equipo vuelve a estar OPERATIVO.` | ✅ OK |
| PF-C08 | Historial de mantenimiento | PF-C07 | Equipos → 7 → `C1` | Muestra la orden con técnico, fecha de cierre y reparación (RF04) | `Orden C1-1 \| ALTA \| ... \| Técnico: Carlos \| Cierre 2026-11-03: Cambio de fuente` | ✅ OK |
| PF-C09 | Autorizar visita | Lote 10 con titular `30111222` | Accesos → 1 → `30111222` → `10` → visitante Juan Gomez `40111222` → `2026-11-10` de `09:00` a `18:00` | Autorización registrada (RN04.2) | `Autorización registrada para Juan Gomez (Visitante): del 2026-11-10 al 2026-11-10, de 09:00 a 18:00.` | ✅ OK |
| PF-C10 | Registrar ingreso | PF-C09 | Accesos → 2 → `40111222` → `10` → `2026-11-10T10:00` | Se registra el ingreso | `Ingreso registrado: Juan Gomez al lote 10 a las 2026-11-10T10:00.` | ✅ OK |
| PF-C11 | Ingreso sin egreso previo | PF-C10 | Accesos → 2 → `40111222` → `2026-11-10T11:00` | Se rechaza (RN04.6) | `Ingreso denegado: 40111222 tiene un ingreso sin egreso registrado.` | ✅ OK |
| PF-C12 | Registrar egreso | PF-C10 | Accesos → 3 → `40111222` → `2026-11-10T12:00` | Se registra el egreso (RN04.5) | `Egreso registrado: Juan Gomez a las 2026-11-10T12:00.` | ✅ OK |
| PF-C13 | Ingreso fuera de la franja horaria | PF-C12 | Accesos → 2 → `40111222` → `2026-11-10T20:00` | Se rechaza (RN04.2 y RN04.3) | `Ingreso denegado: 40111222 no tiene una autorización vigente para el lote 10.` | ✅ OK |
| PF-C14 | Ingreso sin autorización | — | Accesos → 2 → `55555555` → `10` | Se rechaza (RN04.1) | `Ingreso denegado: 55555555 no tiene una autorización vigente para el lote 10.` | ✅ OK |
| PF-C15 | Proveedor recurrente de más de 30 días | Lote 10 | Accesos → 1 → proveedor Jardineria SRL, recurrente del `2026-11-10` al `2026-12-20` | Se rechaza (RN04.4) | `Una autorización recurrente no puede superar los 30 días.` | ✅ OK |
| PF-C16 | Proveedor recurrente de 30 días | Lote 10 | Ídem, del `2026-11-10` al `2026-12-09`; después ingreso el `2026-11-25T10:00` | Se acepta y el ingreso dentro del período se registra | `Autorización registrada ... RECURRENTE` e `Ingreso registrado: Jardineria SRL ...` | ✅ OK |

## 3. Criterios de cálculo usados en las pruebas

- **Interés:** 3 % mensual con prorrateo diario (3 % / 30 por día), sobre el capital impago, desde el día siguiente al vencimiento. Es un supuesto nuestro (supuesto 7 de la Etapa 1).
- **Ajustes:** un ajuste positivo reconoce un pago mayor y se imputa como un pago (intereses y después capital). Uno negativo anula parte de un pago: vuelve a sumar capital y, si no alcanza, intereses. El pago original nunca se modifica.
