package com.itsix.freejob.core;

import java.util.UUID;

public interface Login {

    UUID getId();

    void setId(UUID id);

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getEmail();

    void setEmail(String email);

    String getPassword();

    void setPassword(String password);

}