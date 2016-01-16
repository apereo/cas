/*
 * Copyright 2007 Bruce Fancher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jasig.cas.console.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Misagh Moayyed
 * @since 4.3
 */
@Component("groovyShellService")
public class GroovyShellService {
    private static final String CONTEXT_KEY = "ctx";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Thread serverThread;

    private ServerSocket serverSocket;

    private final List<GroovyShellThread> threads = new ArrayList<>();

    @Value("${cas.console.scripts.location:classpath:/scripts}")
    private Resource scriptsLocation;

    @Value("${groovy.shell.socket.port:6789}")
    private int port;

    private Map<String, Object> bindings;

    @Autowired
    private ApplicationContext context;

    /**
     * Instantiates a new Groovy service.
     */
    protected GroovyShellService() {
    }

    /**
     * Instantiates a new Groovy service.
     *
     * @param bindings the bindings
     */
    public GroovyShellService(final Map<String, Object> bindings) {
        this.bindings = bindings;
    }

    private void launch() {
        logger.info("Launching groovy shell service...");

        try {
            this.serverSocket = new ServerSocket(port);
            logger.info("Opened server port {} on port {}", serverSocket, this.port);

            while (true) {
                final Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                    logger.info("Received client port request {} ", clientSocket);
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }

                final GroovyShellThread clientThread = new GroovyShellThread(clientSocket, createBinding());
                threads.add(clientThread);
                logger.debug("Created groovy shell thread based on client port request {}. Starting...",
                        clientSocket);
                clientThread.start();
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Create binding binding.
     *
     * @return the binding
     */
    protected Binding createBinding() {
        final Binding binding = new Binding();

        final String[] beanNames = context.getBeanDefinitionNames();
        logger.debug("Found [{}] beans in the application context", context.getBeanDefinitionCount());

        for (final String name : beanNames) {
            try {
                binding.setVariable(name, context.getBean(name));
                logger.debug("Added context bean [{}] to groovy bindings", name);
            } catch (final Exception e) {
                logger.warn("Could not add bean id [{}] to the binding. Reason: [{}]", name, e.getMessage());
            }
        }

        if (bindings != null) {
            for (final Map.Entry<String, Object> nextBinding : bindings.entrySet()) {

                logger.debug("Added variable [{}] to groovy bindings", nextBinding.getKey());
                binding.setVariable(nextBinding.getKey(), nextBinding.getValue());
            }
        }

        logger.debug("Added application context [{}] to groovy bindings", CONTEXT_KEY);
        binding.setVariable(CONTEXT_KEY, context);

        loadCustomGroovyScriptsIntoClasspath(binding);

        return binding;
    }

    /**
     * Initialize.
     */
    @PostConstruct
    public void initialize() {
        this.serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    logger.info("Launching groovy service background thread...");
                    launch();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        };

        serverThread.setDaemon(false);
        serverThread.start();
    }

    /**
     * Destroy.
     */
    @PreDestroy
    public void destroy() {
        logger.info("Closing serverSocket: {}", serverSocket);
        try {
            serverSocket.close();
            for (final GroovyShellThread nextThread : threads) {
                logger.info("Closing thread on port: {}", nextThread.getSocket());
                nextThread.destroy();
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setPort(final int port) {
        this.port = port;
    }

    private void loadCustomGroovyScriptsIntoClasspath(final Binding binding) {
        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith("groovy");
            }
        };

        final ClassLoader thisClassLoader = this.getClass().getClassLoader();
        try (final GroovyClassLoader loader = new GroovyClassLoader(thisClassLoader)) {
            final File[] files = this.scriptsLocation.getFile().listFiles(filter);
            for (final File file : files) {
                try {
                    final Class c = loader.parseClass(file);
                    final String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
                    binding.setVariable(fileNameWithOutExt, c.newInstance());
                    logger.debug("Add custom groovy script [{}] to the binding", fileNameWithOutExt);
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setBindings(final Map<String, Object> bindings) {
        this.bindings = bindings;
    }
}
