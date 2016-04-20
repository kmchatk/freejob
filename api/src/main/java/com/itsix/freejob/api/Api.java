package com.itsix.freejob.api;

import java.util.Collection;
import java.util.UUID;

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.WriteFailedException;

public interface Api {

    UUID register(User user) throws WriteFailedException;

    Collection<User> listUsers();

    UUID register(Freelancer freelancer) throws WriteFailedException;

    Collection<Freelancer> listFreelancers();

    UUID createJobType(JobType jobType) throws WriteFailedException;

    Collection<JobType> listJobTypes();

}
