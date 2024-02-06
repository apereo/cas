const assert = require("assert");
const cas = require("../../cas.js");

async function throttleTokenEndpoint() {
    const params = "grant_type=client_credentials&scope=openid";
    const url = `https://localhost:8443/cas/oidc/token?${params}`;

    await cas.log(`Log in attempt: #1 ${new Date().toISOString()}`);
    await submitRequest(url, 401);
    await cas.log(`Log in attempt: #2 ${new Date().toISOString()}`);
    await submitRequest(url, 423);
    await cas.sleep(3000);
    await cas.log(`Log in attempt: #3 ${new Date().toISOString()}`);
    await submitRequest(url, 401);
}

async function throttleUserInfoEndpoint() {
    const url = "https://localhost:8443/cas/oidc/profile?access_token=1234567890";
    await cas.log(`Log in attempt: #1 ${new Date().toISOString()}`);
    await submitRequest(url, 401);
    await cas.log(`Log in attempt: #2 ${new Date().toISOString()}`);
    await submitRequest(url, 423);
    await cas.sleep(3000);
    await cas.log(`Log in attempt: #3 ${new Date().toISOString()}`);
    await submitRequest(url, 401);
}

(async () => {
    await throttleTokenEndpoint();
    await cas.sleep(2000);
    await throttleUserInfoEndpoint();
})();

async function submitRequest(url, status) {
    await cas.log(`Calling ${url}`);
    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("unknown:unknown")}`
    }, async (res) => {
        throw `Operation should not have passed: ${res}`;
    }, async (error) => {
        await cas.log(`Expecting ${status}, Current status: ${error.response.status}`);
        assert(status === error.response.status);
    });
}
