package com.itsix.freejob.api.internal;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import com.itsix.freejob.api.Api;
import com.itsix.freejob.core.User;
import com.itsix.freejob.datastore.DataStore;

@Component(publicFactory = false)
@Instantiate
@Provides
public class ApiProvider implements Api {

    @Requires
    DataStore ds;

    private static final Logger logger = Logger.getLogger(ApiProvider.class);

    @Override
    public void register(User user) {
        ds.createUser(user);
    }

    @Override
    public Collection<User> listUsers() {
        return ds.listUsers();
    }

    @Validate
    private void validate() {
        logger.debug("Initializing API service");
    }

}
