<?php
/*
 * The configuration of SimpleSAMLphp
 *
 */

$config = array(

    /**
     * Setup the following parameters to match the directory of your installation.
     * See the user manual for more details.
     *
     * Valid format for baseurlpath is:
     * [(http|https)://(hostname|fqdn)[:port]]/[path/to/simplesaml/]
     * (note that it must end with a '/')
     *
     * The full url format is useful if your SimpleSAMLphp setup is hosted behind
     * a reverse proxy. In that case you can specify the external url here.
     *
     * Please note that SimpleSAMLphp will then redirect all queries to the
     * external url, no matter where you come from (direct access or via the
     * reverse proxy).
     */
    'baseurlpath' => 'simplesaml/',
    'certdir' => 'cert/',
    'loggingdir' => 'log/',
    'datadir' => 'data/',

    /*
     * A directory where SimpleSAMLphp can save temporary files.
     *
     * SimpleSAMLphp will attempt to create this directory if it doesn't exist.
     */
    'tempdir' => '/tmp/simplesaml',


    /*
     * If you enable this option, SimpleSAMLphp will log all sent and received messages
     * to the log file.
     *
     * This option also enables logging of the messages that are encrypted and decrypted.
     *
     * Note: The messages are logged with the DEBUG log level, so you also need to set
     * the 'logging.level' option to LOG_DEBUG.
     */
    'debug' => true,

    /*
     * When showerrors is enabled, all error messages and stack traces will be output
     * to the browser.
     *
     * When errorreporting is enabled, a form will be presented for the user to report
     * the error to technicalcontact_email.
     */
    'showerrors' => true,
    'errorreporting' => true,

    /**
     * Custom error show function called from SimpleSAML_Error_Error::show.
     * See docs/simplesamlphp-errorhandling.txt for function code example.
     *
     * Example:
     *   'errors.show_function' => array('sspmod_example_Error_Show', 'show'),
     */

    /**
     * This option allows you to enable validation of XML data against its
     * schemas. A warning will be written to the log if validation fails.
     */
    'debug.validatexml' => false,

    /**
     * This password must be kept secret, and modified from the default value 123.
     * This password will give access to the installation page of SimpleSAMLphp with
     * metadata listing and diagnostics pages.
     * You can also put a hash here; run "bin/pwgen.php" to generate one.
     */
    'auth.adminpassword' => getenv('SIMPLESAMLPHP_IDP_ADMIN_PASSWORD') ?: 'secret',
    'admin.protectindexpage' => false,
    'admin.protectmetadata' => false,

    /**
     * This is a secret salt used by SimpleSAMLphp when it needs to generate a secure hash
     * of a value. It must be changed from its default value to a secret value. The value of
     * 'secretsalt' can be any valid string of any length.
     *
     * A possible way to generate a random salt is by running the following command from a unix shell:
     * tr -c -d '0123456789abcdefghijklmnopqrstuvwxyz' </dev/urandom | dd bs=32 count=1 2>/dev/null;echo
     */
    'secretsalt' => getenv('SIMPLESAMLPHP_IDP_SECRET_SALT') ?: 'defaultsecretsalt',

    /*
     * Some information about the technical persons running this installation.
     * The email address will be used as the recipient address for error reports, and
     * also as the technical contact in generated metadata.
     */
    'technicalcontact_name' => 'Administrator',
    'technicalcontact_email' => 'na@example.com',

    /*
     * The timezone of the server. This option should be set to the timezone you want
     * SimpleSAMLphp to report the time in. The default is to guess the timezone based
     * on your system timezone.
     *
     * See this page for a list of valid timezones: http://php.net/manual/en/timezones.php
     */
    'timezone' => null,

    /*
     * Logging.
     *
     * define the minimum log level to log
     *		SimpleSAML_Logger::ERR		No statistics, only errors
     *		SimpleSAML_Logger::WARNING	No statistics, only warnings/errors
     *		SimpleSAML_Logger::NOTICE	Statistics and errors
     *		SimpleSAML_Logger::INFO		Verbose logs
     *		SimpleSAML_Logger::DEBUG	Full debug logs - not recommended for production
     *
     * Choose logging handler.
     *
     * Options: [syslog,file,errorlog]
     *
     */
    'logging.level' => SimpleSAML\Logger::DEBUG,
    'logging.handler' => 'errorlog',

    /*
     * Specify the format of the logs. Its use varies depending on the log handler used (for instance, you cannot
     * control here how dates are displayed when using the syslog or errorlog handlers), but in general the options
     * are:
     *
     * - %date{<format>}: the date and time, with its format specified inside the brackets. See the PHP documentation
     *   of the strftime() function for more information on the format. If the brackets are omitted, the standard
     *   format is applied. This can be useful if you just want to control the placement of the date, but don't care
     *   about the format.
     *
     * - %process: the name of the SimpleSAMLphp process. Remember you can configure this in the 'logging.processname'
     *   option below.
     *
     * - %level: the log level (name or number depending on the handler used).
     *
     * - %stat: if the log entry is intended for statistical purposes, it will print the string 'STAT ' (bear in mind
     *   the trailing space).
     *
     * - %trackid: the track ID, an identifier that allows you to track a single session.
     *
     * - %srcip: the IP address of the client. If you are behind a proxy, make sure to modify the
     *   $_SERVER['REMOTE_ADDR'] variable on your code accordingly to the X-Forwarded-For header.
     *
     * - %msg: the message to be logged.
     *
     */
    //'logging.format' => '%date{%b %d %H:%M:%S} %process %level %stat[%trackid] %msg',

    /*
     * Choose which facility should be used when logging with syslog.
     *
     * These can be used for filtering the syslog output from SimpleSAMLphp into its
     * own file by configuring the syslog daemon.
     *
     * See the documentation for openlog (http://php.net/manual/en/function.openlog.php) for available
     * facilities. Note that only LOG_USER is valid on windows.
     *
     * The default is to use LOG_LOCAL5 if available, and fall back to LOG_USER if not.
     */
    'logging.facility' => defined('LOG_LOCAL5') ? constant('LOG_LOCAL5') : LOG_USER,

    /*
     * The process name that should be used when logging to syslog.
     * The value is also written out by the other logging handlers.
     */
    'logging.processname' => 'simplesamlphp',

    /* Logging: file - Logfilename in the loggingdir from above.
     */
    'logging.logfile' => 'simplesamlphp.log',

    /* (New) statistics output configuration.
     *
     * This is an array of outputs. Each output has at least a 'class' option, which
     * selects the output.
     */
    'statistics.out' => array(// Log statistics to the normal log.
        /*
        array(
            'class' => 'core:Log',
            'level' => 'notice',
        ),
        */
        // Log statistics to files in a directory. One file per day.
        /*
        array(
            'class' => 'core:File',
            'directory' => '/var/log/stats',
        ),
        */
    ),



    /*
     * Database
     *
     * This database configuration is optional. If you are not using
     * core functionality or modules that require a database, you can
     * skip this configuration.
     */

    /*
     * Database connection string.
     * Ensure that you have the required PDO database driver installed
     * for your connection string.
     */
    'database.dsn' => 'mysql:host=localhost;dbname=saml',

    /*
     * SQL database credentials
     */
    'database.username' => 'simplesamlphp',
    'database.password' => 'secret',

    /*
     * (Optional) Table prefix
     */
    'database.prefix' => '',

    /*
     * True or false if you would like a persistent database connection
     */
    'database.persistent' => false,

    /*
     * Database slave configuration is optional as well. If you are only
     * running a single database server, leave this blank. If you have
     * a master/slave configuration, you can define as many slave servers
     * as you want here. Slaves will be picked at random to be queried from.
     *
     * Configuration options in the slave array are exactly the same as the
     * options for the master (shown above) with the exception of the table
     * prefix.
     */
    'database.slaves' => array(
        /*
        array(
            'dsn' => 'mysql:host=myslave;dbname=saml',
            'username' => 'simplesamlphp',
            'password' => 'secret',
            'persistent' => false,
        ),
        */
    ),



    /*
     * Enable
     *
     * Which functionality in SimpleSAMLphp do you want to enable. Normally you would enable only
     * one of the functionalities below, but in some cases you could run multiple functionalities.
     * In example when you are setting up a federation bridge.
     */
    'enable.saml20-idp' => true,
    'enable.shib13-idp' => true,
    'enable.adfs-idp' => false,
    'enable.wsfed-sp' => false,
    'enable.authmemcookie' => false,


    /*
     * Module enable configuration
     *
     * Configuration to override module enabling/disabling.
     *
     * Example:
     *
     * 'module.enable' => array(
     * 	// Setting to TRUE enables.
     * 	'exampleauth' => TRUE,
     * 	// Setting to FALSE disables.
     * 	'saml' => FALSE,
     * 	// Unset or NULL uses default.
     * 	'core' => NULL,
     * ),
     *
     */


    /*
     * This value is the duration of the session in seconds. Make sure that the time duration of
     * cookies both at the SP and the IdP exceeds this duration.
     */
    'session.duration' => intval(getenv('SIMPLESAMLPHP_IDP_SESSION_DURATION_SECONDS')) > 0 ? intval(getenv('SIMPLESAMLPHP_IDP_SESSION_DURATION_SECONDS')) : 8 * (60 * 60), // default 8 hours.

    /*
     * Sets the duration, in seconds, data should be stored in the datastore. As the datastore is used for
     * login and logout requests, this option will control the maximum time these operations can take.
     * The default is 4 hours (4*60*60) seconds, which should be more than enough for these operations.
     */
    'session.datastore.timeout' => (4 * 60 * 60), // 4 hours

    /*
     * Sets the duration, in seconds, auth state should be stored.
     */
    'session.state.timeout' => (60 * 60), // 1 hour

    /*
     * Option to override the default settings for the session cookie name
     */
    'session.cookie.name' => 'SimpleSAMLSessionIDIdp',

    /*
     * Expiration time for the session cookie, in seconds.
     *
     * Defaults to 0, which means that the cookie expires when the browser is closed.
     *
     * Example:
     *  'session.cookie.lifetime' => 30*60,
     */
    'session.cookie.lifetime' => 0,

    /*
     * Limit the path of the cookies.
     *
     * Can be used to limit the path of the cookies to a specific subdirectory.
     *
     * Example:
     *  'session.cookie.path' => '/simplesaml/',
     */
    'session.cookie.path' => '/',

    /*
     * Cookie domain.
     *
     * Can be used to make the session cookie available to several domains.
     *
     * Example:
     *  'session.cookie.domain' => '.example.org',
     */
    'session.cookie.domain' => null,

    /*
     * Set the secure flag in the cookie.
     *
     * Set this to TRUE if the user only accesses your service
     * through https. If the user can access the service through
     * both http and https, this must be set to FALSE.
     */
    'session.cookie.secure' => false,

    /*
     * Enable secure POST from HTTPS to HTTP.
     *
     * If you have some SP's on HTTP and IdP is normally on HTTPS, this option
     * enables secure POSTing to HTTP endpoint without warning from browser.
     *
     * For this to work, module.php/core/postredirect.php must be accessible
     * also via HTTP on IdP, e.g. if your IdP is on
     * https://idp.example.org/ssp/, then
     * http://idp.example.org/ssp/module.php/core/postredirect.php must be accessible.
     */
    'enable.http_post' => true,

    /*
     * Options to override the default settings for php sessions.
     */
    'session.phpsession.cookiename' => 'PHPSESSIDIDP',
    'session.phpsession.savepath' => null,
    'session.phpsession.httponly' => true,

    /*
     * Option to override the default settings for the auth token cookie
     */
    'session.authtoken.cookiename' => 'SimpleSAMLAuthTokenIdp',

    /*
     * Options for remember me feature for IdP sessions. Remember me feature
     * has to be also implemented in authentication source used.
     *
     * Option 'session.cookie.lifetime' should be set to zero (0), i.e. cookie
     * expires on browser session if remember me is not checked.
     *
     * Session duration ('session.duration' option) should be set according to
     * 'session.rememberme.lifetime' option.
     *
     * It's advised to use remember me feature with session checking function
     * defined with 'session.check_function' option.
     */
    'session.rememberme.enable' => false,
    'session.rememberme.checked' => false,
    'session.rememberme.lifetime' => (14 * 86400),

    /**
     * Custom function for session checking called on session init and loading.
     * See docs/simplesamlphp-advancedfeatures.txt for function code example.
     *
     * Example:
     *   'session.check_function' => array('sspmod_example_Util', 'checkSession'),
     */

    /*
     * Languages available, RTL languages, and what language is default
     */
    'language.available' => array(
        'en', 'no', 'nn', 'se', 'da', 'de', 'sv', 'fi', 'es', 'fr', 'it', 'nl', 'lb', 'cs',
        'sl', 'lt', 'hr', 'hu', 'pl', 'pt', 'pt-br', 'tr', 'ja', 'zh', 'zh-tw', 'ru', 'et',
        'he', 'id', 'sr', 'lv', 'ro', 'eu'
    ),
    'language.rtl' => array('ar', 'dv', 'fa', 'ur', 'he'),
    'language.default' => 'en',

    /*
     * Options to override the default settings for the language parameter
     */
    'language.parameter.name' => 'language',
    'language.parameter.setcookie' => true,

    /*
     * Options to override the default settings for the language cookie
     */
    'language.cookie.name' => 'language',
    'language.cookie.domain' => null,
    'language.cookie.path' => '/',
    'language.cookie.lifetime' => (60 * 60 * 24 * 900),

    /**
     * Custom getLanguage function called from SimpleSAML_XHTML_Template::getLanguage().
     * Function should return language code of one of the available languages or NULL.
     * See SimpleSAML_XHTML_Template::getLanguage() source code for more info.
     *
     * This option can be used to implement a custom function for determining
     * the default language for the user.
     *
     * Example:
     *   'language.get_language_function' => array('sspmod_example_Template', 'getLanguage'),
     */

    /*
     * Extra dictionary for attribute names.
     * This can be used to define local attributes.
     *
     * The format of the parameter is a string with <module>:<dictionary>.
     *
     * Specifying this option will cause us to look for modules/<module>/dictionaries/<dictionary>.definition.json
     * The dictionary should look something like:
     *
     * {
     *     "firstattribute": {
     *         "en": "English name",
     *         "no": "Norwegian name"
     *     },
     *     "secondattribute": {
     *         "en": "English name",
     *         "no": "Norwegian name"
     *     }
     * }
     *
     * Note that all attribute names in the dictionary must in lowercase.
     *
     * Example: 'attributes.extradictionary' => 'ourmodule:ourattributes',
     */
    'attributes.extradictionary' => null,

    /*
     * Which theme directory should be used?
     */
    'theme.use' => 'default',


    /*
     * Default IdP for WS-Fed.
     */
    'default-wsfed-idp' => 'urn:federation:pingfederate:localhost',

    /*
     * Whether the discovery service should allow the user to save his choice of IdP.
     */
    'idpdisco.enableremember' => true,
    'idpdisco.rememberchecked' => true,

    // Disco service only accepts entities it knows.
    'idpdisco.validate' => true,

    'idpdisco.extDiscoveryStorage' => null,

    /*
     * IdP Discovery service look configuration.
     * Whether to display a list of idp or to display a dropdown box. For many IdP' a dropdown box
     * gives the best use experience.
     *
     * When using dropdown box a cookie is used to highlight the previously chosen IdP in the dropdown.
     * This makes it easier for the user to choose the IdP
     *
     * Options: [links,dropdown]
     *
     */
    'idpdisco.layout' => 'dropdown',

    /*
     * Whether SimpleSAMLphp should sign the response or the assertion in SAML 1.1 authentication
     * responses.
     *
     * The default is to sign the assertion element, but that can be overridden by setting this
     * option to TRUE. It can also be overridden on a pr. SP basis by adding an option with the
     * same name to the metadata of the SP.
     */
    'shib13.signresponse' => true,


    /*
     * Authentication processing filters that will be executed for all IdPs
     * Both Shibboleth and SAML 2.0
     */
    'authproc.idp' => array(
        /* Enable the authproc filter below to add URN Prefixces to all attributes
         10 => array(
             'class' => 'core:AttributeMap', 'addurnprefix'
         ), */
        /* Enable the authproc filter below to automatically generated eduPersonTargetedID.
        20 => 'core:TargetedID',
        */

        // Adopts language from attribute to use in UI
        30 => 'core:LanguageAdaptor',

        /* Add a realm attribute from edupersonprincipalname
        40 => 'core:AttributeRealm',
         */
        45 => array(
            'class'         => 'core:StatisticsWithAttribute',
            'attributename' => 'realm',
            'type'          => 'saml20-idp-SSO',
        ),

        /* When called without parameters, it will fallback to filter attributes ‹the old way›
         * by checking the 'attributes' parameter in metadata on IdP hosted and SP remote.
         */
        50 => 'core:AttributeLimit',

        /*
         * Search attribute "distinguishedName" for pattern and replaces if found

        60 => array(
            'class' => 'core:AttributeAlter',
            'pattern' => '/OU=studerende/',
            'replacement' => 'Student',
            'subject' => 'distinguishedName',
            '%replace',
        ),
         */

        /*
         * Consent module is enabled (with no permanent storage, using cookies).

        90 => array(
            'class' => 'consent:Consent',
            'store' => 'consent:Cookie',
            'focus' => 'yes',
            'checked' => TRUE
        ),
         */
        // If language is set in Consent module it will be added as an attribute.
        99 => 'core:LanguageAdaptor',
    ),
    /*
     * Authentication processing filters that will be executed for all SPs
     * Both Shibboleth and SAML 2.0
     */
    'authproc.sp' => array(
        /*
        10 => array(
            'class' => 'core:AttributeMap', 'removeurnprefix'
        ),
        */

        /*
         * Generate the 'group' attribute populated from other variables, including eduPersonAffiliation.
         60 => array(
            'class' => 'core:GenerateGroups', 'eduPersonAffiliation'
        ),
        */
        /*
         * All users will be members of 'users' and 'members'
        61 => array(
            'class' => 'core:AttributeAdd', 'groups' => array('users', 'members')
        ),
        */

        // Adopts language from attribute to use in UI
        90 => 'core:LanguageAdaptor',

    ),


    /*
     * This option configures the metadata sources. The metadata sources is given as an array with
     * different metadata sources. When searching for metadata, simpleSAMPphp will search through
     * the array from start to end.
     *
     * Each element in the array is an associative array which configures the metadata source.
     * The type of the metadata source is given by the 'type' element. For each type we have
     * different configuration options.
     *
     * Flat file metadata handler:
     * - 'type': This is always 'flatfile'.
     * - 'directory': The directory we will load the metadata files from. The default value for
     *                this option is the value of the 'metadatadir' configuration option, or
     *                'metadata/' if that option is unset.
     *
     * XML metadata handler:
     * This metadata handler parses an XML file with either an EntityDescriptor element or an
     * EntitiesDescriptor element. The XML file may be stored locally, or (for debugging) on a remote
     * web server.
     * The XML hetadata handler defines the following options:
     * - 'type': This is always 'xml'.
     * - 'file': Path to the XML file with the metadata.
     * - 'url': The URL to fetch metadata from. THIS IS ONLY FOR DEBUGGING - THERE IS NO CACHING OF THE RESPONSE.
     *
     * MDX metadata handler:
     * This metadata handler looks up for the metadata of an entity at the given MDX server.
     * The MDX metadata handler defines the following options:
     * - 'type': This is always 'mdx'.
     * - 'server': URL of the MDX server (url:port). Mandatory.
     * - 'validateFingerprint': The fingerprint of the certificate used to sign the metadata.
     *                          You don't need this option if you don't want to validate the signature on the metadata. Optional.
     * - 'cachedir': Directory where metadata can be cached. Optional.
     * - 'cachelength': Maximum time metadata cah be cached, in seconds. Default to 24
     *                  hours (86400 seconds). Optional.
     *
     * PDO metadata handler:
     * This metadata handler looks up metadata of an entity stored in a database.
     *
     * Note: If you are using the PDO metadata handler, you must configure the database
     * options in this configuration file.
     *
     * The PDO metadata handler defines the following options:
     * - 'type': This is always 'pdo'.
     *
     *
     * Examples:
     *
     * This example defines two flatfile sources. One is the default metadata directory, the other
     * is a metadata directory with autogenerated metadata files.
     *
     * 'metadata.sources' => array(
     *     array('type' => 'flatfile'),
     *     array('type' => 'flatfile', 'directory' => 'metadata-generated'),
     *     ),
     *
     * This example defines a flatfile source and an XML source.
     * 'metadata.sources' => array(
     *     array('type' => 'flatfile'),
     *     array('type' => 'xml', 'file' => 'idp.example.org-idpMeta.xml'),
     *     ),
     *
     * This example defines an mdx source.
     * 'metadata.sources' => array(
     *     array('type' => 'mdx', server => 'http://mdx.server.com:8080', 'cachedir' => '/var/simplesamlphp/mdx-cache', 'cachelength' => 86400)
     *     ),
     *
     * This example defines an pdo source.
     * 'metadata.sources' => array(
     *     array('type' => 'pdo')
     *     ),
     *
     * Default:
     * 'metadata.sources' => array(
     *     array('type' => 'flatfile')
     *     ),
     */
    'metadata.sources' => array(
        array('type' => 'flatfile'),
    ),


    /*
     * Configure the datastore for SimpleSAMLphp.
     *
     * - 'phpsession': Limited datastore, which uses the PHP session.
     * - 'memcache': Key-value datastore, based on memcache.
     * - 'sql': SQL datastore, using PDO.
     *
     * The default datastore is 'phpsession'.
     *
     * (This option replaces the old 'session.handler'-option.)
     */
    'store.type'                    => 'phpsession',


    /*
     * The DSN the sql datastore should connect to.
     *
     * See http://www.php.net/manual/en/pdo.drivers.php for the various
     * syntaxes.
     */
    'store.sql.dsn'                 => 'sqlite:/path/to/sqlitedatabase.sq3',

    /*
     * The username and password to use when connecting to the database.
     */
    'store.sql.username' => null,
    'store.sql.password' => null,

    /*
     * The prefix we should use on our tables.
     */
    'store.sql.prefix' => 'SimpleSAMLphp',


    /*
     * Configuration for the 'memcache' session store. This allows you to store
     * multiple redundant copies of sessions on different memcache servers.
     *
     * 'memcache_store.servers' is an array of server groups. Every data
     * item will be mirrored in every server group.
     *
     * Each server group is an array of servers. The data items will be
     * load-balanced between all servers in each server group.
     *
     * Each server is an array of parameters for the server. The following
     * options are available:
     *  - 'hostname': This is the hostname or ip address where the
     *    memcache server runs. This is the only required option.
     *  - 'port': This is the port number of the memcache server. If this
     *    option isn't set, then we will use the 'memcache.default_port'
     *    ini setting. This is 11211 by default.
     *  - 'weight': This sets the weight of this server in this server
     *    group. http://php.net/manual/en/function.Memcache-addServer.php
     *    contains more information about the weight option.
     *  - 'timeout': The timeout for this server. By default, the timeout
     *    is 3 seconds.
     *
     * Example of redundant configuration with load balancing:
     * This configuration makes it possible to lose both servers in the
     * a-group or both servers in the b-group without losing any sessions.
     * Note that sessions will be lost if one server is lost from both the
     * a-group and the b-group.
     *
     * 'memcache_store.servers' => array(
     *     array(
     *         array('hostname' => 'mc_a1'),
     *         array('hostname' => 'mc_a2'),
     *     ),
     *     array(
     *         array('hostname' => 'mc_b1'),
     *         array('hostname' => 'mc_b2'),
     *     ),
     * ),
     *
     * Example of simple configuration with only one memcache server,
     * running on the same computer as the web server:
     * Note that all sessions will be lost if the memcache server crashes.
     *
     * 'memcache_store.servers' => array(
     *     array(
     *         array('hostname' => 'localhost'),
     *     ),
     * ),
     *
     */
    'memcache_store.servers' => array(
        array(
            array('hostname' => 'localhost'),
        ),
    ),


    /*
     * This value allows you to set a prefix for memcache-keys. The default
     * for this value is 'SimpleSAMLphp', which is fine in most cases.
     *
     * When running multiple instances of SSP on the same host, and more
     * than one instance is using memcache, you probably want to assign
     * a unique value per instance to this setting to avoid data collision.
     */
    'memcache_store.prefix' => null,


    /*
     * This value is the duration data should be stored in memcache. Data
     * will be dropped from the memcache servers when this time expires.
     * The time will be reset every time the data is written to the
     * memcache servers.
     *
     * This value should always be larger than the 'session.duration'
     * option. Not doing this may result in the session being deleted from
     * the memcache servers while it is still in use.
     *
     * Set this value to 0 if you don't want data to expire.
     *
     * Note: The oldest data will always be deleted if the memcache server
     * runs out of storage space.
     */
    'memcache_store.expires' => 36 * (60 * 60), // 36 hours.


    /*
     * Should signing of generated metadata be enabled by default.
     *
     * Metadata signing can also be enabled for a individual SP or IdP by setting the
     * same option in the metadata for the SP or IdP.
     */
    'metadata.sign.enable' => false,

    /*
     * The default key & certificate which should be used to sign generated metadata. These
     * are files stored in the cert dir.
     * These values can be overridden by the options with the same names in the SP or
     * IdP metadata.
     *
     * If these aren't specified here or in the metadata for the SP or IdP, then
     * the 'certificate' and 'privatekey' option in the metadata will be used.
     * if those aren't set, signing of metadata will fail.
     */
    'metadata.sign.privatekey' => null,
    'metadata.sign.privatekey_pass' => null,
    'metadata.sign.certificate' => null,


    /*
     * Proxy to use for retrieving URLs.
     *
     * Example:
     *   'proxy' => 'tcp://proxy.example.com:5100'
     */
    'proxy' => null,

    /*
     * Array of domains that are allowed when generating links or redirections
     * to URLs. SimpleSAMLphp will use this option to determine whether to
     * to consider a given URL valid or not, but you should always validate
     * URLs obtained from the input on your own (i.e. ReturnTo or RelayState
     * parameters obtained from the $_REQUEST array).
     *
     * SimpleSAMLphp will automatically add your own domain (either by checking
     * it dynamically, or by using the domain defined in the 'baseurlpath'
     * directive, the latter having precedence) to the list of trusted domains,
     * in case this option is NOT set to NULL. In that case, you are explicitly
     * telling SimpleSAMLphp to verify URLs.
     *
     * Set to an empty array to disallow ALL redirections or links pointing to
     * an external URL other than your own domain. This is the default behaviour.
     *
     * Set to NULL to disable checking of URLs. DO NOT DO THIS UNLESS YOU KNOW
     * WHAT YOU ARE DOING!
     *
     * Example:
     *   'trusted.url.domains' => array('sp.example.com', 'app.example.com'),
     */
    'trusted.url.domains' => NULL,

);
