package org.apereo.cas.shell.commands;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

/**
 * This is {@link ExitCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ShellCommandGroup("Utilities")
@ShellComponent
public class ExitCommand implements Quit.Command {

    /**
     * Quit.
     */
    @ShellMethod(value = "Exit the shell.", key = {"quit", "exit"})
    public static void quit() {
        System.exit(0);
    }
}

