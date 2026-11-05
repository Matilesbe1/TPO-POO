package modelo.reclamos;

public enum EstadoReclamo {
    ABIERTO, EN_CURSO, RESUELTO, CERRADO;

    public EstadoReclamo siguiente() {
        switch (this) {
            case ABIERTO: return EN_CURSO;
            case EN_CURSO: return RESUELTO;
            case RESUELTO: return CERRADO;
            default: return CERRADO;
        }
    }
}
