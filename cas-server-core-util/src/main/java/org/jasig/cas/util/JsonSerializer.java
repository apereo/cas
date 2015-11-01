/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
