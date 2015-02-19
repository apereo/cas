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

/**
 * This class contains a pair of objects.
 *
 * @author Jerome Leleu
 * @param <A> the generic type, first item
 * @param <B> the generic type, second item
 * @since 4.0.0
 */
public class Pair<A, B> {

    /** The first object of the pair. */
    private final A first;

    /** The second object of the pair. */
    private final B second;

    /**
     * Build a pair.
     *
     * @param first the first object of the pair.
     * @param second the second object of the pair.
     */
    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Return the first object of the pair.
     * @return the first object of the pair.
     */
    public final A getFirst() {
        return this.first;
    }

    /**
     * Return the second object of the pair.
     * @return the second object of the pair.
     */
    public final B getSecond() {
        return this.second;
    }
}
