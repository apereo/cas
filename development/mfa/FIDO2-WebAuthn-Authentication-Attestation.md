# FIDO2 WebAuthn Multifactor Authentication - Attestation Trust & Metadata

Source: https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication-Attestation.html

WebAuthn support in CAS can handle authenticator attestation, which provides a way for the web service to request cryptographic proof of what authenticator the user is using.

Attestation trust metadata can be taught to CAS using the following strategies.

**JSON**

WebAuthn attestation trust metadata can be loaded from a JSON file and CAS by default ships with and has enabled metadata offered by Yubico, for a series of devices that are also primarily offered by Yubico.

The structure of this JSON file is as follows:

```json
{
  "identifier": "...",
  "version": 1,
  "vendorInfo": {
    "url": "...",
    "imageUrl": "...",
    "name": "..."
  },
  "trustedCertificates": [ "..." ],
  "devices": [
    {
      "deviceId": "...",
      "displayName": "...",
      "transports": 4,
      "deviceUrl": "...",
      "imageUrl": ".",
      "selectors": [
        {
          "type": "x509Extension",
          "parameters": {
            "key": "...",
            "value": {
              "type": "hex",
              "value": "..."
            }
          }
        }
      ]
    }
  ]
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.web-authn.core.trust-source.trusted-device-metadata.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**FIDO**

The [FIDO Alliance Metadata Service (MDS)](https://fidoalliance.org/metadata/) is a centralized repository of the Metadata Statement that is used by the relying parties to validate authenticator attestation and prove the genuineness of the device model. MDS also provides information about certification status of the authenticators, and found security issues. Organizations deploying FIDO Authentication are able to use this information to select specific certification levels as required for compliance, and work through the security notifications to ensure effective incident response.

> **Usage**
>
> To activate this functionality, you will need to accept the legal terms and conditions that are put together by FIDO Alliance via CAS configuration by specifying the appropriate legal header.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.web-authn.core.trust-source.fido.legal-header` (Required; type `String`): Set legal headers expected in the metadata BLOB. By using the FIDO Metadata Service, you will be subject to its terms of service. This setting serves two purposes: To remind you and any adopters/reviewers that you need to read those terms of service before using this feature. To help you detect if the legal header changes, so you can take appropriate action. If the legal header in the downloaded BLOB does not equal any of the expected headers, an exception will be thrown in the finalizing configuration step. Note that CAS makes no guarantee that a change to the FIDO Metadata Service terms of service will also cause a change to the legal header in the BLOB. The current legal header is noted by: `#DEFAULT_LEGAL_HEADER` which is the following: `"Retrieval and use of this BLOB indicates acceptance of the appropriate agreement located at https://fidoalliance.org/metadata/metadata-legal-terms/"`.
- `cas.authn.mfa.web-authn.core.trust-source.fido.trust-root-hash` (Required; type `String`; default `cbb522d7b7f127ad6a0113865bdf1cd4102e7d0759af635a7cf4720dc963c53b`): Certificate SHA-256 hash required for PKI to verify the downloaded certificate. Separate hash values with a comma.
- `cas.authn.mfa.web-authn.core.trust-source.fido.trust-root-url` (Required; type `String`; default `https://secure.globalsign.com/cacert/root-r3.crt`): Certificate required for PKI to verify the downloaded blob. This is the current FIDO Metadata Service trust root certificate. If the cert is downloaded, it is also written to the cache File. The certificate will be downloaded if it does not exist in the cache, or if the cached certificate is not currently valid.
- `cas.authn.mfa.web-authn.core.trust-source.fido.blob-cache-file` (Optional; type `File`): Cache metadata BLOB in the file cache file. If cache file exists, is a normal file, is readable, and is not out of date, then it will be used as the FIDO Metadata Service BLOB. Otherwise, the metadata BLOB will be downloaded and written to this file.
- `cas.authn.mfa.web-authn.core.trust-source.fido.metadata-blob-url` (Optional)
- `cas.authn.mfa.web-authn.core.trust-source.fido.trust-root-cache-file` (Optional; type `File`): Cache the trust root certificate in the file cache file. If cache file exists, is a normal file, is readable, matches one of the SHA-256 hashes configured in and contains a currently valid X.509 certificate, then it will be used as the trust root for the FIDO Metadata Service blob. Otherwise, the trust root certificate will be downloaded and written to this file.
