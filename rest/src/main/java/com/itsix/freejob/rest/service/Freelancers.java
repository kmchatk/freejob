package com.itsix.freejob.rest.service;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;

public class Freelancers extends OsgiRestResource {

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result register(Freelancer freelancer) throws WriteFailedException {
        UUID freelancerId = getApi().register(freelancer);
        return Result.ok(freelancerId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listFreelancers() {
        return Result.ok(getApi().listFreelancers());
    }

    @DELETE
    @Path("{freelancerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteFreelancer(
            @PathParam("freelancerId") UUID freelancerId) {
        getApi().deleteFreelancer(freelancerId);
        return Result.ok();
    }

}
