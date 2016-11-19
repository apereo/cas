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
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.EncodingUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link JwtTicketGrantingTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketGrantingTicketFactory extends DefaultTicketGrantingTicketFactory {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected String produceTicketIdentifier(final Authentication authentication) {

        try {
            final String signingKey = EncodingUtils.generateJsonWebKey(512);
            final String encryptionKey = EncodingUtils.generateJsonWebKey(256);

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
            final JWSSigner signer = new MACSigner(signingKey);
            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            final JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
            final Payload payload = new Payload(signedJWT);
            final JWEObject jweObject = new JWEObject(header, payload);
            jweObject.encrypt(new DirectEncrypter(encryptionKey.getBytes(StandardCharsets.UTF_8)));

            final String finalTicket = jweObject.serialize();
            return finalTicket;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
