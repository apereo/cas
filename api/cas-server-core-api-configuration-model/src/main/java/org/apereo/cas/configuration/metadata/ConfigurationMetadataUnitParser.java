package org.apereo.cas.configuration.metadata;

import module java.base;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

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
     * @param property              the p
     * @param typePath              the type path
     * @param typeName              the type name
     * @param indexNameWithBrackets the index name with brackets
     */
    public void parseCompilationUnit(final Set<ConfigurationMetadataProperty> collectedProps,
                                     final Set<ConfigurationMetadataProperty> collectedGroups,
                                     final ConfigurationMetadataProperty property,
                                     final String typePath,
                                     final String typeName,
                                     final boolean indexNameWithBrackets) {
        try (val is = Files.newInputStream(Paths.get(typePath))) {
            val cu = StaticJavaParser.parse(is);
            val separatorIndex = Math.max(typeName.lastIndexOf('.'), typeName.lastIndexOf('$'));
            val simpleTypeName = typeName.substring(separatorIndex + 1);
            val declaration = cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .filter(type -> type.getNameAsString().equals(simpleTypeName))
                .findFirst();
            if (declaration.isEmpty()) {
                val enumType = cu.findAll(EnumDeclaration.class)
                    .stream()
                    .anyMatch(type -> type.getNameAsString().equals(simpleTypeName));
                if (enumType) {
                    return;
                }
                throw new IllegalArgumentException("Unable to locate type " + typeName + " in " + typePath);
            }
            val decl = declaration.get();

            val visitor = new ConfigurationMetadataFieldVisitor(collectedProps, collectedGroups,
                indexNameWithBrackets, typeName, sourcePath);
            decl.getFields().forEach(field -> visitor.visit(field, property));

            for (val parentType : decl.getExtendedTypes()) {
                val instance = ConfigurationMetadataClassSourceLocator.getInstance();
                val parentClazz = instance.locatePropertiesClassForType(parentType);
                val parentTypePath = ConfigurationMetadataClassSourceLocator
                    .buildTypeSourcePath(this.sourcePath, parentClazz.getName());

                parseCompilationUnit(collectedProps, collectedGroups, property,
                    parentTypePath, parentClazz.getName(), indexNameWithBrackets);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
