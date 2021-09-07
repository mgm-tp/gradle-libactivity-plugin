# Gradle Lib Activity Plugin

This plugin checks whether libraries in your project are actively maintained. This allows you spot libraries that might become incompatible with your project or that pose a security risk (e.g. due to bugs that won't get fixed). Once you have recognized these vulnerable candidates you can take appropriate measures like deploying an alternate library of the same kind.

## Usage

Add the plugin to your project's `build.gradle` using:

```groovy
plugins {
  id 'com.mgmtp.gradle-libactivity-plugin' version '1.0.0'
}
```

Alternatively you can check out the plugin's code. To connect it with your project use the `plugins` block from above and specify an included build in your `settings.gradle`:

```groovy
includeBuild( "${ pathToCheckedOutPluginProject}")
```

**Compatibility Note:** This plugin was developed and tested using Gradle `6.7.1`. It might be possible to apply it to a project built by a lower version of Gradle but no guarantee or warranty is given that it will work.

## `checkLibActivity` Task

### Description

Checks if a library is still active, i.e. for a predefined age limit either the latest library release is not older than the limit or there are commits for that library on GitHub not older than the limit. In addition, to find outdated versions the library version currently in use will be checked against a separate age limit. Both limits are configurable. Release and version age checks use library metadata retrieved from Sonatype API.

If applied to a multi-module project the task will automatically check all submodules and will produce only a single report for all modules.

**Check Limitations:**
* If the target project includes submodules as dependencies or constraints these dependencies will not be checked. This is because submodules are typically not published to Maven Central.
* At the moment `buildscript{ ...}` blocks are not scanned for dependencies.

### Configuration

The task exploits dependency information from all configurations in your project. It must therefore run after the project has been evaluated.

```groovy
project.afterEvaluate {
    tasks.named( 'checkLibActivity') {
        maxAgeLatestReleaseInMonths = 12
        maxAgeCurrentVersionInMonths = 60
        outputFormat = 'TXT'
        outputChannel = 'DUAL'
        outputDir = project.file( 'build/libactivity')
        outputFileName = 'libactivityReport'
        gitHubOauthToken = project.properties['my.personal.github.oauth.token']
        xcludes = [
            'com.google.code.findbugs:jsr305'
        ]
        xcludePatterns = [
            'ch\\.qos\\.logback:.*',
            'org\\.apache\\.tiles:.*'
        ]
        localGitHubMappings = [
            'org.apache.commons/commons-text':'apache/commons-text',
            'org.apache.logging.log4j/log4j-to-slf4j':'apache/logging-log4j2:log4j-to-slf4j'
        ]
    }
}
```

#### Local Config Parameters

| Parameter                    | Description | Default Value |
| ---------------------------- | ----------- | ------------- |
| `maxAgeLatestReleaseInMonths`  | Max allowed age of latest library release in months.       | `12`
| `maxAgeCurrentVersionInMonths` | Max allowed age of current library version in months.        | `60`
| `gitHubOauthToken`             | Token used to authenticate against the GitHub API. Note that GitHub applies [rate limiting](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting). If you don't use a token you're limited to checking only 60 libraries per hour against the API. | |
| `outputFormat`                 | Text format to produce: `TXT`, `JSON`. | `TXT` |
| `outputChannel`                | Channel used to publish results: `FILE`, `CONSOLE`, `DUAL` | `DUAL` |
| `outputDir`                    | Directory to keep result file. | `project.file( 'build/libactivity')` |
| `outputFileName`               | Name of output file. | `libactivityReport` |
| `xcludes`                      | Dependencies to exclude from checking. This list takes G:A coordinates, i.e. `groupId`, `artifactId`. | `empty` |
| `xcludePatterns`               | G:A coordinates of your libraries will be matched against these patterns. Any match is excluded from further check. | `empty` |
| `localGitHubMappings`          | Local mappings that can be used to add entries not present in the global mappings or to override them. | `empty` |

#### Global Config

The global config is not meant to be influenced by the user by setting input parameters. It is loaded from within the plugin and therefore applies globally to all target projects.

Part of the global config is the properties file with global GitHub mappings. The principle behind these mappings is that library coordinates are mapped to the corresponding GitHub repository path where the library is hosted:
- **Key format**: G/A, i.e. `groupId`/`artifactId`
- **Value format**: GitHubRepoOwner/RepoName:optionalPathInsideRepo

To reflect changes to these properties a new plugin release or an included build is required.

<details>
<summary>Global GitHub Mappings</summary>

```properties
antlr/antlr=
ch.qos.logback/logback-classic=qos-ch/logback:logback-classic
ch.qos.logback/logback-core=qos-ch/logback:logback-core
com.eatthepath/pushy=jchambers/pushy:pushy
com.fasterxml.jackson.core/jackson-annotations=FasterXML/jackson-annotations
com.fasterxml.jackson.core/jackson-core=FasterXML/jackson-core
com.fasterxml.jackson.core/jackson-databind=FasterXML/jackson-databind
com.fasterxml.jackson.datatype/jackson-datatype-jdk8=FasterXML/jackson-datatype-jdk8
com.fasterxml.jackson.datatype/jackson-datatype-jsr310=FasterXML/jackson-datatype-jsr310
com.fasterxml.woodstox/woodstox-core=FasterXML/woodstox
com.github.ben-manes.caffeine/caffeine=ben-manes/caffeine:caffeine
com.google.code.findbugs/jsr305=findbugsproject/findbugs
com.google.code.gson/gson=google/gson:gson
com.google.guava/guava=google/guava:guava
com.jayway.jsonpath/json-path=json-path/JsonPath
com.lowagie/itext=
com.mysema.querydsl/querydsl-apt=
com.mysema.querydsl/querydsl-core=
com.mysema.querydsl/querydsl-jpa=
com.openhtmltopdf/openhtmltopdf-core=danfickle/openhtmltopdf:openhtmltopdf-core
com.openhtmltopdf/openhtmltopdf-pdfbox=danfickle/openhtmltopdf:openhtmltopdf-pdfbox
com.openhtmltopdf/openhtmltopdf-slf4j=danfickle/openhtmltopdf:openhtmltopdf-slf4j
com.rometools/rome=rometools/rome:rome
com.squareup/javapoet=square/javapoet
com.sun.activation/jakarta.activation=eclipse-ee4j/jaf:activation
com.sun.istack/istack-commons-runtime=javaee/jaxb-istack-commons:istack-commons/runtime
com.sun.mail/jakarta.mail=eclipse-ee4j/mail:mail
com.sun.mail/javax.mail=javaee/javamail:mail
com.sun.xml.ws/jaxws-rt=javaee/metro-jax-ws:jaxws-ri/bundles/jaxws-rt
commons-beanutils/commons-beanutils=apache/commons-beanutils
commons-codec/commons-codec=apache/commons-codec
commons-collections/commons-collections=
commons-io/commons-io=apache/commons-io
commons-lang/commons-lang=
commons-logging/commons-logging=apache/commons-logging
iaik/iaik-jce-min=
jakarta.activation/jakarta.activation-api=
jakarta.annotation/jakarta.annotation-api=eclipse-ee4j/common-annotations-api:api
jakarta.mail/jakarta.mail-api=
jakarta.servlet/jakarta.servlet-api=eclipse-ee4j/servlet-api:api
jakarta.xml.bind/jakarta.xml.bind-api=eclipse-ee4j/jaxb-api:jaxb-api
jakarta.xml.ws/jakarta.xml.ws-api=eclipse-ee4j/jax-ws-api:api
javapns/javapns=
javax.annotation/javax.annotation-api=javaee/javax.annotation
javax.cache/cache-api=jsr107/jsr107spec
javax.el/javax.el-api=javaee/el-spec:api
javax.inject/javax.inject=javax-inject/javax-inject
javax.servlet/javax.servlet-api=javaee/servlet-spec
javax.servlet/jstl=
javax.validation/validation-api=
jaxen/jaxen=jaxen-xpath/jaxen:core
junit/junit=junit-team/junit4
net.sf.jacob-project/jacob=freemansoft/jacob-project
net.sf.saxon/Saxon-HE=
net.sourceforge.cssparser/cssparser=
nl.simplecaptcha/simplecaptcha=
org.antlr/antlr4-runtime=antlr/antlr4:runtime
org.antlr/antlr4=antlr/antlr4
org.apache.commons/commons-collections4=apache/commons-collections
org.apache.commons/commons-lang3=apache/commons-lang
org.apache.commons/commons-text=apache/commons-text
org.apache.httpcomponents/httpclient=
org.apache.httpcomponents/httpcore=
org.apache.httpcomponents/httpmime=
org.apache.logging.log4j/log4j-to-slf4j=apache/logging-log4j2:log4j-to-slf4j
org.apache.pdfbox/pdfbox=
org.apache.santuario/xmlsec=apache/santuario-java
org.apache.solr/solr-solrj=apache/lucene-solr:solr/solrj
org.apache.tiles/tiles-api=apache/tiles:tiles-api
org.apache.tiles/tiles-core=apache/tiles:tiles-core
org.apache.tiles/tiles-extras=apache/tiles:tiles-extras
org.apache.tiles/tiles-jsp=apache/tiles:tiles-jsp
org.apache.tiles/tiles-request-api=apache/tiles-request:tiles-request-api
org.apache.tomcat.embed/tomcat-embed-websocket=
org.apache.tomcat/tomcat-el-api=
org.apache.tomcat/tomcat-jdbc=apache/tomcat:modules/jdbc-pool
org.apache.tomcat/tomcat-jsp-api=
org.aspectj/aspectjweaver=eclipse/org.aspectj:aspectjweaver
org.assertj/assertj-core=assertj/assertj-core
org.awaitility/awaitility=awaitility/awaitility:awaitility
org.bouncycastle/bcpkix-jdk15on=bcgit/bc-java:pkix
org.bouncycastle/bcprov-jdk15on=bcgit/bc-java:prov
org.checkerframework/checker-qual=typetools/checker-framework:checker-qual
org.codehaus.woodstox/stax2-api=FasterXML/stax2-api
org.dbunit/dbunit=NexMirror/DbUnit:dbunit
org.eclipse.persistence/jakarta.persistence=eclipse/javax.persistence
org.eclipse.persistence/org.eclipse.persistence.core=eclipse-ee4j/eclipselink:foundation/org.eclipse.persistence.core
org.eclipse.persistence/org.eclipse.persistence.jpa.jpql=
org.eclipse.persistence/org.eclipse.persistence.jpa=eclipse-ee4j/eclipselink:jpa/org.eclipse.persistence.jpa
org.ehcache/ehcache=ehcache/ehcache3
org.fusesource.jansi/jansi=fusesource/jansi:jansi
org.glassfish.jaxb/jaxb-runtime=
org.glassfish.jaxb/jaxb-xjc=eclipse-ee4j/jaxb-ri:jaxb-ri/xjc
org.glassfish.jaxb/txw2=eclipse-ee4j/jaxb-ri:jaxb-ri/txw/runtime
org.glassfish.web/javax.servlet.jsp.jstl=javaee/jstl-api:impl
org.glassfish/javax.el=javaee/el-spec
org.hamcrest/hamcrest-core=hamcrest/JavaHamcrest:hamcrest-core
org.hamcrest/hamcrest-library=hamcrest/JavaHamcrest:hamcrest-library
org.hamcrest/hamcrest=hamcrest/JavaHamcrest:hamcrest
org.hibernate.validator/hibernate-validator=hibernate/hibernate-validator
org.javers/javers-core=javers/javers:javers-core
org.jdom/jdom2=hunterhacker/jdom
org.jsoup/jsoup=jhy/jsoup
org.jvnet.staxex/stax-ex=eclipse-ee4j/jaxb-stax-ex
org.mockito/mockito-core=mockito/mockito
org.mockito/mockito-junit-jupiter=mockito/mockito:subprojects/junit-jupiter
org.mozilla/rhino=mozilla/rhino
org.postgresql/postgresql=postgres/postgres
org.projectlombok/lombok=rzwitserloot/lombok
org.slf4j/jcl-over-slf4j=qos-ch/slf4j:jcl-over-slf4j
org.slf4j/jul-to-slf4j=qos-ch/slf4j:jul-to-slf4j
org.slf4j/log4j-over-slf4j=qos-ch/slf4j:log4j-over-slf4j
org.slf4j/slf4j-api=qos-ch/slf4j:slf4j-api
org.springframework.hateoas/spring-hateoas=spring-projects/spring-hateoas
org.springframework.restdocs/spring-restdocs-core=spring-projects/spring-restdocs:spring-restdocs-core
org.springframework.restdocs/spring-restdocs-mockmvc=spring-projects/spring-restdocs:spring-restdocs-mockmvc
org.springframework.security.extensions/spring-security-saml2-core=spring-projects/spring-security-saml:core
org.springframework.security/spring-security-acl=spring-projects/spring-security:acl
org.springframework.security/spring-security-aspects=spring-projects/spring-security:aspects
org.springframework.security/spring-security-bom=spring-projects/spring-security:bom
org.springframework.security/spring-security-cas=spring-projects/spring-security:cas
org.springframework.security/spring-security-config=spring-projects/spring-security:config
org.springframework.security/spring-security-core=spring-projects/spring-security:core
org.springframework.security/spring-security-crypto=spring-projects/spring-security:crypto
org.springframework.security/spring-security-data=spring-projects/spring-security:data
org.springframework.security/spring-security-ldap=spring-projects/spring-security:ldap
org.springframework.security/spring-security-messaging=spring-projects/spring-security:messaging
org.springframework.security/spring-security-oauth2-client=spring-projects/spring-security:oauth2/oauth2-client
org.springframework.security/spring-security-oauth2-core=spring-projects/spring-security:oauth2/oauth2-core
org.springframework.security/spring-security-oauth2-jose=spring-projects/spring-security:oauth2/oauth2-jose
org.springframework.security/spring-security-oauth2-resource-server=spring-projects/spring-security:oauth2/oauth2-resource-server
org.springframework.security/spring-security-openid=spring-projects/spring-security:openid
org.springframework.security/spring-security-remoting=spring-projects/spring-security:remoting
org.springframework.security/spring-security-taglibs=spring-projects/spring-security:taglibs
org.springframework.security/spring-security-test=spring-projects/spring-security/test
org.springframework.security/spring-security-web=spring-projects/spring-security:web
org.springframework.statemachine/spring-statemachine-core=spring-projects/spring-statemachine:spring-statemachine-core
org.springframework/spring-aop=spring-projects/spring-framework:spring-aop
org.springframework/spring-aspects=spring-projects/spring-framework:spring-aspects
org.springframework/spring-beans=spring-projects/spring-framework:spring-beans
org.springframework/spring-context-indexer=spring-projects/spring-framework:spring-context-indexer
org.springframework/spring-context-support=spring-projects/spring-framework:spring-context-support
org.springframework/spring-context=spring-projects/spring-framework:spring-context
org.springframework/spring-core=spring-projects/spring-framework:spring-core
org.springframework/spring-expression=spring-projects/spring-framework:spring-expression
org.springframework/spring-framework-bom=spring-projects/spring-framework:framework-bom
org.springframework/spring-instrument=spring-projects/spring-framework:spring-instrument
org.springframework/spring-jcl=spring-projects/spring-framework:spring-jcl
org.springframework/spring-jdbc=spring-projects/spring-framework:spring-jdbc
org.springframework/spring-jms=spring-projects/spring-framework:spring-jms
org.springframework/spring-messaging=spring-projects/spring-framework:spring-messaging
org.springframework/spring-orm=spring-projects/spring-framework:spring-orm
org.springframework/spring-oxm=spring-projects/spring-framework:spring-oxm
org.springframework/spring-test=spring-projects/spring-framework:spring-test
org.springframework/spring-tx=spring-projects/spring-framework:spring-tx
org.springframework/spring-web=spring-projects/spring-framework:spring-web
org.springframework/spring-webflux=spring-projects/spring-framework:spring-webflux
org.springframework/spring-webmvc=spring-projects/spring-framework:spring-webmvc
org.springframework/spring-websocket=spring-projects/spring-framework:spring-websocket
org.unbescape/unbescape=unbescape/unbescape
org.w3c.css/sac=
org.xhtmlrenderer/flying-saucer-pdf=flyingsaucerproject/flyingsaucer:flying-saucer-pdf
org.xmlunit/xmlunit-core=xmlunit/xmlunit:xmlunit-core
```

</details>

### Report

#### Example Output

<details>
<summary>TEXT</summary>

```text
LIB ACTIVITY CHECK RESULT
*************************

(<Detail>) marks detail, e.g.: (number of commits) = number of commits found on GitHub
       (*) marks group with libraries that can be found in other groups as well


85x ACTIVE: latest release within tolerance bounds
--------------------------------------------------
com.jayway.jsonpath:json-path:2.5.0
...

8x ACTIVE: latest release outdated but at least 1 commit on GitHub ---> (number of commits)
-------------------------------------------------------------------------------------------
org.hamcrest:hamcrest:2.2 (8)
org.jsoup:jsoup:1.13.1 (30)
...

11x INACTIVE: no commit on GitHub ---> (years since latest release)
-------------------------------------------------------------------
org.jdom:jdom2:2.0.6 (6.2)
org.unbescape:unbescape:1.1.6.RELEASE (3.0)
...

4x INACTIVE: not hosted on GitHub
---------------------------------
commons-collections:commons-collections:3.2.2
org.w3c.css:sac:1.3
...

3x INACTIVE: library coordinates moved (*) ---> (new address)
-------------------------------------------------------------
com.lowagie:itext:2.1.7 (https://mvnrepository.com/artifact/com.itextpdf/itextpdf)
...

1x UNAVAILABLE RESULT: no GitHub mapping ---> (years since latest release)
--------------------------------------------------------------------------
org.openapitools:jackson-databind-nullable:0.2.1 (1.3)

1x UNKNOWN
----------
lib:abc:1.2.3

5x OUTDATED VERSION (*) ---> (version age in years)
---------------------------------------------------
com.lowagie:itext:2.1.7 (11.8)
...



REDUNDANT LOCAL CONFIG ENTRIES
******************************


1x unused xcludes
-----------------
another.lib:xyz

1x unused xclude patterns
-------------------------
another.lib:.*

2x unused local GitHub mapping keys
-----------------------------------
org.apache.commons/commons-text
org.apache.logging.log4j/log4j-to-slf4j
```

</details>

<details>
<summary>JSON</summary>

```json
{
    "checkResults": [
        {
            "name": "LIB ACTIVITY CHECK RESULT",
            "groups": [
                {
                    "category": "ACTIVE",
                    "subcategory": "latest release within tolerance bounds",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "com.auth0",
                                "artifactId": "java-jwt",
                                "version": "3.11.0"
                            }
                        }
                    ]
                },
                {
                    "category": "ACTIVE",
                    "subcategory": "latest release outdated but at least 1 commit on GitHub",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "ch.qos.logback",
                                "artifactId": "logback-classic",
                                "version": "1.2.3"
                            },
                            "details": {
                                "commits": "1"
                            }
                        }
                    ]
                },
                {
                    "category": "INACTIVE",
                    "subcategory": "no commit on GitHub",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "com.google.code.gson",
                                "artifactId": "gson",
                                "version": "2.8.6"
                            },
                            "details": {
                                "years since latest release": "1.5"
                            }
                        }
                    ]
                },
                {
                    "category": "INACTIVE",
                    "subcategory": "not hosted on GitHub",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "com.lowagie",
                                "artifactId": "itext",
                                "version": "2.1.7"
                            }
                        }
                    ]
                },
                {
                    "category": "INACTIVE",
                    "subcategory": "library coordinates moved",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "com.lowagie",
                                "artifactId": "itext",
                                "version": "2.1.7"
                            },
                            "details": {
                                "new address": "https://mvnrepository.com/artifact/com.itextpdf/itextpdf"
                            }
                        }
                    ]
                },
                {
                    "category": "UNAVAILABLE RESULT",
                    "subcategory": "no GitHub mapping",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "org.openapitools",
                                "artifactId": "jackson-databind-nullable",
                                "version": "0.2.1"
                            },
                            "details": {
                                "years since latest release": "1.3"
                            }
                        }
                    ]
                },
                {
                    "category": "UNKNOWN",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "lib",
                                "artifactId": "abc",
                                "version": "1.2.3"
                            }
                        }
                    ]
                },
                {
                    "category": "OUTDATED VERSION",
                    "members": [
                        {
                            "coordinates": {
                                "groupId": "com.lowagie",
                                "artifactId": "itext",
                                "version": "2.1.7"
                            },
                            "details": {
                                "version age in years": "11.8"
                            }
                        }
                    ]
                }
            ]
        },
        {
            "name": "LOCAL CONFIG REDUNDANCY CHECK RESULT",
            "groups": [
                {
                    "name": "unused local GitHub Mappings",
                    "members": [
                        "my.unused.mapping1",
                        "my.unused.mapping2"
                    ]
                },
                {
                    "name": "unused xcludes",
                    "members": [
                        "my.unused:xclude.for.library"
                    ]
                },
                {
                    "name": "unused xclude patterns",
                    "members": [
                        "my.unused.*xclude[pP]attern"
                    ]
                }
            ]
        }
    ]
}
```

</details>

#### Categories

##### Lib Activity Check Result

| Category | Subcategory | Detail | Description | Is Category With Unique Members? |
| -------- | ----------- | ------ | ----------- | -------------------------------- |
| ACTIVE | latest release within tolerance bounds | | | Y
| | latest release outdated but at least 1 commit on GitHub | number of commits| | Y |
| INACTIVE | no commit on GitHub | age of latest release in years | | Y |
|  | not hosted on GitHub | | GitHub mapping is empty, i.e. lib comes from somewhere else | Y |
|  | library coordinates moved | new address of library | if `mvnrepository.com` has a new address for this lib | N |
| UNAVAILABLE RESULT | no GitHub mapping | age of latest release in years | GitHub query not possible because there is no mapping for this lib | Y |
| | GitHub response NOT FOUND | | e.g. typo in GitHub mapping | Y |
| | GitHub response FORBIDDEN | | e.g. if no more unauthenticated requests allowed | Y |
| UNKNOWN | | | lib coordinates are unknown to Sonatype API | Y |
| OUTDATED VERSION | | age of lib version in years | | N |
| UNKNOWN VERSION | | | | N |

##### Local Config Check Result

To simplify maintenance of local config params any collection is checked for redundant entries.

| Category | Checked Param |
| -------- | ------------- |
| unused local GitHub mapping keys | `localGitHubMappings` |
| unused xcludes | `xcludes` |
| unused xclude patterns | `xcludePatterns` |
