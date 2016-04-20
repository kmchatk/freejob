package com.itsix.freejob.core;

import java.util.UUID;

public class Session {

    private UUID id = UUID.randomUUID();
    private Login user;
    private long created = System.currentTimeMillis();

    public Session(Login user) {
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public Login getUser() {
        return user;
    }

    public long getCreated() {
        return created;
    }

}
