package com.itsix.freejob.datastore;

import java.util.Collection;
import java.util.UUID;

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.WriteFailedException;

public interface DataStore {

    UUID createUser(User user) throws WriteFailedException;

    Collection<User> listUsers();

    UUID createJobType(JobType jobType) throws WriteFailedException;

    Collection<JobType> listJobTypes();

    UUID createFreelancer(Freelancer freelancer) throws WriteFailedException;

    Collection<Freelancer> listFreelancers();

}
