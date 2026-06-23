const assert = require("assert");
const cas = require("../../cas.js");

(async () => {

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=password&";
    params += "username=casuser&";
    params += "password=Mellon&";

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, async (res) => {

        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        await cas.log("Decoding JWT access token...");
        const decoded = await cas.decodeJwt(res.data.access_token);

        assert(decoded.sub === "casuser");
        assert(decoded["staticattribute"] === "xxxx");
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    let params2 = "client_id=client2&";
    params2 += "client_secret=secret2&";
    params2 += "grant_type=password&";
    params2 += "username=casuser&";
    params2 += "password=Mellon&";

    const url2 = `https://localhost:8443/cas/oidc/token?${params2}`;
    await cas.log(`Calling ${url2}`);

    await cas.doPost(url2, "", {
        "Content-Type": "application/json"
    }, async (res) => {

        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        await cas.log("Decoding JWT access token...");
        const decoded = await cas.decodeJwt(res.data.access_token);

        assert(decoded.sub === "casuser");
        assert(decoded["gender"] === "Female");
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

})();
