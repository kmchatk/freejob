package com.itsix.freejob.rest;

import java.math.BigDecimal;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import com.itsix.freejob.api.Api;

public abstract class OsgiRestResource {

    protected static final BigDecimal KM_LAT = new BigDecimal(110.574);
    protected static final BigDecimal KM_LONG = new BigDecimal(111.325);

    protected Api getApi() {
        return (Api) servletContext.getAttribute(Api.class.getName());
    }

    @Context
    private ServletContext servletContext;

    protected BigDecimal longFactor(BigDecimal geo_lat) {
        return new BigDecimal(Math.cos(Math.toRadians(geo_lat.doubleValue())));
    }

}
