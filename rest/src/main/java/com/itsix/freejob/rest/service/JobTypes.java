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

import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;
import com.sun.jersey.api.core.ResourceContext;

public class JobTypes extends OsgiRestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result saveJobType(JobType jobType) throws FreeJobException {
        UUID jobTypeId = getApi().saveJobType(jobType);
        return Result.ok(jobTypeId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listJobTypes() throws ReadFailedException {
        return Result.ok(getApi().listJobTypes());
    }

    @GET
    @Path("{jobTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result editJobType(@PathParam("jobTypeId") UUID jobTypeId)
            throws NotFoundException, ReadFailedException {
        return Result.ok(getApi().editJobType(jobTypeId));
    }

    @DELETE
    @Path("{jobTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteJobType(@PathParam("jobTypeId") UUID jobTypeId)
            throws WriteFailedException {
        getApi().deleteJobType(jobTypeId);
        return Result.ok();
    }

    @Path("{jobTypeId}/jobs")
    public Jobs getJobs(@Context ResourceContext rc) {
        return rc.getResource(Jobs.class);
    }

}
