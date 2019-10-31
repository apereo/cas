package org.apereo.cas.util.spring;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.function.Function;

/**
 * This is {@link SpringExpressionLanguageValueResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class SpringExpressionLanguageValueResolver implements Function {
    private static final ParserContext PARSER_CONTEXT = new TemplateParserContext("${", "}");

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser(
        new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, SpringExpressionLanguageValueResolver.class.getClassLoader())
    );

    private static final StandardEvaluationContext EVALUATION_CONTEXT = new StandardEvaluationContext();

    private static SpringExpressionLanguageValueResolver INSTANCE;

    static {
        val properties = System.getProperties();
        EVALUATION_CONTEXT.setVariable("systemProperties", properties);
        EVALUATION_CONTEXT.setVariable("sysProps", properties);

        val environment = System.getenv();
        EVALUATION_CONTEXT.setVariable("environmentVars", environment);
        EVALUATION_CONTEXT.setVariable("environmentVariables", environment);
        EVALUATION_CONTEXT.setVariable("envVars", environment);
        EVALUATION_CONTEXT.setVariable("env", environment);
    }

    protected SpringExpressionLanguageValueResolver() {
    }

    public static SpringExpressionLanguageValueResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpringExpressionLanguageValueResolver();
        }
        return INSTANCE;
    }

    /**
     * Resolve string.
     *
     * @param value the value
     * @return the string
     */
    public String resolve(final String value) {
        LOGGER.trace("Parsing expression as [{}]", value);
        val expression = EXPRESSION_PARSER.parseExpression(value, PARSER_CONTEXT);
        val result = expression.getValue(EVALUATION_CONTEXT, String.class);
        LOGGER.trace("Parsed expression result is [{}]", result);
        return result;
    }

    @Override
    public Object apply(final Object o) {
        return resolve(o.toString());
    }
}
