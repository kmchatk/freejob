package com.itsix.freejob.rest;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.service.http.HttpContext;

import com.itsix.freejob.api.Api;
import com.sun.jersey.spi.container.servlet.ServletContainer;

@Component
@Instantiate
public class RestOsgiManager {

    private static final Integer LOAD_ON_STARTUP = 1;
    private static final Boolean ASYNC_SUPPORTED = true;

    @Requires(optional = true)
    Api api;

    @Requires(optional = true)
    private WebContainer webContainer;

    //    private HttpContext defaultHttpContext;
    private ServletContainer servletContainer;

    @Validate
    public void start() {
        //        try {
        //            Collection<Shape> shapes = paintApi.listShapes();
        //            System.out.println("There are currently " + shapes.size()
        //                    + " shapes available");
        //        } catch (RuntimeException e) {
        //            System.out.println("Paint API is not currently available");
        //        }

        HttpContext defaultHttpContext = webContainer
                .createDefaultHttpContext();
        servletContainer = new ServletContainer();
        Dictionary<String, String> jerseyInitParams = new Hashtable<String, String>();
        jerseyInitParams.put("com.sun.jersey.api.json.POJOMappingFeature",
                "true");
        jerseyInitParams.put(
                "com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.ClassNamesResourceConfig");
        String classNames = "com.itsix.freejob.rest.service.Service, com.itsix.freejob.rest.service.FreeJobExceptionMapper";
        jerseyInitParams.put("com.sun.jersey.config.property.classnames",
                classNames);
        jerseyInitParams.put("com.sun.jersey.config.feature.DisableWADL",
                "true");
        jerseyInitParams.put(
                "com.sun.jersey.spi.container.ContainerResponseFilters",
                "com.itsix.freejob.rest.filter.JerseyAccessControlFilter");

        try {
            webContainer.registerServlet(servletContainer,
                    new String[] { "/freejob/*" }, jerseyInitParams,
                    LOAD_ON_STARTUP, ASYNC_SUPPORTED, defaultHttpContext);
            servletContainer.getServletContext()
                    .setAttribute(Api.class.getName(), api);
        } catch (Exception e) {
        }
    }

    @Invalidate
    public void stop() {
        servletContainer.getServletContext()
                .removeAttribute(Api.class.getName());
        webContainer.unregisterServlet(servletContainer);
    }

}
