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

import com.itsix.freejob.core.Location;
import com.itsix.freejob.core.exceptions.FreeJobException;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.Result;

public class Locations extends OsgiRestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result createLocation(@PathParam("userId") UUID userId,
            Location location) throws FreeJobException {
        UUID locationId = getApi().createLocation(userId, location);
        return Result.ok(locationId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result listLocations(@PathParam("userId") UUID userId) {
        return Result.ok(getApi().listLocations(userId));
    }

    @GET
    @Path("{locationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result editLocation(@PathParam("userId") UUID userId,
            @PathParam("locationId") UUID locationId)
                    throws NotFoundException, ReadFailedException {
        return Result.ok(getApi().editLocation(userId, locationId));
    }

    @DELETE
    @Path("{locationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result deleteLocation(@PathParam("locationId") UUID locationId)
            throws WriteFailedException {
        getApi().deleteLocation(locationId);
        return Result.ok();
    }
}
