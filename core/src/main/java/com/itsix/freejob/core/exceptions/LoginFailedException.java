package com.itsix.freejob.core.exceptions;

public class LoginFailedException extends FreeJobException {

    private static final long serialVersionUID = 0L;

    public LoginFailedException() {
        super("Username and password do not match or you don't have an account yet.");
    }

}
