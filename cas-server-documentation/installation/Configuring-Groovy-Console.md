---
layout: default
title: CAS - Groovy Shell
---

# CAS Groovy Shell
This is a [Groovy shell embedded inside the CAS server](http://bit.ly/1P68woD) 
that could be used by deployers to interact with the CAS API at runtime,
to query the runtime state of the software and execute custom Groovy scripts. The console is aware of the CAS application 
context and is also able to load custom groovy scripts which 
may want to peek inside the CAS configuration, invoke its API and perhaps report back various bits of configuration about CAS.

Support is provided via the following dependency:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-shell</artifactId>
     <version>${cas.version}</version>
</dependency>
```

## Configuration
CAS supports an integrated Java shell called [CRaSH](http://www.crashub.org/). 
By default the remote shell will listen for connections 
on port 2000. The default user is `user` and the 
default password will be randomly generated and displayed in the log output if one is not configured.

```bash
ssh -p 2000 user@localhost


Using default password for shell access: ec03326c-4cf4-49ee-b745-7bb255c1dd7e
```


Type `help` for a list of commands.

### Custom Groovy Scripts

The shell by default will compile and load all groovy scripts that are found at the specified location below.
Scripts are loaded by their class name and added to the shell.


### Settings

```properties
# shell.command-refresh-interval=15
# shell.command-path-patterns=classpath*:/commands/**
# shell.auth.simple.user.name=user
# shell.auth.simple.user.password=password
# shell.ssh.enabled=true
# shell.ssh.port=2000
# shell.telnet.enabled=false
# shell.telnet.port=5000
# shell.ssh.auth-timeout=3000
# shell.ssh.idle-timeout=30000
```
