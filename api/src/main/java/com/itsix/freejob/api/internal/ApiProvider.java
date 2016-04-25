package com.itsix.freejob.api.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import com.itsix.freejob.api.Api;
import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.Login;
import com.itsix.freejob.core.Role;
import com.itsix.freejob.core.Session;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.LoginFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.datastore.DataStore;

@Component(publicFactory = false)
@Instantiate
@Provides
public class ApiProvider implements Api {

    @Requires
    DataStore ds;

    private Map<UUID, Session> sessions = new HashMap<>();

    private static final Logger logger = Logger.getLogger(ApiProvider.class);

    @Override
    public UUID register(Login user) throws WriteFailedException {
        return ds.createUser(user);
    }

    @Override
    public Collection<User> listUsers() {
        return ds.listUsers();
    }

    @Override
    public UUID register(Freelancer freelancer) throws WriteFailedException {
        return ds.createFreelancer(freelancer);
    }

    @Override
    public Collection<Freelancer> listFreelancers() {
        return ds.listFreelancers();
    }

    @Override
    public UUID createJobType(JobType jobType) throws WriteFailedException {
        return ds.createJobType(jobType);
    }

    @Override
    public Collection<JobType> listJobTypes() {
        return ds.listJobTypes();
    }

    @Override
    public Session login(String email, String password, Role role)
            throws LoginFailedException {
        Login user = ds.login(email, password, role);
        if (user == null) {
            throw new LoginFailedException();
        }
        synchronized (sessions) {
            Session session = new Session(user);
            sessions.put(session.getSessionId(), session);
            return session;
        }
    }
}
