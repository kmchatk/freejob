package com.itsix.freejob.api.internal;

import java.math.BigDecimal;
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
import com.itsix.freejob.core.Job;
import com.itsix.freejob.core.Job.Status;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.Location;
import com.itsix.freejob.core.Login;
import com.itsix.freejob.core.Role;
import com.itsix.freejob.core.Session;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.LoginFailedException;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
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
    public void deleteUser(UUID userId) throws WriteFailedException {
        ds.deleteUser(userId);
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
    public void deleteFreelancer(UUID freelancerId)
            throws WriteFailedException {
        ds.deleteFreelancer(freelancerId);
    }

    @Override
    public Session login(String email, String password, Role role)
            throws LoginFailedException, ReadFailedException {
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

    @Override
    public UUID createJobType(JobType jobType) throws WriteFailedException {
        return ds.createJobType(jobType);
    }

    @Override
    public Collection<JobType> listJobTypes() {
        return ds.listJobTypes();
    }

    @Override
    public void deleteJobType(UUID jobTypeId) throws WriteFailedException {
        ds.deleteJobType(jobTypeId);
    }

    @Override
    public UUID createLocation(UUID userId, Location location)
            throws WriteFailedException {
        return ds.createLocation(userId, location);
    }

    @Override
    public Collection<Location> listLocations(UUID userId) {
        return ds.listLocations(userId);
    }

    @Override
    public void deleteLocation(UUID locationId) throws WriteFailedException {
        ds.deleteLocation(locationId);
    }

    @Override
    public UUID createJob(UUID userId, Job job) throws WriteFailedException {
        return ds.createJob(userId, job);
    }

    @Override
    public Collection<Job> listOpenJobs(UUID jobTypeId) {
        return ds.listJobsByType(jobTypeId, Status.OPEN);
    }

    @Override
    public Collection<Job> listOpenJobs(UUID jobTypeId, BigDecimal minLat,
            BigDecimal maxLat, BigDecimal minLong, BigDecimal maxLong) {
        return ds.listJobsByType(jobTypeId, minLat, maxLat, minLong, maxLong);
    }

    @Override
    public Collection<Job> listUserJobs(UUID userId, Status status) {
        return ds.listUserJobs(userId, status);
    }

    @Override
    public Collection<Job> listFreelancerJobs(UUID freelancerId,
            Status status) {
        return ds.listFreelancerJobs(freelancerId, status);
    }

    @Override
    public Job editJob(UUID jobId)
            throws NotFoundException, ReadFailedException {
        return ds.editJob(jobId);
    }

    @Override
    public Location editLocation(UUID userId, UUID locationId)
            throws NotFoundException, ReadFailedException {
        return ds.editLocation(userId, locationId);
    }

    @Override
    public Freelancer editFreelancer(UUID freelancerId)
            throws NotFoundException, ReadFailedException {
        return ds.editFreelancer(freelancerId);
    }

    @Override
    public JobType editJobType(UUID jobTypeId)
            throws NotFoundException, ReadFailedException {
        return ds.editJobType(jobTypeId);
    }

    @Override
    public User editUser(UUID userId)
            throws NotFoundException, ReadFailedException {
        return ds.editUser(userId);
    }

}
