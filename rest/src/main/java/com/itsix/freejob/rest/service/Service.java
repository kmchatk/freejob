package com.itsix.freejob.rest.service;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.rest.data.BundleDTO;
import com.itsix.freejob.rest.data.Result;
import com.sun.jersey.api.core.ResourceContext;

@Path("/")
public class Service {

    @Context
    ServletContext context;

    @GET
    @Path("about")
    @Produces(MediaType.APPLICATION_JSON)
    public Result about() {
        return Result.ok(new BundleDTO());
    }

    @Path("users")
    public Users getUsers(@Context ResourceContext rc) {
        return rc.getResource(Users.class);
    }

}
