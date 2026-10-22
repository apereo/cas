const {execFileSync} = require("child_process");
const cas = require("../../cas.js");

async function awslocal(args, input = undefined) {
    const command = ["awslocal", "s3api", ...args].map((arg) => `'${arg}'`).join(" ");
    await cas.log(`Running ${command}`);
    const output = execFileSync("docker", ["exec", "-i", "localstack", "sh", "-c",
        input === undefined ? command : `cat > /tmp/service.json && ${command}`], {input: input}).toString();
    await cas.log(output);
    return output;
}

(async () => {
    await cas.verifyServiceRegistry({
        insert: async (definition) => {
            const bucket = `cas-external-${definition.id}`;
            await awslocal(["create-bucket", "--bucket", bucket]);
            await awslocal(["put-object", "--bucket", bucket, "--key", String(definition.id),
                "--content-type", "application/json", "--body", "/tmp/service.json"], JSON.stringify(definition));
        },
        contains: async (id) => {
            const buckets = await awslocal(["list-buckets", "--query", "Buckets[].Name", "--output", "text"]);
            return buckets.split(/\s+/).some((bucket) => bucket.startsWith("cas-") && bucket.endsWith(`-${id}`));
        }
    });
})();
