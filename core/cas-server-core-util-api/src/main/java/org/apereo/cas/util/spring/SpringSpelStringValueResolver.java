package org.apereo.cas.util.spring;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.expression.ParserContext;
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
@Slf4j
public class SpringSpelStringValueResolver {
    private final ParserContext parserContext = new TemplateParserContext("${", "}");

    private final SpelExpressionParser parser = new SpelExpressionParser(
        new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, getClass().getClassLoader())
    );

    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    public SpringSpelStringValueResolver() {

        val properties = System.getProperties();
        evaluationContext.setVariable("systemProperties", properties);
        evaluationContext.setVariable("sysProps", properties);

        val environment = System.getenv();
        evaluationContext.setVariable("environmentVars", environment);
        evaluationContext.setVariable("envVars", environment);
        evaluationContext.setVariable("env", environment);
    }

    /**
     * Resolve string.
     *
     * @param value the value
     * @return the string
     */
    public String resolve(final String value) {
        val expression = parser.parseExpression(value, parserContext);
        return expression.getValue(evaluationContext, String.class);
    }
}
