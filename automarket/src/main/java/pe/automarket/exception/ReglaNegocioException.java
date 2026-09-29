package pe.automarket.exception;

/** Se lanza cuando una operación viola una regla de negocio (RN-xx) o una validación. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
}
