---
layout: default
title: CAS - High Availability Performance Testing
---

# High Availability Performance Testing

Load testing is an important part of ensuring that the CAS server deployment is ready for prime time production use. This page outlines a number of strategies and tools you may use to run performance tests on your deployment and observe results.

## Locust

[Locust](http://locust.io/) is an easy-to-use, distributed, user load testing tool. It is intended for load-testing web sites (or other systems) and figuring out how many concurrent users a system can handle. [See this guide](http://docs.locust.io/en/latest/what-is-locust.html) for more info.

### Setup

A fundamental feature of Locust is that you describe all your test in Python code. No need for clunky UIs or bloated XML, just plain code. For this to work, you will need to [download Python](https://www.python.org/downloads/).

Download the Locust test suite [from here](https://github.com/apereo/cas/blob/master/etc/loadtests/locustfiles.tgz). Then configure a virtual environment and install modules:

```bash
pip install virtualenv
virtualenv mylocustenv/

# Use `requirements.py3.txt` for Python 3.x
cd mylocustenv
pip install -r requirements.txt
```

For more info on virtual environments: https://virtualenv.pypa.io/en/stable/

Install Locust via the following:

```bash
pip install locustio
```

Once you have the python modules installed in your `virtualenv`:

```bash
virtualenv locusttests
```

Create a `credentials.csv` file that contains `username,password` entries used for load tests.

```bash
echo casuser,Mellon > credentials.csv
```

 and run it as such:

```bash
locust -f locustfile.py --host=https://cas.example.org
...
[2017-05-02 16:31:49,742] test/INFO/locust.main: Starting web monitor at *:8089
[2017-05-02 16:31:49,744] test/INFO/locust.main: Starting Locust 0.8a2
```

Navigate to `http://localhost:8089` and proceed with starting the test swarm.