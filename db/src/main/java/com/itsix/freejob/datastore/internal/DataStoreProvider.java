package com.itsix.freejob.datastore.internal;

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
import com.itsix.freejob.core.JobType;
import com.itsix.freejob.core.Location;
import com.itsix.freejob.core.Login;
import com.itsix.freejob.core.Role;
import com.itsix.freejob.core.User;
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
                "CREATE TABLE IF NOT EXISTS job (id UUID PRIMARY KEY, created TIMESTAMP, rating INT, jobtypeid UUID, freelancerid UUID, locationid UUID, userid UUID)");
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
                "CREATE TABLE IF NOT EXISTS location (id UUID PRIMARY KEY, userid UUID, address VARCHAR(512), city VARCHAR(128), county VARCHAR(128), geo_lat DECIMAL(8,6), geo_long DECIMAL(9,6))");
        runSQL(statement,
                "ALTER TABLE location ADD CONSTRAINT IF NOT EXISTS location_userid_fk FOREIGN KEY(userid) REFERENCES user(id)");
        runSQL(statement,
                "ALTER TABLE location ALTER COLUMN userid SET NOT NULL");
        runSQL(statement,
                "ALTER TABLE location ADD COLUMN IF NOT EXISTS county VARCHAR(128) AFTER city");
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
            logger.debug("Failed to insert user", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException(e);
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
    public void deleteUser(UUID userId) {
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
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public Login login(String email, String password, Role role) {
        switch (role) {
        case FREELANCER:
            return findFreelancer(email, password);
        default:
            return findUser(email, password);
        }
    }

    public User findUser(String email, String password) {
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
        } finally {
            dbm.releaseConnection(cx);
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
                    "INSERT INTO jobtype(id, name, description) " + values(3));
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
            logger.debug("Failed to insert job type", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException(e);
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

    private JobType getJobType(ResultSet rs) throws SQLException {
        JobType jobType = new JobType();
        jobType.setId((UUID) rs.getObject("id"));
        jobType.setName(rs.getString("name"));
        jobType.setDescription(rs.getString("description"));
        jobType.setCommission(rs.getBigDecimal("commission"));
        return jobType;
    }

    @Override
    public void deleteJobType(UUID jobTypeId) {
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
            logger.debug("Failed to insert freelancer", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException(e);
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

    public Freelancer findFreelancer(String email, String password) {
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
        } finally {
            dbm.releaseConnection(cx);
        }
        return freelancer;
    }

    @Override
    public void deleteFreelancer(UUID freelancerId) {
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
                    "INSERT INTO location(id, userid, address, city, county, geo_lat, geo_long) "
                            + values(7));
            UUID locationId = UUID.randomUUID();
            px.setObject(1, locationId);
            px.setObject(2, userId);
            px.setString(3, location.getAddress());
            px.setString(4, location.getCity());
            px.setString(5, location.getCounty());
            px.setBigDecimal(6, location.getLatitude());
            px.setBigDecimal(7, location.getLongitude());

            px.execute();
            px.close();
            cx.commit();
            return locationId;
        } catch (SQLException e) {
            logger.debug("Failed to insert location", e);
            try {
                cx.rollback();
            } catch (SQLException e1) {
                logger.debug("Failed to rollback transaction", e1);
            }
            throw new WriteFailedException(e);
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
                    "SELECT id, userid, address, city, county, geo_lat, geo_long FROM location WHERE userid = ?");
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

    private Location getLocation(ResultSet rs) throws SQLException {
        Location location = new Location();
        location.setId((UUID) rs.getObject("id"));
        location.setUserId((UUID) rs.getObject("userid"));
        location.setAddress(rs.getString("address"));
        location.setCity(rs.getString("city"));
        location.setCounty(rs.getString("county"));
        location.setLatitude(rs.getBigDecimal("geo_lat"));
        location.setLongitude(rs.getBigDecimal("geo_long"));
        return location;
    }

    @Override
    public void deleteLocation(UUID locationId) {
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
        } finally {
            dbm.releaseConnection(cx);
        }
    }

}
