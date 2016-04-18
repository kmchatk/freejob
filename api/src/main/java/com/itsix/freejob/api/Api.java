package com.itsix.freejob.api;

import java.util.Collection;

import com.itsix.freejob.core.User;

public interface Api {

    void register(User user);

    Collection<User> listUsers();

}
