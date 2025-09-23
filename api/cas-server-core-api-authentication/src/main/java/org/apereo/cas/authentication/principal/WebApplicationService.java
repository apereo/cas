package org.apereo.cas.authentication.principal;

import org.apereo.cas.validation.ValidationResponseType;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a service using CAS that comes from the web.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public interface WebApplicationService extends Service {
    /**
     * Bean name for the factory implementation that creates this service type.
     */
    String BEAN_NAME_FACTORY = "webApplicationServiceFactory";

    /**
     * Retrieves the artifact supplied with the service. May be null.
     *
     * @return the artifact if it exists, null otherwise.
     */
    String getArtifactId();

    /**
     * Indicates the source from which the service object was extracted and built.
     * Typically maps to a request parameter or attribute that provides the request.
     *
     * @return the source if it exists, null otherwise.
     */
    String getSource();

    /**
     * Ticket validation response MUST be produced based on the parameter value.
     * Supported values are XML and JSON. If this parameter is not set,
     * the default XML format will be used. If the parameter value is not supported by the CAS server,
     * an error must be produced.
     *
     * @return the requested format
     * @since 4.2
     */
    ValidationResponseType getFormat();

    /**
     * Return if the service is already logged out.
     *
     * @return if the service is already logged out.
     */
    boolean isLoggedOutAlready();

    /**
     * Set if the service is already logged out.
     *
     * @param loggedOutAlready if the service is already logged out.
     */
    void setLoggedOutAlready(boolean loggedOutAlready);

    /**
     * Collect fragment in web application service.
     *
     * @param fragment the fragment
     * @return the web application service
     */
    @JsonIgnore
    WebApplicationService setFragment(String fragment);

    /**
     * Gets fragment.
     *
     * @return the fragment
     */
    String getFragment();
}
