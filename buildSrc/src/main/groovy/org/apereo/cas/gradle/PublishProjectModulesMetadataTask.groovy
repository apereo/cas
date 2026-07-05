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
import org.gradle.work.DisableCachingByDefault

import java.nio.charset.StandardCharsets

import javax.inject.Inject

@DisableCachingByDefault(because = "Uploads module metadata to MongoDB as an external side effect.")
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

    @Inject
    abstract ExecOperations getExecOperations()

    PublishProjectModulesMetadataTask() {
        group = "Publishing"
        description = "Publish generated CAS module metadata to MongoDB."
        mongoImportExecutable.convention("mongoimport")
        mongoUriEnvironmentVariable.convention("CAS_MODULE_METADATA_MONGODB_URL")
        mongoUriSystemProperty.convention("casModuleMetadataMongoDbUrl")
    }

    @TaskAction
    void publishMetadata() {
        def metadataFile = modulesMetadataFile.get().asFile
        if (!metadataFile.exists()) {
            throw new GradleException("CAS module metadata file does not exist: ${metadataFile}")
        }

        def mongoUri = findMongoUri()
        def version = casVersion.get()
        def versionNumbers = version.tokenize("-").first().replace(".", "")
        if (!versionNumbers) {
            throw new GradleException("Unable to determine CAS module metadata collection from version ${version}")
        }
        def collectionName = "casmodules${versionNumbers}"

        logger.quiet("Checking CAS version ${version}...")
        logger.quiet("CAS simple version number is: ${versionNumbers}")
        logger.quiet("CAS module collection is ${collectionName}")
        logger.quiet("Uploading module records for ${version} to ${collectionName}")

        def output = new ByteArrayOutputStream()
        def error = new ByteArrayOutputStream()
        def result
        try {
            result = execOperations.exec {
                executable = mongoImportExecutable.get()
                args "--uri", mongoUri,
                    "--collection", collectionName,
                    "--file", metadataFile.absolutePath,
                    "--type", "json",
                    "--jsonArray",
                    "--drop"
                standardOutput = output
                errorOutput = error
                ignoreExitValue = true
            }
        } catch (final Exception e) {
            logProcessOutput("mongoimport stdout", output.toString(StandardCharsets.UTF_8.name()).trim(), true)
            logProcessOutput("mongoimport stderr", error.toString(StandardCharsets.UTF_8.name()).trim(), true)
            throw new GradleException("Failed to execute ${mongoImportExecutable.get()} for CAS module metadata collection ${collectionName}", e)
        }

        def standardOutput = output.toString(StandardCharsets.UTF_8.name()).trim()
        def errorOutput = error.toString(StandardCharsets.UTF_8.name()).trim()
        if (result.exitValue != 0) {
            logger.error("mongoimport failed with exit code ${result.exitValue}")
            logProcessOutput("mongoimport stdout", standardOutput, true)
            logProcessOutput("mongoimport stderr", errorOutput, true)
            throw new GradleException("Failed to upload CAS module metadata to MongoDB collection ${collectionName}")
        }

        logProcessOutput("mongoimport stdout", standardOutput)
        logProcessOutput("mongoimport stderr", errorOutput)
        logger.quiet("Uploaded CAS module metadata to MongoDB collection ${collectionName}")
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
