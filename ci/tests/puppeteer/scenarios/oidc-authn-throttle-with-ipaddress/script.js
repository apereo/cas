const assert = require('assert');
const cas = require('../../cas.js');

async function throttleTokenEndpoint() {
    let params = "client_id=unknown&";
    params += "client_secret=unknown&";
    params += "grant_type=client_credentials&";
    params += "scope=openid";
    let url = `https://localhost:8443/cas/oidc/token?${params}`;

    console.log(`Log in attempt: #1 ${new Date().toISOString()}`)
    await submitRequest(url, 401);
    console.log(`Log in attempt: #2 ${new Date().toISOString()}`)
    await submitRequest(url, 423);
    await cas.sleep(3000)
    console.log(`Log in attempt: #3 ${new Date().toISOString()}`)
    await submitRequest(url, 401);
}

async function throttleUserInfoEndpoint() {
    let url = 'https://localhost:8443/cas/oidc/profile?access_token=1234567890';
    console.log(`Log in attempt: #1 ${new Date().toISOString()}`)
    await submitRequest(url, 401);
    console.log(`Log in attempt: #2 ${new Date().toISOString()}`)
    await submitRequest(url, 423);
    await cas.sleep(3000)
    console.log(`Log in attempt: #3 ${new Date().toISOString()}`)
    await submitRequest(url, 401);
}

(async () => {
    await throttleTokenEndpoint();
    await cas.sleep(2000)
    await throttleUserInfoEndpoint();
})();

async function submitRequest(url, status) {
    console.log(`Calling ${url}`);
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, function (res) {
        throw `Operation should not have passed: ${res}`
    }, function (error) {
        console.log(`Expecting ${status}, Current status: ${error.response.status}`);
        assert(status === error.response.status)
    });
}
