package org.apereo.cas.shell.commands.util;

import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

/**
 * Override default exit command to make sure any threads still running don't keep shell from exiting.
 * @author Hal Deadman
 * @since 6.1.0
 */
public class ExitCommand extends Quit {

    @ShellMethod(
            value = "Exit the shell.",
            key = {"quit", "exit"}
    )
    /**
     * Quit the shell when requested, regardless of running threads.
     */
    public void quit() {
        super.quit();
        System.exit(0);
    }
}
