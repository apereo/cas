package org.apereo.cas.ticket.registry;

import com.google.common.base.Throwables;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link JwtTicketGrantingTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketGrantingTicketFactory extends DefaultTicketGrantingTicketFactory {

    private Pair<String, String> keyPair;

    @Autowired
    private CasConfigurationProperties casProperties;

    public JwtTicketGrantingTicketFactory(final Pair<String, String> keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    protected String produceTicketIdentifier(final Authentication authentication) {
        try {
            final Principal principal = authentication.getPrincipal();
            final String tgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(casProperties.getServer().getPrefix())
                            .issuer(casProperties.getServer().getPrefix())
                            .jwtID(tgtId)
                            .issueTime(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                            .subject(principal.getId());

            authentication.getAttributes().forEach((k, v) -> claims.claim(k, CollectionUtils.toCollection(v)));
            principal.getAttributes().forEach((k, v) -> claims.claim(k, CollectionUtils.toCollection(v)));

            final JWTClaimsSet claimsSet = claims.build();
            final JWSSigner signer = new MACSigner(this.keyPair.getKey());
            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            final JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
            final Payload payload = new Payload(signedJWT);
            final JWEObject jweObject = new JWEObject(header, payload);
            jweObject.encrypt(new DirectEncrypter(this.keyPair.getValue().getBytes(StandardCharsets.UTF_8)));

            final String finalTicket = jweObject.serialize();
            return finalTicket;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
