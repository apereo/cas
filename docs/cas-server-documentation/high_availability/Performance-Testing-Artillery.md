---
layout: default
title: CAS - High Availability Performance Testing
category: High Availability
---

{% include variables.html %}

# Artillery Performance Testing

Load testing is an important part of ensuring that the CAS server deployment is ready for prime time production use. 
[Artillery](https://artillery.io/) is a scalable, flexible and easy-to-use platform that contains tools you need for production-grade load testing.

## Installation

You can install Artillery via npm:

```bash
npm install -g artillery
artillery --version
```

## Scripts

The scripts and scenarios can be downloaded from [here](https://github.com/apereo/cas/raw/master/etc/loadtests/artillery).

## Test Execution

For each test script, you can run the following command:

```bash
cd ./etc/loadtests/artillery
artillery run "${artilleryScript}"
```
