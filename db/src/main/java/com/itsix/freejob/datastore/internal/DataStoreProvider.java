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
import com.itsix.freejob.core.Subscription;
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

    private static final UUID ADMIN_ID = new UUID(0, 0);

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
            runSqlSubscription(statement);
            statement.close();
        } catch (SQLException e) {
            logger.error("Could not initialize datastore", e);
        } finally {
            dbm.releaseConnection(connection);
        }
        try {
            editUser(ADMIN_ID);
        } catch (NotFoundException e) {
            User admin = new User();
            admin.setEmail("admin@change.me");
            admin.setFirstName("System Admin");
            admin.setLastName("");
            admin.setRole(Role.ADMIN);
            admin.setId(ADMIN_ID);
            admin.setPassword("change.me");
            try {
                saveUser(admin);
            } catch (WriteFailedException e1) {
                logger.error("Could not save admin user", e1);
            }
        } catch (ReadFailedException e) {
            logger.error("Could not read admin user", e);
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
        runSQL(statement,
                "ALTER TABLE freelancer ADD COLUMN IF NOT EXISTS created BIGINT");
        runSQL(statement, "UPDATE freelancer SET created = "
                + System.currentTimeMillis() + " WHERE created IS NULL");
    }

    private void runSqlJob(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS job (id UUID PRIMARY KEY, status VARCHAR(20), created BIGINT, rating INT, title varchar(120), description VARCHAR(4096), jobtypeid UUID, freelancerid UUID, locationid UUID, userid UUID, netamount DECIMAL(10,2), total DECIMAL(10,2), message VARCHAR(4096))");
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

        runSQL(statement,
                "ALTER TABLE job ADD COLUMN IF NOT EXISTS netamount DECIMAL(10,2)");
        runSQL(statement,
                "ALTER TABLE job ADD COLUMN IF NOT EXISTS total DECIMAL(10,2)");
        runSQL(statement,
                "ALTER TABLE job ADD COLUMN IF NOT EXISTS message VARCHAR(4096)");
    }

    private void runSqlUser(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS user (id UUID PRIMARY KEY, firstname VARCHAR(50), lastname VARCHAR(50), email VARCHAR(255), password VARCHAR(32), role VARCHAR(32), created BIGINT)");
        runSQL(statement,
                "ALTER TABLE user ADD CONSTRAINT IF NOT EXISTS user_email_unique UNIQUE(email)");
        runSQL(statement,
                "ALTER TABLE user ADD COLUMN IF NOT EXISTS created BIGINT");
        runSQL(statement, "UPDATE user SET created = "
                + System.currentTimeMillis() + " WHERE created IS NULL");

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

    private void runSqlSubscription(Statement statement) {
        runSQL(statement,
                "CREATE TABLE IF NOT EXISTS subscription (jobid UUID, freelancerid UUID, message VARCHAR(4096), PRIMARY KEY(jobid, freelancerid))");
        runSQL(statement,
                "ALTER TABLE subscription ADD CONSTRAINT IF NOT EXISTS subscription_jobid_fk FOREIGN KEY(jobid) REFERENCES job(id)");
        runSQL(statement,
                "ALTER TABLE subscription ADD CONSTRAINT IF NOT EXISTS subscription_freelancerid_fk FOREIGN KEY(freelancerid) REFERENCES freelancer(id)");
    }

    private void runSQL(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (java.sql.SQLException e) {
            logger.error("Could not execute statement: " + sql, e);
        }
    }

    @Override
    public UUID saveUser(User user) throws WriteFailedException {
        logger.debug("Save user: " + user);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "MERGE INTO user(id, firstname, lastname, email, role) "
                            + values(5));
            UUID userId = user.getId();
            Role role = user.getRole();
            if (role == null) {
                role = Role.CUSTOMER;
            }
            if (userId == null) {
                userId = UUID.randomUUID();
            }
            px.setObject(1, userId);
            px.setString(2, user.getFirstName());
            px.setString(3, user.getLastName());
            px.setString(4, user.getEmail());
            px.setString(5, role.name());
            px.execute();
            px.close();
            if (user.getId() == null) {
                px = cx.prepareStatement(
                        "UPDATE user set created = ? WHERE id = ?");
                px.setLong(1, System.currentTimeMillis());
                px.setObject(2, userId);
                px.execute();
                px.close();
            }
            if (user.getPassword() != null) {
                px = cx.prepareStatement(
                        "UPDATE user SET password = ? where id = ?");
                px.setString(1, md5(user.getPassword()));
                px.setObject(2, userId);
                px.execute();
                px.close();
            }
            cx.commit();
            return userId;
        } catch (SQLException e) {
            logger.debug("Failed to save user", e);
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
    public Collection<User> listUsers() throws ReadFailedException {
        List<User> users = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, firstname, lastname, email, role, created FROM user where role = ?");
            px.setString(1, Role.CUSTOMER.name());
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                users.add(getUser(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list users", e);
            throw new ReadFailedException(e);
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
        user.setCreated(rs.getLong("created"));
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
            throw new WriteFailedException(e);
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
        case CUSTOMER:
        case ADMIN:
            return findUser(email, password, role);
        default:
            return findUser(email, password, role);
        }
    }

    @Override
    public Login login(String email, Role role) throws ReadFailedException {
        switch (role) {
        case FREELANCER:
            return findFreelancer(email);
        default:
            return findUser(email);
        }
    }

    public User findUser(String email, String password, Role role)
            throws ReadFailedException {
        User user = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, firstname, lastname, email, role, created FROM user WHERE email = ? AND password = ? AND role = ?");
            px.setString(1, email);
            px.setString(2, md5(password));
            px.setString(3, role.name());
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

    public User findUser(String email) throws ReadFailedException {
        User user = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, firstname, lastname, email, role, created FROM user WHERE email = ? AND password IS NULL");
            px.setString(1, email);
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
                    "SELECT id, firstname, lastname, email, role, created FROM user WHERE id = ?");
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
    public UUID saveJobType(JobType jobType) throws WriteFailedException {
        logger.debug("Save job type: " + jobType);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "MERGE INTO jobtype(id, name, description, commission) "
                            + values(4));
            UUID jobTypeId = jobType.getId();
            if (jobTypeId == null) {
                jobTypeId = UUID.randomUUID();
            }
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
            throw new WriteFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public Collection<JobType> listJobTypes() throws ReadFailedException {
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
            throw new ReadFailedException(e);
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
            px.setObject(1, jobTypeId);
            ResultSet rs = px.executeQuery();
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
            throw new WriteFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public UUID saveFreelancer(Freelancer freelancer)
            throws WriteFailedException {
        logger.debug("Updating freelancer: " + freelancer);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "MERGE INTO freelancer(id, jobtypeid, firstname, lastname, email, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number) "
                            + values(13));
            UUID freelancerId = freelancer.getId();
            if (freelancerId == null) {
                freelancerId = UUID.randomUUID();
            }
            px.setObject(1, freelancerId);
            px.setObject(2, freelancer.getJobTypeId());
            px.setString(3, freelancer.getFirstName());
            px.setString(4, freelancer.getLastName());
            px.setString(5, freelancer.getEmail());
            px.setString(6, freelancer.getAddress());
            px.setBigDecimal(7, freelancer.getLatitude());
            px.setBigDecimal(8, freelancer.getLongitude());
            px.setString(9, freelancer.getCity());
            px.setString(10, freelancer.getCounty());
            px.setInt(11, freelancer.getAvgRating());
            px.setString(12, freelancer.getBankName());
            px.setString(13, freelancer.getAccountNumber());

            px.execute();
            px.close();

            if (freelancer.getId() == null) {
                px = cx.prepareStatement(
                        "UPDATE freelancer set created = ? WHERE id = ?");
                px.setLong(1, System.currentTimeMillis());
                px.setObject(2, freelancerId);
                px.execute();
                px.close();
            }

            if (freelancer.getPassword() != null) {
                px = cx.prepareStatement(
                        "UPDATE freelancer SET password = ? where id = ?");
                px.setString(1, md5(freelancer.getPassword()));
                px.setObject(2, freelancerId);
                px.execute();
                px.close();
            }
            cx.commit();
            return freelancerId;
        } catch (SQLException e) {
            logger.debug("Failed to save freelancer", e);
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
    public Collection<Freelancer> listFreelancers() throws ReadFailedException {
        List<Freelancer> freelancers = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number, created FROM freelancer");
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                freelancers.add(getFreelancer(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list freelancers", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return freelancers;
    }

    @Override
    public Collection<Freelancer> listFreelancers(UUID jobTypeId,
            BigDecimal minLat, BigDecimal maxLat, BigDecimal minLong,
            BigDecimal maxLong) throws ReadFailedException {
        List<Freelancer> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number, created FROM freelancer WHERE jobtypeid = ? AND (geo_lat BETWEEN ? AND ?) AND (geo_long BETWEEN ? AND ?)";
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, jobTypeId);
            px.setBigDecimal(2, minLat);
            px.setBigDecimal(3, maxLat);
            px.setBigDecimal(4, minLong);
            px.setBigDecimal(5, maxLong);
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getFreelancer(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list jobs", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
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
        freelancer.setCreated(rs.getLong("created"));
        try {
            freelancer.setMessage(rs.getString("message"));
        } catch (SQLException e) {
        }
        try {
            freelancer.setJobTypeName(rs.getString("jobtypename"));
        } catch (SQLException e) {

        }
        return freelancer;
    }

    public Freelancer findFreelancer(String email, String password)
            throws ReadFailedException {
        Freelancer freelancer = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number, created FROM freelancer WHERE email = ? and password = ?");
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

    public Freelancer findFreelancer(String email) throws ReadFailedException {
        Freelancer freelancer = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number, created FROM freelancer WHERE email = ? and password IS NULL");
            px.setString(1, email);
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
                    "SELECT id, jobtypeid, firstname, lastname, email, password, address, geo_lat, geo_long, city, county, avg_rating, bank_name, account_number, created FROM freelancer WHERE id = ?");
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
            throw new WriteFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public UUID saveLocation(UUID userId, Location location)
            throws WriteFailedException {
        logger.debug("Creating location: " + location);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "MERGE INTO location(id, name, userid, address, city, county, geo_lat, geo_long) "
                            + values(8));
            UUID locationId = location.getId();
            if (locationId == null) {
                locationId = UUID.randomUUID();
            }
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
            logger.debug("Failed to save location", e);
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
    public Collection<Location> listLocations(UUID userId)
            throws ReadFailedException {
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
            throw new ReadFailedException(e);
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
            throw new WriteFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public UUID saveJob(UUID userId, Job job) throws WriteFailedException {
        logger.debug("Creating job: " + job);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "MERGE INTO job(id, title, description, status, rating, jobtypeid, freelancerid, locationid, userid, netamount, total, message) "
                            + values(12));
            UUID jobId = job.getId();
            if (jobId == null) {
                jobId = UUID.randomUUID();
            }
            Status status = job.getStatus();
            if (status == null) {
                status = Status.OPEN;
            }
            px.setObject(1, jobId);
            px.setString(2, job.getTitle());
            px.setString(3, job.getDescription());
            px.setString(4, status.name());
            px.setInt(5, job.getRating());
            px.setObject(6, job.getJobTypeId());
            px.setObject(7, job.getFreelancerId());
            px.setObject(8, job.getLocationId());
            px.setObject(9, userId);
            px.setBigDecimal(10, job.getNetAmount());
            px.setBigDecimal(11, job.getTotal());
            px.setString(12, job.getMessage());

            px.execute();
            px.close();

            if (job.getId() == null) {
                px = cx.prepareStatement(
                        "UPDATE job set created = ? WHERE id = ?");
                px.setLong(1, System.currentTimeMillis());
                px.setObject(2, jobId);
                px.execute();
                px.close();
            }
            cx.commit();
            return jobId;
        } catch (SQLException e) {
            logger.debug("Failed to create job", e);
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
    public Job editJob(UUID jobId)
            throws NotFoundException, ReadFailedException {
        Job job = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid, j.netamount, j.total, j.message, l.geo_lat, l.geo_long FROM job AS j LEFT JOIN location AS l ON j.locationid = l.id WHERE j.id = ?");
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
    public Collection<Job> listJobsByType(UUID jobTypeId, Status status)
            throws ReadFailedException {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid, j.netamount, j.total, j.message, l.geo_lat, l.geo_long FROM job AS j LEFT JOIN location AS l ON j.locationid = l.id WHERE j.jobtypeid = ? and j.status = ?");
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
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    @Override
    public Collection<Job> listJobsByType(UUID jobTypeId, BigDecimal minLat,
            BigDecimal maxLat, BigDecimal minLong, BigDecimal maxLong)
                    throws ReadFailedException {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid, j.netamount, j.total, j.message, l.geo_lat, l.geo_long FROM job AS j LEFT JOIN location AS l ON j.locationid = l.id WHERE j.jobtypeid = ? AND (l.geo_lat BETWEEN ? AND ?) AND (l.geo_long BETWEEN ? AND ?)");
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
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    @Override
    public Collection<Job> listUserJobs(UUID userId, Status status)
            throws ReadFailedException {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid, j.netamount, j.total, j.message, l.geo_lat, l.geo_long FROM job AS j LEFT JOIN location AS l ON j.locationid = l.id WHERE j.userid = ? ";
            if (status != null) {
                sql += "and status = ?";
            }
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, userId);
            if (status != null) {
                px.setString(2, status.name());
            }
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list jobs", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    @Override
    public Collection<Job> listJobsByStatus(Status status)
            throws ReadFailedException {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid, j.netamount, j.total, j.message, l.geo_lat, l.geo_long FROM job AS j LEFT JOIN location AS l ON j.locationid = l.id ";
            if (status != null) {
                sql += " WHERE status = ?";
            }
            PreparedStatement px = cx.prepareStatement(sql);
            if (status != null) {
                px.setString(1, status.name());
            }
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list jobs", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return jobs;
    }

    @Override
    public Collection<Job> listFreelancerJobs(UUID freelancerId, Status status)
            throws ReadFailedException {
        List<Job> jobs = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid, j.netamount, j.total, j.message, l.geo_lat, l.geo_long, u.firstname, u.lastname FROM job AS j LEFT JOIN location AS l ON j.locationid = l.id LEFT JOIN user AS u ON j.userid = u.id WHERE j.freelancerid = ? ";
            if (status != null) {
                sql += "and status = ?";
            }
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, freelancerId);
            if (status != null) {
                px.setString(2, status.name());
            }
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                jobs.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list jobs", e);
            throw new ReadFailedException(e);
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
        job.setNetAmount(rs.getBigDecimal("netamount"));
        job.setTotal(rs.getBigDecimal("total"));
        job.setMessage(rs.getString("message"));
        try {
            job.setLatitude(rs.getBigDecimal("geo_lat"));
            job.setLongitude(rs.getBigDecimal("geo_long"));
        } catch (SQLException e) {

        }

        try {
            job.setFirstName(rs.getString("firstname"));
            job.setLastName(rs.getString("lastname"));
        } catch (SQLException e) {

        }
        return job;
    }

    @Override
    public void deleteJob(UUID jobId) throws WriteFailedException {
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx
                    .prepareStatement("DELETE FROM job WHERE id = ?");
            px.setObject(1, jobId);
            px.executeUpdate();
            px.close();
        } catch (java.sql.SQLException e) {
            logger.warn("Failed to delete job", e);
            throw new WriteFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

    @Override
    public void saveSubscription(UUID freelancerId, UUID jobId, String message)
            throws WriteFailedException {
        logger.debug("Saving subscription: [freelancerId=" + freelancerId
                + ",jobId=" + jobId);
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            cx.setAutoCommit(false);
            PreparedStatement px = cx.prepareStatement(
                    "MERGE INTO subscription(jobid, freelancerid, message) "
                            + values(3));
            px.setObject(1, jobId);
            px.setObject(2, freelancerId);
            px.setString(3, message);

            px.execute();
            px.close();
            cx.commit();
        } catch (SQLException e) {
            logger.debug("Failed to create subscription", e);
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
    public Collection<Freelancer> listSubscribers(UUID jobId)
            throws ReadFailedException {
        List<Freelancer> subscriptions = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT f.id, f.jobtypeid, f.firstname, f.lastname, f.email, f.password, f.address, f.geo_lat, f.geo_long, f.city, f.county, f.avg_rating, f.bank_name, f.account_number, f.created, s.message FROM subscription AS s LEFT JOIN freelancer AS f on s.freelancerid = f.id where s.jobid = ?";
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, jobId);
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                subscriptions.add(getFreelancer(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list freelancers", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return subscriptions;
    }

    @Override
    public Subscription editSubscription(UUID freelancerId, UUID jobId)
            throws ReadFailedException, NotFoundException {
        Subscription subscription = null;
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT jobid, freelancerid, message from subscription WHERE jobid = ? AND freelancerid = ?";
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, jobId);
            px.setObject(2, freelancerId);
            ResultSet rs = px.executeQuery();
            if (rs.next()) {
                subscription = getSubscription(rs);
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list jobs", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        if (subscription == null) {
            throw new NotFoundException();
        }
        return subscription;
    }

    @Override
    public Collection<Job> listSubscriptions(UUID freelancerId)
            throws ReadFailedException {
        List<Job> subscriptions = new LinkedList<>();
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            String sql = "SELECT j.id, j.title, j.description, j.status, j.created, j.rating, j.jobtypeid, j.freelancerid, j.locationid, j.userid, j.netamount, j.total, s.message, l.geo_lat, l.geo_long FROM subscription AS s LEFT JOIN job AS j on s.jobid = j.id LEFT JOIN location l on j.locationid = l.id WHERE s.freelancerid = ? AND j.status = ?";
            PreparedStatement px = cx.prepareStatement(sql);
            px.setObject(1, freelancerId);
            px.setString(2, Status.OPEN.name());
            ResultSet rs = px.executeQuery();
            while (rs.next()) {
                subscriptions.add(getJob(rs));
            }
            rs.close();
            px.close();
        } catch (SQLException e) {
            logger.warn("Failed to list jobs", e);
            throw new ReadFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
        return subscriptions;
    }

    private Subscription getSubscription(ResultSet rs) throws SQLException {
        Subscription subscription = new Subscription();
        subscription.setJobId((UUID) rs.getObject("jobid"));
        subscription.setFreelancerId((UUID) rs.getObject("freelancerid"));
        subscription.setMessage(rs.getString("jobid"));
        return subscription;
    }

    @Override
    public void deleteSubscription(UUID freelancerId, UUID jobId)
            throws WriteFailedException {
        Connection cx = null;
        try {
            cx = dbm.getConnection("freejob");
            PreparedStatement px = cx.prepareStatement(
                    "DELETE FROM subscription WHERE jobid = ? and freelancerid = ?");
            px.setObject(1, jobId);
            px.setObject(2, freelancerId);
            px.executeUpdate();
            px.close();
        } catch (java.sql.SQLException e) {
            logger.warn("Failed to delete subscription", e);
            throw new WriteFailedException(e);
        } finally {
            dbm.releaseConnection(cx);
        }
    }

}
