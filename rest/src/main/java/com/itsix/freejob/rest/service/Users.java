package com.itsix.freejob.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.core.User;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;

public class Users extends OsgiRestResource {

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result register(User user) {
        getApi().register(user);
        return Result.ok();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listUsers() {
        return Result.ok(getApi().listUsers());
    }
}
