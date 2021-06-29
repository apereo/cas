package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.model.TriStateBoolean;

import lombok.val;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CoreAuthenticationUtilsTests}.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@Tag("Authentication")
public class CoreAuthenticationUtilsTests {
    @Test
    public void verifyPersonDirectoryOverrides() {
        val principal = new PersonDirectoryPrincipalResolverProperties();
        val personDirectory = new PersonDirectoryPrincipalResolverProperties();
        val principalResolutionContext = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            principal, personDirectory);
        assertFalse(principalResolutionContext.isUseCurrentPrincipalId());
        assertTrue(principalResolutionContext.isResolveAttributes());
        assertFalse(principalResolutionContext.isReturnNullIfNoAttributes());
        assertTrue(principalResolutionContext.getActiveAttributeRepositoryIdentifiers().isEmpty());
        assertTrue(principalResolutionContext.getPrincipalAttributeNames().isEmpty());

        personDirectory.setUseExistingPrincipalId(TriStateBoolean.TRUE);
        personDirectory.setAttributeResolutionEnabled(TriStateBoolean.TRUE);
        personDirectory.setReturnNull(TriStateBoolean.TRUE);
        personDirectory.setAttributeResolutionEnabled(TriStateBoolean.FALSE);
        personDirectory.setActiveAttributeRepositoryIds("test1,test2");
        personDirectory.setPrincipalAttribute("principalAttribute");
        val principalResolutionContext2 = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            principal, personDirectory);
        assertTrue(principalResolutionContext2.isUseCurrentPrincipalId());
        assertFalse(principalResolutionContext2.isResolveAttributes());
        assertTrue(principalResolutionContext2.isReturnNullIfNoAttributes());
        assertTrue(principalResolutionContext2.getActiveAttributeRepositoryIdentifiers().size()==2);
        assertEquals("principalAttribute", principalResolutionContext2.getPrincipalAttributeNames());

        principal.setUseExistingPrincipalId(TriStateBoolean.FALSE);
        principal.setAttributeResolutionEnabled(TriStateBoolean.FALSE);
        principal.setReturnNull(TriStateBoolean.FALSE);
        principal.setAttributeResolutionEnabled(TriStateBoolean.TRUE);
        principal.setActiveAttributeRepositoryIds("test1,test2,test3");
        principal.setPrincipalAttribute("principalAttribute2");
        val principalResolutionContext3 = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            principal, personDirectory);
        assertFalse(principalResolutionContext3.isUseCurrentPrincipalId());
        assertTrue(principalResolutionContext3.isResolveAttributes());
        assertFalse(principalResolutionContext3.isReturnNullIfNoAttributes());
        assertTrue(principalResolutionContext3.getActiveAttributeRepositoryIdentifiers().size()==3);
        assertEquals("principalAttribute2", principalResolutionContext3.getPrincipalAttributeNames());

        val principalResolutionContext4 = CoreAuthenticationUtils.buildPrincipalResolutionContext(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(Collections.EMPTY_MAP),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            personDirectory);
        assertTrue(principalResolutionContext4.isUseCurrentPrincipalId());
        assertFalse(principalResolutionContext4.isResolveAttributes());
        assertTrue(principalResolutionContext4.isReturnNullIfNoAttributes());
        assertTrue(principalResolutionContext4.getActiveAttributeRepositoryIdentifiers().size()==2);
        assertEquals("principalAttribute", principalResolutionContext4.getPrincipalAttributeNames());
    }
}
