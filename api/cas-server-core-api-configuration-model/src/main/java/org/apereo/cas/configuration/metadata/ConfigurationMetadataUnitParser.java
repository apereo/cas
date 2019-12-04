package org.apereo.cas.configuration.metadata;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

/**
 * This is {@link ConfigurationMetadataUnitParser}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class ConfigurationMetadataUnitParser {
    private final String sourcePath;

    /**
     * Parse compilation unit.
     *
     * @param collectedProps        the collected props
     * @param collectedGroups       the collected groups
     * @param p                     the p
     * @param typePath              the type path
     * @param typeName              the type name
     * @param indexNameWithBrackets the index name with brackets
     */
    @SneakyThrows
    public void parseCompilationUnit(final Set<ConfigurationMetadataProperty> collectedProps,
                                     final Set<ConfigurationMetadataProperty> collectedGroups,
                                     final ConfigurationMetadataProperty p,
                                     final String typePath,
                                     final String typeName,
                                     final boolean indexNameWithBrackets) {
        try (val is = Files.newInputStream(Paths.get(typePath))) {
            val cu = StaticJavaParser.parse(is);
            new ConfigurationMetadataFieldVisitor(collectedProps, collectedGroups, indexNameWithBrackets, typeName, sourcePath).visit(cu, p);
            if (!cu.getTypes().isEmpty()) {
                val decl = ClassOrInterfaceDeclaration.class.cast(cu.getType(0));
                for (var i = 0; i < decl.getExtendedTypes().size(); i++) {
                    val parentType = decl.getExtendedTypes().get(i);
                    val instance = ConfigurationMetadataClassSourceLocator.getInstance();
                    val parentClazz = instance.locatePropertiesClassForType(parentType);
                    val parentTypePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(this.sourcePath, parentClazz.getName());

                    parseCompilationUnit(collectedProps, collectedGroups, p,
                        parentTypePath, parentClazz.getName(), indexNameWithBrackets);
                }
            }
        }
    }
}
