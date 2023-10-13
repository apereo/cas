const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    let value = `casuser:Mellon`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    await cas.log(`Authorization header: ${authzHeader}`);
    await cas.doGet("https://localhost:8443/cas/login?service=https://apereo.github.io",
        async res => {
            assert(res.status === 200);
            assert(res.request.host === "apereo.github.io");
            assert(res.request.protocol === "https:");
            assert(res.request.method === "GET");
            assert(res.request.path.includes("/?ticket=ST-"));
        },
        async error => {
            throw error;
        }, {
           'Authorization': authzHeader
        });
})();
