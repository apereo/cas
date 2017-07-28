package org.apereo.cas.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link ConfigurationMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMetadataGenerator.class);

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
        final File jsonFile = new File(args[0], "classes/java/main/META-INF/spring-configuration-metadata.json");
        final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final TypeReference<Map<String, Set<ConfigurationMetadataProperty>>> values = 
                new TypeReference<Map<String, Set<ConfigurationMetadataProperty>>>() {
        };
        final Map<String, Set<ConfigurationMetadataProperty>> jsonMap = mapper.readValue(jsonFile, values);
        final Set<ConfigurationMetadataProperty> properties = jsonMap.get("properties");
        properties.stream()
                .filter(p -> NESTED_TYPE_PATTERN.matcher(p.getType()).matches())
                .forEach(Unchecked.consumer(p -> {
                    final Matcher matcher = NESTED_TYPE_PATTERN.matcher(p.getType());
                    // matcher always creates a new matcher, so you'd need to call matches() again.
                    matcher.matches();
                    final String type = matcher.group(1);
                    //final CompilationUnit cu = JavaParser.parse(type);
                    System.out.println(type);
                }));
    }
}
