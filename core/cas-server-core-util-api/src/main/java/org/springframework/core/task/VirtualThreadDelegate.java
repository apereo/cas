package org.springframework.core.task;

import java.util.concurrent.ThreadFactory;

/**
 * The following class exist to assist with creating
 * task executors and schedulers when virtual threads are enabled,
 * and to bypass issues related to the correct selection of the
 * JDK-21 version of {@link VirtualThreadDelegate} that is packaged in
 * Spring in a format compliant with multi-release (MR) JARs.
 * <p>
 * In other words, at runtime we expect to see the copy of {@code VirtualThreadDelegate}
 * that ships with Spring and is designed to support virtual threads in JDK 21, and yet, the default version
 * of this class in Spring core is loaded which leads to the following error when virtual threads are enabled:
 * * {@code UnsupportedOperationException: Virtual threads not supported on JDK <21}.
 * <p>
 * As of this writing, it's not immediately clear why the JDK-21 version of this class is not chosen correctly.
 * Our best guess is that this is likely due to Gradle's lack of support for MR JARs, or it might
 * have something to do with how the Spring Boot classloader loads Uber JARs.
 * <p>
 * We expect to remove this class once support for MR JARs improves,
 * or when the default version {@code VirtualThreadDelegate} is able to directly support JDK 21
 * and virtual threads without relying on MR JARs. Owning and keeping the JDK-21 variant of this
 * class directly on the classpath will force the classloader to choose this, and not have to
 * deal with the complexities and weirdness of MR JARs.
 *
 * @author Juergen Hoeller
 * @author Misagh Moayyed
 * @see VirtualThreadTaskExecutor
 * @since 7.0.0
 * @deprecated since 7.0.0 for removal.
 */
@Deprecated(since = "7.0.0", forRemoval = true)
@SuppressWarnings("UnusedMethod")
final class VirtualThreadDelegate {

    private final Thread.Builder threadBuilder = Thread.ofVirtual();

    public ThreadFactory virtualThreadFactory() {
        return this.threadBuilder.factory();
    }

    public ThreadFactory virtualThreadFactory(final String threadNamePrefix) {
        return this.threadBuilder.name(threadNamePrefix, 0).factory();
    }

    public Thread newVirtualThread(final String name, final Runnable task) {
        return this.threadBuilder.name(name).unstarted(task);
    }

}
