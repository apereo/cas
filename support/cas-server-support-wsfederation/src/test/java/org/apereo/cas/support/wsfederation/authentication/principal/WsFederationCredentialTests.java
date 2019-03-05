package org.apereo.cas.support.wsfederation.authentication.principal;

import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;

import lombok.val;
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

    public static WsFederationCredential getCredential() {
        val standardCred = new WsFederationCredential();
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setIssuer(ISSUER);
        standardCred.setAudience(AUDIENCE);
        standardCred.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");
        standardCred.setRetrievedOn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        return standardCred;
    }

    @Test
    public void verifyIsValidAllGood() {
        assertTrue(getCredential().isValid(AUDIENCE, ISSUER, 2000), "testIsValidAllGood() - True");
    }

    @Test
    public void verifyIsValidBadAudience() {
        val standardCred = getCredential();
        standardCred.setAudience("urn:NotUs");
        assertFalse(standardCred.isValid(AUDIENCE, ISSUER, 2000), "testIsValidBadAudeience() - False");
    }

    @Test
    public void verifyIsValidBadIssuer() {
        val standardCred = getCredential();
        standardCred.setIssuer("urn:NotThem");
        assertFalse(standardCred.isValid(AUDIENCE, ISSUER, 2000), "testIsValidBadIssuer() - False");
    }

    @Test
    public void verifyIsValidEarlyToken() {
        val standardCred = getCredential();
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1).plusDays(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1));

        assertFalse(standardCred.isValid(AUDIENCE, ISSUER, 2000), "testIsValidEarlyToken() - False");
    }

    @Test
    public void verifyIsValidOldToken() {
        val standardCred = getCredential();
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1).minusDays(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));

        assertFalse(standardCred.isValid(AUDIENCE, ISSUER, 2000), "testIsValidOldToken() - False");
    }

    @Test
    public void verifyIsValidExpiredIssuedOn() {
        val standardCred = getCredential();
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(3));

        assertFalse(standardCred.isValid(AUDIENCE, ISSUER, 2000), "testIsValidOldToken() - False");
    }
}
