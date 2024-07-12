package org.apereo.inspektr.audit.spi.support;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SpringWebflowActionExecutionAuditablePrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
class SpringWebflowActionExecutionAuditablePrincipalResolverTests {
    @ParameterizedTest
    @MethodSource("contextProvider")
    void verifyOperation(final Supplier<MockRequestContext> contextSupplier) {
        val context = contextSupplier.get();
        val resolver = new SpringWebflowActionExecutionAuditablePrincipalResolver("name");
        
        val request = new MockHttpServletRequest();
        request.addParameter("name", UUID.randomUUID().toString());
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertFalse(resolver.resolveFrom(context, mock(JoinPoint.class), false).isEmpty());
    }

    static Stream<Arguments> contextProvider() {
        return Stream.of(
            arguments((Supplier<MockRequestContext>) () -> {
                val ctx = new MockRequestContext();
                ctx.getFlashScope().put("name", UUID.randomUUID().toString());
                return ctx;
            }),
            arguments((Supplier<MockRequestContext>) () -> {
                val ctx = new MockRequestContext();
                ctx.getFlowScope().put("name", UUID.randomUUID().toString());
                return ctx;
            }),
            arguments((Supplier<MockRequestContext>) () -> {
                val ctx = new MockRequestContext();
                ctx.getConversationScope().put("name", UUID.randomUUID().toString());
                return ctx;
            }),
            arguments((Supplier<MockRequestContext>) () -> {
                val ctx = new MockRequestContext();
                ctx.getRequestScope().put("name", UUID.randomUUID().toString());
                return ctx;
            }),
            arguments((Supplier<MockRequestContext>) MockRequestContext::new)
        );
    }
}
