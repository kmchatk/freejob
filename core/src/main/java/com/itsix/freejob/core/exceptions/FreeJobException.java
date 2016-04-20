package com.itsix.freejob.core.exceptions;

public abstract class FreeJobException extends Exception {

    private static final long serialVersionUID = 0L;

    public FreeJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public FreeJobException(String message) {
        super(message);
    }

}
