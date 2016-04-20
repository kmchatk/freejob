package com.itsix.freejob.api.internal;

import java.util.Collection;
import java.util.UUID;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import com.itsix.freejob.api.Api;
import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.datastore.DataStore;

@Component(publicFactory = false)
@Instantiate
@Provides
public class ApiProvider implements Api {

    @Requires
    DataStore ds;

    private static final Logger logger = Logger.getLogger(ApiProvider.class);

    @Override
    public UUID register(User user) throws WriteFailedException {
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
}
