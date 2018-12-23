package org.apereo.cas.authentication.credential;

import org.apereo.cas.services.RegisteredService;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpBasedServiceCredential extends AbstractCredential {

    /**
     * Unique Serializable ID.
     */
    private static final long serialVersionUID = 1492607216336354503L;

    /**
     * The callbackURL to check that identifies the application.
     */
    private URL callbackUrl;

    /**
     * The registered service associated with this callback.
     **/
    private RegisteredService service;

    @JsonCreator
    @SneakyThrows
    public HttpBasedServiceCredential(@JsonProperty("callbackUrl") final String callbackUrl,
                                      @JsonProperty("service") final RegisteredService service) {
        this.callbackUrl = new URL(callbackUrl);
        this.service = service;
    }

    @JsonIgnore
    @Override
    public String getId() {
        return this.callbackUrl.toExternalForm();
    }
}
