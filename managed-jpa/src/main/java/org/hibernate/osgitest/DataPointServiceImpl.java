package org.hibernate.osgitest;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.osgitest.entity.DataPoint;

public class DataPointServiceImpl implements DataPointService {

    private EntityManager entityManager;

    public void add(DataPoint dp) {
        entityManager.persist(dp);
        entityManager.flush();
    }

    public List<DataPoint> getAll() {
        return entityManager
                .createQuery("select d from DataPoint d", DataPoint.class)
                .getResultList();
    }

    public void deleteAll() {
        entityManager.createQuery("delete from DataPoint").executeUpdate();
        entityManager.flush();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
