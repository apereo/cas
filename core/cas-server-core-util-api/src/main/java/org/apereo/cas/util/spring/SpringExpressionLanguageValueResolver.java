package org.apereo.cas.util.spring;

import org.apereo.cas.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import jakarta.annotation.Nonnull;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This is {@link SpringExpressionLanguageValueResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class SpringExpressionLanguageValueResolver implements Function {
    private static final int STARTING_PORT_RANGE = 3000;
    private static final int ENDING_PORT_RANGE = 9999;

    private static final int HOUR_23 = 23;

    private static final int MINUTE_59 = 59;

    private static final int SECOND_59 = 59;

    private static final ParserContext PARSER_CONTEXT = new TemplateParserContext("${", "}");

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser(
        new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, SpringExpressionLanguageValueResolver.class.getClassLoader())
    );

    private static SpringExpressionLanguageValueResolver INSTANCE;

    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    protected SpringExpressionLanguageValueResolver() {
        val systemProperties = System.getProperties();
        evaluationContext.setVariable("systemProperties", systemProperties);
        evaluationContext.setVariable("sysProps", systemProperties);

        val environmentVariables = System.getenv();
        evaluationContext.setVariable("environmentVars", environmentVariables);
        evaluationContext.setVariable("environmentVariables", environmentVariables);
        evaluationContext.setVariable("envVars", environmentVariables);
        evaluationContext.setVariable("env", environmentVariables);

        evaluationContext.setVariable("tempDir", FileUtils.getTempDirectoryPath());
        evaluationContext.setVariable("zoneId", ZoneId.systemDefault().getId());
        withApplicationContext(ApplicationContextProvider.getApplicationContext());
    }

    /**
     * Format string using string substitution and Spring expressions.
     *
     * @param contents   the contents
     * @param parameters the parameters
     * @return the string
     */
    public String format(final String contents, final Map<String, Object> parameters) {
        val sub = new StringSubstitutor(parameters, "${", "}");
        val body = sub.replace(contents);

        val expressionSub = new StringSubstitutor();
        expressionSub.setVariablePrefix("${");
        expressionSub.setVariableSuffix("}");
        expressionSub.setVariableResolver(variable -> !variable.isEmpty() && variable.charAt(0) == '#'
            ? resolve("${%s}".formatted(variable), parameters, String.class)
            : null);
        return expressionSub.replace(body);
    }

    /**
     * Gets instance of the resolver as a singleton.
     *
     * @return the instance
     */
    public static SpringExpressionLanguageValueResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpringExpressionLanguageValueResolver();
        }
        INSTANCE.initializeDynamicVariables();
        return INSTANCE;
    }

    /**
     * Resolve string.
     *
     * @param value the value
     * @return the string
     */
    public String resolve(final String value) {
        return resolve(value, String.class);
    }

    /**
     * Resolve value for a type.
     *
     * @param <T>   the type parameter
     * @param value the value
     * @param clazz the clazz
     * @return the t
     */
    public <T> T resolve(final String value, final Class<T> clazz) {
        if (StringUtils.isNotBlank(value)) {
            LOGGER.trace("Parsing expression as [{}]", value);
            val expression = EXPRESSION_PARSER.parseExpression(value, PARSER_CONTEXT);
            val result = expression.getValue(evaluationContext, clazz);
            LOGGER.trace("Parsed expression result is [{}]", result);
            return result;
        }
        return (T) value;
    }

    private <T> T resolve(final String value, final Map<String, Object> variables, final Class<T> clazz) {
        val activeContext = new StandardEvaluationContext() {
            @Override
            public Object lookupVariable(@Nonnull final String name) {
                return variables.containsKey(name) ? variables.get(name) : super.lookupVariable(name);
            }
        };
        activeContext.setVariables(variables);
        val expression = EXPRESSION_PARSER.parseExpression(value, PARSER_CONTEXT);
        return expression.getValue(activeContext, clazz);
    }

    @Override
    public Object apply(final Object o) {
        return resolve(o.toString());
    }

    /**
     * With application context.
     *
     * @param applicationContext the application context
     * @return the spring expression language value resolver
     */
    public SpringExpressionLanguageValueResolver withApplicationContext(final ApplicationContext applicationContext) {
        evaluationContext.setVariable("applicationContext", (Supplier<ApplicationContext>) () -> applicationContext);
        return this;
    }

    private void initializeDynamicVariables() {
        evaluationContext.setVariable("randomNumber2", RandomUtils.randomNumeric(2));
        evaluationContext.setVariable("randomNumber4", RandomUtils.randomNumeric(4));
        evaluationContext.setVariable("randomNumber6", RandomUtils.randomNumeric(6));
        evaluationContext.setVariable("randomNumber8", RandomUtils.randomNumeric(8));
        evaluationContext.setVariable("randomNumber12", RandomUtils.randomNumeric(8));

        evaluationContext.setVariable("randomString4", RandomUtils.randomAlphabetic(4));
        evaluationContext.setVariable("randomString6", RandomUtils.randomAlphabetic(6));
        evaluationContext.setVariable("randomString8", RandomUtils.randomAlphabetic(8));

        evaluationContext.setVariable("uuid", UUID.randomUUID().toString());
        evaluationContext.setVariable("randomPort", RandomUtils.nextInt(STARTING_PORT_RANGE, ENDING_PORT_RANGE));

        evaluationContext.setVariable("localDateTime", LocalDateTime.now(ZoneId.systemDefault()).toString());
        evaluationContext.setVariable("localDateTimeUtc", LocalDateTime.now(Clock.systemUTC()).toString());

        val localStartWorkDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay().plusHours(8);
        evaluationContext.setVariable("localStartWorkDay", localStartWorkDay.toString());
        evaluationContext.setVariable("localEndWorkDay", localStartWorkDay.plusHours(9).toString());

        val localStartDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay();
        evaluationContext.setVariable("localStartDay", localStartDay.toString());
        evaluationContext.setVariable("localEndDay",
            localStartDay.plusHours(HOUR_23).plusMinutes(MINUTE_59).plusSeconds(SECOND_59).toString());

        evaluationContext.setVariable("localDate", LocalDate.now(ZoneId.systemDefault()).toString());
        evaluationContext.setVariable("localDateUtc", LocalDate.now(Clock.systemUTC()).toString());

        evaluationContext.setVariable("zonedDateTime", ZonedDateTime.now(ZoneId.systemDefault()).toString());
        evaluationContext.setVariable("zonedDateTimeUtc", ZonedDateTime.now(Clock.systemUTC()).toString());
    }
}
