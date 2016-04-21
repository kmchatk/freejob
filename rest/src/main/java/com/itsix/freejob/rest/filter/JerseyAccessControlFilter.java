package com.itsix.freejob.rest.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * Servlet filter responsible for setting the appropriate header to HTTP
 * Response to allow cross-domain requests
 */
public class JerseyAccessControlFilter implements ContainerResponseFilter {

    public ContainerResponse filter(ContainerRequest request,
            ContainerResponse response) {
        response.getHttpHeaders().add("Access-Control-Allow-Origin", //$NON-NLS-1$
                "*"); //$NON-NLS-1$
        String requestHeaders = request
                .getHeaderValue("Access-Control-Request-Headers");
        if (requestHeaders != null) {
            response.getHttpHeaders().add("Access-Control-Allow-Headers",
                    requestHeaders);
        }
        response.getHttpHeaders().add("Access-Control-Allow-Methods", //$NON-NLS-1$
                "GET, POST, PUT, DELETE, OPTIONS"); //$NON-NLS-1$

        //        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { //$NON-NLS-1$
        //            response.getHttpHeaders().add(
        //                    "Access-Control-Allow-Credentials", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        //        }
        //        
        return response;
    }
}
