package com.itsix.freejob.core;

import java.util.UUID;

public class Session {

    private UUID sessionId = UUID.randomUUID();
    private Login user;
    private long created = System.currentTimeMillis();

    public Session(Login user) {
        this.user = user;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public Login getUser() {
        return user;
    }

    public long getCreated() {
        return created;
    }

}
