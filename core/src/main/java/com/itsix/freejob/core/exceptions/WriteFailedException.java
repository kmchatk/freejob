package com.itsix.freejob.core.exceptions;

import java.sql.SQLException;

public class WriteFailedException extends FreeJobException {

    public WriteFailedException(SQLException e) {
        super(e.getMessage(), e);
    }
}
