package org.apereo.cas.web.view;

import org.apereo.cas.services.web.CasThymeleafTemplatesDirector;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import java.util.Set;

/**
 * This is {@link CasThymeleafExpressionDialect}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class CasThymeleafExpressionDialect implements IExpressionObjectDialect {
    private final ObjectProvider<CasThymeleafTemplatesDirector> director;

    @Override
    public String getName() {
        return "CAS Dialect";
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new CasThymeleafExpressionObjectFactory();
    }

    private final class CasThymeleafExpressionObjectFactory implements IExpressionObjectFactory {

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return Set.of("cas");
        }

        @Override
        public Object buildObject(final IExpressionContext context, final String expressionObjectName) {
            return director.getObject();
        }

        @Override
        public boolean isCacheable(final String expressionObjectName) {
            return false;
        }
    }
}
