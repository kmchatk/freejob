package com.itsix.freejob.rest.service;

import java.math.BigDecimal;
import java.math.MathContext;
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

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;
import com.sun.jersey.api.core.ResourceContext;

public class Freelancers extends OsgiRestResource {

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result register(Freelancer freelancer) throws WriteFailedException {
        UUID freelancerId = getApi().register(freelancer);
        return Result.ok(freelancerId);
    }

    @POST
    @Path("register/social")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result registerSocial(Freelancer freelancer)
            throws WriteFailedException {
        freelancer.setPassword(null);
        UUID freelancerId = getApi().register(freelancer);
        return Result.ok(freelancerId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result saveFreelancer(Freelancer freelancer)
            throws WriteFailedException {
        UUID freelancerId = getApi().saveFreelancer(freelancer);
        return Result.ok(freelancerId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listFreelancers() throws ReadFailedException {
        return Result.ok(getApi().listFreelancers());
    }

    @GET
    @Path("{freelancerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result editFreelancer(@PathParam("freelancerId") UUID freelancerId)
            throws NotFoundException, ReadFailedException {
        return Result.ok(getApi().editFreelancer(freelancerId));
    }

    @DELETE
    @Path("{freelancerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteFreelancer(@PathParam("freelancerId") UUID freelancerId)
            throws WriteFailedException {
        getApi().deleteFreelancer(freelancerId);
        return Result.ok();
    }

    @Path("{freelancerId}/jobs")
    public Jobs getJobs(@Context ResourceContext rc) {
        return rc.getResource(Jobs.class);
    }

    @Path("{freelancerId}/subscriptions")
    public Subscriptions getSubscriptions(@Context ResourceContext rc) {
        return rc.getResource(Subscriptions.class);
    }

}
