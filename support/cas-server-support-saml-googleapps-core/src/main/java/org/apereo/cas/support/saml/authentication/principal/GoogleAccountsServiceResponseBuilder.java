package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.util.AbstractSamlObjectBuilder;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.UrlValidator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import java.io.Serial;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Builds the google accounts service response.
 *
 * @author Misagh Moayyed
 * @since 4.2
 * @deprecated Since 6.2, to be replaced with CAS SAML2 identity provider functionality.
 */
@Slf4j
@Getter
@Setter
@Deprecated(since = "6.2.0")
@EqualsAndHashCode(callSuper = true,
    of = {"publicKeyLocation", "privateKeyLocation", "keyAlgorithm", "samlObjectBuilder", "skewAllowance"})
public class GoogleAccountsServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    @Serial
    private static final long serialVersionUID = -4584732364007702423L;

    private final String publicKeyLocation;

    private final String privateKeyLocation;

    private final String keyAlgorithm;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    private final GoogleSaml20ObjectBuilder samlObjectBuilder;

    private final String skewAllowance;

    private final String casServerPrefix;

    public GoogleAccountsServiceResponseBuilder(final String privateKeyLocation,
                                                final String publicKeyLocation,
                                                final String keyAlgorithm,
                                                final ServicesManager servicesManager,
                                                final GoogleSaml20ObjectBuilder samlObjectBuilder,
                                                final String skewAllowance,
                                                final String casServerPrefix,
                                                final UrlValidator urlValidator) {
        super(servicesManager, urlValidator);
        this.privateKeyLocation = privateKeyLocation;
        this.publicKeyLocation = publicKeyLocation;
        this.keyAlgorithm = keyAlgorithm;
        this.skewAllowance = skewAllowance;
        this.samlObjectBuilder = samlObjectBuilder;
        this.casServerPrefix = casServerPrefix;
        FunctionUtils.doAndHandle(__ -> {
            createGoogleAppsPrivateKey();
            createGoogleAppsPublicKey();
        });
    }

    @Override
    public Response build(final WebApplicationService webApplicationService, final String serviceTicket,
                          final Authentication authentication) {
        val service = (GoogleAccountsService) webApplicationService;
        val parameters = new HashMap<String, String>();
        val samlResponse = constructSamlResponse(service, authentication);
        val signedResponse = AbstractSamlObjectBuilder.signSamlResponse(samlResponse, this.privateKey, publicKey);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, signedResponse);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, service.getRelayState());
        return buildPost(service, parameters);
    }

    @Override
    public boolean supports(final WebApplicationService service) {
        return service instanceof GoogleAccountsService;
    }

    protected String constructSamlResponse(final GoogleAccountsService service,
                                           final Authentication authentication) {
        val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        val notBeforeIssueInstant = ZonedDateTime.parse("2003-04-17T00:46:02Z");
        val registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            throw UnauthorizedServiceException.denied("Unauthorized: %s".formatted(service.getId()));
        }

        val principal = authentication.getPrincipal();
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(principal)
            .applicationContext(getSamlObjectBuilder().getOpenSamlConfigBean().getApplicationContext())
            .build();
        val userId = registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext);

        val response = samlObjectBuilder.newResponse(
            Saml20HexRandomIdGenerator.INSTANCE.getNewString(), currentDateTime, null, service);
        response.setStatus(samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));

        val sessionIndex = '_' + String.valueOf(RandomUtils.nextLong());
        val authnStatement = samlObjectBuilder.newAuthnStatement(AuthnContext.PASSWORD_AUTHN_CTX, currentDateTime, sessionIndex);
        val assertion = samlObjectBuilder.newAssertion(authnStatement, casServerPrefix,
            notBeforeIssueInstant, Saml20HexRandomIdGenerator.INSTANCE.getNewString());

        val skew = Beans.newDuration(skewAllowance).toSeconds();
        val conditions = samlObjectBuilder.newConditions(notBeforeIssueInstant,
            currentDateTime.plusSeconds(skew), service.getId());
        assertion.setConditions(conditions);

        val subjectConfirmation = samlObjectBuilder.newSubjectConfirmation(service.getId(),
            currentDateTime.plusSeconds(skew), service.getRequestId(), null,
            InetAddressUtils.getByName(service.getRequestId()));
        val subject = samlObjectBuilder.newSubject(NameIDType.EMAIL, userId, subjectConfirmation);
        assertion.setSubject(subject);

        response.getAssertions().add(assertion);

        val result = SamlUtils.transformSamlObject(samlObjectBuilder.getOpenSamlConfigBean(), response, true).toString();
        LOGGER.debug("Generated Google SAML response: [{}]", result);
        return result;
    }

    protected void createGoogleAppsPrivateKey() throws Exception {
        if (!isValidConfiguration()) {
            LOGGER.debug("Google Apps private key bean will not be created, because it's not configured");
            return;
        }

        val bean = new PrivateKeyFactoryBean();

        if (privateKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            bean.setLocation(new ClassPathResource(StringUtils.removeStart(privateKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
        } else if (privateKeyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            bean.setLocation(new FileSystemResource(StringUtils.removeStart(privateKeyLocation, ResourceUtils.FILE_URL_PREFIX)));
        } else {
            bean.setLocation(new FileSystemResource(privateKeyLocation));
        }

        bean.setAlgorithm(keyAlgorithm);
        LOGGER.debug("Loading Google Apps private key from [{}] with key algorithm [{}]",
            bean.getLocation(), bean.getAlgorithm());
        bean.afterPropertiesSet();
        LOGGER.debug("Creating Google Apps private key instance via [{}]", privateKeyLocation);
        privateKey = bean.getObject();
    }

    /**
     * Create the public key.
     *
     * @throws Exception if key creation ran into an error
     */
    protected void createGoogleAppsPublicKey() throws Exception {
        if (!isValidConfiguration()) {
            LOGGER.debug("Google Apps public key bean will not be created, because it's not configured");
            return;
        }

        var resource = (Resource) null;
        if (publicKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            resource = new ClassPathResource(StringUtils.removeStart(publicKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX));
        } else if (publicKeyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            resource = new FileSystemResource(StringUtils.removeStart(publicKeyLocation, ResourceUtils.FILE_URL_PREFIX));
        } else {
            resource = new FileSystemResource(publicKeyLocation);
        }
        val bean = new PublicKeyFactoryBean(resource, keyAlgorithm);
        LOGGER.debug("Loading Google Apps public key from [{}] with key algorithm [{}]",
            bean.getResource(), bean.getAlgorithm());
        bean.afterPropertiesSet();
        LOGGER.debug("Creating Google Apps public key instance via [{}]", publicKeyLocation);
        publicKey = bean.getObject();
    }

    private boolean isValidConfiguration() {
        return Stream.of(privateKeyLocation, publicKeyLocation, keyAlgorithm).anyMatch(StringUtils::isNotBlank);
    }
}
