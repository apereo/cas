package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

/**
 * This is {@link OAuth20X509Authenticator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OAuth20X509Authenticator implements OAuth20ClientCredentialsAuthenticator {
    private static final String SUBJECT_ALT_NAME_SPIFFE_PREFIX = "spiffe://";

    private static final Pattern COMMON_NAME_PATTERN = Pattern.compile("CN=(.*?)(?:,|$)");

    private static final String X509_SAN_EMAIL = "x509-sanEmail";

    private static final String X509_SAN_DNS = "x509-sanDNS";

    private static final String X509_SAN_IP = "x509-sanIP";

    private static final String X509_SAN_URI = "x509-sanURI";

    private final ServicesManager servicesManager;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    @Override
    public Optional<OAuth20UserProfile> authenticate(final OAuth20ClientAuthenticationToken token) {
        val certificate = (X509Certificate) Objects.requireNonNull(token.getCredentials());
        val request = token.getRequest();
        val registeredService = locateRegisteredService(request, certificate);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        if (!isAuthenticationMethodSupported(request, registeredService)) {
            LOGGER.warn("TLS authentication method is not supported for service [{}]", registeredService.getName());
            return Optional.empty();
        }

        val registeredSubjectAltNames = CollectionUtils.<String, String>wrap(
            X509_SAN_EMAIL, registeredService.getTlsClientAuthSanEmail(),
            X509_SAN_DNS, registeredService.getTlsClientAuthSanDns(),
            X509_SAN_IP, registeredService.getTlsClientAuthSanIp(),
            X509_SAN_URI, registeredService.getTlsClientAuthSanUri()
        );
        if (StringUtils.isBlank(registeredService.getTlsClientAuthSubjectDn())
            && registeredSubjectAltNames.values().stream().allMatch(StringUtils::isBlank)
            && !hasSpiffeIdOf(certificate, registeredService.getClientId())) {
            throw new BadCredentialsException("Client " + registeredService.getClientId()
                + " registers neither the subject DN nor a subject alternative name of its certificate");
        }
        val registeredSubject = registeredService.getTlsClientAuthSubjectDn();
        val profile = buildCertificateProfile(certificate, StringUtils.isBlank(registeredSubject) ? null : registeredSubject);
        profile.addAttribute(OAuth20Constants.X509_CERTIFICATE_DIGEST, OAuth20Utils.computeCertificateThumbprint(certificate));
        profile.addAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, "X.509");
        profile.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val accepted = registeredSubjectAltNames
            .entrySet()
            .stream()
            .allMatch(entry -> isAcceptableX509Attribute(profile, entry.getValue(), entry.getKey()));
        if (!accepted) {
            throw new BadCredentialsException("Unable to accept certificate");
        }
        return Optional.of(profile);
    }

    /**
     * Build the profile of the certificate. The subject pattern is first compared with the certificate's subject as a
     * distinguished name, as RFC 8705 registers {@code tls_client_auth_subject_dn}. Otherwise a subject pattern with a
     * capturing group is a regular expression, as pac4j's {@code X509Authenticator} read it: the identifier is the single
     * match of the first group in the RFC 2253 subject DN, and a blank match refuses the certificate. Without a subject
     * pattern, when the client registered only subject alternative names or is known by its SPIFFE ID, the subject is not
     * matched. When the subject matches as a distinguished name or is not matched, the identifier is the certificate's
     * common name when it has exactly one, as the default pattern of pac4j made it, or else its SPIFFE ID, its subject DN,
     * or its first subject alternative name, since RFC 5280 allows an empty subject and SPIFFE certificates often carry no
     * common name. The attributes carry the encoded certificate, its subject and issuer, validity, signature algorithm and
     * subject alternative names.
     *
     * @param certificate    the certificate
     * @param subjectPattern the subject pattern the client registered, if any
     * @return the profile
     * @throws BadCredentialsException when the certificate's subject does not match the subject pattern
     */
    protected DefaultOAuth20UserProfile buildCertificateProfile(final X509Certificate certificate, final @Nullable String subjectPattern) {
        val subjectDN = certificate.getSubjectX500Principal().getName(X500Principal.RFC2253);
        LOGGER.debug("Certificate subject DN is [{}]", subjectDN);
        val profile = new DefaultOAuth20UserProfile(resolveProfileId(certificate, subjectDN, subjectPattern),
            OAuth20UserProfile.X509_PROFILE_HANDLER_NAME);
        try {
            profile.addAttribute("x509-certificate", Base64.getEncoder().encodeToString(certificate.getEncoded()));
        } catch (final CertificateEncodingException e) {
            throw new BadCredentialsException("Unable to encode the certificate", e);
        }
        profile.addAttribute("x509-subjectDN", subjectDN);
        profile.addAttribute("x509-notAfter", certificate.getNotAfter());
        profile.addAttribute("x509-notBefore", certificate.getNotBefore());
        profile.addAttribute("x509-sigAlgName", certificate.getSigAlgName());
        profile.addAttribute("x509-sigAlgOid", certificate.getSigAlgOID());
        addSubjectAlternativeNames(profile, certificate, X509_SAN_EMAIL, 1);
        addSubjectAlternativeNames(profile, certificate, X509_SAN_DNS, 2);
        addSubjectAlternativeNames(profile, certificate, X509_SAN_URI, 6);
        addSubjectAlternativeNames(profile, certificate, X509_SAN_IP, 7);
        addSubjectAlternativeNames(profile, certificate, "x509-sanRegisteredID", 8);
        addSubjectAlternativeNames(profile, certificate, "x509-sanDirectory", 9);
        profile.addAttribute("x509-issuer", certificate.getIssuerX500Principal().toString());
        profile.addAttribute("x509-issuerX500", certificate.getIssuerX500Principal().getName());
        return profile;
    }

    private static String resolveProfileId(final X509Certificate certificate, final String subjectDN, final @Nullable String subjectPattern) {
        if (subjectPattern == null || isSubjectDistinguishedName(certificate, subjectPattern)) {
            return findSingleMatch(COMMON_NAME_PATTERN, subjectDN)
                .or(() -> findSubjectAlternativeNames(certificate)
                    .filter(name -> Strings.CI.startsWith(name, SUBJECT_ALT_NAME_SPIFFE_PREFIX))
                    .findFirst())
                .or(() -> Optional.of(subjectDN).filter(StringUtils::isNotBlank))
                .or(() -> findSubjectAlternativeNames(certificate).findFirst())
                .orElseThrow(() -> new BadCredentialsException("Certificate carries neither a subject nor a subject alternative name"));
        }
        val pattern = compileSubjectPattern(subjectPattern);
        if (pattern != null && pattern.matcher(StringUtils.EMPTY).groupCount() > 0) {
            return findSingleMatch(pattern, subjectDN).orElseThrow(() ->
                new BadCredentialsException("No single match for pattern " + subjectPattern + " in subject DN " + subjectDN));
        }
        throw new BadCredentialsException("Certificate subject " + subjectDN + " does not match " + subjectPattern);
    }

    private static Optional<String> findSingleMatch(final Pattern pattern, final String subjectDN) {
        val matcher = pattern.matcher(subjectDN);
        val matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches.size() == 1 && StringUtils.isNotBlank(matches.getFirst()) ? Optional.of(matches.getFirst()) : Optional.empty();
    }

    private static Stream<String> findSubjectAlternativeNames(final X509Certificate certificate) {
        return Optional.ofNullable(FunctionUtils.doUnchecked(certificate::getSubjectAlternativeNames))
            .stream()
            .flatMap(Collection::stream)
            .flatMap(List::stream)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .filter(StringUtils::isNotBlank);
    }

    private static boolean isSubjectDistinguishedName(final X509Certificate certificate, final String subjectPattern) {
        try {
            return new X500Principal(subjectPattern).equals(certificate.getSubjectX500Principal());
        } catch (final IllegalArgumentException e) {
            LOGGER.debug("Subject [{}] is not a distinguished name", subjectPattern);
            return false;
        }
    }

    private static @Nullable Pattern compileSubjectPattern(final String subjectPattern) {
        try {
            return Pattern.compile(subjectPattern);
        } catch (final PatternSyntaxException e) {
            LOGGER.debug("Subject [{}] is not a regular expression", subjectPattern);
            return null;
        }
    }

    private static boolean hasSpiffeIdOf(final X509Certificate certificate, final String clientId) {
        return Strings.CI.startsWith(clientId, SUBJECT_ALT_NAME_SPIFFE_PREFIX)
            && findSubjectAlternativeNames(certificate).anyMatch(name -> Strings.CI.equals(name, clientId));
    }

    protected @Nullable OAuthRegisteredService locateRegisteredService(final HttpServletRequest request, final X509Certificate certificate) {
        val subjectAltNames = FunctionUtils.doUnchecked(certificate::getSubjectAlternativeNames);
        if (subjectAltNames != null) {
            val spiffeEntries = new ArrayList<String>();
            subjectAltNames.forEach(altName -> altName.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(name -> Strings.CI.startsWith(name, SUBJECT_ALT_NAME_SPIFFE_PREFIX))
                .forEach(spiffeEntries::add));
            LOGGER.debug("Located SPIFFE IDs [{}] from certificate subject alternative names", spiffeEntries);
            val result = spiffeEntries
                .stream()
                .map(spiffeId -> {
                    LOGGER.debug("Locating registered service for SPIFFE ID [{}]", spiffeId);
                    return OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, spiffeId);
                })
                .filter(Objects::nonNull)
                .findFirst();
            if (result.isPresent()) {
                return result.get();
            }
        }
        val clientIdAndSecret = requestParameterResolver.resolveClientIdAndClientSecret(request);
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientIdAndSecret.getKey());
    }

    protected boolean isAuthenticationMethodSupported(final HttpServletRequest request, final OAuthRegisteredService registeredService) {
        return OAuth20Utils.isTokenAuthenticationMethodSupportedFor(request, registeredService, OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH);
    }

    /**
     * Whether the certificate carries the subject alternative name that the client registered, as RFC 8705, section
     * 2.1.2, registers it: an exact value, compared ignoring case for DNS names and email addresses, and as an address for
     * IP addresses, so {@code ::1} and {@code 0:0:0:0:0:0:0:1} are the same.
     *
     * @param profile   the profile of the certificate
     * @param expected  the registered subject alternative name
     * @param attribute the profile attribute that holds the certificate's subject alternative names of that type
     * @return true or false
     */
    protected boolean isAcceptableX509Attribute(final OAuth20UserProfile profile, final String expected, final String attribute) {
        return FunctionUtils.doIfNotBlank(expected,
            () -> CollectionUtils.toCollection(profile.getAttribute(attribute))
                .stream()
                .map(Object::toString)
                .anyMatch(value -> switch (attribute) {
                    case X509_SAN_DNS, X509_SAN_EMAIL -> Strings.CI.equals(value, expected);
                    case X509_SAN_IP -> isSameAddress(value, expected);
                    default -> value.equals(expected);
                }),
            () -> Boolean.TRUE);
    }

    private static boolean isSameAddress(final String address, final String expected) {
        try {
            return InetAddress.ofLiteral(address).equals(InetAddress.ofLiteral(expected));
        } catch (final IllegalArgumentException e) {
            LOGGER.debug("Subject alternative name [{}] or [{}] is not an IP address", address, expected);
            return address.equals(expected);
        }
    }

    private static void addSubjectAlternativeNames(final DefaultOAuth20UserProfile profile, final X509Certificate certificate,
                                                   final String attribute, final int type) {
        try {
            val subjectAlternativeNames = certificate.getSubjectAlternativeNames();
            if (subjectAlternativeNames != null) {
                val values = subjectAlternativeNames
                    .stream()
                    .filter(entry -> entry.size() == 2)
                    .filter(entry -> entry.getFirst().equals(type))
                    .map(entry -> entry.get(1).toString())
                    .toList();
                if (!values.isEmpty()) {
                    profile.addAttribute(attribute, values);
                }
            }
        } catch (final CertificateParsingException e) {
            LOGGER.debug("Unable to extract subject alternative names", e);
        }
    }
}
