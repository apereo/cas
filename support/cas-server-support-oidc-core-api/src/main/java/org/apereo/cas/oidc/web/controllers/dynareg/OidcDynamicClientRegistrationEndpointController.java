package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OidcDynamicClientRegistrationEndpointController extends BaseOAuth20Controller {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final int GENERATED_CLIENT_NAME_LENGTH = 8;

    public OidcDynamicClientRegistrationEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request.
     *
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     * @return the model and view
     */
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handleRequestInternal(@RequestBody final String jsonInput,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response) {
        try {
            val registrationRequest = (OidcClientRegistrationRequest) getOAuthConfigurationContext()
                .getClientRegistrationRequestSerializer().from(jsonInput);
            LOGGER.debug("Received client registration request [{}]", registrationRequest);

            val registeredService = new OidcRegisteredService();

            if (StringUtils.isNotBlank(registrationRequest.getClientName())) {
                registeredService.setName(registrationRequest.getClientName());
            } else {
                registeredService.setName(RandomStringUtils.randomAlphabetic(GENERATED_CLIENT_NAME_LENGTH));
            }
            registeredService.setSectorIdentifierUri(registrationRequest.getSectorIdentifierUri());
            registeredService.setSubjectType(registrationRequest.getSubjectType());
            if (StringUtils.equalsIgnoreCase(OidcSubjectTypes.PAIRWISE.getType(), registeredService.getSubjectType())) {
                registeredService.setUsernameAttributeProvider(new PairwiseOidcRegisteredServiceUsernameAttributeProvider());
            }

            if (StringUtils.isNotBlank(registrationRequest.getJwksUri())) {
                registeredService.setJwks(registrationRequest.getJwksUri());
                registeredService.setSignIdToken(true);
            }
            val uri = String.join("|", registrationRequest.getRedirectUris());
            registeredService.setServiceId(uri);

            registeredService.setClientId(getOAuthConfigurationContext().getClientIdGenerator().getNewString());
            registeredService.setClientSecret(getOAuthConfigurationContext().getClientSecretGenerator().getNewString());
            registeredService.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            registeredService.setLogoutUrl(
                org.springframework.util.StringUtils.collectionToCommaDelimitedString(registrationRequest.getPostLogoutRedirectUris()));

            if (StringUtils.isNotBlank(registrationRequest.getLogo())) {
                registeredService.setLogo(registrationRequest.getLogo());
            }
            if (StringUtils.isNotBlank(registrationRequest.getPolicyUri())) {
                registeredService.setInformationUrl(registrationRequest.getPolicyUri());
            }
            if (StringUtils.isNotBlank(registrationRequest.getTermsOfUseUri())) {
                registeredService.setPrivacyUrl(registrationRequest.getTermsOfUseUri());
            }

            val properties = getOAuthConfigurationContext().getCasProperties();
            val supportedScopes = new HashSet<String>(properties.getAuthn().getOidc().getScopes());
            val prefix = properties.getServer().getPrefix();
            val clientResponse = OidcClientRegistrationUtils.getClientRegistrationResponse(registeredService, prefix);

            val accessToken = generateRegistrationAccessToken(request, response, registeredService, registrationRequest);
            clientResponse.setRegistrationAccessToken(accessToken.getId());

            registeredService.setScopes(supportedScopes);
            val processedScopes = new LinkedHashSet<String>(supportedScopes);
            registeredService.setScopes(processedScopes);

            if (!registrationRequest.getDefaultAcrValues().isEmpty()) {
                val multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
                multifactorPolicy.setMultifactorAuthenticationProviders(new HashSet<>(registrationRequest.getDefaultAcrValues()));
                registeredService.setMultifactorPolicy(multifactorPolicy);
            }

            if (StringUtils.isNotBlank(registrationRequest.getIdTokenSignedResponseAlg())) {
                registeredService.setIdTokenSigningAlg(registrationRequest.getIdTokenSignedResponseAlg());
                registeredService.setSignIdToken(true);
            }

            if (StringUtils.isNotBlank(registrationRequest.getIdTokenEncryptedResponseAlg())) {
                registeredService.setIdTokenEncryptionAlg(registrationRequest.getIdTokenEncryptedResponseAlg());
                registeredService.setEncryptIdToken(true);
            }

            if (StringUtils.isNotBlank(registrationRequest.getIdTokenEncryptedResponseEncoding())) {
                registeredService.setIdTokenEncryptionEncoding(registrationRequest.getIdTokenEncryptedResponseEncoding());
                registeredService.setEncryptIdToken(true);
            }

            registrationRequest.getContacts().forEach(c -> {
                val contact = new DefaultRegisteredServiceContact();
                contact.setName(c);
                registeredService.getContacts().add(contact);
            });

            registeredService.setDescription("Registered service ".concat(registeredService.getName()));
            registeredService.setDynamicallyRegistered(true);


            validate(registrationRequest, registeredService);

            getOAuthConfigurationContext().getProfileScopeToAttributesFilter().reconcile(registeredService);
            return new ResponseEntity<>(clientResponse, HttpStatus.CREATED);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            val map = new HashMap<String, String>();
            map.put("error", "invalid_client_metadata");
            map.put("error_description", StringUtils.defaultString(e.getMessage(), "None"));
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
    }

    @SneakyThrows
    private void validate(final OidcClientRegistrationRequest registrationRequest, final OidcRegisteredService registeredService) {
        if (StringUtils.isNotBlank(registeredService.getSectorIdentifierUri())) {
            HttpResponse sectorResponse = null;
            try {
                sectorResponse = HttpUtils.executeGet(registeredService.getSectorIdentifierUri());
                if (sectorResponse != null && sectorResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                    val result = IOUtils.toString(sectorResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                    val urls = MAPPER.readValue(result, List.class);
                    if (!urls.equals(registrationRequest.getRedirectUris())) {
                        throw new IllegalArgumentException("Invalid sector identifier uri");
                    }
                }
            } finally {
                HttpUtils.close(sectorResponse);
            }
        }
    }


    /**
     * Generate registration access token access token.
     *
     * @param request             the request
     * @param response            the response
     * @param registeredService   the registered service
     * @param registrationRequest the registration request
     * @return the access token
     */
    @SneakyThrows
    protected AccessToken generateRegistrationAccessToken(final HttpServletRequest request,
                                                          final HttpServletResponse response,
                                                          final OidcRegisteredService registeredService,
                                                          final OidcClientRegistrationRequest registrationRequest) {
        val authn = DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(registeredService.getClientId()))
            .build();
        val clientConfigUri = OidcClientRegistrationUtils.getClientConfigurationUri(registeredService,
            getOAuthConfigurationContext().getCasProperties().getServer().getPrefix());
        val service = getOAuthConfigurationContext().getWebApplicationServiceServiceFactory().createService(clientConfigUri);
        val accessToken = getOAuthConfigurationContext().getAccessTokenFactory()
            .create(service, authn, List.of(OidcConstants.CLIENT_REGISTRATION_SCOPE), registeredService.getClientId());
        getOAuthConfigurationContext().getTicketRegistry().addTicket(accessToken);
        return accessToken;
    }
}
