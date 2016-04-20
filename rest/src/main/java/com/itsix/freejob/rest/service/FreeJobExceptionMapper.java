package com.itsix.freejob.rest.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.rest.data.Result;

public class FreeJobExceptionMapper
        implements ExceptionMapper<FreeJobException> {

    @Override
    public Response toResponse(FreeJobException e) {
        return Response.ok(Result.error(e.getMessage())).build();
    }

}
