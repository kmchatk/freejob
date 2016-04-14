package com.itsix.freejob.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import com.itsix.freejob.api.Api;

public abstract class OsgiRestResource {

    protected Api getApi() {
        return (Api) servletContext.getAttribute(Api.class.getName());
    }

    @Context
    private ServletContext servletContext;

}
