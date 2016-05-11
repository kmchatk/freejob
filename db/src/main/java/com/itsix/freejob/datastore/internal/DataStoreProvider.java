package com.itsix.freejob.datastore.internal;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import com.itsix.freejob.core.Freelancer;
import com.itsix.freejob.core.Job;
import com.itsix.freejob.core.Job.Status;
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.Location;
import com.itsix.freejob.core.Login;
import com.itsix.freejob.core.Role;
import com.itsix.freejob.core.User;
import com.itsix.freejob.core.exceptions.NotFoundException;
import com.itsix.freejob.core.exceptions.ReadFailedException;
import com.itsix.freejob.core.exceptions.WriteFailedException;
import com.itsix.freejob.datastore.DataStore;
import com.itsix.freejob.datastore.DatabaseManager;

@Component(publicFactory = false)
@Instantiate
@Provides
public class DataStoreProvider implements DataStore {

    @Requires
    DatabaseManager dbm;

    private static final Logger logger = Logger
            .getLogger(DataStoreProvider.class);

    @Validate
    private void initialize() {
        logger.debug("Initializing datastore");
        Connection connection = null;
        Statement statement = null;
        try {
            dbm.registerDatabase("freejob", null);
            connection = dbm.getConnection("freejob");
            statement = connection.createStatement();
            runSqlTest(statement);
            runSqlUser(statement);
            runSqlJobType(statement);
            runSqlLocation(statement);
            runSqlFreelancer(statement);
            runSqlJob(statement);

            statement.close();
        } catch (SQLException e) {
            logger.error("Could not initialize datastore", e);
        } finally {
            dbm.releaseConnection(connection);
        }
    }

    private void runSqlTest(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS test (name VARCHAR PRIMARY KEY, value VARCHAR)");
    }

    private void runSqlFreelancer(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS freelancer (id UUID PRIMARY KEY, jobtypeid UUID, firstname VARCHAR(50), lastname VARCHAR(50), email VARCHAR(255), password VARCHAR(32), address VARCHAR(512), geo_lat DECIMAL(8,6), geo_long DECIMAL(9,6), city VARCHAR(128), county VARCHAR(128), avg_rating INT, bank_name VARCHAR(128), account_number VARCHAR(128))");
        runSQL(statement,
                "ALTER TABLE freelancer ALTER COLUMN jobtypeid SET NOT NULL");
        runSQL(statement,
                "ALTER TABLE freelancer ADD CONSTRAINT IF NOT EXISTS freelancer_email_unique UNIQUE(email)");
        runSQL(statement,
                "ALTER TABLE freelancer ADD CONSTRAINT IF NOT EXISTS freelancer_jobtypeid_fk FOREIGN KEY(jobtypeid) REFERENCES jobtype(id)");
    }

    private void runSqlJob(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS job (id UUID PRIMARY KEY, status VARCHAR(20), created BIGINT, rating INT, title varchar(120), description VARCHAR(4096), jobtypeid UUID, freelancerid UUID, locationid UUID, userid UUID)");
        runSQL(statement,
                "ALTER TABLE job ALTER COLUMN jobtypeid SET NOT NULL");
        runSQL(statement,
                "ALTER TABLE job ADD CONSTRAINT IF NOT EXISTS job_jobtypeid_fk FOREIGN KEY(jobtypeid) REFERENCES jobtype(id)");

        runSQL(statement,
                "ALTER TABLE job ADD CONSTRAINT IF NOT EXISTS job_freelancerid_fk FOREIGN KEY(freelancerid) REFERENCES freelancer(id)");

        runSQL(statement,
                "ALTER TABLE job ALTER COLUMN locationid SET NOT NULL");
        runSQL(statement,
                "ALTER TABLE job ADD CONSTRAINT IF NOT EXISTS job_locationid_fk FOREIGN KEY(locationid) REFERENCES location(id)");

        runSQL(statement, "ALTER TABLE job ALTER COLUMN userid SET NOT NULL");
        runSQL(statement,
                "ALTER TABLE job ADD CONSTRAINT IF NOT EXISTS job_userid_fk FOREIGN KEY(userid) REFERENCES user(id)");

        runSQL(statement,
                "ALTER TABLE job ADD COLUMN IF NOT EXISTS status VARCHAR(20) AFTER id");

        runSQL(statement,
                "ALTER TABLE job ADD COLUMN IF NOT EXISTS title VARCHAR(120) AFTER rating");

        runSQL(statement,
                "ALTER TABLE job ADD COLUMN IF NOT EXISTS description VARCHAR(4096) AFTER title");

        runSQL(statement, "ALTER TABLE job ALTER COLUMN created BIGINT");
    }

    private void runSqlUser(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS user (id UUID PRIMARY KEY, firstname VARCHAR(50), lastname VARCHAR(50), email VARCHAR(255), password VARCHAR(32), role VARCHAR(32))");
        runSQL(statement,
                "ALTER TABLE user ADD CONSTRAINT IF NOT EXISTS user_email_unique UNIQUE(email)");
    }

    private void runSqlJobType(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS jobtype (id UUID PRIMARY KEY, name VARCHAR(64), description VARCHAR(512), commission decimal(4,2))");
        runSQL(statement,
                "ALTER TABLE jobtype ADD COLUMN IF NOT EXISTS commission decimal(4,2)");
        runSQL(statement,
                "UPDATE jobtype SET commission = 0.0 WHERE commission IS NULL");
    }

    private void runSqlLocation(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS location (id UUID PRIMARY KEY, name VARCHAR(64), userid UUID, address VARCHAR(512), city VARCHAR(128), county VARCHAR(128), geo_lat DECIMAL(8,6), geo_long DECIMAL(9,6))");
        runSQL(statement,
                "ALTER TABLE location ADD CONSTRAINT IF NOT EXISTS location_userid_fk FOREIGN KEY(userid) REFERENCES user(id)");
        runSQL(statement,
                "ALTER TABLE location ALTER COLUMN userid SET NOT NULL");
        runSQL(statement,
                "ALTER TABLE location ADD COLUMN IF NOT EXISTS county VARCHAR(128) AFTER city");
        runSQL(statement,
                "ALTER TABLE location ADD COLUMN IF NOT EXISTS name VARCHAR(64) AFTER id");
    }

    private void runSQL(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (java.sql.SQLException e) {
            logger.error("Could not execute statement: " + sql, e);
        }
    }

    @Override
    public UUID createUser(Login user) throws WriteFailedException {
        logger.debug("Creating user: " + user);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "INSERT INTO user(id, firstname, lastname, email, password, role) "
                            + values(6));
            UUID userId = UUID.randomUUID();
            px.setObject(1, userId);
            px.setString(2, user.getFirstName());
            px.setString(3, user.getLastName());
            px.setString(4, user.getEmail());
            px.setString(5, md5(user.getPassword()));
            px.setString(6, Role.CUSTOMER.name());
            px.execute();
            px.close();
            cx.commit();
            return userId;
        } catch (SQLException e) {
            logger.debug("Failed to create user", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException("Failed to create user", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    private String values(int count) {
        StringBuffer values = new StringBuffer(" VALUES (");
        for (int i = 1; i < count; i++) {
            values.append("?,");
        }
        values.append("?)");
        return values.toString();
    }

    public String md5(String md5) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] array;
            array = md.digest(md5.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Collection<User> listUsers() {
        List<User> users = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, firstname, lastname, email, role FROM user");
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                users.add(getUser(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list users", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return users;
    }

    private User getUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId((UUID) rs.getObject("id"));
        user.setFirstName(rs.getString("firstname"));
        user.setLastName(rs.getString("lastname"));
        user.setEmail(rs.getString("email"));
        user.setRole(Role.valueOf(rs.getString("role")));
        return user;
    }

    @Override
    public void deleteUser(UUID userId) throws WriteFailedException {
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx
                    .prepareStatement("DELETE FROM user WHERE id = ?");
            px.setObject(1, userId);
            px.executeUpdate();
            px.close();
        } catch (java.sql.SQLException e) {
            logger.warn("Failed to delete user", e);
            throw new WriteFailedException("Failed to delete user", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public Login login(String email, String password, Role role)
            throws ReadFailedException {
        switch (role) {
        case FREELANCER:
            return findFreelancer(email, password);
        default:
            return findUser(email, password);
        }
    }

    public User findUser(String email, String password)
            throws ReadFailedException {
        User user = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, firstname, lastname, email, role FROM user WHERE email = ? AND password = ?");
            px.setString(1, email);
            px.setString(2, md5(password));
            ResultSet rs = px.executeQuery();
            if (rs.next()) {
                user = getUser(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to find user", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return user;
    }

    public User editUser(UUID userId)
            throws NotFoundException, ReadFailedException {
        User user = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, firstname, lastname, email, role FROM user WHERE id = ?");
            px.setObject(1, userId);
            ResultSet rs = px.executeQuery();
            if (rs.next()) {
                user = getUser(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to find user", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        if (user == null) {
            throw new NotFoundException();
        }
        return user;
    }

    @Override
    public UUID createJobType(JobType jobType) throws WriteFailedException {
        logger.debug("Creating job type: " + jobType);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "INSERT INTO jobtype(id, name, description, commission) "
                            + values(4));
            UUID jobTypeId = UUID.randomUUID();
            px.setObject(1, jobTypeId);
            px.setString(2, jobType.getName());
            px.setString(3, jobType.getDescription());
            px.setBigDecimal(4, jobType.getCommission());
            px.execute();
            px.close();
            cx.commit();
            return jobTypeId;
        } catch (SQLException e) {
            logger.debug("Failed to create job type", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException("Failed to create job type", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public Collection<JobType> listJobTypes() {
        List<JobType> jobTypes = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, name, description, commission FROM jobtype");
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobTypes.add(getJobType(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {

            logger.warn("Failed to list job types", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobTypes;
    }

    @Override
    public JobType editJobType(UUID jobTypeId)
            throws NotFoundException, ReadFailedException {
        JobType jobType = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, name, description, commission FROM jobtype where id = ?");
            ResultSet rs = px.executeQuery();
            px.setObject(1, jobTypeId);
            if (rs.next()) {
                jobType = getJobType(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to read job type", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        if (jobType == null) {
            throw new NotFoundException();
        }
        return jobType;
    }

    private JobType getJobType(ResultSet rs) throws SQLException {
        JobType jobType = new JobType();
        jobType.setId((UUID) rs.getObject("id"));
        jobType.setName(rs.getString("name"));
        jobType.setDescription(rs.getString("description"));
        jobType.setCommission(rs.getBigDecimal("commission"));
        return jobType;
    }

    @Override
    public void deleteJobType(UUID jobTypeId) throws WriteFailedException {
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx
                    .prepareStatement("DELETE FROM jobtype WHERE id = ?");
            px.setObject(1, jobTypeId);
            px.executeUpdate();
            px.close();
        } catch (java.sql.SQLException e) {
            logger.warn("Failed to delete job type", e);
            throw new WriteFailedException("Failed to delete job type", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public UUID createFreelancer(Freelancer freelancer)
            throws WriteFailedException {
        logger.debug("Creating freelancer: " + freelancer);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "INSERT INTO freelancer(id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number) "
                            + values(14));
            UUID freelancerId = UUID.randomUUID();
            px.setObject(1, freelancerId);
            px.setObject(2, freelancer.getJobTypeId());
            px.setString(3, freelancer.getFirstName());
            px.setString(4, freelancer.getLastName());
            px.setString(5, freelancer.getEmail());
            px.setString(6, md5(freelancer.getPassword()));
            px.setString(7, freelancer.getAddress());
            px.setBigDecimal(8, freelancer.getLatitude());
            px.setBigDecimal(9, freelancer.getLongitude());
            px.setString(10, freelancer.getCity());
            px.setString(11, freelancer.getCounty());
            px.setInt(12, freelancer.getAvgRating());
            px.setString(13, freelancer.getBankName());
            px.setString(14, freelancer.getAccountNumber());

            px.execute();
            px.close();
            cx.commit();
            return freelancerId;
        } catch (SQLException e) {
            logger.debug("Failed to create freelancer", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException("Failed to create freelancer", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public Collection<Freelancer> listFreelancers() {
        List<Freelancer> freelancers = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number FROM freelancer");
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                freelancers.add(getFreelancer(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {

            logger.warn("Failed to list freelancers", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return freelancers;
    }

    private Freelancer getFreelancer(ResultSet rs) throws SQLException {
        Freelancer freelancer = new Freelancer();
        freelancer.setId((UUID) rs.getObject("id"));
        freelancer.setJobTypeId((UUID) rs.getObject("jobtypeid"));
        freelancer.setFirstName(rs.getString("firstname"));
        freelancer.setLastName(rs.getString("lastname"));
        freelancer.setEmail(rs.getString("email"));
        freelancer.setAddress(rs.getString("address"));
        freelancer.setLatitude(rs.getBigDecimal("geo_lat"));
        freelancer.setLongitude(rs.getBigDecimal("geo_long"));
        freelancer.setCity(rs.getString("city"));
        freelancer.setCounty(rs.getString("county"));
        freelancer.setAvgRating(rs.getInt("avg_rating"));
        freelancer.setBankName(rs.getString("bank_name"));
        freelancer.setAccountNumber(rs.getString("account_number"));
        return freelancer;
    }

    public Freelancer findFreelancer(String email, String password)
            throws ReadFailedException {
        Freelancer freelancer = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number FROM freelancer WHERE email = ? and password = ?");
            px.setString(1, email);
            px.setString(2, md5(password));
            ResultSet rs = px.executeQuery();
            if (rs.next()) {
                freelancer = getFreelancer(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to find freelancer", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return freelancer;
    }

    public Freelancer editFreelancer(UUID freelancerId)
            throws ReadFailedException, NotFoundException {
        Freelancer freelancer = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number FROM freelancer WHERE id = ?");
            px.setObject(1, freelancerId);
            ResultSet rs = px.executeQuery();
            if (rs.next()) {
                freelancer = getFreelancer(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to find freelancer", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        if (freelancer == null) {
            throw new NotFoundException();
        }
        return freelancer;
    }

    @Override
    public void deleteFreelancer(UUID freelancerId)
            throws WriteFailedException {
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx
                    .prepareStatement("DELETE FROM freelancer WHERE id = ?");
            px.setObject(1, freelancerId);
            px.executeUpdate();
            px.close();
        } catch (java.sql.SQLException e) {
            logger.warn("Failed to delete freelancer", e);
            throw new WriteFailedException("Failed to delete freelancer", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public UUID createLocation(UUID userId, Location location)
            throws WriteFailedException {
        logger.debug("Creating location: " + location);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "INSERT INTO location(id, name, userid, address, city, county, geo_lat, geo_long) "
                            + values(8));
            UUID locationId = UUID.randomUUID();
            px.setObject(1, locationId);
            px.setString(2, location.getName());
            px.setObject(3, userId);
            px.setString(4, location.getAddress());
            px.setString(5, location.getCity());
            px.setString(6, location.getCounty());
            px.setBigDecimal(7, location.getLatitude());
            px.setBigDecimal(8, location.getLongitude());

            px.execute();
            px.close();
            cx.commit();
            return locationId;
        } catch (SQLException e) {
            logger.debug("Failed to create location", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException("Failed to create location", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public Collection<Location> listLocations(UUID userId) {
        List<Location> locations = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, name, userid, address, city, county, geo_lat, geo_long FROM location WHERE userid = ?");
            px.setObject(1, userId);
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                locations.add(getLocation(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {

            logger.warn("Failed to list locations", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return locations;
    }

    @Override
    public Location editLocation(UUID userId, UUID locationId)
            throws NotFoundException, ReadFailedException {
        Location location = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, name, userid, address, city, county, geo_lat, geo_long FROM location WHERE id = ? and userid = ?");
            px.setObject(1, locationId);
            px.setObject(2, userId);
            ResultSet rs = px.executeQuery();
            if (rs.next()) {
                location = getLocation(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to read location", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        if (location == null) {
            throw new NotFoundException();
        }
        return location;
    }

    private Location getLocation(ResultSet rs) throws SQLException {
        Location location = new Location();
        location.setId((UUID) rs.getObject("id"));
        location.setName(rs.getString("name"));
        location.setUserId((UUID) rs.getObject("userid"));
        location.setAddress(rs.getString("address"));
        location.setCity(rs.getString("city"));
        location.setCounty(rs.getString("county"));
        location.setLatitude(rs.getBigDecimal("geo_lat"));
        location.setLongitude(rs.getBigDecimal("geo_long"));
        return location;
    }

    @Override
    public void deleteLocation(UUID locationId) throws WriteFailedException {
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx
                    .prepareStatement("DELETE FROM location WHERE id = ?");
            px.setObject(1, locationId);
            px.executeUpdate();
            px.close();
        } catch (java.sql.SQLException e) {
            logger.warn("Failed to delete location", e);
            throw new WriteFailedException("Failed to delete location", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public UUID createJob(UUID userId, Job job) throws WriteFailedException {
        logger.debug("Creating job: " + job);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "INSERT INTO job(id, title, description, status, created, rating, jobtypeid, freelancerid, locationid, userid) "
                            + values(10));
            UUID jobId = UUID.randomUUID();
            long created = System.currentTimeMillis();
            px.setObject(1, jobId);
            px.setString(2, job.getTitle());
            px.setString(3, job.getDescription());
            px.setString(4, Status.OPEN.name());
            px.setLong(5, created);
            px.setInt(6, job.getRating());
            px.setObject(7, job.getJobTypeId());
            px.setObject(8, job.getFreelancerId());
            px.setObject(9, job.getLocationId());
            px.setObject(10, userId);

            px.execute();
            px.close();
            cx.commit();
            return jobId;
        } catch (SQLException e) {
            logger.debug("Failed to create job", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException("Failed to create job", e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public Job editJob(UUID jobId)
            throws NotFoundException, ReadFailedException {
        Job job = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, title, description, status, created, rating, jobtypeid, freelancerid, locationid, userid FROM job WHERE id = ?");
            px.setObject(1, jobId);
            ResultSet rs = px.executeQuery();
            if (rs.next()) {
                job = getJob(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list jobs", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        if (job == null) {
            throw new NotFoundException();
        }
        return job;
    }

    @Override
    public Collection<Job> listJobsByType(UUID jobTypeId, Status status) {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, title, description, status, created, rating, jobtypeid, freelancerid, locationid, userid FROM job WHERE jobtypeid = ? and status = ?");
            px.setObject(1, jobTypeId);
            px.setString(2, status.name());
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {

            logger.warn("Failed to list jobs", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    @Override
    public Collection<Job> listJobsByType(UUID jobTypeId, BigDecimal minLat,
            BigDecimal maxLat, BigDecimal minLong, BigDecimal maxLong) {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid FROM job AS j LEFT JOIN location AS l ON j.locationid = l.id WHERE jobtypeid = ? AND (l.geo_lat BETWEEN ? AND ?) AND (l.geo_long BETWEEN ? AND ?)");
            px.setObject(1, jobTypeId);
            px.setBigDecimal(2, minLat);
            px.setBigDecimal(3, maxLat);
            px.setBigDecimal(4, minLong);
            px.setBigDecimal(5, maxLong);
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {

            logger.warn("Failed to list jobs", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    @Override
    public Collection<Job> listUserJobs(UUID userId, Status status) {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT id, title, description, status, created, rating, jobtypeid, freelancerid, locationid, userid FROM job WHERE userid = ? ";
            if (status != null) {
                sql += "and status = ?";
            }
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, userId);
            if (status != null) {
                px.setObject(2, status);
            }
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {

            logger.warn("Failed to list jobs", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    @Override
    public Collection<Job> listFreelancerJobs(UUID freelancerId,
            Status status) {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT id, title, description, status, created, rating, jobtypeid, freelancerid, locationid, userid FROM job WHERE freelancerid = ? ";
            if (status != null) {
                sql += "and status = ?";
            }
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, freelancerId);
            if (status != null) {
                px.setObject(2, status);
            }
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {

            logger.warn("Failed to list jobs", e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    private Job getJob(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId((UUID) rs.getObject("id"));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setStatus(Status.valueOf(rs.getString("status")));
        job.setCreated(rs.getLong("created"));
        job.setRating(rs.getInt("rating"));
        job.setJobTypeId((UUID) rs.getObject("jobtypeid"));
        job.setFreelancerId((UUID) rs.getObject("freelancerid"));
        job.setLocationId((UUID) rs.getObject("locationid"));
        job.setUserId((UUID) rs.getObject("userid"));
        return job;
    }

}
