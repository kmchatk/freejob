package com.itsix.freejob.core;

import java.math.BigDecimal;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Freelancer implements Login {

    private UUID id;
    private UUID jobTypeId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String city;
    private String county;
    private int avgRating;
    private String bankName;
    private String accountNumber;
    private String message;
    private long created;
    private String jobTypeName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public UUID getJobTypeId() {
        return jobTypeId;
    }

    public void setJobTypeId(UUID jobTypeId) {
        this.jobTypeId = jobTypeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public int getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(int avgRating) {
        this.avgRating = avgRating;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setRole(Role role) {
        //Does nothing.
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJobTypeName() {
        return jobTypeName;
    }

    public void setJobTypeName(String jobTypeName) {
        this.jobTypeName = jobTypeName;
    }

    public Role getRole() {
        return Role.FREELANCER;
    }

    public String toString() {
        return "Freelancer [id=" + id + ", firstName=" + firstName
                + ", lastName=" + lastName + ", email=" + email + ", jobTypeId="
                + jobTypeId + ", address=" + address + ", city=" + city
                + ", county=" + county + ", bankName=" + bankName
                + ", accountNumber=" + accountNumber + ", avgRating="
                + avgRating + "]";
    }

}
