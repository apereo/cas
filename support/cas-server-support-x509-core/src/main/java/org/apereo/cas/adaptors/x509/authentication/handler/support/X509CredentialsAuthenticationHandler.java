package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.NoOpRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

/**
 * Authentication Handler that accepts X509 Certificates, determines their
 * validity and ensures that they were issued by a trusted issuer. (targeted at
 * X509v3) Optionally checks KeyUsage extension in the user certificate
 * (container should do that too). Note that this handler trusts the servlet
 * container to do some initial checks like path validation. Deployers can
 * supply an optional pattern to match subject dns against to further restrict
 * certificates in case they are not using their own issuer. It's also possible
 * to specify a maximum pathLength for the SUPPLIED certificates. (note that
 * this does not include a pathLength check for the root certificate)
 * [PathLength is 0 for the CA certificate that issues the end-user certificate]
 *
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.4
 */
@Slf4j
public class X509CredentialsAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    /**
     * OID for KeyUsage X.509v3 extension field.
     */
    private static final String KEY_USAGE_OID = "2.5.29.15";

    /**
     * The compiled pattern supplied by the deployer.
     */
    private final Pattern regExTrustedIssuerDnPattern;

    /**
     * Deployer supplied setting for maximum pathLength in a SUPPLIED
     * certificate.
     */
    private final int maxPathLength;

    /**
     * Deployer supplied setting to allow unlimited pathLength in a SUPPLIED
     * certificate.
     */
    private final boolean maxPathLengthAllowUnspecified;

    /**
     * Deployer supplied setting to check the KeyUsage extension.
     */
    private final boolean checkKeyUsage;

    /**
     * Deployer supplied setting to force require the correct KeyUsage
     * extension.
     */
    private final boolean requireKeyUsage;

    /**
     * The compiled pattern for trusted DN's supplied by the deployer.
     */
    private final Pattern regExSubjectDnPattern;

    /**
     * Certificate revocation checker component.
     */
    private final RevocationChecker revocationChecker;

    public X509CredentialsAuthenticationHandler(final String name,
                                                final ServicesManager servicesManager,
                                                final PrincipalFactory principalFactory,
                                                final Pattern regExTrustedIssuerDnPattern,
                                                final int maxPathLength,
                                                final boolean maxPathLengthAllowUnspecified,
                                                final boolean checkKeyUsage,
                                                final boolean requireKeyUsage,
                                                final Pattern regExSubjectDnPattern,
                                                final RevocationChecker revocationChecker,
                                                final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.regExTrustedIssuerDnPattern = regExTrustedIssuerDnPattern;
        this.maxPathLength = maxPathLength;
        this.maxPathLengthAllowUnspecified = maxPathLengthAllowUnspecified;
        this.checkKeyUsage = checkKeyUsage;
        this.requireKeyUsage = requireKeyUsage;
        this.regExSubjectDnPattern = regExSubjectDnPattern;
        this.revocationChecker = revocationChecker;
    }

    public X509CredentialsAuthenticationHandler(final Pattern regExTrustedIssuerDnPattern) {
        this(regExTrustedIssuerDnPattern, new NoOpRevocationChecker());
    }

    public X509CredentialsAuthenticationHandler(final Pattern regExTrustedIssuerDnPattern,
                                                final boolean maxPathLengthAllowUnspecified,
                                                final Pattern regExSubjectDnPattern) {
        this(StringUtils.EMPTY, null, null, regExTrustedIssuerDnPattern,
            Integer.MAX_VALUE, maxPathLengthAllowUnspecified, false,
            false, regExSubjectDnPattern,
            new NoOpRevocationChecker(),
            null);
    }

    public X509CredentialsAuthenticationHandler(final Pattern regExTrustedIssuerDnPattern,
                                                final boolean maxPathLengthAllowUnspecified,
                                                final boolean checkKeyUsage,
                                                final boolean requireKeyUsage) {
        this(StringUtils.EMPTY, null, null, regExTrustedIssuerDnPattern,
            Integer.MAX_VALUE, maxPathLengthAllowUnspecified,
            checkKeyUsage, requireKeyUsage, null,
            new NoOpRevocationChecker(),
            null);
    }

    public X509CredentialsAuthenticationHandler(final Pattern regExTrustedIssuerDnPattern, final RevocationChecker revocationChecker) {
        this(StringUtils.EMPTY, null, null,
            regExTrustedIssuerDnPattern, Integer.MAX_VALUE, false,
            false, false, null,
            revocationChecker,
            null);
    }

    /**
     * Checks if critical extension oids contain the extension oid.
     *
     * @param certificate  the certificate
     * @param extensionOid the extension oid
     * @return true, if  critical
     */
    private static boolean isCritical(final X509Certificate certificate, final String extensionOid) {
        val criticalOids = certificate.getCriticalExtensionOIDs();
        return criticalOids != null && !criticalOids.isEmpty() && criticalOids.contains(extensionOid);
    }

    /**
     * Does principal name match pattern?
     *
     * @param principal the principal
     * @param pattern   the pattern
     * @return true, if successful
     */
    private static boolean doesNameMatchPattern(final Principal principal, final Pattern pattern) {
        if (pattern != null) {
            val name = principal.getName();
            val result = pattern.matcher(name).matches();
            LOGGER.debug("[{}] matches [{}] == [{}]", pattern.pattern(), name, result);
            return result;
        }
        return true;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && X509CertificateCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return X509CertificateCredential.class.isAssignableFrom(clazz);
    }

    /**
     * Note: The call to getBasicConstraints returns pathLenConstraints which is generally greater than or equal to zero
     * when this is a CA cert and -1 when it's not.
     *
     * @param credential Credential to authenticate.
     * @return Authn handler execution result.
     * @throws GeneralSecurityException security exception
     */
    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {

        val x509Credential = (X509CertificateCredential) credential;
        val certificates = x509Credential.getCertificates();

        var clientCert = (X509Certificate) null;
        var hasTrustedIssuer = false;
        for (var i = certificates.length - 1; i >= 0; i--) {
            val certificate = certificates[i];
            LOGGER.debug("Evaluating [{}]", CertUtils.toString(certificate));

            validate(certificate);

            if (!hasTrustedIssuer) {
                hasTrustedIssuer = isCertificateFromTrustedIssuer(certificate);
            }

            val pathLength = certificate.getBasicConstraints();
            if (pathLength < 0) {
                LOGGER.debug("Found valid client certificate");
                clientCert = certificate;
            } else {
                LOGGER.debug("Found valid CA certificate");
            }
        }
        if (hasTrustedIssuer && clientCert != null) {
            x509Credential.setCertificate(clientCert);
            return new DefaultAuthenticationHandlerExecutionResult(this, x509Credential,
                this.principalFactory.createPrincipal(x509Credential.getId()));
        }
        LOGGER.warn("Either client certificate could not be determined, or a trusted issuer could not be located");
        throw new FailedLoginException();
    }

    /**
     * Validate the X509Certificate received.
     *
     * @param cert the cert
     * @throws GeneralSecurityException the general security exception
     */
    private void validate(final X509Certificate cert) throws GeneralSecurityException {
        cert.checkValidity();
        this.revocationChecker.check(cert);

        val pathLength = cert.getBasicConstraints();
        if (pathLength < 0) {
            if (!isCertificateAllowed(cert)) {
                val msg = "Certificate subject does not match pattern " + this.regExSubjectDnPattern.pattern();
                LOGGER.error(msg);
                throw new FailedLoginException(msg);
            }
            if (this.checkKeyUsage && !isValidKeyUsage(cert)) {
                val msg = "Certificate keyUsage constraint forbids SSL client authentication.";
                LOGGER.error(msg);
                throw new FailedLoginException(msg);
            }
        } else {
            if (pathLength == Integer.MAX_VALUE && !this.maxPathLengthAllowUnspecified) {
                val msg = "Unlimited certificate path length not allowed by configuration.";
                LOGGER.error(msg);
                throw new FailedLoginException(msg);
            }
            if (pathLength > this.maxPathLength && pathLength < Integer.MAX_VALUE) {
                val msg = String.format("Certificate path length %s exceeds maximum value %s.", pathLength, this.maxPathLength);
                LOGGER.error(msg);
                throw new FailedLoginException(msg);
            }
        }
    }

    /**
     * Checks if is valid key usage. <p>
     * KeyUsage ::= BIT STRING { digitalSignature (0), nonRepudiation (1),
     * keyEncipherment (2), dataEncipherment (3), keyAgreement (4),
     * keyCertSign (5), cRLSign (6), encipherOnly (7), decipherOnly (8) }
     *
     * @param certificate the certificate
     * @return true, if  valid key usage
     */
    private boolean isValidKeyUsage(final X509Certificate certificate) {
        LOGGER.debug("Checking certificate keyUsage extension");
        val keyUsage = certificate.getKeyUsage();
        if (keyUsage == null) {
            LOGGER.warn("Configuration specifies checkKeyUsage but keyUsage extension not found in certificate.");
            return !this.requireKeyUsage;
        }

        val func = FunctionUtils.doIf(c -> isCritical(certificate, KEY_USAGE_OID) || requireKeyUsage,
            t -> {
                LOGGER.debug("KeyUsage extension is marked critical or required by configuration.");
                return keyUsage[0];
            },
            f -> {
                LOGGER.debug("KeyUsage digitalSignature=%s, Returning true since keyUsage validation not required by configuration.");
                return Boolean.TRUE;
            });
        return func.apply(certificate);
    }

    /**
     * Checks if is certificate allowed based no the pattern given.
     *
     * @param cert the cert
     * @return true, if  certificate allowed
     */
    private boolean isCertificateAllowed(final X509Certificate cert) {
        return doesNameMatchPattern(cert.getSubjectDN(), this.regExSubjectDnPattern);
    }

    /**
     * Checks if is certificate from trusted issuer based on the regex pattern.
     *
     * @param cert the cert
     * @return true, if  certificate from trusted issuer
     */
    private boolean isCertificateFromTrustedIssuer(final X509Certificate cert) {
        return doesNameMatchPattern(cert.getIssuerDN(), this.regExTrustedIssuerDnPattern);
    }
}
