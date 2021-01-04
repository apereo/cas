package org.apereo.cas.acme;

import org.apereo.cas.config.CasAcmeConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link BaseAcmeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasAcmeConfiguration.class
}, properties = {
    "cas.acme.domains=cas.example.org",
    "cas.acme.server-url=acme://letsencrypt.org/staging",
    "cas.acme.user-key.location=file:${java.io.tmpdir}/user.key",
    "cas.acme.domain-key.location=file:${java.io.tmpdir}/domain.key",
    "cas.acme.domain-csr.location=file:${java.io.tmpdir}/domain.csr",
    "cas.acme.domain-chain.location=file:${java.io.tmpdir}/domain-chain.crt"
})
public abstract class BaseAcmeTests {

    @Autowired
    @Qualifier("acmeWellKnownChallengeController")
    protected AcmeWellKnownChallengeController acmeWellKnownChallengeController;

    @Autowired
    @Qualifier("acmeChallengeRepository")
    protected AcmeChallengeRepository acmeChallengeRepository;

    @Autowired
    protected CasConfigurationProperties casProperties;

}
