package org.apereo.cas.web;

import org.apereo.cas.entity.IdentityProviderEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link IdentityProviderDiscoveryFeedController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RestController("identityProviderDiscoveryFeedController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/idp/discovery")
public class IdentityProviderDiscoveryFeedController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<IdentityProviderEntity> getDiscoveryFeed() {
        return new ArrayList<>();
    }
}
