package com.mgmtp.gradle.libactivity.plugin.config

import com.mgmtp.gradle.libactivity.plugin.logging.LazyLogger
import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import java.time.LocalDate

/** Contains properties that come from within the plugin and are not meant to be set by the user. */
@TupleConstructor( post = { NullCheck.ALL_PROPS.call( this)})
@VisibilityOptions( Visibility.PRIVATE)
class GlobalConfig {

    final LocalDate startOfCheckDate

    final Map<String,String> gitHubMappings

    private static final LazyLogger LOGGER = LazyLogger.fromClazz( GlobalConfig.class)

    private static Map<String,String> initGitHubMappings( String propertiesPathRelativeToClasspath) {
        Properties properties = new Properties( )
        if( propertiesPathRelativeToClasspath) {
            LOGGER.info( 'Loading GitHub mapping properties from "classpath:{}"', propertiesPathRelativeToClasspath)
            try {
                properties.load( GlobalConfig.class.getResourceAsStream( propertiesPathRelativeToClasspath))
                if( !properties) {
                    LOGGER.warn( 'Empty GitHub mapping properties.')
                }
            } catch( IOException e) {
                LOGGER.warn{ "Error reading GitHub mapping properties: ${ e.getMessage( )}."}
            }
        }
        return properties.collectEntries { Map.Entry<?,?> entry -> [(entry.key): entry.value]} as Map<String, String>
    }

    static GlobalConfigBuilder builder( ) {
        return new GlobalConfigBuilder( )
    }

    @TupleConstructor
    @VisibilityOptions( Visibility.PRIVATE)
    static class GlobalConfigBuilder {

        private LocalDate startOfCheckDate

        private String gitHubPropertiesPathRelativeToClasspath

        GlobalConfigBuilder startOfCheckDate( LocalDate startOfCheckDate) {
            this.startOfCheckDate = startOfCheckDate
            return this
        }

        GlobalConfigBuilder gitHubPropertiesPathRelativeToClasspath( String gitHubPropertiesPathRelativeToClasspath) {
            this.gitHubPropertiesPathRelativeToClasspath = gitHubPropertiesPathRelativeToClasspath
            return this
        }

        GlobalConfig build( ) {
            LOGGER.info( 'Initializing global config.')
            GlobalConfig globalConfig = new GlobalConfig( startOfCheckDate, initGitHubMappings( gitHubPropertiesPathRelativeToClasspath))
            LOGGER.info( 'Global config complete: {}', globalConfig)
            return globalConfig
        }
    }

    @Override
    String toString( ) {
"""
startOfCheckDate: ${ startOfCheckDate}
gitHubMappings: ${ gitHubMappings.size( )} entries"""
    }
}