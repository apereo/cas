import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.bouncycastle.openpgp.PGPPublicKey
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
import java.util.regex.Matcher
import java.util.regex.Pattern

class PGPVerifyPluginExtension {
    Boolean failNoSignature
    Boolean failWeakSignature
    Boolean verifyPomFiles
    String pgpKeyServer
}

@Slf4j
class PGPVerifyPlugin implements Plugin<Project> {
    def MAVEN_CENTRAL = "https://repo1.maven.org/maven2/"

    def artifacts = [:]
    
    String keysMapLocation
    File pgpKeysCachePath = new File(System.getProperty("user.home"), ".gradle/pgp-cache")
    String pgpKeyServer = "https://pgp.mit.edu"
    KeysMap keysMap = new KeysMap()
    PGPKeysCache pgpKeysCache
    
    void apply(Project project) {
        project.extensions.create("PGPVerifyPlugin", PGPVerifyPluginExtension)
        
        project.task('cleanPGPCache') << {
            if (pgpKeysCachePath.exists()) {
                FileUtils.forceDelete(pgpKeysCachePath)
            }
        }
        
        project.task('verifyPGPs') << {
            
        }
        
        project.task('obtainPGPs') << {
            log.info "Evaluating project " + project.name
            
            if (pgpKeysCachePath.exists()) {
                if (!pgpKeysCachePath.isDirectory()) {
                    throw new InvalidUserDataException("PGP keys cache path exist but is not a directory: " + pgpKeysCachePath)
                }
            } else {
                if (pgpKeysCachePath.mkdirs()) {
                    log.info("Create cache for PGP keys: " + pgpKeysCachePath)
                } else {
                    throw new InvalidUserDataException("Cache directory create error")
                }
            }
            
            project.configurations.compile.resolvedConfiguration.resolvedArtifacts.each {
                def groupId = it.moduleVersion.id.group
                def version = it.moduleVersion.id.version

                def artifactName = groupId + ":" + it.name + ":" + version

                if (!version.endsWith("SNAPSHOT")) {
                    log.info "Looking at " + artifactName + " @ " + it.file.canonicalPath

                    def tempPomName = it.name + '-' + version + ".pom"
                    def pomFile = new File(pgpKeysCachePath, tempPomName)
                    if (it.extension.equals("jar") && project.PGPVerifyPlugin.verifyPomFiles) {
                        if (!pomFile.exists()) {
                            def pomFilePath = groupId.replace('.', '/')
                            
                            
                            def pomFileUrl = new URL(MAVEN_CENTRAL + pomFilePath + '/'
                                    + it.name + '/' + version + '/' + tempPomName);
                            log.info "Retrieving POM file from " + pomFileUrl + " for " + artifactName
                            retrieveUrl(pomFileUrl, pomFile)
                            
                            
                            
                        }
                    } else if (!pomFile.exists()) {
                        log.warn("POM file does not exist for " + artifactName + " @ " + pomFile.canonicalPath)
                    }

                    def ascFilePath = groupId.replace('.', '/')

                    def tempName = it.name + '-' + version + ".jar.asc"

                    def ascFileUrl = new URL(MAVEN_CENTRAL + ascFilePath + '/'
                            + it.name + '/' + version + '/' + tempName);

                    log.info "Retrieving signature file from " + ascFileUrl + " for " + artifactName
                    def destSigFile = new File(pgpKeysCachePath, tempName)
                    log.info "Destination signature file for " + artifactName + " is @ " + destSigFile

                    if (!destSigFile.exists()) {
                        retrieveUrl(ascFileUrl, destSigFile)
                    }
                }
            }
        }
    }

    void retrieveUrl(URL url, def file) {
        def inputStream
        try {
            inputStream = url.openStream()
        } catch (FileNotFoundException e) {
            throw new InvalidUserDataException("Could not locate resource: " + url)
        } finally {
            if (inputStream != null) {
                def writer = new FileWriter(file)
                writer.withCloseable {
                    IOUtils.copy(inputStream, writer)
                }
            }
        }
    }
    
    void initCache() {
        try {
            pgpKeysCache = new PGPKeysCache(pgpKeysCachePath, pgpKeyServer)
        } catch (Throwable e) {
            throw new InvalidUserDataException(e.getMessage(), e)
        }
    }


}

class ArtifactInfo {

    KeyInfo keyInfo

    Pattern groupIdPattern
    Pattern artifactIdPattern
    Pattern versionPattern

    public ArtifactInfo(String strArtifact, KeyInfo keyInfo) {

        String[] split = strArtifact.split(":")
        String groupId = split.length > 0 ? split[0].trim().toLowerCase(Locale.US) : ""
        String artifactId = split.length > 1 ? split[1].trim().toLowerCase(Locale.US) : ""
        String version = split.length > 2 ? split[2].trim().toLowerCase(Locale.US) : ""

        groupIdPattern = Pattern.compile(patternPrepare(groupId))
        artifactIdPattern = Pattern.compile(patternPrepare(artifactId))
        versionPattern = Pattern.compile(patternPrepare(version))

        this.keyInfo = keyInfo
    }

    String patternPrepare(String str) {

        if (str.length() == 0) {
            return ".*"
        }

        String ret = str.replaceAll("\\.", "\\\\.")
        ret = ret.replaceAll("\\*", ".*")
        return ret
    }

    boolean isMatch(def groupId, def artifactId, def version) {
        isMatchPattern(groupIdPattern, groupId) && isMatchPattern(artifactIdPattern, artifactId) && isMatchPattern(versionPattern, version)
    }

    boolean isMatchPattern(Pattern pattern, String str) {
        Matcher m = pattern.matcher(str.toLowerCase(Locale.US))
        return m.matches()
    }

    boolean isKeyMatch(PGPPublicKey key) {
        return keyInfo.isKeyMatch(key)
    }
}


class KeysMap {

    List<ArtifactInfo> keysMapList = new ArrayList<>()

    /**
     * Properties.load recognize ':' as key value separator.
     * This reader adds backlash before ':' char.
     */
    class Reader extends InputStream {

        InputStream inputStream
        private Character backSpace

        Reader(InputStream inputStream) {
            this.inputStream = inputStream
        }

        @Override
        public int read() throws IOException {

            int c
            if (backSpace == null) {
                c = inputStream.read()
            } else {
                c = backSpace
                backSpace = null
                return c
            }

            if (c == ':') {
                backSpace = ':'
                return '\\'
            }
            return c
        }
    }

    void load(String locale) {
        if (locale == null || locale.trim().isEmpty()) {
            return
        }

        InputStream inputStream = new File(locale).newInputStream()
        Properties properties = new Properties()
        properties.load(new Reader(inputStream))
        processProps(properties)
    }

    boolean isValidKey(def groupId, def artifactId, def version, PGPPublicKey key) {

        if (keysMapList.isEmpty()) {
            return true
        }

        for (ArtifactInfo artifactInfo : keysMapList) {
            if (artifactInfo.isMatch(groupId, artifactId, version)) {
                return artifactInfo.isKeyMatch(key)
            }
        }

        false
    }

    void processProps(Properties properties) {
        for (String propKey : properties.stringPropertyNames()) {
            ArtifactInfo artifactInfo = createArtifactInfo(propKey, properties.getProperty(propKey))
            keysMapList.add(artifactInfo)
        }
    }

    ArtifactInfo createArtifactInfo(String strArtifact, String strKeys) {
        return new ArtifactInfo(strArtifact, new KeyInfo(strKeys))
    }
}

class KeyInfo {

    boolean matchAny
    List<byte[]> keysID = new ArrayList<>()

    public KeyInfo(String strKeys) {

        if ("*".equals(strKeys) || "any".equalsIgnoreCase(strKeys)) {
            matchAny = true
            return
        }

        matchAny = false

        if (strKeys == null) {
            throw new InvalidUserDataException("null key not allowed")
        }

        for (String key : strKeys.split(",")) {
            key = key.trim()
            if (key.startsWith("0x")) {
                byte[] bytes = strKeyToBytes(key.substring(2))
                keysID.add(bytes)
            } else {
                throw new InvalidUserDataException("Invalid keyID " + key + " must start with 0x")
            }
        }
    }

    byte[] strKeyToBytes(String key) {

        BigInteger bigInteger = new BigInteger(key, 16)

        if (bigInteger.bitLength() < 32 || bigInteger.bitLength() > 160) {
            throw new InvalidUserDataException("Invalid key length key=" + key)
        }

        byte[] bytes = bigInteger.toByteArray()
        if (bytes[0] == 0 && bytes.length % 2 != 0) {
            // we can remove sign byte
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length)
        }
        bytes
    }

    boolean isKeyMatch(PGPPublicKey pgpPublicKeyey) {

        if (matchAny) {
            return true
        }

        byte[] fingerprint = pgpPublicKeyey.getFingerprint()
        for (byte[] keyBytes : keysID) {
            if (compareArrays(keyBytes, fingerprint)) {
                return true
            }
        }
        false
    }

    boolean compareArrays(byte[] keyBytes, byte[] fingerprint) {
        for (int i = 1; i <= keyBytes.length && i <= fingerprint.length; i++) {
            if (keyBytes[keyBytes.length - i] != fingerprint[fingerprint.length - i]) {
                return false
            }
        }
        true
    }
}

class PGPKeysServerClientHttps {
    URI keyserver
    SSLSocketFactory sslSocketFactory

    PGPKeysServerClientHttps(URI uri) {
        this.keyserver = new URI("https", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null)
        this.sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault()
    }


    InputStream getInputStreamForKey(URL keyURL) {
        final HttpsURLConnection keyServerUrlConnection = (HttpsURLConnection) keyURL.openConnection()
        keyServerUrlConnection.setSSLSocketFactory(sslSocketFactory)
        keyServerUrlConnection.getInputStream()
    }
}

class PGPKeysCache {

    def File cachePath
    def PGPKeysServerClientHttps keysServerClient

    public PGPKeysCache(def cachePath, def keyServer) {
        this.cachePath = cachePath
        keysServerClient = new PGPKeysServerClientHttps(new URI(keyServer))
    }

    String getUrlForShowKey(def keyID) {
        return keysServerClient.getUriForShowKey(keyID).toString()
    }

    PGPPublicKey getKey(def keyID) {
        File keyFile = null
        PGPPublicKey key = null

        try {
            String path = String.format("%02X/%02X/%016X.asc",
                    (byte) (keyID >> 56), (byte) (keyID >> 48 & 0xff), keyID)

            keyFile = new File(cachePath, path)
            if (!keyFile.exists()) {
                receiveKey(keyFile, keyID)
            }

            InputStream keyIn = PGPUtil.getDecoderStream(new FileInputStream(keyFile))
            PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(keyIn, new BcKeyFingerprintCalculator())
            key = pgpRing.getPublicKey(keyID)
        } finally {
            if (key == null) {
                deleteFile(keyFile)
            }
        }
        return key
    }

    void receiveKey(def keyFile, def keyID) {
        File dir = keyFile.getParentFile()
        if (dir == null) {
            throw new InvalidUserDataException("No parent dir for: " + keyFile)
        }

        if (dir.exists() && !dir.isDirectory()) {
            throw new InvalidUserDataException("Path exist but it isn't directory: " + dir)
        }

        if (!dir.exists() && !dir.mkdirs()) {
            throw new InvalidUserDataException("Can't create directory: " + dir)
        }

        InputStream inputStream = keysServerClient.getInputStreamForKey(keyID)
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(keyFile))
        IOUtils.copy(inputStream, outputStream)
    }

    void deleteFile(File keyFile) {
        if (keyFile == null || !keyFile.exists()) {
            return
        }
        keyFile.delete()
    }
}

@TupleConstructor
class ResolvedArtifact {
    File original
    File pom
    File signature
}
