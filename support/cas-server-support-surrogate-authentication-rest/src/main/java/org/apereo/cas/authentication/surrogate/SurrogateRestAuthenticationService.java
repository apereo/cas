package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * This is {@link SurrogateRestAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SurrogateRestAuthenticationService extends BaseSurrogateAuthenticationService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final SurrogateAuthenticationProperties.Rest properties;

    public SurrogateRestAuthenticationService(final SurrogateAuthenticationProperties.Rest properties,
                                              final ServicesManager servicesManager) {
        super(servicesManager);
        this.properties = properties;
    }

    @Override
    public boolean canAuthenticateAsInternal(final String surrogate, final Principal principal, final Service service) {
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(properties.getUrl(), properties.getMethod(),
                properties.getBasicAuthUsername(), properties.getBasicAuthPassword(),
                CollectionUtils.wrap("surrogate", surrogate, "principal", principal.getId()), new HashMap<>());
            val statusCode = response.getStatusLine().getStatusCode();
            return HttpStatus.valueOf(statusCode).is2xxSuccessful();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(properties.getUrl(), properties.getMethod(),
                properties.getBasicAuthUsername(), properties.getBasicAuthPassword(),
                CollectionUtils.wrap("principal", username), new HashMap<>());
            return MAPPER.readValue(response.getEntity().getContent(), List.class);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }
}
