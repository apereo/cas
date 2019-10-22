package org.apereo.cas.web;

import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.entity.SamlIdentityProviderEntityParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link SamlIdentityProviderDiscoveryFeedController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RestController("identityProviderDiscoveryFeedController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/idp/discovery")
public class SamlIdentityProviderDiscoveryFeedController {
    private final List<SamlIdentityProviderEntityParser> parsers;

    @GetMapping(path = "/feed", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<SamlIdentityProviderEntity> getDiscoveryFeed() {
        return parsers
            .stream()
            .map(SamlIdentityProviderEntityParser::getIdentityProviders)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

}
