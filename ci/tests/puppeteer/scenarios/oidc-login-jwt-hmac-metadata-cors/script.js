const assert = require("assert");
const cas = require("../../cas.js");
const axios = require("axios");
const https = require("https");


(async () => {
    const privateKey = "enTHR15K28p0N6f404HaC9Vp1cfIBgQiHhmbgBiO7UHEnSiNJudxtDhPQNFjFQtOVSjEYu0pr5yxEeBAiO6IlA";
    const jwt = await cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
        "iss": "https://localhost:8443/cas/oidc",
        "aud": "client",
        "exp": 185542587100,
        "iat": 1653737633,
        "nbf": 1653737573,
        "sub": "casuser",
        "client_id": "client"
    }, privateKey, "HS512");

    let params = "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&";
    params += `client_assertion=${jwt}&client_id=client&`;
    params += "grant_type=client_credentials&";
    params += "scope=openid";

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.access_token !== null);
        assert(res.data.refresh_token !== null);
        assert(res.data.id_token !== null);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const discoveryUrl = "https://localhost:8443/cas/oidc/.well-known";
    await cas.log(`Calling discovery URL ${discoveryUrl}`);
    await doOptions(discoveryUrl, {
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
