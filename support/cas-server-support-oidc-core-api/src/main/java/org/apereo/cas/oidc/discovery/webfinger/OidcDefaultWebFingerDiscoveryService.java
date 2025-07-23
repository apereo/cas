package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.configuration.model.support.oidc.OidcWebFingerProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OidcDefaultWebFingerDiscoveryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class OidcDefaultWebFingerDiscoveryService implements OidcWebFingerDiscoveryService {
    
    private static final int PATTERN_GROUP_INDEX_SCHEME = 2;

    private static final int PATTERN_GROUP_INDEX_USERINFO = 6;

    private static final int PATTERN_GROUP_INDEX_HOST = 8;

    private static final int PATTERN_GROUP_INDEX_PORT = 10;

    private static final int PATTERN_GROUP_INDEX_PATH = 11;

    private static final int PATTERN_GROUP_INDEX_QUERY = 13;

    private static final int PATTERN_GROUP_INDEX_FRAGMENT = 15;

    private final OidcWebFingerUserInfoRepository userInfoRepository;

    private final OidcServerDiscoverySettings discovery;

    private final OidcWebFingerProperties properties;

    @Override
    public ResponseEntity<Map> handleRequest(final String resource, final String rel) throws Throwable {
        if (StringUtils.isNotBlank(rel) && !OidcConstants.WEBFINGER_REL.equalsIgnoreCase(rel)) {
            LOGGER.warn("Handling discovery request for a non-standard OIDC relation [{}]", rel);
        }

        val issuer = discovery.getIssuer();
        if (!Strings.CI.equals(resource, issuer)) {
            val resourceUri = normalize(resource);
            if (resourceUri == null) {
                LOGGER.error("Unable to parse and normalize resource: [{}]", resource);
                return buildNotFoundResponseEntity("Unable to normalize provided resource");
            }
            val issuerUri = normalize(issuer);
            if (!"acct".equals(resourceUri.getScheme())) {
                LOGGER.error("Unable to accept resource scheme: [{}]", resourceUri.toUriString());
                return buildNotFoundResponseEntity("Unable to recognize/accept resource scheme " + resourceUri.getScheme());
            }

            var user = Optional.ofNullable(userInfoRepository.findByEmailAddress(resourceUri.getUserInfo() + '@' + resourceUri.getHost()));
            if (user.isEmpty()) {
                user = Optional.ofNullable(userInfoRepository.findByUsername(resourceUri.getUserInfo()));
            }
            if (user.isEmpty()) {
                LOGGER.info("User/Resource not found: [{}]", resource);
                return buildNotFoundResponseEntity("Unable to find resource");
            }

            if (!Strings.CI.equals(issuerUri.getHost(), resourceUri.getHost())) {
                LOGGER.info("Host mismatch for resource [{}]: expected [{}] and yet received [{}]", resource,
                    issuerUri.getHost(), resourceUri.getHost());
                return buildNotFoundResponseEntity("Unable to match resource host");
            }
        }

        val body = new LinkedHashMap<String, Object>();
        body.put("subject", resource);

        val links = new ArrayList<>();
        links.add(CollectionUtils.wrap("rel", OidcConstants.WEBFINGER_REL, "href", issuer));
        body.put("links", links);

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    protected ResponseEntity buildNotFoundResponseEntity(final String message) {
        return new ResponseEntity<>(CollectionUtils.wrap("message", message), HttpStatus.NOT_FOUND);
    }

    protected UriComponents normalize(final String resource) {
        val builder = UriComponentsBuilder.newInstance();

        val resourcePattern = RegexUtils.createPattern(properties.getResourcePattern());
        val matcher = resourcePattern.matcher(resource);
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
