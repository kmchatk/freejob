package com.itsix.freejob.core.exceptions;

public class NotFoundException extends FreeJobException {

    private static final long serialVersionUID = 0L;

    public NotFoundException() {
        super("Not found");
    }

}
