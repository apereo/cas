package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link RestAcceptableUsagePolicyRepository}.
 * Examines the principal attribute collection to determine if
 * the policy has been accepted, and if not, allows for a configurable
 * way so that user's choice can later be remembered and saved back via REST.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final long serialVersionUID = 1600024683199961892L;

    public RestAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final AcceptableUsagePolicyProperties aupProperties) {
        super(ticketRegistrySupport, aupProperties);
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        HttpResponse response = null;
        try {
            val rest = aupProperties.getRest();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);

            val service = WebUtils.getService(requestContext);
            val parameters = CollectionUtils.wrap(
                "username", credential.getId(),
                "locale", request.getLocale().toString());
            if (service != null) {
                parameters.put("service", service.getId());
            }

            response = HttpUtils.execute(rest.getUrl(), rest.getMethod(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(), parameters,
                new HashMap<>(0));
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
    public Optional<AcceptableUsagePolicyTerms> fetchPolicy(final RequestContext requestContext, final Credential credential) {
        HttpResponse response = null;
        try {
            val rest = aupProperties.getRest();
            val url = StringUtils.appendIfMissing(rest.getUrl(), "/").concat("policy");
            response = HttpUtils.execute(url, rest.getMethod(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                CollectionUtils.wrap("username", credential.getId()), new HashMap<>(0));
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val terms = MAPPER.readValue(JsonValue.readHjson(result).toString(), AcceptableUsagePolicyTerms.class);
                return Optional.ofNullable(terms);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
