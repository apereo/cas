---
layout: default
title: CAS - AWS CLI Integration
category: Integration
---

{% include variables.html %}

# Overview

The functionality described here presents a dedicated integration strategy between CAS and AWS. Support is enabled by 
including the following dependency in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-aws" %}

## Configuration

{% include casproperties.html properties="cas.amazon-sts." %}

## Administrative Endpoints

The following endpoints are provided by CAS:

| Endpoint                 | Description
|--------------------------|--------------------------------------------------------
| `awsSts`                 | Obtain temporary AWS access credentials via `POST`. Can accept a `duration` parameter to specify the expiration policy for the credentials. User credentials can be provided in the `POST` request body via `username` and `password` parameters similar to the [CAS REST protocol](../protocol/REST-Protocol.html). The endpoint support all available [multifactor authentication triggers](../mfa/Configuring-Multifactor-Authentication-Triggers.html).

## Temporary Security Credentials

Obtaining temporary security credentials from AWS STS is done using the `GetSessionToken` API operation. The primary occasion for 
calling this operation is when a user must be authenticated with multi-factor authentication (MFA). THe authenticated user can qualify and 
initiate multifactor authentication using the available [multifactor authentication triggers](../mfa/Configuring-Multifactor-Authentication-Triggers.html).

For convenience you can simply copy and paste the temporary AWS access credentials
generated above to set them as environment variables or save the output into `~/.aws/credentials` 
that would be loaded and recognized by AWS CLI given the profile name.

The AWS CLI stores sensitive credential information that you specify 
with `aws configure` in a local file named `credentials`, in a folder named `.aws` in your 
home directory. The less sensitive configuration options that you specify with aws 
configure are stored in a local file named `config`, also stored in the `.aws` folder in your home directory.

To learn more about AWS CLI, please [see this guide](https://docs.aws.amazon.com/cli/latest).

### Required Permissions

Per Amazon Web Services,

> No permissions are required for a user to get a session token. The purpose of the
API operation is to authenticate the user using MFA. You cannot use policies to control authentication operations.

### Granted Permissions

Per Amazon Web Services,

> If the API is called with the credentials of an IAM user, the temporary security 
> credentials have the same permissions as the IAM user. Similarly, if 
> the API is called with AWS account root user credentials, the temporary security credentials have root user permissions.

AWS recommends that you do not call the API with root user 
credentials. Instead, create IAM users with the
permissions they need. Then use these IAM users for everyday interaction with AWS.

Note that you **cannot** use the credentials to call IAM or AWS STS 
API operations. You **can** use them to call API operations for other AWS services.
