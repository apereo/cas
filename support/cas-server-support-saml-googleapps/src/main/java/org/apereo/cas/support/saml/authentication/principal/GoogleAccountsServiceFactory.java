package org.apereo.cas.support.saml.authentication.principal;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Builds {@link GoogleAccountsService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceFactory extends AbstractServiceFactory<GoogleAccountsService> {
    
    private GoogleSaml20ObjectBuilder builder;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    private String publicKeyLocation;

    private String privateKeyLocation;

    private String keyAlgorithm;
    
    private int skewAllowance;

    /**
     * Instantiates a new Google accounts service factory.
     */
    public GoogleAccountsServiceFactory() {
    }

    /**
     * Init public and private keys.
     *
     * @throws RuntimeException if key creation encountered an error.
     */
    @PostConstruct
    public void init() {
        try {
            createGoogleAppsPrivateKey();
            createGoogleAppsPublicKey();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
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

        final String xmlRequest = this.builder.decodeSamlAuthnRequest(
                request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST));

        if (StringUtils.isBlank(xmlRequest)) {
            logger.trace("SAML AuthN request not found in the request");
            return null;
        }

        final Document document = this.builder.constructDocumentFromXml(xmlRequest);

        if (document == null) {
            return null;
        }

        final Element root = document.getRootElement();
        final String assertionConsumerServiceUrl = root.getAttributeValue("AssertionConsumerServiceURL");
        final String requestId = root.getAttributeValue("ID");

        final GoogleAccountsServiceResponseBuilder responseBuilder =
                new GoogleAccountsServiceResponseBuilder(this.privateKey, this.publicKey, this.builder);
        responseBuilder.setSkewAllowance(this.skewAllowance);
        return new GoogleAccountsService(assertionConsumerServiceUrl, relayState, requestId, responseBuilder);
    }

    @Override
    public GoogleAccountsService createService(final String id) {
        throw new NotImplementedException("This operation is not supported.");
    }

    /**
     * Create the private key.
     *
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


        logger.debug("Creating Google Apps private key instance via {}", this.privateKeyLocation);
        this.privateKey = bean.getObject();
    }

    /**
     * Create the public key.
     *
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
                    StringUtils.removeStart(this.publicKeyLocation, ResourceUtils.CLASSPATH_URL_PREFIX)));
        } else if (this.publicKeyLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            bean.setLocation(new FileSystemResource(
                    StringUtils.removeStart(this.publicKeyLocation, ResourceUtils.FILE_URL_PREFIX)));
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
        return StringUtils.isNotBlank(this.privateKeyLocation)
                || StringUtils.isNotBlank(this.publicKeyLocation)
                || StringUtils.isNotBlank(this.keyAlgorithm);
    }

    public void setSkewAllowance(final int skewAllowance) {
        this.skewAllowance = skewAllowance;
    }

    public void setPublicKeyLocation(final String publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public void setPrivateKeyLocation(final String privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public void setKeyAlgorithm(final String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public void setBuilder(final GoogleSaml20ObjectBuilder builder) {
        this.builder = builder;
    }
}
