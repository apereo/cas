const cas = require("../../cas.js");
const assert = require("assert");
const axios = require("axios");
const https = require("https");

(async () => {

    const discoveryUrl = "https://localhost:8443/cas/oidc/.well-known";
    await cas.log(`Calling discovery URL ${discoveryUrl}`);
    await doOptions(discoveryUrl,
        {
            "Content-Type": "application/json",
            "Origin": "https://myapp:4200",
            "Host": "localhost:8443",
            "Access-Control-Request-Method": "GET",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
        },
        (res) => {
            assert(res.status === 200);
        },
        (error) => {
            throw `Operation failed: ${error}`;
        });

})();

async function doOptions(url, headers, successHandler, failureHandler) {
    const instance = axios.create({
        timeout: 8000,
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    const config = {
        headers: headers
    };
    return instance
        .options(url, config)
        .then((res) => successHandler(res))
        .catch((error) => failureHandler(error));
}
