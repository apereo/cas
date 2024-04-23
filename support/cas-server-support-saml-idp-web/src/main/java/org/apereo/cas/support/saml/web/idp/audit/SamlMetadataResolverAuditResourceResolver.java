package org.apereo.cas.support.saml.web.idp.audit;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * This is {@link SamlMetadataResolverAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SamlMetadataResolverAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object returnValue) {
        val resolvers = (Collection<? extends MetadataResolver>) returnValue;
        val values = new HashMap<>();
        values.put("resolvers", resolvers.stream().map(MetadataResolver::getId).collect(Collectors.joining(",")));

        if ("method-execution".equals(joinPoint.getStaticPart().getKind())) {
            val jp = (MethodInvocationProceedingJoinPoint) joinPoint;
            val service = (SamlRegisteredService) jp.getArgs()[0];
            values.put("name", service.getName());
            values.put("metadataLocation", service.getMetadataLocation());
        }
        return new String[]{auditFormat.serialize(values)};
    }
}
