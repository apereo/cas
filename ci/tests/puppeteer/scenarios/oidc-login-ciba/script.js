const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const clientNotificationToken = "8d67dc78-7faa-4d41-aabd-67707b374255";
    const userCode = "393515b9-4bb0-43ef-9973-91f3c3236ffe";
    let url = `https://localhost:8443/cas/oidc/oidcCiba?scope=${encodeURIComponent("openid profile")}`;
    url += `&client_notification_token=${clientNotificationToken}`;
    url += `&login_hint=${encodeURIComponent("casuser@apereo.org")}`;
    url += "&binding_message=HelloFromCAS";
    url += `&user_code=${userCode}`;
    url += "&requested_expiry=60";

    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    const authRequestId = await cas.doPost(url,"",
        {
            "Authorization": authzHeader,
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => {
            cas.log(res.data);
            assert(res.data.expires_in === 60);
            assert(res.data.auth_req_id !== undefined);
            return res.data.auth_req_id;
        }, (error) => {
            throw `CIBA operation failed: ${error}`;
        });
    await cas.log(`CIBA request id is ${authRequestId}`);
})();

