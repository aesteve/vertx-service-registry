package io.vertx.serviceregistry.engines.react.exceptions;

public class JSXTransformException extends Throwable {

    private static final long serialVersionUID = 4652989081992374552L;

    public enum Phase {
        INIT, TRANSFORM
    }

    private Phase phase;
    private Throwable cause;

    public JSXTransformException(Phase phase, Throwable cause) {
        super(cause);
        this.phase = phase;
    }

    @Override
    public String getMessage() {
        String msg = "Error during JSX transformation [phase : " + this.phase + "].";
        if (cause != null)
            msg += "(" + cause.getMessage() + ")";
        return msg;
    }
}
