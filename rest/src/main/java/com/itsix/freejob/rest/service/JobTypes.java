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
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;

public class JobTypes extends OsgiRestResource {

    private static final BigDecimal KM_LAT = new BigDecimal(110.574);
    private static final BigDecimal KM_LONG = new BigDecimal(111.325);

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

    @GET
    @Path("{jobTypeId}/jobs")
    @Produces(MediaType.APPLICATION_JSON)
    public Result listJobs(@PathParam("jobTypeId") UUID jobTypeId) {
        return Result.ok(getApi().listOpenJobs(jobTypeId));
    }

    @GET
    @Path("{jobTypeId}/jobs/latitude/{latitude}/longitude/{longitude}/range/{range}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result listJobs(@PathParam("jobTypeId") UUID jobTypeId,
            @PathParam("latitude") BigDecimal latitude,
            @PathParam("longitude") BigDecimal longitude,
            @PathParam("range") BigDecimal range) {
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

    private BigDecimal longFactor(BigDecimal geo_lat) {
        return new BigDecimal(Math.cos(Math.toRadians(geo_lat.doubleValue())));
    }

}
