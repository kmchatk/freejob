package com.itsix.freejob.datastore;

import java.util.UUID;

import com.itsix.freejob.core.User;

public interface DataStore {

    UUID createUser(User user);

}
