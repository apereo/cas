const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require("path");

(async () => {
    let body = {"configuredLevel": "INFO"};
    await ["org.apereo.cas", "org.apereo.cas.web", "org.apereo.cas.web.flow"].forEach(p => {
        cas.doRequest(`https://localhost:8443/cas/actuator/loggers/${p}`, "POST",
            {'Content-Type': 'application/json'}, 204, JSON.stringify(body, undefined, 2));
    });
})();
