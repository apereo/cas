package org.apereo.cas.support.saml.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ResourceUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the google accounts service response.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -4584732364007702423L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAccountsServiceResponseBuilder.class);

    @JsonIgnore
    private PrivateKey privateKey;

    @JsonIgnore
    private PublicKey publicKey;

    @JsonIgnore
    private ServicesManager servicesManager;

    @JsonProperty
    private final String publicKeyLocation;

    @JsonProperty
    private final String privateKeyLocation;

    @JsonProperty
    private final String keyAlgorithm;

    @JsonProperty
    private GoogleSaml20ObjectBuilder samlObjectBuilder;

    @JsonProperty
    private int skewAllowance;

    @JsonProperty
    private String casServerPrefix;

    /**
     * Instantiates a new Google accounts service response builder.
     *
     * @param privateKeyLocation the private key
     * @param publicKeyLocation  the public key
     * @param keyAlgorithm       the key algorithm
     * @param servicesManager    the services manager
     * @param samlObjectBuilder  the saml object builder
     * @param skewAllowance      the skew allowance
     * @param casServerPrefix    the cas server prefix
     */
    public GoogleAccountsServiceResponseBuilder(final String privateKeyLocation,
                                                final String publicKeyLocation,
                                                final String keyAlgorithm,
                                                final ServicesManager servicesManager,
                                                final GoogleSaml20ObjectBuilder samlObjectBuilder,
                                                final int skewAllowance,
                                                final String casServerPrefix) {

        this(privateKeyLocation, publicKeyLocation, keyAlgorithm, 0);
        this.samlObjectBuilder = samlObjectBuilder;
        this.servicesManager = servicesManager;
        this.skewAllowance = skewAllowance;
        this.casServerPrefix = casServerPrefix;
    }

    /**
     * Instantiates a new Google accounts service response builder.
     *
     * @param privateKeyLocation the private key
     * @param publicKeyLocation  the public key
     * @param keyAlgorithm       the key algorithm
     * @param skewAllowance      the skew allowance
     */
    @JsonCreator
    public GoogleAccountsServiceResponseBuilder(@JsonProperty("privateKeyLocation") final String privateKeyLocation,
                                                @JsonProperty("publicKeyLocation") final String publicKeyLocation,
                                                @JsonProperty("keyAlgorithm") final String keyAlgorithm,
                                                @JsonProperty("skewAllowance") final int skewAllowance) {
        try {
            this.privateKeyLocation = privateKeyLocation;
            this.publicKeyLocation = publicKeyLocation;
            this.keyAlgorithm = keyAlgorithm;
            this.skewAllowance = skewAllowance;

            createGoogleAppsPrivateKey();
            createGoogleAppsPublicKey();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Response build(final WebApplicationService webApplicationService, final String serviceTicket,
                          final Authentication authentication) {
        final GoogleAccountsService service = (GoogleAccountsService) webApplicationService;
        final Map<String, String> parameters = new HashMap<>();
        final String samlResponse = constructSamlResponse(service, authentication);
        final String signedResponse = this.samlObjectBuilder.signSamlResponse(samlResponse, this.privateKey, this.publicKey);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, signedResponse);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, service.getRelayState());
        return buildPost(service, parameters);
    }

    /**
     * Construct SAML response.
     * <a href="http://bit.ly/1uI8Ggu">See this reference for more info.</a>
     *
     * @param service        the service
     * @param authentication the authentication
     * @return the SAML response
     */
    protected String constructSamlResponse(final GoogleAccountsService service,
                                           final Authentication authentication) {
        final ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime notBeforeIssueInstant = ZonedDateTime.parse("2003-04-17T00:46:02Z");
        final RegisteredService registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        
        final Principal principal = authentication.getPrincipal();
        final String userId = registeredService.getUsernameAttributeProvider()
                .resolveUsername(principal, service, registeredService);

        final org.opensaml.saml.saml2.core.Response response = this.samlObjectBuilder.newResponse(
                this.samlObjectBuilder.generateSecureRandomId(), currentDateTime, null, service);
        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));

        final String sessionIndex = '_' + String.valueOf(Math.abs(RandomUtils.getInstanceNative().nextLong()));
        final AuthnStatement authnStatement = this.samlObjectBuilder.newAuthnStatement(AuthnContext.PASSWORD_AUTHN_CTX, currentDateTime, sessionIndex);
        final Assertion assertion = this.samlObjectBuilder.newAssertion(authnStatement, casServerPrefix,
                notBeforeIssueInstant, this.samlObjectBuilder.generateSecureRandomId());

        final Conditions conditions = this.samlObjectBuilder.newConditions(notBeforeIssueInstant,
                currentDateTime.plusSeconds(this.skewAllowance), service.getId());
        assertion.setConditions(conditions);
        
        final Subject subject = this.samlObjectBuilder.newSubject(NameID.EMAIL, userId,
                service.getId(), currentDateTime.plusSeconds(this.skewAllowance), service.getRequestId(), null);
        assertion.setSubject(subject);

        response.getAssertions().add(assertion);

        final String result = SamlUtils.transformSamlObject(this.samlObjectBuilder.getConfigBean(), response, true).toString();
        LOGGER.debug("Generated Google SAML response: [{}]", result);
        return result;
    }

    /**
     * Sets the allowance for time skew in seconds
     * between CAS and the client server.  Default 0s.
     * This value will be subtracted from the current time when setting the SAML
     * {@code NotBeforeDate} attribute, thereby allowing for the
     * CAS server to be ahead of the client by as much as the value defined here.
     *
     * @param skewAllowance Number of seconds to allow for variance.
     */
    public void setSkewAllowance(final int skewAllowance) {
        LOGGER.debug("Using [{}] seconds as skew allowance.", skewAllowance);
        this.skewAllowance = skewAllowance;
    }

    /**
     * Create the private key.
     *
     * @throws Exception if key creation ran into an error
     */
    protected void createGoogleAppsPrivateKey() throws Exception {
        if (!isValidConfiguration()) {
            LOGGER.debug("Google Apps private key bean will not be created, because it's not configured");
            return;
        }

        final PrivateKeyFactoryBean bean = new PrivateKeyFactoryBean();

        if (this.privateKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            bean.setLocation(new ClassPathResource(StringUtils.removeStart(this.privateKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
        } else if (this.privateKeyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            bean.setLocation(new FileSystemResource(StringUtils.removeStart(this.privateKeyLocation, ResourceUtils.FILE_URL_PREFIX)));
        } else {
            bean.setLocation(new FileSystemResource(this.privateKeyLocation));
        }

        bean.setAlgorithm(this.keyAlgorithm);
        LOGGER.debug("Loading Google Apps private key from [{}] with key algorithm [{}]",
                bean.getLocation(), bean.getAlgorithm());
        bean.afterPropertiesSet();
        LOGGER.debug("Creating Google Apps private key instance via [{}]", this.privateKeyLocation);
        this.privateKey = bean.getObject();
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

        final PublicKeyFactoryBean bean = new PublicKeyFactoryBean();
        if (this.publicKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            bean.setLocation(new ClassPathResource(StringUtils.removeStart(this.publicKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
        } else if (this.publicKeyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            bean.setLocation(new FileSystemResource(StringUtils.removeStart(this.publicKeyLocation, ResourceUtils.FILE_URL_PREFIX)));
        } else {
            bean.setLocation(new FileSystemResource(this.publicKeyLocation));
        }

        bean.setAlgorithm(this.keyAlgorithm);
        LOGGER.debug("Loading Google Apps public key from [{}] with key algorithm [{}]",
                bean.getResource(), bean.getAlgorithm());
        bean.afterPropertiesSet();
        LOGGER.debug("Creating Google Apps public key instance via [{}]", this.publicKeyLocation);
        this.publicKey = bean.getObject();
    }

    private boolean isValidConfiguration() {
        return StringUtils.isNotBlank(this.privateKeyLocation)
                || StringUtils.isNotBlank(this.publicKeyLocation)
                || StringUtils.isNotBlank(this.keyAlgorithm);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final GoogleAccountsServiceResponseBuilder rhs = (GoogleAccountsServiceResponseBuilder) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .appendSuper(super.equals(obj))
                .append(this.publicKeyLocation, rhs.publicKeyLocation)
                .append(this.privateKeyLocation, rhs.privateKeyLocation)
                .append(this.keyAlgorithm, rhs.keyAlgorithm)
                .append(this.samlObjectBuilder, rhs.samlObjectBuilder)
                .append(this.skewAllowance, rhs.skewAllowance)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(publicKeyLocation)
                .append(privateKeyLocation)
                .append(keyAlgorithm)
                .append(skewAllowance)
                .append(samlObjectBuilder)
                .toHashCode();
    }

    @Override
    public boolean supports(final WebApplicationService service) {
        return service instanceof GoogleAccountsService;
    }
}
