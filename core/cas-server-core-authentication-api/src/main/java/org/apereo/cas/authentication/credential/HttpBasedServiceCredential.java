package org.apereo.cas.authentication.credential;

import module java.base;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@SuppressWarnings("NullAway.Init")
public class HttpBasedServiceCredential extends AbstractCredential {

    @Serial
    private static final long serialVersionUID = 1492607216336354503L;

    private URL callbackUrl;

    private CasModelRegisteredService service;

    @JsonCreator
    public HttpBasedServiceCredential(
        @JsonProperty("callbackUrl") final String callbackUrl,
        @JsonProperty("service") final CasModelRegisteredService service) {
        this.callbackUrl = FunctionUtils.doUnchecked(() -> new URI(callbackUrl).toURL());
        this.service = service;
    }

    @JsonIgnore
    @Override
    public String getId() {
        return this.callbackUrl.toExternalForm();
    }
}
