package com.itsix.freejob.rest.service;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.core.Job;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;

public class Jobs extends OsgiRestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result createJob(@PathParam("userId") UUID userId, Job job)
            throws FreeJobException {
        UUID jobId = getApi().createJob(userId, job);
        return Result.ok(jobId);
    }
}
