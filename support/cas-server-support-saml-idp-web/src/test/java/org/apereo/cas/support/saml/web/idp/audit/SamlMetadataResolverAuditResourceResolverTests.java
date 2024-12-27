package org.apereo.cas.support.saml.web.idp.audit;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlMetadataResolverAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAML2")
class SamlMetadataResolverAuditResourceResolverTests {
    @Test
    void verifyActionUnknown() {
        val resourceRes = new SamlMetadataResolverAuditResourceResolver();
        val resolver = mock(MetadataResolver.class);
        when(resolver.getId()).thenReturn(UUID.randomUUID().toString());
        val jp = mock(MethodInvocationProceedingJoinPoint.class);
        val staticPart = mock(JoinPoint.StaticPart.class);
        when(staticPart.getKind()).thenReturn("method-execution");
        when(jp.getStaticPart()).thenReturn(staticPart);
        val service = new SamlRegisteredService();
        service.setName("SAML");
        service.setMetadataLocation("https://example.md.org");
        when(jp.getArgs()).thenReturn(new Object[]{service});
        val result = resourceRes.resolveFrom(jp, List.of(resolver));
        assertTrue(result.length > 0);
    }
}
