package org.apereo.cas.palantir.controller;

import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.web.report.StatisticsEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link SystemController}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RestController
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR + "/system")
@RequiredArgsConstructor
public class SystemController {
    private final ObjectProvider<InfoEndpoint> infoProvider;
    private final ObjectProvider<StatisticsEndpoint> statisticsProvider;
    private final ObjectProvider<HealthEndpoint> healthProvider;

    /**
     * Gets all services.
     *
     * @return the all services
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSystemInfo() {
        return infoProvider
            .stream()
            .map(endpoint -> ResponseEntity.ok(endpoint.info()))
            .findFirst()
            .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
    }

    @GetMapping(path = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getStatistics() {
        return statisticsProvider
            .stream()
            .map(endpoint -> ResponseEntity.ok(endpoint.statistics()))
            .findFirst()
            .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
    }

    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getHealth() {
        return healthProvider
            .stream()
            .map(endpoint -> ResponseEntity.ok(endpoint.health()))
            .findFirst()
            .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
    }

}
