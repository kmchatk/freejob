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
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;

public interface DataStore {

    UUID createUser(Login user) throws WriteFailedException;

    Collection<User> listUsers();

    void deleteUser(UUID userId) throws WriteFailedException;

    UUID createJobType(JobType jobType) throws WriteFailedException;

    Collection<JobType> listJobTypes();

    void deleteJobType(UUID jobTypeId) throws WriteFailedException;

    UUID createFreelancer(Freelancer freelancer) throws WriteFailedException;

    Collection<Freelancer> listFreelancers();

    void deleteFreelancer(UUID freelancerId) throws WriteFailedException;

    Login login(String email, String password, Role role)
            throws ReadFailedException;

    UUID createLocation(UUID userId, Location location)
            throws WriteFailedException;

    Collection<Location> listLocations(UUID userId);

    void deleteLocation(UUID locationId) throws WriteFailedException;

    UUID createJob(UUID userId, Job job) throws WriteFailedException;

    Collection<Job> listUserJobs(UUID userId, Status status);

    Collection<Job> listJobsByType(UUID jobTypeId, BigDecimal minLat,
            BigDecimal maxLat, BigDecimal minLong, BigDecimal maxLong);

    Location editLocation(UUID userId, UUID locationId)
            throws NotFoundException, ReadFailedException;

    Collection<Job> listFreelancerJobs(UUID freelancerId, Status status);

    Collection<Job> listJobsByType(UUID jobTypeId, Status open);

    Freelancer editFreelancer(UUID freelancerId)
            throws NotFoundException, ReadFailedException;

    JobType editJobType(UUID jobTypeId)
            throws NotFoundException, ReadFailedException;

    User editUser(UUID userId) throws NotFoundException, ReadFailedException;

    Job editJob(UUID jobId) throws NotFoundException, ReadFailedException;

}
