package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * This is {@link OidcWebFingerDiscoveryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcWebFingerDiscoveryService {
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
            LOGGER.warn("Handling webfinger request for a non-standard OIDC relation [{}]", rel);
        }

        val issuer = this.discovery.getIssuer();
        if (!StringUtils.equalsIgnoreCase(resource, issuer)) {
            val resourceUri = UriComponentsBuilder.fromUriString(resource).build();
            if ("acct".equals(resourceUri.getScheme())) {
                var user = userInfoRepository.findByEmailAddress(resourceUri.getUserInfo() + '@' + resourceUri.getHost());
                if (user == null) {
                    user = userInfoRepository.findByUsername(resourceUri.getUserInfo());
                }
                if (user == null) {
                    LOGGER.warn("User not found: [{}]", resource);
                    return new ResponseEntity(HttpStatus.NOT_FOUND);
                }

                val issuerComponents = UriComponentsBuilder.fromHttpUrl(issuer).build();
                if (!StringUtils.equalsIgnoreCase(issuerComponents.getHost(), resourceUri.getHost())) {
                    LOGGER.warn("Host mismatch, expected [{}], and yet received [{}]", issuerComponents.getHost(), resourceUri.getHost());
                    return new ResponseEntity(HttpStatus.NOT_FOUND);
                }
            }
        }
        val body = new LinkedHashMap<String, Object>();
        body.put("subject", resource);

        val links = new ArrayList<>();
        links.add(CollectionUtils.wrap("rel", OidcConstants.WEBFINGER_REL, "href", issuer));
        body.put("links", links);

        return new ResponseEntity(body, HttpStatus.OK);
    }
}
