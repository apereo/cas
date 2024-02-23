const assert = require("assert");
const fs = require("fs");
const {JSONPath} = require("jsonpath-plus");
const cas = require("../../cas.js");

const startPuppeteerLoadTest = require("puppeteer-loadtest");
const args = process.argv.slice(2);
const config = JSON.parse(fs.readFileSync(args[0]));

const paramOptions = {
    file: config.loadScript,
    samplesRequested: config.samplesRequested,
    concurrencyRequested: config.concurrencyRequested
};
const loattest = async () => startPuppeteerLoadTest(paramOptions);

loattest().then((results) => {
    cas.log(JSON.stringify(results, null, 2));
    const samples = JSONPath({path: "$..sample", json: results });
    assert(samples.length === parseInt(config.samplesRequested));
});
