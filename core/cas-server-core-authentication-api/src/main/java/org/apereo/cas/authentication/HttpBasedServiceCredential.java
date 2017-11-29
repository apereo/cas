package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.services.RegisteredService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A credential representing an HTTP endpoint given by a URL. Authenticating the credential usually involves
 * contacting the endpoint via the URL and observing the resulting connection (e.g. SSL certificate) and response
 * (e.g. status, headers).
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class HttpBasedServiceCredential extends AbstractCredential {

    /** Unique Serializable ID. */
    private static final long serialVersionUID = 1492607216336354503L;

    /** The callbackURL to check that identifies the application. */
    private final URL callbackUrl;

    /** String form of callbackUrl. */
    private final String callbackUrlAsString;

    /** The registered service associated with this callback. **/
    private final RegisteredService service;

    /**
     * Empty constructor used by Kryo for de-serialization.
     */
    protected HttpBasedServiceCredential() {
        this.callbackUrl = null;
        this.callbackUrlAsString = null;
        this.service = null;
    }

    /**
     * Creates a new instance for an HTTP endpoint located at the given URL.
     *
     * @param callbackUrl Non-null URL that will be contacted to validate the credential.
     * @param service The registered service associated with this callback.
     */
    public HttpBasedServiceCredential(final URL callbackUrl, final RegisteredService service) {
        this.callbackUrl = callbackUrl;
        this.callbackUrlAsString = callbackUrl.toExternalForm();
        this.service = service;
    }

    @JsonCreator
    public HttpBasedServiceCredential(@JsonProperty("callbackUrl") final String callbackUrl, 
                                      @JsonProperty("service") final RegisteredService service) throws MalformedURLException {
        final URL url = new URL(callbackUrl);
        this.callbackUrl = url;
        this.callbackUrlAsString = url.toExternalForm();
        this.service = service;
    }

    @JsonIgnore
    @Override
    public String getId() {
        return this.callbackUrlAsString;
    }

    /**
     * @return Returns the callbackUrl.
     */
    public URL getCallbackUrl() {
        return this.callbackUrl;
    }

    /**
     * Gets service associated with credentials.
     *
     * @return the service
     */
    public RegisteredService getService() {
        return this.service;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder bldr = new HashCodeBuilder(13, 133);
        return bldr.append(this.callbackUrlAsString)
                   .append(this.service)
                   .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpBasedServiceCredential other = (HttpBasedServiceCredential) obj;
        if (this.callbackUrlAsString == null) {
            if (other.callbackUrlAsString != null) {
                return false;
            }
        } else if (!this.callbackUrlAsString.equals(other.callbackUrlAsString)) {
            return false;
        }

        return this.service.equals(other.getService());
    }
}
