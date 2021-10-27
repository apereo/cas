package org.jasig.cas.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

/**
 * Interface to define operations needed to map objects from/to JSON clobs.
 * @author Misagh Moayyed
 * @param <T>  the type parameter
 * @since 4.1.0
 */
public interface JsonSerializer<T> extends Serializable {
    /**
     * Create the object type from the given JSON string.
     * @param json the json string
     * @return the object instance constructed from JSON
     */
    T fromJson(String json);

    /**
     * Create the object type from the given JSON reader.
     * @param json the json string
     * @return the object instance constructed from JSON
     */
    T fromJson(Reader json);

    /**
     * Create the object type from the given JSON stream.
     * @param json the json string
     * @return the object instance constructed from JSON
     */
    T fromJson(InputStream json);

    /**
     * Create the object type from the given JSON file.
     * @param json the json string
     * @return the object instance constructed from JSON
     */
    T fromJson(File json);

    /**
     * Serialize the given object to its JSON equivalent to the output stream.
     * @param out the output stream
     * @param object the object to serialize
     */
    void toJson(OutputStream out, T object);

    /**
     * Serialize the given object to its JSON equivalent to the output writer.
     * @param out the output writer
     * @param object the object to serialize
     */
    void toJson(Writer out, T object);

    /**
     * Serialize the given object to its JSON equivalent to the output file.
     * @param out the output file
     * @param object the object to serialize
     */
    void toJson(File out, T object);
}
