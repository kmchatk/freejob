package com.itsix.freejob.core;

import java.math.BigDecimal;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobType {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal commission;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    @Override
    public String toString() {
        return "JobType [id=" + id + ", name=" + name + ", description="
                + description + ", commission=" + commission + "]";
    }

}
