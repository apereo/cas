const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const https = require('https');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    const service = "https://example.com";

    await page.goto("https://localhost:8443/cas/login?service=" + service);
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    let result = new URL(page.url());
    let ticket = result.searchParams.get("ticket");
    console.log(ticket);
    assert(ticket != null);

    let options = {
        protocol: 'https:',
        hostname: 'localhost',
        port: 8443,
        path: '/cas/validate?service=' + service + "&ticket=" + ticket,
        method: 'GET',
        rejectUnauthorized: false,
    };

    const httpGet = options => {
        return new Promise((resolve, reject) => {
            https.get(options, res => {
                res.setEncoding('utf8');
                const body = [];
                res.on('data', chunk => body.push(chunk));
                res.on('end', () => resolve(body.join('')));
            }).on('error', reject);
        });
    };
    const body = await httpGet(options);
    console.log(body)
    assert(body === "yes\ncasuser\n")
    await browser.close();
})();
