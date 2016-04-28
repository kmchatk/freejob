package com.itsix.freejob.rest.service;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;
import com.sun.jersey.api.core.ResourceContext;

public class Users extends OsgiRestResource {

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result register(User user) throws FreeJobException {
        UUID userId = getApi().register(user);
        return Result.ok(userId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listUsers() {
        return Result.ok(getApi().listUsers());
    }

    @DELETE
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteUser(@PathParam("userId") UUID userId) {
        getApi().deleteUser(userId);
        return Result.ok();
    }

    @Path("{userId}/locations")
    public Locations getLocations(@Context ResourceContext rc) {
        return rc.getResource(Locations.class);
    }
}
