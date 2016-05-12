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

import com.itsix.freejob.core.Subscription;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;

public class Subscriptions extends OsgiRestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result saveSubscription(@PathParam("freelancerId") UUID freelancerId,
            @PathParam("jobId") UUID jobId, Subscription subscription)
                    throws FreeJobException {
        getApi().saveSubscription(freelancerId, jobId,
                subscription.getMessage());
        return Result.ok();
    }

    @POST
    @Path("{freelancerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result acceptSubscription(
            @PathParam("freelancerId") UUID freelancerId,
            @PathParam("jobId") UUID jobId) throws FreeJobException {
        getApi().acceptSubscription(freelancerId, jobId);
        return Result.ok();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listSubscriptions(
            @PathParam("freelancerId") UUID freelancerId,
            @PathParam("jobId") UUID jobId) throws ReadFailedException {
        if (freelancerId != null) {
            return Result
                    .ok(getApi().listFreelancerSubscriptions(freelancerId));
        }
        return Result.ok(getApi().listJobSubscriptions(jobId));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteSubscription(
            @PathParam("freelancerId") UUID freelancerId,
            @PathParam("jobId") UUID jobId) throws WriteFailedException {
        getApi().deleteSubscription(freelancerId, jobId);
        return Result.ok();
    }

}
