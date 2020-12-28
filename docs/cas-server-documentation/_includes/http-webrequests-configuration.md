Control how CAS should respond and validate incoming HTTP requests.

```properties
# cas.http-web-request.header.enabled=true

# cas.http-web-request.header.xframe=true
# cas.http-web-request.header.xframe-options=DENY

# cas.http-web-request.header.xss=true
# cas.http-web-request.header.xss-options=1; mode=block

# cas.http-web-request.header.hsts=true
# cas.http-web-request.header.xcontent=true
# cas.http-web-request.header.cache=true
# cas.http-web-request.header.content-security-policy=

# cas.http-web-request.cors.enabled=false
# cas.http-web-request.cors.allow-credentials=false
# cas.http-web-request.cors.allow-origins[0]=
# cas.http-web-request.cors.allow-methods[0]=*
# cas.http-web-request.cors.allow-headers[0]=*
# cas.http-web-request.cors.max-age=3600
# cas.http-web-request.cors.exposed-headers[0]=

# cas.http-web-request.web.force-encoding=true
# cas.http-web-request.web.encoding=UTF-8

# cas.http-web-request.allow-multi-value-parameters=false
# cas.http-web-request.only-post-params=username,password
# cas.http-web-request.params-to-check=ticket,service,renew,gateway,warn,method,target,SAMLart,pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token
# cas.http-web-request.pattern-to-block=
# cas.http-web-request.characters-to-forbid=none

# cas.http-web-request.custom-headers.header-name1=headerValue1
# cas.http-web-request.custom-headers.header-name2=headerValue2

# server.servlet.encoding.charset=UTF-8
# server.servlet.encoding.enabled=true
# server.servlet.encoding.force=true
```
