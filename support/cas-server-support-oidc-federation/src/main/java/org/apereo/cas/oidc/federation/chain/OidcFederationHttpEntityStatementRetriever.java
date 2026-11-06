package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.openid.connect.sdk.federation.api.FetchEntityStatementRequest;
import com.nimbusds.openid.connect.sdk.federation.api.FetchEntityStatementResponse;
import com.nimbusds.openid.connect.sdk.federation.config.FederationEntityConfigurationRequest;
import com.nimbusds.openid.connect.sdk.federation.config.FederationEntityConfigurationResponse;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.ResolveException;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Retrieves entity configurations, at the well-known location of the entity identifier (OpenID Federation 1.0,
 * section 9), and subordinate statements, from the fetch endpoint of their issuer (section 8.1), through the federation
 * HTTP client, so that every request is bounded in time and size and nothing is kept between resolutions. An entity
 * that cannot be reached, does not answer in the time a request is given, or answers with a server error, is remembered
 * by the running resolution, which can then tell an unreachable trust anchor from an entity without a valid trust chain;
 * a request that could not
 * be given that time, because the resolution had used it up, or found no room among the running requests, tells nothing
 * about the entity and leaves the resolution unable to tell.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcFederationHttpEntityStatementRetriever implements EntityStatementRetriever {
    private final OidcFederationHttpClient httpClient;

    @Override
    public EntityStatement fetchEntityConfiguration(final EntityID target) throws ResolveException {
        val request = new FederationEntityConfigurationRequest(target).toHTTPRequest();
        try {
            val httpResponse = httpClient.send(request);
            markUnreachableOnServerError(target, httpResponse);
            val response = FederationEntityConfigurationResponse.parse(httpResponse);
            if (!response.indicatesSuccess()) {
                val error = response.toErrorResponse().getErrorObject();
                throw new ResolveException("Entity configuration error response from %s: %s %s"
                    .formatted(request.getURL(), error.getHTTPStatusCode(), error.getCode()), error);
            }
            return response.toSuccessResponse().getEntityStatement();
        } catch (final IOException e) {
            markUnreachable(target, e);
            throw new ResolveException("Couldn't retrieve entity configuration for %s: %s".formatted(request.getURL(), e.getMessage()), e);
        } catch (final ParseException e) {
            throw new ResolveException("Error parsing entity configuration response from %s: %s".formatted(request.getURL(), e.getMessage()), e);
        }
    }

    @Override
    public EntityStatement fetchEntityStatement(final URI federationAPIEndpoint, final EntityID issuer, final EntityID subject)
            throws ResolveException {
        val request = new FetchEntityStatementRequest(federationAPIEndpoint, issuer, subject).toHTTPRequest();
        try {
            val httpResponse = httpClient.send(request);
            markUnreachableOnServerError(issuer, httpResponse);
            val response = FetchEntityStatementResponse.parse(httpResponse);
            if (!response.indicatesSuccess()) {
                val error = response.toErrorResponse().getErrorObject();
                throw new ResolveException("Entity statement error response from %s at %s: %s %s"
                    .formatted(issuer, federationAPIEndpoint, error.getHTTPStatusCode(), error.getCode()), error);
            }
            return response.toSuccessResponse().getEntityStatement();
        } catch (final IOException e) {
            markUnreachable(issuer, e);
            throw new ResolveException("Couldn't fetch entity statement from %s at %s: %s".formatted(issuer, federationAPIEndpoint, e.getMessage()), e);
        } catch (final ParseException e) {
            throw new ResolveException("Error parsing entity statement response from %s at %s: %s"
                .formatted(issuer, federationAPIEndpoint, e.getMessage()), e);
        }
    }

    private static void markUnreachableOnServerError(final EntityID entityId, final HTTPResponse response) {
        if (response.getStatusCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            OidcFederationResolutionLimiter.currentBudget().ifPresent(budget -> budget.markUnreachable(entityId.getValue()));
        }
    }

    private static void markUnreachable(final EntityID entityId, final IOException failure) {
        if (!(failure instanceof OidcFederationHttpClient.OutOfTimeException) && !(failure instanceof OidcFederationHttpClient.TooManyRequestsException)) {
            OidcFederationResolutionLimiter.currentBudget().ifPresent(budget -> budget.markUnreachable(entityId.getValue()));
        }
    }
}
