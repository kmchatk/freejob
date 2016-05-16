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
    @Path("jobtypes/{jobTypeId}/latitude/{latitude}/longitude/{longitude}/range/{range}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result listJobs(@PathParam("jobTypeId") UUID jobTypeId,
            @PathParam("latitude") BigDecimal latitude,
            @PathParam("longitude") BigDecimal longitude,
            @PathParam("range") BigDecimal range) throws ReadFailedException {
        BigDecimal kmLong = KM_LONG.multiply(longFactor(latitude),
                MathContext.DECIMAL64);
        BigDecimal kmLat = KM_LAT;
        BigDecimal dlong = range.divide(kmLong, MathContext.DECIMAL64);
        BigDecimal dlat = range.divide(kmLat, MathContext.DECIMAL64);
        BigDecimal maxLat = latitude.add(dlat);
        BigDecimal minLat = latitude.subtract(dlat);
        BigDecimal maxLong = longitude.add(dlong);
        BigDecimal minLong = longitude.subtract(dlong);
        return Result.ok(getApi().listFreelancers(jobTypeId, minLat, maxLat,
                minLong, maxLong));
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
