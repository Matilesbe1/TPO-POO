# Análisis del enunciado de la Propuesta A: 

## 1. Actores del sistema

| Actor |                                | Descripción |
|---|---|
| **Administrador** |            | Gestiona los reclamos, asigna responsables, genera órdenes de mantenimiento y emite reportes. Coordina al personal del barrio. |
| **Vecino** |                   | Puede ser titular de un lote o residente autorizado. Presenta reclamos, consulta su seguimiento y autoriza visitas. |
| **Guardia de seguridad** |     | Registra fallas de equipos, consulta autorizaciones y registra los ingresos y egresos de visitas y proveedores. |
| **Empleado de mantenimiento**| | Recibe reclamos y órdenes de mantenimiento, realiza reparaciones y registra el trabajo efectuado. |
| **Visitante** |                | Ingresa al barrio con autorización previa de un vecino. |
| **Proveedor** |                | Ingresa al barrio para prestar servicios, con autorización previa. Puede contar con una autorización recurrente. |

**Aclaración:** el administrador, los guardias y los empleados de mantenimiento son roles diferenciados del personal del barrio. Un vecino puede ser titular de uno o varios lotes o residir en un lote ajeno.

## 2. Requerimientos funcionales

### RF01. Gestión de vecinos y lotes
- Registrar los datos personales de los titulares y residentes.
- Registrar los lotes, incluyendo número, superficie y titular.
- Permitir que una persona sea titular de más de un lote.
- Registrar a los residentes de cada lote.

### RF02. Gestión de reclamos y solicitudes
- Registrar reclamos y solicitudes de los vecinos.
- Registrar el tipo, la descripción, la fecha, la prioridad y el estado de cada reclamo.
- Asociar cada reclamo con el lote correspondiente y la persona que lo presenta.
- Asignar un empleado responsable a cada reclamo.
- Consultar el estado y el seguimiento de los reclamos.
- Registrar el historial de cambios de estado y sus observaciones.
- Permitir comentarios del vecino y del personal.
- Permitir la reapertura de reclamos resueltos dentro del plazo establecido.
- Escalar al administrador los reclamos de prioridad alta que no hayan sido atendidos a tiempo.

### RF03. Gestión de expensas y pagos
- Emitir una expensa mensual por cada lote.
- Calcular el importe según el presupuesto mensual y el coeficiente de participación del lote.
- Registrar la fecha de vencimiento y el estado de cada expensa.
- Registrar pagos indicando fecha, importe y medio de pago.
- Permitir pagos parciales.
- Calcular los intereses por mora sobre el saldo impago.
- Imputar los pagos primero a los intereses y después al capital, comenzando por los períodos más antiguos.
- Identificar lotes morosos y generar notificaciones formales a sus titulares.
- Registrar ajustes cuando se detecten errores en pagos previamente registrados, conservando el registro original.

### RF04. Gestión de luminarias y cámaras
- Registrar las luminarias y cámaras instaladas en el barrio.
- Almacenar el código, sector, fecha de instalación y estado de cada equipo.
- Registrar las fallas detectadas por vecinos o guardias.
- Generar órdenes de mantenimiento para los equipos que presenten fallas.
- Asignar las órdenes a empleados de mantenimiento.
- Registrar la prioridad, la descripción del trabajo, la fecha de apertura y la fecha de cierre.
- Registrar el técnico responsable y la descripción de la reparación realizada.
- Consultar el estado actual y el historial de mantenimiento de cada equipo.
- Informar al administrador cuando una cámara permanezca sin funcionar durante más de 48 horas.

### RF05. Gestión de visitas y proveedores
- Registrar autorizaciones de ingreso emitidas por los vecinos.
- Registrar el nombre y documento del visitante, el lote de destino y la vigencia de la autorización.
- Permitir autorizaciones recurrentes para proveedores habituales.
- Validar la autorización antes de habilitar el ingreso.
- Registrar la hora de ingreso y la hora de egreso de cada persona.
- Impedir un nuevo ingreso de la misma persona mientras exista un ingreso anterior sin egreso registrado.

### RF06. Generación de reportes
- Generar reportes de reclamos pendientes.
- Generar reportes de morosidad por lote.
- Generar reportes de equipos fuera de servicio.

## 3. Reglas de negocio

### RN01. Reclamos y solicitudes
1. Cada reclamo debe tener un tipo: seguridad, mantenimiento, convivencia o administrativo.
2. La prioridad depende del tipo: seguridad tiene prioridad alta, mantenimiento media y convivencia y administrativo baja.
3. Los estados permitidos son abierto, asignado, en curso, resuelto y cerrado.
4. Los reclamos solo pueden avanzar al estado siguiente, respetando el orden establecido.
5. El vecino puede reabrir un reclamo resuelto dentro de los cinco días establecidos.
6. Si transcurre el plazo de cinco días sin respuesta, el reclamo se cierra.
7. Cada reclamo debe tener un único empleado responsable asignado.
8. Un empleado no puede tener más de cinco reclamos en curso simultáneamente.
9. Un reclamo de prioridad alta que no sea atendido dentro de las primeras 24 horas debe escalarse al administrador.
10. Solo el titular del lote o una persona autorizada por él puede presentar reclamos.

### RN02. Expensas y pagos
1. La expensa mensual se calcula multiplicando el presupuesto del mes por el coeficiente de participación del lote.
2. La fecha de vencimiento de las expensas es el día 10 de cada mes.
3. Desde el vencimiento se aplica un interés punitorio del 3 % mensual sobre el saldo impago.
4. Los pagos parciales se imputan primero a los intereses y después al capital, empezando por el período más antiguo.
5. Un lote con más de tres períodos impagos se considera moroso.
6. Cuando un lote pasa a ser moroso, se genera una notificación formal dirigida a su titular.
7. La condición de moroso se elimina cuando se regulariza la deuda.
8. Los pagos registrados no se eliminan. Los errores deben corregirse mediante asientos de ajuste.

### RN03. Luminarias y cámaras
1. Cada equipo debe tener uno de estos estados: operativo, con falla, en reparación o fuera de servicio.
2. Una falla en una cámara genera una orden de mantenimiento de prioridad alta.
3. Una falla en una luminaria genera una orden de prioridad normal.
4. Si fallan más de tres luminarias del mismo sector, la prioridad de mantenimiento pasa a ser alta.
5. Si una cámara permanece sin funcionar durante más de 48 horas, se debe informar al administrador.
6. Una orden de mantenimiento no puede cerrarse sin registrar la fecha de cierre, el técnico responsable y la descripción de la reparación.

### RN04. Ingreso de visitas y proveedores
1. Toda visita requiere una autorización previa del vecino.
2. La autorización debe indicar la fecha y la franja horaria durante la cual se permite el ingreso.
3. Sin una autorización válida, el personal de seguridad no puede habilitar el ingreso.
4. Los proveedores habituales pueden tener autorizaciones recurrentes por un período máximo de 30 días.
5. Cada ingreso debe tener su correspondiente registro de egreso.
6. Una misma persona no puede ingresar nuevamente mientras no se haya registrado su salida anterior.

## 4. Supuestos adoptados

1. **Identificación de personas:** el documento será el identificador único de cada persona registrada.
2. **Titularidad de lotes:** cada lote tendrá exactamente un titular, pero una persona podrá ser titular de varios lotes.
3. **Residentes:** los residentes podrán registrarse independientemente del titular. Para presentar reclamos en nombre del titular deberán contar con autorización.
4. **Asignación de reclamos:** el administrador será responsable de asignar cada reclamo a un empleado habilitado. La asignación deberá respetar el límite de cinco reclamos en curso por empleado.
5. **Estados de los reclamos:** se considerará la secuencia abierto → asignado → en curso → resuelto → cerrado. La reapertura será una excepción permitida por la regla de los cinco días.
6. **Plazo de reapertura:** se considerarán cinco días corridos desde la resolución del reclamo. Si el vecino no responde dentro de ese plazo, el reclamo se cerrará automáticamente. Se deberá confirmar con el docente si esta interpretación coincide con el criterio esperado.
7. **Cálculo de intereses:** se tomará el 3 % mensual como tasa de referencia sobre el saldo impago. El método exacto de prorrateo para períodos inferiores a un mes deberá definirse como criterio del sistema.
8. **Regularización de deudas:** un lote dejará de considerarse moroso cuando no tenga períodos impagos pendientes.
9. **Órdenes de mantenimiento:** cada orden tendrá un empleado responsable y solo podrá cerrarse cuando se haya completado la información de la reparación.
10. **Autorizaciones recurrentes:** se considerará que la vigencia de una autorización recurrente no puede superar los 30 días. Los ingresos deberán validarse según su fecha y franja horaria.
11. **Identificación de visitantes:** se utilizará el documento como identificador para controlar que no existan dos ingresos simultáneos sin egreso registrado para una misma persona.
12. **Reportes:** los reportes serán generados a partir de la información registrada en el sistema y estarán disponibles para el administrador.
13. **Coeficiente de participación:** se calculará a partir de la superficie del lote y de un criterio de distribución del presupuesto definido por la administración.

## 5. Conclusión

El sistema de gestión del barrio cerrado Los Aromos permitirá centralizar la información de vecinos, lotes, reclamos, expensas, pagos, equipos, mantenimiento y accesos. Los requerimientos funcionales describen las operaciones que debe ofrecer el sistema, mientras que las reglas de negocio establecen las condiciones obligatorias que deben cumplirse.
Los supuestos documentados permitirán completar los aspectos ambiguos del enunciado y servirán como base para elaborar el diagrama de clases y los siguientes modelos del sistema.
