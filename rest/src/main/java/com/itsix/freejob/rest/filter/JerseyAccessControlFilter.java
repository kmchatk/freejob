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
        response.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        String requestHeaders = request
                .getHeaderValue("Access-Control-Request-Headers");
        if (requestHeaders != null) {
            response.getHttpHeaders().add("Access-Control-Allow-Headers",
                    requestHeaders);
        }
        response.getHttpHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        return response;
    }
}
