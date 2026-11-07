package modelo.reclamos;

public enum EstadoReclamo {
    ABIERTO, ASIGNADO, EN_CURSO, RESUELTO, CERRADO;

    // RN01.4: los reclamos solo avanzan al estado siguiente.
    public EstadoReclamo siguiente() {
        switch (this) {
            case ABIERTO: return ASIGNADO;
            case ASIGNADO: return EN_CURSO;
            case EN_CURSO: return RESUELTO;
            case RESUELTO: return CERRADO;
            default: return CERRADO;
        }
    }
}
