package org.apereo.cas.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

import java.nio.charset.StandardCharsets

import javax.inject.Inject

abstract class PublishProjectModulesMetadataTask extends DefaultTask {
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getModulesMetadataFile()

    @Input
    abstract Property<String> getCasVersion()

    @Input
    abstract Property<String> getMongoImportExecutable()

    @Input
    abstract Property<String> getMongoUriEnvironmentVariable()

    @Input
    abstract Property<String> getMongoUriSystemProperty()

    @Input
    abstract Property<String> getCollectionNamePrefix()

    @Input
    abstract Property<String> getMetadataDescription()

    @Input
    abstract Property<Boolean> getJsonArray()

    @Inject
    abstract ExecOperations getExecOperations()

    PublishProjectModulesMetadataTask() {
        group = "Publishing"
        description = "Publish generated CAS module metadata to MongoDB."
        mongoImportExecutable.convention("mongoimport")
        mongoUriEnvironmentVariable.convention("CAS_MODULE_METADATA_MONGODB_URL")
        mongoUriSystemProperty.convention("casModuleMetadataMongoDbUrl")
        collectionNamePrefix.convention("casmodules")
        metadataDescription.convention("CAS module metadata")
        jsonArray.convention(true)
    }

    @TaskAction
    void publishMetadata() {
        def metadataFile = modulesMetadataFile.get().asFile
        if (!metadataFile.exists()) {
            throw new GradleException("${metadataDescription.get()} file does not exist: ${metadataFile}")
        }

        def mongoUri = findMongoUri()
        def version = casVersion.get()
        def versionNumbers = version.tokenize("-").first().replace(".", "")
        if (!versionNumbers) {
            throw new GradleException("Unable to determine ${metadataDescription.get()} collection from version ${version}")
        }
        def collectionName = "${collectionNamePrefix.get()}${versionNumbers}"

        logger.quiet("Checking CAS version ${version}...")
        logger.quiet("CAS simple version number is: ${versionNumbers}")
        logger.quiet("CAS metadata collection is ${collectionName}")
        logger.quiet("Uploading ${metadataDescription.get()} for ${version} to ${collectionName}")

        def importArguments = [
            "--uri", mongoUri,
            "--collection", collectionName,
            "--file", metadataFile.absolutePath,
            "--type", "json"
        ]
        if (jsonArray.get()) {
            importArguments.add("--jsonArray")
        }
        importArguments.add("--drop")

        def output = new ByteArrayOutputStream()
        def error = new ByteArrayOutputStream()
        def result
        try {
            result = execOperations.exec {
                executable = mongoImportExecutable.get()
                args importArguments
                standardOutput = output
                errorOutput = error
                ignoreExitValue = true
            }
        } catch (final Exception e) {
            logProcessOutput("mongoimport stdout", output.toString(StandardCharsets.UTF_8.name()).trim(), true)
            logProcessOutput("mongoimport stderr", error.toString(StandardCharsets.UTF_8.name()).trim(), true)
            throw new GradleException("Failed to execute ${mongoImportExecutable.get()} for ${metadataDescription.get()} collection ${collectionName}", e)
        }

        def standardOutput = output.toString(StandardCharsets.UTF_8.name()).trim()
        def errorOutput = error.toString(StandardCharsets.UTF_8.name()).trim()
        if (result.exitValue != 0) {
            logger.error("mongoimport failed with exit code ${result.exitValue}")
            logProcessOutput("mongoimport stdout", standardOutput, true)
            logProcessOutput("mongoimport stderr", errorOutput, true)
            throw new GradleException("Failed to upload ${metadataDescription.get()} to MongoDB collection ${collectionName}")
        }

        logProcessOutput("mongoimport stdout", standardOutput)
        logProcessOutput("mongoimport stderr", errorOutput)
        logger.quiet("Uploaded ${metadataDescription.get()} to MongoDB collection ${collectionName}")
    }

    private String findMongoUri() {
        def systemPropertyName = mongoUriSystemProperty.get()
        def environmentVariableName = mongoUriEnvironmentVariable.get()
        def mongoUri = System.getProperty(systemPropertyName)
        if (!mongoUri) {
            mongoUri = System.getenv(environmentVariableName)
        }
        if (!mongoUri) {
            throw new GradleException("MongoDB URI must be provided via -D${systemPropertyName}=... or ${environmentVariableName}")
        }
        mongoUri
    }

    private void logProcessOutput(final String label, final String value, final boolean failure = false) {
        if (value) {
            if (failure) {
                logger.error("${label}:\n${value}")
            } else {
                logger.quiet("${label}:\n${value}")
            }
        }
    }
}
