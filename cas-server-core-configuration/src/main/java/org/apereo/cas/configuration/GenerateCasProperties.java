package org.apereo.cas.configuration;

import org.jooq.lambda.Unchecked;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link GenerateCasProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GenerateCasProperties {
    protected GenerateCasProperties() {}
    
    private static Object createInstance(final Class o) {
        try {
            if (o.isPrimitive()) {
                return null;
            }
            if (o.equals(String.class)) {
                return null;
            }
            return o.newInstance();
        } catch (final Throwable e) {
            return null;
        }
    }

    private static void props(final Object o, final String initialLine) {
        final HashSet<Field> set1 = new HashSet<>();
        getAllFields(set1, o.getClass());

        set1.forEach(Unchecked.consumer(
                f -> {

                    final Object instance = createInstance(f.getType());

                    if (instance != null) {
                        props(instance, initialLine + f.getName() + ".");
                    }
                }));

        final StringBuilder builder = new StringBuilder();
        final HashSet<Field> set = new HashSet<>();
        getAllFields(set, o.getClass());
        set.forEach(Unchecked.consumer(f -> {
            f.setAccessible(true);

            final Object v = f.get(o);

            if (!f.getType().getName().contains("Properties")) {
                builder.append("# " + initialLine + f.getName() + "=" + getFieldValue(v) + "\n");
            }
        }));

        if (builder.length() > 0) {
            System.out.println("##");
            if (o.getClass().isMemberClass()) {
                System.out.println("# " + o.getClass().getDeclaringClass().getSimpleName()
                        + " -> " + o.getClass().getSimpleName());
            } else {
                System.out.println("# " + o.getClass().getSimpleName());
            }
            System.out.println("#");
            System.out.println(builder.toString());
        }

    }

    private static StringBuilder getFieldValue(final Object o) {
        if (o == null) {
            return new StringBuilder();
        }

        if (Collection.class.isAssignableFrom(o.getClass())) {
            final Collection cc = Collection.class.cast(o);

            if (cc.isEmpty()) {
                return new StringBuilder("value1,value2,...");
            }

            StringBuilder b = new StringBuilder();
            cc.forEach(item -> {
                b.append(getFieldValue(item));
            });

            return b;
        }
        if (o.getClass().isArray()) {
            Object[] v = (Object[]) o;

            final Collection cc = new ArrayList<>();
            for (final Object o1 : v) {
                cc.add(o1);
            }

            if (cc.isEmpty()) {
                return new StringBuilder("value1,value2,...");
            }

            StringBuilder b = new StringBuilder();
            cc.forEach(item -> {
                b.append(getFieldValue(item));
            });

            return b;
        }

        if (Resource.class.isAssignableFrom(o.getClass())) {
            final Resource r = Resource.class.cast(o);

            if (r instanceof ClassPathResource) {
                return new StringBuilder("classpath:/" + r.getFilename());

            }
            if (r instanceof FileSystemResource) {
                return new StringBuilder("file:/" + r.getFilename());
            }

            return new StringBuilder(r.getFilename());
        }

        return new StringBuilder(o.toString());
    }

    private static Set<Field> getAllFields(final Set<Field> fields, final Class<?> type) {
        Arrays.stream(type.getDeclaredFields()).forEach(f -> {
            if (!Modifier.isFinal(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())) {
                fields.add(f);
            }
        });

        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(fields, type.getSuperclass()));
        }

        return fields;
    }

    /**
     * Generate CAS Properties.
     * @param args cmd-line args
     */
    public static void main(final String[] args) {
        System.out.println();
        CasConfigurationProperties c = new CasConfigurationProperties();
        props(c, "cas.");
    }
}
