package org.jasig.cas.console.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
final class GroovyShellThread extends Thread {
    private static final String OUT_KEY = "out";

    private Socket socket;
    private Binding binding;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new Groovy shell thread.
     *
     * @param socket  the socket
     * @param binding the binding
     */
    GroovyShellThread(final Socket socket, final Binding binding) {
        super();
        this.socket = socket;
        this.binding = binding;
    }

    @Override
    public void run() {
        try (final PrintStream out = new PrintStream(socket.getOutputStream());
             final InputStream in =  socket.getInputStream()) {
            logger.debug("Created socket IO streams...");
            
            binding.setVariable(OUT_KEY, out);
            logger.debug("Added output stream to binding collection as {}", OUT_KEY);
            
            final GroovyClassLoader loader = new GroovyClassLoader(this.getContextClassLoader());
            final IO io = new IO(in, out, out);
                        
            final Groovysh gsh = new Groovysh(loader, binding, io);
            
            try {
                logger.debug("Launching groovy interactive shell");
                gsh.run("");
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (socket != null) {
                IOUtils.closeQuietly(this.socket);
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void destroy() {
        IOUtils.closeQuietly(this.socket);
    }
}
