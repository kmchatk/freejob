package com.itsix.freejob.core.exceptions;

import java.sql.SQLException;

public class ReadFailedException extends FreeJobException {

    private static final long serialVersionUID = 0L;

    public ReadFailedException(SQLException cause) {
        super("Read failed", cause);
    }
}
