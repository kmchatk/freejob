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

import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;

public class JobTypes extends OsgiRestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result saveJobType(JobType jobType) throws FreeJobException {
        UUID jobTypeId = getApi().createJobType(jobType);
        return Result.ok(jobTypeId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listJobTypes() {
        return Result.ok(getApi().listJobTypes());
    }

    @DELETE
    @Path("{jobTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteJobType(@PathParam("jobTypeId") UUID jobTypeId) {
        getApi().deleteJobType(jobTypeId);
        return Result.ok();
    }

}
