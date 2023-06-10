package org.apereo.cas.nativex;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import com.hazelcast.logging.Slf4jFactory;
import com.hazelcast.shaded.org.apache.calcite.DataContext;
import com.hazelcast.shaded.org.apache.calcite.adapter.enumerable.MatchUtils;
import com.hazelcast.shaded.org.apache.calcite.adapter.java.ReflectiveSchema;
import com.hazelcast.shaded.org.apache.calcite.adapter.jdbc.JdbcSchema;
import com.hazelcast.shaded.org.apache.calcite.interpreter.Row;
import com.hazelcast.shaded.org.apache.calcite.linq4j.AbstractEnumerable;
import com.hazelcast.shaded.org.apache.calcite.linq4j.EnumerableDefaults;
import com.hazelcast.shaded.org.apache.calcite.linq4j.Enumerator;
import com.hazelcast.shaded.org.apache.calcite.linq4j.Grouping;
import com.hazelcast.shaded.org.apache.calcite.linq4j.Linq4j;
import com.hazelcast.shaded.org.apache.calcite.linq4j.Lookup;
import com.hazelcast.shaded.org.apache.calcite.linq4j.MemoryFactory;
import com.hazelcast.shaded.org.apache.calcite.linq4j.QueryProvider;
import com.hazelcast.shaded.org.apache.calcite.linq4j.Queryable;
import com.hazelcast.shaded.org.apache.calcite.linq4j.QueryableDefaults;
import com.hazelcast.shaded.org.apache.calcite.linq4j.RawQueryable;
import com.hazelcast.shaded.org.apache.calcite.linq4j.function.Function;
import com.hazelcast.shaded.org.apache.calcite.linq4j.function.Functions;
import com.hazelcast.shaded.org.apache.calcite.linq4j.tree.FunctionExpression;
import com.hazelcast.shaded.org.apache.calcite.runtime.ArrayBindable;
import com.hazelcast.shaded.org.apache.calcite.runtime.Enumerables;
import com.hazelcast.shaded.org.apache.calcite.runtime.FlatLists;
import com.hazelcast.shaded.org.apache.calcite.runtime.JsonFunctions;
import com.hazelcast.shaded.org.apache.calcite.runtime.Matcher;
import com.hazelcast.shaded.org.apache.calcite.runtime.Pattern;
import com.hazelcast.shaded.org.apache.calcite.runtime.ResultSetEnumerable;
import com.hazelcast.shaded.org.apache.calcite.runtime.SqlFunctions;
import com.hazelcast.shaded.org.apache.calcite.runtime.Utilities;
import com.hazelcast.shaded.org.apache.calcite.runtime.XmlFunctions;
import com.hazelcast.shaded.org.apache.calcite.schema.QueryableTable;
import com.hazelcast.shaded.org.apache.calcite.schema.Schema;
import com.hazelcast.shaded.org.apache.calcite.schema.SchemaPlus;
import com.hazelcast.shaded.org.apache.calcite.schema.Schemas;
import com.hazelcast.sql.impl.SqlServiceImpl;
import com.hazelcast.sql.impl.type.converter.Converter;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link HazelcastTicketRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuppressWarnings("all")
public class HazelcastTicketRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints,
            List.of(
                Slf4jFactory.class,
                FunctionExpression.class,
                SqlServiceImpl.class,
                AbstractEnumerable.class,
                Schema.class,
                Schemas.class,
                SchemaPlus.class,
                ReflectiveSchema.class,
                JdbcSchema.class,
                Queryable.class,
                Lookup.class,
                Row.class,
                Pattern.class,
                Matcher.class,
                Matcher.Builder.class,
                MatchUtils.class,
                Pattern.PatternBuilder.class,
                Pattern.RepeatPattern.class,
                Pattern.SymbolPattern.class,
                Pattern.AbstractPattern.class,
                Utilities.class,
                Enumerables.class,
                Enumerables.Emitter.class,
                QueryProvider.class,
                RawQueryable.class,
                DataContext.class,
                ResultSetEnumerable.class,
                Grouping.class,
                Functions.class,
                SqlFunctions.class,
                JsonFunctions.class,
                XmlFunctions.class,
                Enumerator.class,
                EnumerableDefaults.class,
                Linq4j.class,
                QueryableTable.class,
                QueryableDefaults.ReplayableQueryable.class,
                QueryableDefaults.NonLeafReplayableQueryable.class,
                FlatLists.class,
                MemoryFactory.class,
                MemoryFactory.Memory.class,
                ArrayBindable.class
            )
        );
        var classes = findSubclassesInPackage(Converter.class, "com.hazelcast.sql");
        registerReflectionHints(hints, classes);
        registerSerializationHints(hints, classes);
        
        classes = findSubclassesInPackage(Function.class, Function.class.getPackageName());
        registerReflectionHints(hints, classes);

        FunctionUtils.doAndHandle(__ -> {
            var clazz = ClassUtils.getClass("com.hazelcast.shaded.org.jctools.queues.ConcurrentCircularArrayQueueL0Pad", false);
            registerReflectionHints(hints,
                findSubclassesInPackage(clazz, "com.hazelcast.shaded.org.jctools"));
        });
    }

    private static void registerSerializationHints(final RuntimeHints hints, final Collection<Class> entries) {
        entries.forEach(el -> hints.serialization().registerType(el));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS};
        entries.forEach(el -> hints.reflection().registerType((Class) el, memberCategories));
    }
}
