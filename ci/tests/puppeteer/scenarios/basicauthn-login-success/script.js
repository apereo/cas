
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const value = "casuser:Mellon";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);
    await cas.doGet("https://localhost:8443/cas/login?service=https://apereo.github.io",
        async (res) => {
            assert(res.status === 200);
            assert(res.request.host === "apereo.github.io");
            assert(res.request.protocol === "https:");
            assert(res.request.method === "GET");
            assert(res.request.path.includes("/?ticket=ST-"));
        },
        async (error) => {
            throw error;
        }, {
            "Authorization": authzHeader
        });
})();
