package org.apereo.cas.acme;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.AcmeJsonResource;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This is {@link AcmeCertificateManager}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class AcmeCertificateManager {
    private final AcmeChallengeRepository acmeChallengeRepository;

    private final CasConfigurationProperties casProperties;

    private final AcmeAuthorizationExecutor locator;

    /**
     * Generates a certificate for the given domains. Also takes care for the registration
     * process.
     *
     * @param domains Domains to get a common certificate for
     * @throws Exception the exception
     */
    public void fetchCertificate(final Collection<String> domains) throws Exception {
        val acme = casProperties.getAcme();

        val userKeyPair = loadOrCreateUserKeyPair();
        val domainKeyPair = loadOrCreateDomainKeyPair();

        val csrb = new CSRBuilder();
        csrb.addDomains(domains);
        csrb.sign(domainKeyPair);
        try (val out = new FileWriter(acme.getDomainCsr().getLocation().getFile(), StandardCharsets.UTF_8)) {
            csrb.write(out);
        }

        val session = new Session(acme.getServerUrl());
        val acct = findOrRegisterAccount(session, userKeyPair);

        val order = getCertificateOrder(domains, csrb, acct);

        fetchStatusAndUpdate(order, order::getStatus);

        val certificate = order.getCertificate();

        LOGGER.info("The certificate for domains [{}] has been successfully generated.", domains);
        LOGGER.info("Certificate URL is [{}]", Objects.requireNonNull(certificate).getLocation());

        try (val fw = new FileWriter(acme.getDomainChain().getLocation().getFile(), StandardCharsets.UTF_8)) {
            certificate.writeCertificate(fw);
        }
        LOGGER.info("Configure the web server to use [{}] and [{}] for domains [{}]",
            acme.getDomainKey().getLocation(), acme.getDomainChain().getLocation(), acme.getDomains());
    }

    private Order getCertificateOrder(final Collection<String> domains, final CSRBuilder csrb,
                                      final Account acct) throws Exception {
        val order = acct.newOrder().domains(domains).create();
        order.getAuthorizations().forEach(Unchecked.consumer(this::authorize));
        return locator.execute(order, csrb);
    }

    @SneakyThrows
    private void fetchStatusAndUpdate(final AcmeJsonResource resource,
                                      final Supplier<Status> statusSupplier) {
        val acme = casProperties.getAcme();
        var attempts = acme.getRetryAttempts();

        while (statusSupplier.get() != Status.VALID && attempts-- > 0) {
            if (statusSupplier.get() == Status.INVALID) {
                throw new AcmeException("Order failed");
            }
            val timeout = Beans.newDuration(acme.getRetryInternal()).toMillis();
            Thread.sleep(timeout);
            resource.update();
        }
    }

    /**
     * Prepares a HTTP challenge.
     * <p>
     * The verification of this challenge expects a file with a certain content to be
     * reachable at a given path under the domain to be tested.
     *
     * @param auth {@link Authorization} to find the challenge in
     * @return {@link Challenge} to verify
     */
    private Challenge httpChallenge(final Authorization auth) {
        val challenge = locator.find(auth);
        acmeChallengeRepository.add(challenge.getToken(), challenge.getAuthorization());
        return challenge;
    }

    /**
     * Authorize a domain. It will be associated with your account, so you will be able to
     * retrieve a signed certificate for the domain later.
     *
     * @param auth {@link Authorization} to perform
     */
    private void authorize(final Authorization auth) throws AcmeException {
        val challenge = httpChallenge(auth);
        if (challenge.getStatus() != Status.VALID) {
            challenge.trigger();
            fetchStatusAndUpdate(challenge, challenge::getStatus);
            if (challenge.getStatus() != Status.VALID) {
                throw new AcmeException("Failed to pass the challenge for domain " + auth.getIdentifier().getDomain());
            }
        }
    }

    /**
     * Loads a domain key pair. If the file does not exist,
     * a new key pair is generated and saved.
     *
     * @return Domain {@link KeyPair}.
     */
    private KeyPair loadOrCreateDomainKeyPair() throws Exception {
        val acme = casProperties.getAcme();
        return loadOrCreateKeyPair(acme.getDomainKey().getLocation().getFile());
    }

    private KeyPair loadOrCreateKeyPair(final File file) throws IOException {
        val acme = casProperties.getAcme();
        if (file.exists()) {
            try (val fr = new FileReader(file, StandardCharsets.UTF_8)) {
                return KeyPairUtils.readKeyPair(fr);
            }
        } else {
            val userKeyPair = KeyPairUtils.createKeyPair(acme.getKeySize());
            try (val fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                KeyPairUtils.writeKeyPair(userKeyPair, fw);
            }
            return userKeyPair;
        }
    }

    /**
     * Loads a user key pair. If the file does not exist, a
     * new key pair is generated and saved.
     *
     * @return User's {@link KeyPair}.
     */
    private KeyPair loadOrCreateUserKeyPair() throws IOException {
        val acme = casProperties.getAcme();
        val file = acme.getUserKey().getLocation().getFile();
        LOGGER.info("Locating user keypair [{}]. Keep this key pair in a safe place. "
            + "In a production, you will not be able to access your account if you lose the key pair.", file);
        return loadOrCreateKeyPair(file);
    }

    /**
     * Finds your {@link Account} at the ACME server. It will be found by your user's
     * public key. If your key is not known to the server yet, a new account will be
     * created.
     *
     * @param session {@link Session} to bind with
     * @return {@link Account}
     */
    private static Account findOrRegisterAccount(final Session session, final KeyPair accountKey) throws AcmeException {
        val tos = session.getMetadata().getTermsOfService();
        LOGGER.debug("Accepted terms of service url: [{}]", tos);
        val account = new AccountBuilder()
            .agreeToTermsOfService()
            .useKeyPair(accountKey)
            .create(session);
        LOGGER.info("Registered new user w/ URL: [{}]", account.getLocation());
        return account;
    }
}
