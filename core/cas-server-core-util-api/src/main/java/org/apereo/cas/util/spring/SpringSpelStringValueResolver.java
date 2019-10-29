package org.apereo.cas.util.spring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * This is {@link SpringSpelStringValueResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class SpringSpelStringValueResolver {

    private final String value;

    /**
     * Resolve string.
     *
     * @return the string
     */
    public String resolve() {
        val configuration = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, getClass().getClassLoader());
        val parser = new SpelExpressionParser(configuration);
        val expression = parser.parseExpression(this.value, new TemplateParserContext("${", "}"));

        val context = new StandardEvaluationContext();
        context.setVariable("systemProperties", System.getProperties());
        context.setVariable("environmentVars", System.getenv());
        return expression.getValue(context, String.class);
    }
}
