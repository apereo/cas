# Actuator Endpoint - SBOM

Source: https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Sbom.html

The `sbom` endpoint provides information about the software bill of materials (SBOM).

The resulting response is similar to the following:

```
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 31

{
    "ids" : [ "application" ]
}
```
