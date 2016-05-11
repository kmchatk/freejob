package com.itsix.freejob.datastore;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.Job;
import com.itsix.freejob.core.Job.Status;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.Location;
import com.itsix.freejob.core.Login;
import com.itsix.freejob.core.Role;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.WriteFailedException;

public interface DataStore {

    UUID createUser(Login user) throws WriteFailedException;

    Collection<User> listUsers();

    void deleteUser(UUID userId);

    UUID createJobType(JobType jobType) throws WriteFailedException;

    Collection<JobType> listJobTypes();

    void deleteJobType(UUID jobTypeId);

    UUID createFreelancer(Freelancer freelancer) throws WriteFailedException;

    Collection<Freelancer> listFreelancers();

    void deleteFreelancer(UUID freelancerId);

    Login login(String email, String password, Role role);

    UUID createLocation(UUID userId, Location location)
            throws WriteFailedException;

    Collection<Location> listLocations(UUID userId);

    void deleteLocation(UUID locationId);

    UUID createJob(UUID userId, Job job) throws WriteFailedException;

    Collection<Job> listJobs(UUID jobTypeId, Status status);

    Collection<Job> listJobs(UUID userId);

    Collection<Job> listJobs(UUID jobTypeId, BigDecimal minLat,
            BigDecimal maxLat, BigDecimal minLong, BigDecimal maxLong);

}
