package org.apereo.cas.util.serialization;

import org.apereo.cas.util.CollectionUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Interface to define operations needed to map objects from/to  clobs.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.1.0
 */
public interface StringSerializer<T> extends Serializable {
    /**
     * Create the object type from the given  string.
     *
     * @param data the  string
     * @return the object instance constructed from
     */
    T from(String data);

    /**
     * Create the object type from the given  reader.
     *
     * @param reader the  string
     * @return the object instance constructed from
     */
    T from(Reader reader);

    /**
     * Create the object type from the given  stream.
     *
     * @param stream the  string
     * @return the object instance constructed from
     */
    T from(InputStream stream);

    /**
     * Create the object type from the given  file.
     *
     * @param file the  string
     * @return the object instance constructed from
     */
    T from(File file);

    /**
     * Create the object type from the given writer.
     *
     * @param writer the writer instance
     * @return the object instance constructed from
     */
    T from(Writer writer);

    /**
     * Serialize the given object to its  equivalent to the output stream.
     *
     * @param out    the output stream
     * @param object the object to serialize
     */
    void to(OutputStream out, T object);

    /**
     * Serialize the given object to its  equivalent to the output writer.
     *
     * @param out    the output writer
     * @param object the object to serialize
     */
    void to(Writer out, T object);

    /**
     * Serialize the given object to its  equivalent to the output file.
     *
     * @param out    the output file
     * @param object the object to serialize
     */
    void to(File out, T object);

    /**
     * Load a collection of specified objects from the stream.
     *
     * @param stream the stream
     * @return the collection
     */
    default Collection<T> load(final InputStream stream) {
        final T result = from(stream);
        if (result != null) {
            return CollectionUtils.wrapList(result);
        }
        return new ArrayList<>(0);
    }

    /**
     * Supports the input stream for serialization?
     *
     * @param file the file
     * @return true/false
     */
    default boolean supports(final File file) {
        return true;
    }
}
