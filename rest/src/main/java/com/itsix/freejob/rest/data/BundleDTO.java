package com.itsix.freejob.rest.data;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class BundleDTO {

    private Bundle bundle;

    public BundleDTO() {
        this.bundle = FrameworkUtil.getBundle(BundleDTO.class);
    }

    public String getName() {
        return bundle.getHeaders().get("Bundle-Name");
    }

    public String getVersion() {
        return bundle.getVersion().toString();
    }

}
