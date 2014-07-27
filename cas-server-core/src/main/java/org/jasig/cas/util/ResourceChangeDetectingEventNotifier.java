/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * A class responsible for detecting contents changes of configured resource (file, classpath, URL, etc.)
 * by comparing their SHA1 digests - the one saved at the last check and the latest one. If the change
 * is detected, it publishes Spring's <code>ApplicationEvent</code> typed as <code>ResourceChangedEvent</code>
 * wrapping the resource's URI in question within it.
 * <p/>
 * Any interested <code>ApplicationListener</code>s within ApplicationContext could then pick up those events and
 * react to them appropriately.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1
 */
public final class ResourceChangeDetectingEventNotifier extends FileAlterationListenerAdaptor implements ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceChangeDetectingEventNotifier.class);

    private ApplicationEventPublisher applicationEventPublisher;

    private final Resource watchedResource;

    private final FileAlterationMonitor monitor;

    private final int repeatInterval = 5000;

    /**
     * Instantiates a new Resource change detecting event notifier.
     *
     * @param watchedResource the watched resource
     */
    public ResourceChangeDetectingEventNotifier(final Resource watchedResource) {
        try {
            this.watchedResource = watchedResource;
            if (!this.watchedResource.exists()) {
                throw new BeanCreationException(String.format("The 'watchedResource' [%s] must point to an existing resource. "
                        + "Make sure such resource exists.", this.watchedResource.getURI()));
            }

            final FileAlterationObserver observer = new FileAlterationObserver(this.watchedResource.getFile());
            observer.addListener(this);

            this.monitor = new FileAlterationMonitor(this.repeatInterval);
            this.monitor.addObserver(observer);
            this.monitor.start();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void finalize() throws Throwable {
        this.monitor.stop();
        super.finalize();
    }

    @Override
    public void onFileDelete(final File file) {
        notifyOfTheResourceChangeEvent();
    }

    @Override
    public void onFileChange(final File file) {
        notifyOfTheResourceChangeEvent();
    }

    @Override
    public void onFileCreate(final File file) {
        notifyOfTheResourceChangeEvent();
    }

    @Override
    public void onDirectoryDelete(final File directory) {
        notifyOfTheResourceChangeEvent();
    }

    @Override
    public void onDirectoryChange(final File directory) {
        notifyOfTheResourceChangeEvent();
    }

    @Override
    public void onDirectoryCreate(final File directory) {
        notifyOfTheResourceChangeEvent();
    }

    /**
     * Notify of the resource change event.
     * Publishes a {@link org.jasig.cas.util.ResourceChangeDetectingEventNotifier.ResourceChangedEvent}
     * to the receiving parties.
     */
    private synchronized void notifyOfTheResourceChangeEvent() {
        try {
            this.applicationEventPublisher.publishEvent(new ResourceChangedEvent(this, this.watchedResource.getURI()));
        } catch (final IOException e) {
            LOGGER.error("An exception is caught during 'watchedResource' access", e);
        }
    }

    /**
     * Application event representing the resource contents change.
     * Intended to be processed by subscribed <code>ApplicationListener</code>s
     * managed by ApplicationContext.
     */
    public static class ResourceChangedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 1L;

        private final URI resourceUri;

        /**
         * Instantiates a new Resource changed event.
         *
         * @param source the source
         * @param resourceUri the resource uri
         */
        public ResourceChangedEvent(final Object source, final URI resourceUri) {
            super(source);
            this.resourceUri = resourceUri;
        }

        public URI getResourceUri() {
            return this.resourceUri;
        }
    }

}
