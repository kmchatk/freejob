package com.itsix.freejob.core;

import java.math.BigDecimal;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

    public static enum Status {
        OPEN, ACCEPTED, ONGOING, FINISHED, PAYMENT_REQUESTED, BILLED, CANCELLED;
    }

    private UUID id;
    private Status status;
    private String title;
    private String description;
    private long created;
    private int rating;
    private UUID jobTypeId;
    private UUID freelancerId;
    private UUID userId;
    private UUID locationId;
    private BigDecimal netAmount;
    private BigDecimal total;
    private String message;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String lastName;
    private String firstName;
    private String freelancerLastName;
    private String freelancerFirstName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public UUID getJobTypeId() {
        return jobTypeId;
    }

    public void setJobTypeId(UUID jobTypeId) {
        this.jobTypeId = jobTypeId;
    }

    public UUID getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(UUID freelancerId) {
        this.freelancerId = freelancerId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFreelancerLastName() {
        return freelancerLastName;
    }

    public void setFreelancerLastName(String freelancerLastName) {
        this.freelancerLastName = freelancerLastName;
    }

    public String getFreelancerFirstName() {
        return freelancerFirstName;
    }

    public void setFreelancerFirstName(String freelancerFirstName) {
        this.freelancerFirstName = freelancerFirstName;
    }

}
