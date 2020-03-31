package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link X509Properties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class X509Properties implements Serializable {

    private static final long serialVersionUID = -9032744084671270366L;

    /**
     * Default setting to limit the number of intermediate certificates.
     */
    private static final int DEFAULT_MAXPATHLENGTH = 1;

    /**
     * Default setting whether to allow unspecified number of intermediate certificates.
     */
    private static final boolean DEFAULT_MAXPATHLENGTH_ALLOW_UNSPECIFIED = false;

    /**
     * Default setting to check keyUsage extension.
     */
    private static final boolean DEFAULT_CHECK_KEYUSAGE = false;

    /**
     * Default setting to force require "KeyUsage" extension.
     */
    private static final boolean DEFAULT_REQUIRE_KEYUSAGE = false;

    /**
     * Default name of header containing certificate from the proxy.
     * <p>
     * Format of header should be compatible with Tomcat SSLValve.
     */
    private static final String DEFAULT_CERT_HEADER_NAME = "ssl_client_cert";

    /**
     * Threshold value if expired CRL revocation policy is to be handled via threshold.
     */
    private int revocationPolicyThreshold = 172_800;

    /**
     * Whether revocation checking should check all resources, or stop at first one.
     */
    private boolean checkAll;

    /**
     * The refresh interval of the internal scheduler in cases where CRL revocation checking
     * is done via resources.
     */
    private int refreshIntervalSeconds = 3_600;

    /**
     * The principal descriptor used for principal resolution
     * when type is set to {@link PrincipalTypes#SUBJECT}.
     */
    private String principalDescriptor;

    /**
     * When CRL revocation checking is done via distribution points,
     * decide if fetch failures should throw errors.
     */
    private boolean throwOnFetchFailure;

    /**
     * Indicates the type of principal resolution for X509.
     * <ul>
     * <li>{@code SERIAL_NO}: Resolve the principal by the serial number with a configurable radix,
     * ranging from 2 to 36. If radix is 16, then the serial number could be filled with leading zeros to even the number of digits.</li>
     * <li>{@code SERIAL_NO_DN}: Resolve the principal by serial number and issuer dn.</li>
     * <li>{@code SUBJECT}: Resolve the principal by extracting one or more attribute values from the
     * certificate subject DN and combining them with intervening delimiters.</li>
     * <li>{@code SUBJECT_ALT_NAME}: Resolve the principal by the subject alternative name extension.</li>
     * <li>{@code SUBJECT_DN}: The default type; Resolve the principal by the certificate’s subject dn.</li>
     * </ul>
     */
    private PrincipalTypes principalType;

    /**
     * Revocation certificate checking can be carried out in one of the following ways:
     * <ul>
     * <li>{@code NONE}: No revocation is performed.</li>
     * <li>{@code CRL}: The CRL URI(s) mentioned in the certificate cRLDistributionPoints extension field.
     * Caches are available to prevent excessive IO against CRL endpoints; CRL data is fetched if does not exist in the cache or if it is expired.</li>
     * <li>{@code RESOURCE}: A CRL hosted at a fixed location. The CRL is fetched at periodic intervals and cached.</li>
     * </ul>
     */
    private String revocationChecker = "NONE";

    /**
     * To fetch CRLs, the following options are available:
     * <ul>
     * <li>{@code RESOURCE}: By default, all revocation checks use fixed resources to fetch the CRL resource from the specified location.</li>
     * <li>{@code LDAP}: A CRL resource may be fetched from a pre-configured attribute, in the event that the CRL resource location is an LDAP URI.</li>
     * </ul>
     */
    private String crlFetcher = "RESOURCE";

    /**
     * List of CRL resources to use for fetching.
     */
    private List<String> crlResources = new ArrayList<>(0);

    /**
     * When CRLs are cached, indicate maximum number of elements kept in memory.
     */
    private int cacheMaxElementsInMemory = 1_000;

    /**
     * When CRLs are cached, indicate whether cache should overflow to disk.
     */
    private boolean cacheDiskOverflow;

    /**
     * Size of cache on disk.
     */
    private String cacheDiskSize = "100MB";

    /**
     * When CRLs are cached, indicate if cache items should be eternal.
     */
    private boolean cacheEternal;

    /**
     * When CRLs are cached, indicate the time-to-live of cache items.
     */
    private long cacheTimeToLiveSeconds = TimeUnit.HOURS.toSeconds(4);

    /**
     * If the CRL resource is unavailable, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code RESOURCE}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlResourceUnavailablePolicy = "DENY";

    /**
     * If the CRL resource has expired, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code RESOURCE}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlResourceExpiredPolicy = "DENY";

    /**
     * If the CRL is unavailable, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code CRL}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlUnavailablePolicy = "DENY";

    /**
     * If the CRL has expired, activate the this policy.
     * Activated if {@link #revocationChecker} is {@code CRL}.
     * Accepted values are:
     * <ul>
     * <li>{@code ALLOW}: Allow authentication to proceed.</li>
     * <li>{@code DENY}: Deny authentication and block.</li>
     * <li>{@code THRESHOLD}: Applicable to CRL expiration, throttle the request whereby expired
     * data is permitted up to a threshold period of time but not afterward.</li>
     * </ul>
     */
    private String crlExpiredPolicy = "DENY";

    /**
     * Principal resolution properties.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * LDAP settings when fetching CRLs from LDAP.
     */
    private Ldap ldap = new Ldap();

    /**
     * The compiled pattern supplied by the deployer.
     */
    private String regExTrustedIssuerDnPattern;

    /**
     * Deployer supplied setting for maximum pathLength in a SUPPLIED
     * certificate.
     */
    private int maxPathLength = DEFAULT_MAXPATHLENGTH;

    /**
     * Deployer supplied setting to allow unlimited pathLength in a SUPPLIED
     * certificate.
     */
    private boolean maxPathLengthAllowUnspecified = DEFAULT_MAXPATHLENGTH_ALLOW_UNSPECIFIED;

    /**
     * Deployer supplied setting to check the KeyUsage extension.
     */
    private boolean checkKeyUsage = DEFAULT_CHECK_KEYUSAGE;

    /**
     * Deployer supplied setting to force require the correct KeyUsage
     * extension.
     */
    private boolean requireKeyUsage = DEFAULT_REQUIRE_KEYUSAGE;

    /**
     * The pattern that authorizes an acceptable certificate by its subject dn.
     */
    private String regExSubjectDnPattern = ".+";

    /**
     * The authentication handler name.
     */
    private String name;

    /**
     * The order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Whether to extract certificate from request.
     * <p>
     * The default implementation extracts certificate from header via Tomcat SSLValve parsing logic
     * and using the {@link #DEFAULT_CERT_HEADER_NAME} header.
     * Must be false by default because if someone enables it they need to make sure they are
     * behind proxy that won't let the header arrive directly from the browser.
     */
    private boolean extractCert;

    /**
     * The name of the header to consult for an X509 cert (e.g. when behind proxy).
     */
    private String sslHeaderName = DEFAULT_CERT_HEADER_NAME;

    /**
     * Principal resolver properties for SUBJECT_DN resolver type.
     */
    @NestedConfigurationProperty
    private SubjectDnPrincipalResolverProperties subjectDn = new SubjectDnPrincipalResolverProperties();

    /**
     * Principal resolver properties for CN_EDIPI resolver type.
     */
    @NestedConfigurationProperty
    private CnEdipiPrincipalResolverProperties cnEdipi = new CnEdipiPrincipalResolverProperties();

    /**
     * Principal resolver properties for SUBJECT_ALT_NAME resolver type.
     */
    @NestedConfigurationProperty
    private SubjectAltNamePrincipalResolverProperties subjectAltName = new SubjectAltNamePrincipalResolverProperties();

    /**
     * Principal resolver properties for RFC822_EMAIL resolver type.
     */
    @NestedConfigurationProperty
    private Rfc822EmailPrincipalResolverProperties rfc822Email = new Rfc822EmailPrincipalResolverProperties();

    /**
     * Principal resolver properties for SERIAL_NO_DN resolver type.
     */
    @NestedConfigurationProperty
    private SerialNoDnPrincipalResolverProperties serialNoDn = new SerialNoDnPrincipalResolverProperties();

    /**
     * Principal resolver properties for SERIAL_NO resolver type.
     */
    @NestedConfigurationProperty
    private SerialNoPrincipalResolverProperties serialNo = new SerialNoPrincipalResolverProperties();

    /**
     * The webflow configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties(100);

    /**
     * The  Principal types.
     */
    public enum PrincipalTypes {

        /**
         * Create principal by subject.
         */
        SUBJECT,
        /**
         * Create principal by subject DN.
         */
        SUBJECT_DN,
        /**
         * Create principal by serial no.
         */
        SERIAL_NO,
        /**
         * Create principal by serial no and DN.
         */
        SERIAL_NO_DN,
        /**
         * Create principal by subject alternative name.
         */
        SUBJECT_ALT_NAME,
        /**
         * Create principal by common name and EDIPI.
         */
        CN_EDIPI,
        /**
         * Create principal from the RFC822 type name (aka email address) in the subject alternative name field.
         * The subject alternative name field contains a list of various types of names, one type is RFC822 e-mail
         * address. This will return the first e-mail address that is found (if there are more than one).
         */
        RFC822_EMAIL
    }

    @Getter
    @Setter
    public static class Ldap extends AbstractLdapSearchProperties {

        private static final long serialVersionUID = -1655068554291000206L;

        /**
         * The LDAP attribute that holds the certificate revocation list.
         */
        private String certificateAttribute = "certificateRevocationList";
    }
}
