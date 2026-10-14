package org.apereo.cas.util.scripting;

import module java.base;
import groovy.transform.CompilationUnitAware;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.transform.sc.StaticCompileTransformation;

/**
 * Applies static compilation without Groovy's dynamic AST transformation discovery.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
final class GroovyStaticCompilationCustomizer extends CompilationCustomizer implements CompilationUnitAware {
    private final StaticCompileTransformation transformation = new StaticCompileTransformation();

    private final AnnotationNode annotation = new AnnotationNode(ClassHelper.make(CompileStatic.class));

    GroovyStaticCompilationCustomizer() {
        super(CompilePhase.INSTRUCTION_SELECTION);
    }

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        transformation.setCompilationUnit(unit);
    }

    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode node) {
        annotation.setSourcePosition(node);
        transformation.visit(new ASTNode[]{annotation, node}, source);
    }
}
