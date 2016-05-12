package com.itsix.freejob.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.Job;
import com.itsix.freejob.core.Job.Status;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.Location;
import com.itsix.freejob.core.Role;
import com.itsix.freejob.core.Session;
import com.itsix.freejob.core.Subscription;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.LoginFailedException;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;

public interface Api {

    UUID register(User user) throws WriteFailedException;

    Collection<User> listUsers() throws ReadFailedException;

    void deleteUser(UUID userId) throws WriteFailedException;

    UUID register(Freelancer freelancer) throws WriteFailedException;

    Collection<Freelancer> listFreelancers() throws ReadFailedException;

    void deleteFreelancer(UUID freelancerId) throws WriteFailedException;

    UUID saveJobType(JobType jobType) throws WriteFailedException;

    Collection<JobType> listJobTypes() throws ReadFailedException;

    void deleteJobType(UUID jobTypeId) throws WriteFailedException;

    Session login(String email, String password, Role role)
            throws LoginFailedException, ReadFailedException;

    UUID saveLocation(UUID userId, Location location)
            throws WriteFailedException;

    Collection<Location> listLocations(UUID userId) throws ReadFailedException;

    void deleteLocation(UUID locationId) throws WriteFailedException;

    UUID saveJob(UUID userId, Job job) throws WriteFailedException;

    Collection<Job> listOpenJobs(UUID jobTypeId) throws ReadFailedException;

    Collection<Job> listOpenJobs(UUID jobTypeId, BigDecimal minLat,
            BigDecimal maxLat, BigDecimal minLong, BigDecimal maxLong)
                    throws ReadFailedException;

    Collection<Job> listUserJobs(UUID userId, Status status)
            throws ReadFailedException;

    Location editLocation(UUID userId, UUID locationId)
            throws NotFoundException, ReadFailedException;

    Collection<Job> listFreelancerJobs(UUID freelancerId, Status status)
            throws ReadFailedException;

    Freelancer editFreelancer(UUID freelancerId)
            throws NotFoundException, ReadFailedException;

    User editUser(UUID userId) throws NotFoundException, ReadFailedException;

    JobType editJobType(UUID jobTypeId)
            throws NotFoundException, ReadFailedException;

    Job editJob(UUID jobId) throws NotFoundException, ReadFailedException;

    void saveSubscription(UUID freelancerId, UUID jobId, String message)
            throws WriteFailedException;

    Collection<Subscription> listFreelancerSubscriptions(UUID freelancerId)
            throws ReadFailedException;

    Collection<Subscription> listJobSubscriptions(UUID jobId)
            throws ReadFailedException;

    UUID saveFreelancer(Freelancer freelancer) throws WriteFailedException;

    UUID saveUser(User user) throws WriteFailedException;

    void deleteJob(UUID jobId) throws WriteFailedException;

    void deleteSubscription(UUID freelancerId, UUID jobId)
            throws WriteFailedException;

    void acceptSubscription(UUID freelancerId, UUID jobId)
            throws WriteFailedException, ReadFailedException, NotFoundException;

    void requestPayment(UUID freelancerId, UUID jobId, BigDecimal netAmount) throws WriteFailedException, NotFoundException, ReadFailedException;

}
