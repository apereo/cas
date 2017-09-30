---
layout: default
title: CAS - OS Service Deployment
---

# OS Service Deployment

CAS can be easily started as Unix/Linux services using either `init.d` or `systemd`. Windows support is also made available via an exteral daemon.

## `init.d` Service

If CAS is built and run as [a fully executable web application](Configuring-Servlet-Container.html), then it can be used as an `init.d` service. Simply `symlink` the web application file to `init.d` to support the standard `start`, `stop`, `restart` and `status` commands.

The configuration built into CAS allows it to interact with the OS system configuration as such:

- Start the service as the user that owns the jar file
- Track CAS web applications' PID using `/var/run/cas/cas.pid`
- Write console logs to `/var/log/cas.log`

To install CAS as an `init.d` service simply create a symlink:

```bash
sudo ln -s /path/to/cas.war /etc/init.d/cas
service cas start
```

You can also flag the application to start automatically using your standard operating system tools. For example, on Debian:

```bash
update-rc.d myapp defaults <priority>
```

### Security

When executed as `root`, as is the case when `root` is being used to start an `init.d` service, the CAS default executable script will run the web application as the user which owns the web application file. You should **never** run CAS as `root` so the web applicatio file should never be owned by `root`. Instead, create a specific user to run CAS and use `chown` to make it the owner of the file. For example:

```bash
chown bootapp:bootapp /path/to/cas.war
```

You may also take steps to prevent the modification of the CAS web application file. Firstly, configure its permissions so that it cannot be written and can only be read or executed by its owner:

```bash
chmod 500 /path/to/cas.war
```

Additionally, you should also take steps to limit the damage if the CAS web application or the account that’s running it is compromised. If an attacker does gain access, they could make the web application file writable and change its contents. One way to protect against this is to make it immutable using `chattr`:

```bash
$ sudo chattr +i /path/to/cas.war
```

This will prevent any user, including `root`, from modifying the file.

## `systemd` Service

To install CAS as a `systemd` service create a script named `cas.service` using the following example and place it in `/etc/systemd/system` directory:

```ini
[Unit]
Description=CAS
After=syslog.target

[Service]
User=bootapp
ExecStart=/path/to/cas.war
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

<div class="alert alert-info"><strong>Not So Fast</strong><p>Remember to change the <code>Description</code>, <code>User</code> and <code>ExecStart</code> fields for your deployment.</p></div>

The user that runs the CAS web application, PID file and console log file are managed by `systemd` itself and therefore must be configured using appropriate fields in `service` script. Consult [the service unit configuration man page](https://www.freedesktop.org/software/systemd/man/systemd.service.html) for more details.

To flag the application to start automatically on system boot use the following command:

```bash
systemctl enable cas.service
```

Refer to `man systemctl` for more details.

## Windows Service

CAS may be started as Windows service using [winsw](https://github.com/kohsuke/winsw). Refer to [this example](https://github.com/snicoll-scratches/spring-boot-daemon) to learn more.
