---
layout: default
title: CAS - Swivel Secure Authentication
---

# Swivel Secure Authentication

Swivel Secure offers a wide range of authentication factors, allowing the use of 2FA and image based authentication. To learn more, please refer to [the official website](https://swivelsecure.com/).

CAS supports Swivel Secure's TURing-image based authentication. TURing uses the PINsafe protocol to provide a One Time Code for authentication. Each image is unique for that session. 

![image](https://user-images.githubusercontent.com/1205228/27012173-e8e32020-4e98-11e7-935f-c5166f228bd5.png)

## Configuration

Support is enabled by including the following module in the overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-swivel</artifactId>
     <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#swivel-secure).
