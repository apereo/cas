package org.apereo.cas.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link ConfigurationMetadataGenerator}.
 * This class is invoked by the build during the finalization of the compile phase.
 * Its job is to scan the generated configuration metadata and produce metadata
 * for settings that the build process is unable to parse. Specifically,
 * this includes fields that are of collection type (indexed) where the inner type is an
 * externalized class.
 * 
 * Example:
 * <code>
 *     private List&lt;SomeClassProperties&gt; list = new ArrayList&lt;&gt;()
 * </code>
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataGenerator {
    private static final int SHORT_DESC_LENGTH = 50;

    private static final Pattern NESTED_TYPE_PATTERN = Pattern.compile("java\\.util\\.\\w+<(org\\.apereo\\.cas\\..+)>");

    protected ConfigurationMetadataGenerator() {
    }

    /**
     * Main.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        final String buildDir = args[0];
        final String sourcePath = args[1];

        final File jsonFile = new File(buildDir, "classes/java/main/META-INF/spring-configuration-metadata.json");
        final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final TypeReference<Map<String, Set<ConfigurationMetadataProperty>>> values = new TypeReference<Map<String, Set<ConfigurationMetadataProperty>>>() {
        };
        final Map<String, Set<ConfigurationMetadataProperty>> jsonMap = mapper.readValue(jsonFile, values);
        final Set<ConfigurationMetadataProperty> properties = jsonMap.get("properties");
        
        final Set<ConfigurationMetadataProperty> collectedProps = new HashSet<>();
        properties.stream()
                .filter(p -> NESTED_TYPE_PATTERN.matcher(p.getType()).matches())
                .forEach(Unchecked.consumer(p -> {
                    final Matcher matcher = NESTED_TYPE_PATTERN.matcher(p.getType());
                    // matcher always creates a new matcher, so you'd need to call matches() again.
                    matcher.matches();
                    final String type = matcher.group(1).replace(".", File.separator);
                    final String typePath = sourcePath + "/src/main/java/" + type + ".java";
                    try (InputStream is = new FileInputStream(typePath)) {
                        final CompilationUnit cu = JavaParser.parse(is);
                        new FieldVisitor(collectedProps).visit(cu, p);
                    }
                }));
        properties.addAll(collectedProps);
        jsonMap.put("properties", properties);
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonMap);
    }

    private static class FieldVisitor extends VoidVisitorAdapter<ConfigurationMetadataProperty> {
        private final Set<ConfigurationMetadataProperty> properties;

        FieldVisitor(final Set<ConfigurationMetadataProperty> properties) {
            this.properties = properties;
        }

        @Override
        public void visit(final FieldDeclaration n, final ConfigurationMetadataProperty arg) {
            if (n.getJavadoc().isPresent() && !n.getVariables().isEmpty()) {
                final VariableDeclarator variable = n.getVariables().get(0);
                final String indexedName = arg.getName().concat("[].").concat(variable.getNameAsString());
                final ConfigurationMetadataProperty prop = new ConfigurationMetadataProperty();
                final String description = n.getJavadoc().get().getDescription().toText();
                prop.setDescription(description);
                prop.setShortDescription(StringUtils.abbreviate(description, SHORT_DESC_LENGTH));
                prop.setName(indexedName);
                prop.setId(indexedName);
                prop.setType(n.getElementType().asString());

                if (variable.getInitializer().isPresent()) {
                    final Expression exp = variable.getInitializer().get();
                    if (exp instanceof LiteralStringValueExpr) {
                        prop.setDefaultValue(((LiteralStringValueExpr) exp).getValue());
                    } else if (exp instanceof BooleanLiteralExpr) {
                        prop.setDefaultValue(((BooleanLiteralExpr) exp).getValue());
                    }
                }
                properties.add(prop);
            }

        }
    }
}
