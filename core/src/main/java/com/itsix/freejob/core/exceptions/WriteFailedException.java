package com.itsix.freejob.core.exceptions;

import java.sql.SQLException;

public class WriteFailedException extends FreeJobException {

    private static final long serialVersionUID = 0L;

    public WriteFailedException(String message, SQLException cause) {
        super(message, cause);
    }
}
