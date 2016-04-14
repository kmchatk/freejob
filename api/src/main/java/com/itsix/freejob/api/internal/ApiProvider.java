package com.itsix.freejob.api.internal;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import com.itsix.freejob.api.Api;
import com.itsix.freejob.core.User;
import com.itsix.freejob.datastore.DataStore;

@Component(publicFactory = false)
@Instantiate
@Provides
public class ApiProvider implements Api {

    @Requires
    DataStore ds;

    @Override
    public void register(User user) {
        ds.createUser(user);
    }

}
