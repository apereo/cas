package org.apereo.cas.shell.commands;

/**
 * This is {@link CasShellCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface CasShellCommand {
    /**
     * Shell namespace.
     */
    String NAMESPACE = CasShellCommand.class.getPackage().getName();
}
