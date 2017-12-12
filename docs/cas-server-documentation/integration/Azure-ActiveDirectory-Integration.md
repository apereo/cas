---
layout: default
title: CAS -Azure Active Directory Integration
---

# Overview

The integration between the CAS Server and Azure Active Directory delegates user authentication from CAS Server
to Azure Active Directory, making CAS Server an Azure Active Directory client. Claims released from Azure Active Directory are made available as attributes to CAS Server, and by extension CAS Clients.

The task of delegating authentication is handled via the OAuth2 protocol, where CAS as a client asks for a `code` as part of the code and subsequent obtains an `id_token` and `access_token` once that code is exchanged. The CAS server needs to be registered in Azure Active Directory as an authorized application. As part of the registration process, you are required to obtain a tenant id, an application id for which you have created a secret key and authorized various claims. The `redirect_uri` of the registered CAS server is expected to be its own login endpoint (i.e. `https://sso.example.org/cas/login`).

## Registering CAS

![image](https://user-images.githubusercontent.com/1205228/33871153-806bfaaa-dece-11e7-83c8-04025e4fd8f6.png)

Note that the tenant id is equal to the Azure Active Directory id:

![image](https://user-images.githubusercontent.com/1205228/33871265-029985ba-decf-11e7-9b83-05360b0ebd26.png)

## Reply (Redirect) URLs

![image](https://user-images.githubusercontent.com/1205228/33871115-4e4c419c-dece-11e7-8b23-2642a6cf7f63.png)

## Generating Client Secret

![image](https://user-images.githubusercontent.com/1205228/33871053-fd326c96-decd-11e7-919b-fefceaa6fb72.png)

## Required Permissions

![image](https://user-images.githubusercontent.com/1205228/33871097-3372d9ee-dece-11e7-9fca-e319b14f2f1a.png)

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-azure-ad-authentication</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html#azure-active-directory-authentication).