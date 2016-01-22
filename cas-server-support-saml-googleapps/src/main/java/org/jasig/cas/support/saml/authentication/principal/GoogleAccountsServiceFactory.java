package org.jasig.cas.support.saml.authentication.principal;

import org.apache.commons.lang3.NotImplementedException;
import org.jasig.cas.authentication.principal.AbstractServiceFactory;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.jasig.cas.util.PublicKeyFactoryBean;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Builds {@link GoogleAccountsService} objects.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("googleAccountsServiceFactory")
public class GoogleAccountsServiceFactory extends AbstractServiceFactory<GoogleAccountsService> {

    private static final GoogleSaml20ObjectBuilder BUILDER = new GoogleSaml20ObjectBuilder();

    private PublicKey publicKey;

    private PrivateKey privateKey;


    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Value("${cas.saml.googleapps.publickey.file:}")
    private String publicKeyLocation;

    @Value("${cas.saml.googleapps.privatekey.file:}")
    private String privateKeyLocation;

    @Value("${cas.saml.googleapps.key.alg:}")
    private String keyAlgorithm;

    @Value("${cas.saml.response.skewAllowance:0}")
    private int skewAllowance;

    /**
     * Instantiates a new Google accounts service factory.
     */
    public GoogleAccountsServiceFactory() {}

    /**
     * Init public and private keys.
     */
    @PostConstruct
    public void init() {
        try {
            createGoogleAppsPrivateKey();
            createGoogleAppsPublicKey();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoogleAccountsService createService(final HttpServletRequest request) {

        if (this.publicKey == null || this.privateKey == null) {
            logger.debug("{} will not turn on because private/public keys are not configured",
                    getClass().getName());
            return null;
        }

        final String relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);

        final String xmlRequest = BUILDER.decodeSamlAuthnRequest(
                request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST));

        if (!StringUtils.hasText(xmlRequest)) {
            logger.trace("SAML AuthN request not found in the request");
            return null;
        }

        final Document document = BUILDER.constructDocumentFromXml(xmlRequest);

        if (document == null) {
            return null;
        }

        final Element root = document.getRootElement();
        final String assertionConsumerServiceUrl = root.getAttributeValue("AssertionConsumerServiceURL");
        final String requestId = root.getAttributeValue("ID");

        final GoogleAccountsServiceResponseBuilder builder =
            new GoogleAccountsServiceResponseBuilder(this.privateKey, this.publicKey, BUILDER);
        builder.setSkewAllowance(this.skewAllowance);
        return new GoogleAccountsService(assertionConsumerServiceUrl, relayState, requestId, builder);
    }

    @Override
    public GoogleAccountsService createService(final String id) {
        throw new NotImplementedException("This operation is not supported. ");
    }

    /**
     * Create the private key.
     * @throws Exception if key creation ran into an error
     */
    protected void createGoogleAppsPrivateKey() throws Exception {
        if (!isValidConfiguration()) {
            logger.debug("Google Apps private key bean will not be created, because it's not configured");
            return;
        }

        final PrivateKeyFactoryBean bean = new PrivateKeyFactoryBean();

        if (this.privateKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            bean.setLocation(new ClassPathResource(
                    org.apache.commons.lang3.StringUtils.removeStart(this.privateKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
        } else if (this.privateKeyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            bean.setLocation(new FileSystemResource(
                    org.apache.commons.lang3.StringUtils.removeStart(this.privateKeyLocation, ResourceUtils.FILE_URL_PREFIX)));
        } else {
            bean.setLocation(new FileSystemResource(this.privateKeyLocation));
        }

        bean.setAlgorithm(this.keyAlgorithm);
        logger.debug("Loading Google Apps private key from {} with key algorithm {}",
                bean.getLocation(), bean.getAlgorithm());

        bean.afterPropertiesSet();


        logger.debug("Creating Google Apps private key instance via {}", this.publicKeyLocation);
        this.privateKey = bean.getObject();
    }

    /**
     * Create the public key.
     * @throws Exception if key creation ran into an error
     */
    protected void createGoogleAppsPublicKey() throws Exception {
        if (!isValidConfiguration()) {
            logger.debug("Google Apps public key bean will not be created, because it's not configured");
            return;
        }

        final PublicKeyFactoryBean bean = new PublicKeyFactoryBean();
        if (this.publicKeyLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            bean.setLocation(new ClassPathResource(
                    org.apache.commons.lang3.StringUtils.removeStart(this.publicKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
        } else if (this.publicKeyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            bean.setLocation(new FileSystemResource(
                    org.apache.commons.lang3.StringUtils.removeStart(this.publicKeyLocation, ResourceUtils.FILE_URL_PREFIX)));
        } else {
            bean.setLocation(new FileSystemResource(this.publicKeyLocation));
        }

        bean.setAlgorithm(this.keyAlgorithm);

        logger.debug("Loading Google Apps public key from {} with key algorithm {}",
                bean.getResource(), bean.getAlgorithm());

        bean.afterPropertiesSet();

        logger.debug("Creating Google Apps public key instance via {}", this.publicKeyLocation);
        this.publicKey = bean.getObject();
    }

    private boolean isValidConfiguration() {
        return org.apache.commons.lang3.StringUtils.isNotBlank(this.privateKeyLocation)
                || org.apache.commons.lang3.StringUtils.isNotBlank(this.publicKeyLocation)
                || org.apache.commons.lang3.StringUtils.isNotBlank(this.keyAlgorithm);
    }

    public void setSkewAllowance(final int skewAllowance) {
        this.skewAllowance = skewAllowance;
    }
}
