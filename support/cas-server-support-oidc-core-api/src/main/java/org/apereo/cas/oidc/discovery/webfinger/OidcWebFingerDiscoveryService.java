package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * This is {@link OidcWebFingerDiscoveryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class OidcWebFingerDiscoveryService {
    private static final Pattern RESOURCE_NORMALIZED_PATTERN = Pattern.compile('^'
        + "((https|acct|http|mailto|tel|device):(//)?)?"
        + '('
        + "(([^@]+)@)?"
        + "(([^\\?#:/]+)"
        + "(:(\\d*))?)"
        + ')'
        + "([^\\?#]+)?"
        + "(\\?([^#]+))?"
        + "(#(.*))?"
        + '$'
    );

    private static final int PATTERN_GROUP_INDEX_SCHEME = 2;

    private static final int PATTERN_GROUP_INDEX_USERINFO = 6;

    private static final int PATTERN_GROUP_INDEX_HOST = 8;

    private static final int PATTERN_GROUP_INDEX_PORT = 10;

    private static final int PATTERN_GROUP_INDEX_PATH = 11;

    private static final int PATTERN_GROUP_INDEX_QUERY = 13;

    private static final int PATTERN_GROUP_INDEX_FRAGMENT = 15;

    private final OidcWebFingerUserInfoRepository userInfoRepository;

    private final OidcServerDiscoverySettings discovery;

    /**
     * Handle web finger discovery request and produce response entity.
     *
     * @param resource the resource
     * @param rel      the rel
     * @return the response entity
     */
    public ResponseEntity handleWebFingerDiscoveryRequest(final String resource, final String rel) {
        if (StringUtils.isNotBlank(rel) && !OidcConstants.WEBFINGER_REL.equalsIgnoreCase(rel)) {
            LOGGER.warn("Handling discovery request for a non-standard OIDC relation [{}]", rel);
        }

        val issuer = this.discovery.getIssuer();
        if (!StringUtils.equalsIgnoreCase(resource, issuer)) {
            val resourceUri = normalize(resource);
            if (resourceUri == null) {
                LOGGER.error("Unable to parse and normalize resource: [{}]", resource);
                return buildNotFoundResponseEntity("Unable to normalize provided resource");
            }
            val issuerUri = normalize(issuer);
            if (issuerUri == null) {
                LOGGER.error("Unable to parse and normalize issuer: [{}]", issuer);
                return buildNotFoundResponseEntity("Unable to normalize issuer");
            }

            if (!"acct".equals(resourceUri.getScheme())) {
                LOGGER.error("Unable to accept resource scheme: [{}]", resourceUri.toUriString());
                return buildNotFoundResponseEntity("Unable to recognize/accept resource scheme " + resourceUri.getScheme());
            }

            var user = userInfoRepository.findByEmailAddress(resourceUri.getUserInfo() + '@' + resourceUri.getHost());
            if (user.isEmpty()) {
                user = userInfoRepository.findByUsername(resourceUri.getUserInfo());
            }
            if (user.isEmpty()) {
                LOGGER.info("User/Resource not found: [{}]", resource);
                return buildNotFoundResponseEntity("Unable to find resource");
            }

            if (!StringUtils.equalsIgnoreCase(issuerUri.getHost(), resourceUri.getHost())) {
                LOGGER.info("Host mismatch for resource [{}]: expected [{}] and yet received [{}]", resource,
                    issuerUri.getHost(), resourceUri.getHost());
                return buildNotFoundResponseEntity("Unable to match resource host");
            }
        }

        val body = new LinkedHashMap<String, Object>();
        body.put("subject", resource);

        val links = new ArrayList<>(0);
        links.add(CollectionUtils.wrap("rel", OidcConstants.WEBFINGER_REL, "href", issuer));
        body.put("links", links);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    /**
     * Build not found response entity response entity.
     *
     * @param message the message
     * @return the response entity
     */
    protected ResponseEntity buildNotFoundResponseEntity(final String message) {
        return new ResponseEntity<>(CollectionUtils.wrap("message", message), HttpStatus.NOT_FOUND);
    }

    /**
     * Normalize uri components.
     *
     * @param resource the resource
     * @return the uri components
     */
    protected UriComponents normalize(final String resource) {
        val builder = UriComponentsBuilder.newInstance();

        val matcher = RESOURCE_NORMALIZED_PATTERN.matcher(resource);
        if (!matcher.matches()) {
            LOGGER.error("Unable to match the resource [{}] against pattern [{}] for normalization", resource, matcher.pattern().pattern());
            return null;
        }

        builder.scheme(matcher.group(PATTERN_GROUP_INDEX_SCHEME));
        builder.userInfo(matcher.group(PATTERN_GROUP_INDEX_USERINFO));
        builder.host(matcher.group(PATTERN_GROUP_INDEX_HOST));
        val port = matcher.group(PATTERN_GROUP_INDEX_PORT);
        if (!StringUtils.isBlank(port)) {
            builder.port(Integer.parseInt(port));
        }
        builder.path(matcher.group(PATTERN_GROUP_INDEX_PATH));
        builder.query(matcher.group(PATTERN_GROUP_INDEX_QUERY));
        builder.fragment(matcher.group(PATTERN_GROUP_INDEX_FRAGMENT));

        val currentBuilder = builder.build();

        if (StringUtils.isBlank(currentBuilder.getScheme())) {
            if (StringUtils.isNotBlank(currentBuilder.getUserInfo()) && StringUtils.isBlank(currentBuilder.getPath())
                && StringUtils.isBlank(currentBuilder.getQuery()) && currentBuilder.getPort() < 0) {
                builder.scheme("acct");
            } else {
                builder.scheme("https");
            }
        }
        builder.fragment(null);
        return builder.build();
    }
}
