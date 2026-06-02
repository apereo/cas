package org.apereo.cas.util.spring;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Operation;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.ParserContext;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

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
        new SpelParserConfiguration(SpelCompilerMode.OFF, null)
    );

    private static SpringExpressionLanguageValueResolver INSTANCE;

    private final StandardEvaluationContext evaluationContext;

    protected SpringExpressionLanguageValueResolver() {
        val systemProperties = System.getProperties();

        evaluationContext = new StandardEvaluationContext();
        evaluationContext.setVariable("systemProperties", systemProperties);
        evaluationContext.setVariable("sysProps", systemProperties);

        val environmentVariables = System.getenv();
        evaluationContext.setVariable("environmentVars", environmentVariables);
        evaluationContext.setVariable("environmentVariables", environmentVariables);
        evaluationContext.setVariable("envVars", environmentVariables);
        evaluationContext.setVariable("env", environmentVariables);

        evaluationContext.setVariable("tempDir", FileUtils.getTempDirectoryPath());
        evaluationContext.setVariable("zoneId", ZoneId.systemDefault().getId());

        evaluationContext.setTypeLocator(new NoTypeLocator());
        evaluationContext.setBeanResolver(null);
        evaluationContext.setConstructorResolvers(List.of());
        evaluationContext.setMethodResolvers(List.of());
        evaluationContext.setPropertyAccessors(List.of(
            DataBindingPropertyAccessor.forReadOnlyAccess()
        ));
        evaluationContext.setIndexAccessors(List.of());
        evaluationContext.setTypeConverter(new NoTypeConverter());
        evaluationContext.setTypeComparator(new NoTypeComparator());
        evaluationContext.setOperatorOverloader(new NoOperatorOverloader());
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
    public <T> @Nullable T resolve(final String value, final Class<T> clazz) {
        if (StringUtils.isNotBlank(value)) {
            LOGGER.trace("Parsing expression as [{}]", value);
            val expression = EXPRESSION_PARSER.parseExpression(value, PARSER_CONTEXT);
            val result = expression.getValue(evaluationContext, clazz);
            LOGGER.trace("Parsed expression result is [{}]", result);
            return result;
        }
        return (T) value;
    }

    private <T> @Nullable T resolve(final String value, final Map<String, Object> variables, final Class<T> clazz) {
        val activeContext = new StandardEvaluationContext() {
            @Override
            public Object lookupVariable(@NonNull final String name) {
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

    private static final class NoTypeLocator implements TypeLocator {

        @Override
        public Class<?> findType(final String typeName) throws EvaluationException {
            throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
        }

    }

    private static final class NoTypeConverter implements TypeConverter {
        @Override
        public boolean canConvert(@Nullable final TypeDescriptor sourceType, final TypeDescriptor targetType) {
            return false;
        }

        @Override
        public @Nullable Object convertValue(@Nullable final Object value, @Nullable final TypeDescriptor sourceType,
                                             final TypeDescriptor targetType) {
            return null;
        }
    }


    private static final class NoTypeComparator implements TypeComparator {
        @Override
        public boolean canCompare(@Nullable final Object firstObject, @Nullable final Object secondObject) {
            return false;
        }

        @Override
        public int compare(@Nullable final Object firstObject, @Nullable final Object secondObject) throws EvaluationException {
            return 0;
        }
    }

    private static final class NoOperatorOverloader implements OperatorOverloader {
        @Override
        public boolean overridesOperation(final Operation operation, @Nullable final Object leftOperand,
                                          @Nullable final Object rightOperand) throws EvaluationException {
            return false;
        }

        @Override
        public @Nullable Object operate(final Operation operation, @Nullable final Object leftOperand,
                                        @Nullable final Object rightOperand) throws EvaluationException {
            return null;
        }
    }
}
