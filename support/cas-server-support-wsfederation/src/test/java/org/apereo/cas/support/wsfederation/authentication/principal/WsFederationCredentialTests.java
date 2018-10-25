package org.apereo.cas.support.wsfederation.authentication.principal;

import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test cases for {@link WsFederationCredential}.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationCredentialTests extends AbstractWsFederationTests {

    private static final String ISSUER = "http://adfs.example.com/adfs/services/trust";
    private static final String AUDIENCE = "urn:federation:cas";
    private WsFederationCredential standardCred;

    @BeforeEach
    public void initialize() {
        standardCred = new WsFederationCredential();
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setIssuer(ISSUER);
        standardCred.setAudience(AUDIENCE);
        standardCred.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");
        standardCred.setRetrievedOn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(1));
    }

    @Test
    public void verifyIsValidAllGood() {
        val result = standardCred.isValid(AUDIENCE, ISSUER, 2000);
        assertTrue(result, "testIsValidAllGood() - True");
    }

    @Test
    public void verifyIsValidBadAudience() {
        standardCred.setAudience("urn:NotUs");
        val result = standardCred.isValid(AUDIENCE, ISSUER, 2000);
        assertFalse(result, "testIsValidBadAudeience() - False");
    }

    @Test
    public void verifyIsValidBadIssuer() {
        standardCred.setIssuer("urn:NotThem");
        val result = standardCred.isValid(AUDIENCE, ISSUER, 2000);
        assertFalse(result, "testIsValidBadIssuer() - False");
    }

    @Test
    public void verifyIsValidEarlyToken() {
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1).plusDays(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1));

        val result = standardCred.isValid(AUDIENCE, ISSUER, 2000);
        assertFalse(result, "testIsValidEarlyToken() - False");
    }

    @Test
    public void verifyIsValidOldToken() {
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1).minusDays(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));

        val result = standardCred.isValid(AUDIENCE, ISSUER, 2000);
        assertFalse(result, "testIsValidOldToken() - False");
    }

    @Test
    public void verifyIsValidExpiredIssuedOn() {
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(3));

        val result = standardCred.isValid(AUDIENCE, ISSUER, 2000);
        assertFalse(result, "testIsValidOldToken() - False");
    }
}
