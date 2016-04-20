package com.itsix.freejob.api;

import java.util.Collection;
import java.util.UUID;

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.Login;
import com.itsix.freejob.core.Role;
import com.itsix.freejob.core.Session;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.LoginFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;

public interface Api {

    UUID register(Login user) throws WriteFailedException;

    Collection<User> listUsers();

    UUID register(Freelancer freelancer) throws WriteFailedException;

    Collection<Freelancer> listFreelancers();

    UUID createJobType(JobType jobType) throws WriteFailedException;

    Collection<JobType> listJobTypes();

    Session login(String email, String password, Role role)
            throws LoginFailedException;

}
