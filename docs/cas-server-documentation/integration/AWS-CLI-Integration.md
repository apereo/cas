---
layout: default
title: CAS - AWS CLI Integration
category: Integration
---

{% include variables.html %}

# Overview

Support is enabled by including the following dependency in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-aws" %}

## Configuration

{% include casproperties.html properties="cas.aamazon-sts." %}

## Administrative Endpoints

The following endpoints are provided by CAS:

| Endpoint                 | Description
|--------------------------|--------------------------------------------------------
| `awsSts`                 | Obtain temporary AWS access credentials.
