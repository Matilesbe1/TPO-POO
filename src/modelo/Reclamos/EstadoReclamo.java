package modelo;

import excepciones.EstadoReclamoInvalidoException;

public enum EstadoReclamo {
    ABIERTO {
        @Override
        public EstadoReclamo siguiente() { return ASIGNADO; }
    },
    ASIGNADO {
        @Override
        public EstadoReclamo siguiente() { return EN_CURSO; }
    },
    EN_CURSO {
        @Override
        public EstadoReclamo siguiente() { return RESUELTO; }
    },
    RESUELTO {
        @Override
        public EstadoReclamo siguiente() { return CERRADO; }
    },
    CERRADO {
        @Override
        public EstadoReclamo siguiente() {
            throw new IllegalStateException("El reclamo ya se encuentra cerrado.");
        }
    };

    public abstract EstadoReclamo siguiente();
}