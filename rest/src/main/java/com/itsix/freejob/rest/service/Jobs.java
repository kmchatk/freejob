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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.core.Job;
import com.itsix.freejob.core.Job.Status;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;
import com.sun.jersey.api.core.ResourceContext;

public class Jobs extends OsgiRestResource {

  
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result saveJob(@PathParam("userId") UUID userId, Job job)
            throws FreeJobException {
        UUID jobId = getApi().saveJob(userId, job);
        return Result.ok(jobId);
    }

    @POST
    @Path("netAmount/{netAmount}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result requestPayment(@PathParam("freelancerId") UUID freelancerId,
            @PathParam("jobId") UUID jobId,
            @PathParam("netAmount") BigDecimal netAmount)
                    throws FreeJobException {
        getApi().requestPayment(freelancerId, jobId, netAmount);
        return Result.ok();
    }

    @POST
    @Path("rating/{rating}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result rateJob(@PathParam("freelancerId") UUID userId,
            @PathParam("jobId") UUID jobId, @PathParam("rating") int rating)
                    throws FreeJobException {
        getApi().rateJob(userId, jobId, rating);
        return Result.ok();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listJobs(@PathParam("jobTypeId") UUID jobTypeId,
            @PathParam("userId") UUID userId,
            @PathParam("freelancerId") UUID freelancerId,
            @QueryParam("status") Status status) throws ReadFailedException {
        if (jobTypeId != null) {
            return Result.ok(getApi().listOpenJobs(jobTypeId));
        } else if (userId != null) {
            return Result.ok(getApi().listUserJobs(userId, status));
        } else if (freelancerId != null) {
            return Result.ok(getApi().listFreelancerJobs(freelancerId, status));
        } else {
            return Result.ok(getApi().listJobs(status));
        }

    }

    @GET
    @Path("latitude/{latitude}/longitude/{longitude}/range/{range}")
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
        return Result.ok(getApi().listOpenJobs(jobTypeId, minLat, maxLat,
                minLong, maxLong));
    }

    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result editJob(@PathParam("jobId") UUID jobId)
            throws NotFoundException, ReadFailedException {
        return Result.ok(getApi().editJob(jobId));

    }

    @DELETE
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteJob(@PathParam("jobId") UUID jobId)
            throws WriteFailedException {
        getApi().deleteJob(jobId);
        return Result.ok();
    }

    @Path("{jobId}/subscriptions")
    public Subscriptions getSubscriptions(@Context ResourceContext rc) {
        return rc.getResource(Subscriptions.class);
    }

}
