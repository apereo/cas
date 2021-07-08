const puppeteer = require('puppeteer');
const assert = require('assert');
const https = require('https');
const cas = require('../../cas.js');

(async () => {

    for (const endpoint of ["validate", "serviceValidate", "p3/serviceValidate"]) {
        console.log("Checking validation endpoint: " + endpoint);

        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);

        const service1 = "https://httpbin.org/get";
        console.log("Logging into " + service1 + " without renew to create SSO");
        await page.goto("https://localhost:8443/cas/login?service=" + service1);
        await cas.loginWith(page, "casuser", "Mellon");

        let ticket = await cas.assertTicketParameter(page);
        let body = await validate(endpoint, service1, ticket, false)

        if (endpoint === "validate") {
            assert(body === "yes\ncasuser\n")
        } else {
            assert(body.includes("<cas:authenticationSuccess>"))
        }

        const service2 = "https://httpbin.org/get";
        console.log("Logging into " + service2 + " to validate with renew=true and existing SSO");
        await page.goto("https://localhost:8443/cas/login?service=" + service2);
        ticket = await cas.assertTicketParameter(page);
        body = await validate(endpoint, service2, ticket, true);

        if (endpoint === "validate") {
            assert(body === "no\n\n")
        } else {
            assert(body.includes('<cas:authenticationFailure code="INVALID_TICKET">'))
        }
        await browser.close();
    }
})();

async function validate(endpoint, service, ticket, renew = false) {
    let httpGet = options => {
        return new Promise((resolve, reject) => {
            https.get(options, res => {
                res.setEncoding('utf8');
                const body = [];
                res.on('data', chunk => body.push(chunk));
                res.on('end', () => resolve(body.join('')));
            }).on('error', reject);
        });
    };
    let path = "/cas/" + endpoint + "?service=" + service + "&ticket=" + ticket;
    if (renew) {
        path = path + "&renew=true";
    }

    console.log("Validating " + path);
    let result = await httpGet({
        protocol: 'https:',
        hostname: 'localhost',
        port: 8443,
        path: path,
        method: 'GET',
        rejectUnauthorized: false,
    });

    console.log(result);
    return result;
}
