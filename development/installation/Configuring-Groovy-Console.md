---
layout: default
title: CAS - Groovy Console
---

# CAS Groovy Console
This is a Groovy shell embedded inside the CAS server that could be used by deployers to interact with the CAS API at runtime,
to query the runtime state of the software and execute custom Groovy scripts. The console is fully aware of the CAS application 
context and is also able to load custom groovy scripts which 
may want to peek inside the CAS configuration, invoke its API and perhaps report back various bits of configuration about CAS.

Support is provided via the following dependency:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-console</artifactId>
     <version>${cas.version}</version>
</dependency>
```

## Security
Note that deployers have access to the full power of CAS APIs via the console. Extra care must be taken into account when
interacting with the shell, otherwise the runtime instance may be totally corrupted. Furthermore, be advised that connections
to the console are done over a configured port that MUST be **secured** to only allow access from trusted connections.

## Configuration

The shell is simply a wrapper around the `Groovysh` tool that is able to respond to client requests by 
launching a separate thread for each. The groovy shell service listener component on the server side launches 
on startup and binds on the specified port below. It is also able to load Groovy scripts from the provided location
in the settings. 

This means that in order to connect, you could use:

* **Telnet**: `telnet localhost 6789` (You may have to turn off local echos)
* Use **Putty** with the following configuration:
    * Connect to `localhost` and `6789`
    * Connection Type: `Telnet`
    * Close Window on Exit: `Never`
    * Telnet Negotiation Mode: `Passive`
    * Session Logging: `All Session Output`

Successful connection attempts should present the groovy shell prompt, that is:

```groovy
Groovy Shell (2.1.7, JVM: 1.7.0_25)
Type 'help' or '\h' for help.
--------------------------------------------------------------------------------------
groovy:000>
```

### Groovy Bindings
The following variables are available to the shell automatically:

* All beans that are registered with the application context. In other words, every bean that 
is registered with CAS application context is available to the console, 
except of course those that cannot be instantiated, such as abstract beans. Note that you cannot invoke aliases for bean names. 
You would instead need to invoke the actual bean name that is aliased. For instance, invoking `ticketRegistry` in the console has no 
affect. Rather, you would need to invoke `defaultTicketRegistry` which is the real bean mapped to the alias `ticketRegistry`. 
* The output stream as the `out` variable.
* The application context as the `ctx` variable

### Executing Groovy Commands
    
* Use `help` to learn groovy shell commands. In particular `show classes`, `display`, `history` and `load <groovy-file>` 
are extra helpful.
* Directly interact with the bindings. For instance you may inspect the `defaultTicketRegistry` bean:

```groovy
groovy:000> defaultTicketRegistry
===> org.apereo.cas.ticket.registry.DefaultTicketRegistry@bc1fe6
groovy:000> defaultTicketRegistry.getTickets()
===> []
```

All CAS public APIs may be used by the shell to interact with the application context.

### Custom Groovy Scripts

The shell by default will compile and load all groovy scripts that are found at the specified location below.
Scripts are loaded by their class name and added to the shell binding collection. A sample `CasVersion` is provided
that shows how a groovy script, with access to the application context may report back results about the webapp:

```groovy
groovy:000> CasVersion.run(ctx)

CasVersion.run(ctx)
===> CAS version: 4.3.0-SNAPSHOT
Ticket registry instance: DefaultTicketRegistry

groovy:000>
```

The script itself is available as `CasVersion.groovy` that is the following:

```groovy
package scripts

def class CasVersion {
    def static run(def ctx) {
        def output = "CAS version: " + org.apereo.cas.util.CasVersion.getVersion()
        def ticketRegistry = ctx.getBean("ticketRegistry");
        output += "\nTicket registry instance: " + ticketRegistry.getClass().getSimpleName()
        return output
    }
}
```

### Settings
```properties
# cas.console.scripts.location:classpath:/scripts
# cas.console.socket.port=6789
```
