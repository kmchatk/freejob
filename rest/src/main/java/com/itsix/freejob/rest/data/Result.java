package com.itsix.freejob.rest.data;

public class Result {

    private Object data;
    private boolean success = true;
    private String message;

    private Result(Object data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
    }

    public static Result ok() {
        return new Result(null, null, true);
    }

    public static Result ok(Object data) {
        return new Result(data, null, true);
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public static Result error(String message) {
        return new Result(null, message, false);
    }

}
