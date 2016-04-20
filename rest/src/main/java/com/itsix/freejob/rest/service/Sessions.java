package com.itsix.freejob.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.itsix.freejob.core.Session;
import com.itsix.freejob.core.exceptions.LoginFailedException;
import com.itsix.freejob.rest.OsgiRestResource;
import com.itsix.freejob.rest.data.LoginDTO;
import com.itsix.freejob.rest.data.Result;

public class Sessions extends OsgiRestResource {

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Result login(LoginDTO loginDTO) throws LoginFailedException {
        Session session = getApi().login(loginDTO.getEmail(),
                loginDTO.getPassword(), loginDTO.getRole());
        return Result.ok(session);
    }

}
