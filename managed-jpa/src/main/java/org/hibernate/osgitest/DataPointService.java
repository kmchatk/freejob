package org.hibernate.osgitest;

import java.util.List;

import org.hibernate.osgitest.entity.DataPoint;

public interface DataPointService {

    public void add(DataPoint dp);

    public List<DataPoint> getAll();

    public void deleteAll();
}
