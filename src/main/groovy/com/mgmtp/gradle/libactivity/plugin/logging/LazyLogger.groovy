package com.mgmtp.gradle.libactivity.plugin.logging

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.slf4j.Marker

/**
 * <p>
 * Logger that builds upon standard Gradle Logger. Evaluates log messages lazily, i.e. message closure is called only when log level is enabled.
 * This facilitates the use of method calls as log arguments.
 * </p>
 * Tries to apply the custom log message pattern of "[<LOGLEVEL>] <MSG>" when possible, i.e. when not otherwise dictated by Gradle.
 */
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LazyLogger implements Logger {

    final Logger delegate

    static LazyLogger fromClazz(Class<?> clazz) {
        return new LazyLogger(Logging.getLogger(clazz))
    }

    void log(LogLevel logLevel, Closure<String> msg) {
        if (isEnabled(logLevel)) {
            log(logLevel, msg.call())
        }
    }

    void error(Closure<String> msg) {
        log(LogLevel.ERROR, msg)
    }

    void quiet(Closure<String> msg) {
        log(LogLevel.QUIET, msg)
    }

    void warn(Closure<String> msg) {
        log(LogLevel.WARN, msg)
    }

    void lifecycle(Closure<String> msg) {
        log(LogLevel.LIFECYCLE, msg)
    }

    void info(Closure<String> msg) {
        log(LogLevel.INFO, msg)
    }

    void debug(Closure<String> msg) {
        log(LogLevel.DEBUG, msg)
    }

    @Override
    boolean isLifecycleEnabled() {
        return delegate.lifecycleEnabled
    }

    @Override
    String getName() {
        return delegate.name
    }

    @Override
    boolean isTraceEnabled() {
        return delegate.traceEnabled
    }

    @Override
    void trace(String s) {
        delegate.trace(s)
    }

    @Override
    void trace(String s, Object o) {
        delegate.trace(s, o)
    }

    @Override
    void trace(String s, Object o, Object o1) {
        delegate.trace(s, o, o1)
    }

    @Override
    void trace(String s, Object... objects) {
        delegate.trace(s, objects)
    }

    @Override
    void trace(String s, Throwable throwable) {
        delegate.trace(s, throwable)
    }

    @Override
    boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker)
    }

    @Override
    void trace(Marker marker, String s) {
        delegate.trace(marker, s)
    }

    @Override
    void trace(Marker marker, String s, Object o) {
        delegate.trace(marker, s, o)
    }

    @Override
    void trace(Marker marker, String s, Object o, Object o1) {
        delegate.trace(marker, s, o, o1)
    }

    @Override
    void trace(Marker marker, String s, Object... objects) {
        delegate.trace(marker, s, objects)
    }

    @Override
    void trace(Marker marker, String s, Throwable throwable) {
        delegate.trace(marker, s, throwable)
    }

    @Override
    boolean isDebugEnabled() {
        return delegate.debugEnabled
    }

    @Override
    void debug(String s) {
        log(LogLevel.DEBUG, s)
    }

    @Override
    void debug(String s, Object o) {
        log(LogLevel.DEBUG, s, o)
    }

    @Override
    void debug(String s, Object o, Object o1) {
        log(LogLevel.DEBUG, s, o, o1)
    }

    @Override
    void debug(String s, Object... objects) {
        log(LogLevel.DEBUG, s, objects)
    }

    @Override
    void debug(String s, Throwable throwable) {
        log(LogLevel.DEBUG, s, throwable)
    }

    @Override
    boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker)
    }

    @Override
    void debug(Marker marker, String s) {
        delegate.debug(marker, s)
    }

    @Override
    void debug(Marker marker, String s, Object o) {
        delegate.debug(marker, s, o)
    }

    @Override
    void debug(Marker marker, String s, Object o, Object o1) {
        delegate.debug(marker, s, o, o1)
    }

    @Override
    void debug(Marker marker, String s, Object... objects) {
        delegate.debug(marker, s, objects)
    }

    @Override
    void debug(Marker marker, String s, Throwable throwable) {
        delegate.debug(marker, s, throwable)
    }

    @Override
    boolean isInfoEnabled() {
        return delegate.infoEnabled
    }

    @Override
    void info(String s) {
        log(LogLevel.INFO, s)
    }

    @Override
    void info(String s, Object o) {
        log(LogLevel.INFO, s, o)
    }

    @Override
    void info(String s, Object o, Object o1) {
        log(LogLevel.INFO, s, o, o1)
    }

    @Override
    void lifecycle(String s) {
        log(LogLevel.LIFECYCLE, s)
    }

    @Override
    void lifecycle(String s, Object... objects) {
        log(LogLevel.LIFECYCLE, s, objects)
    }

    @Override
    void lifecycle(String s, Throwable throwable) {
        log(LogLevel.LIFECYCLE, s, throwable)
    }

    @Override
    boolean isQuietEnabled() {
        return delegate.quietEnabled
    }

    @Override
    void quiet(String s) {
        log(LogLevel.QUIET, s)
    }

    @Override
    void quiet(String s, Object... objects) {
        log(LogLevel.QUIET, s, objects)
    }

    @Override
    void info(String s, Object... objects) {
        log(LogLevel.INFO, s, objects)
    }

    @Override
    void info(String s, Throwable throwable) {
        log(LogLevel.INFO, s, throwable)
    }

    @Override
    boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker)
    }

    @Override
    void info(Marker marker, String s) {
        delegate.info(marker, s)
    }

    @Override
    void info(Marker marker, String s, Object o) {
        delegate.info(marker, s, o)
    }

    @Override
    void info(Marker marker, String s, Object o, Object o1) {
        delegate.info(marker, s, o, o1)
    }

    @Override
    void info(Marker marker, String s, Object... objects) {
        delegate.info(marker, s, objects)
    }

    @Override
    void info(Marker marker, String s, Throwable throwable) {
        delegate.info(marker, s, throwable)
    }

    @Override
    boolean isWarnEnabled() {
        return delegate.warnEnabled
    }

    @Override
    void warn(String s) {
        log(LogLevel.WARN, s)
    }

    @Override
    void warn(String s, Object o) {
        log(LogLevel.WARN, s, o)
    }

    @Override
    void warn(String s, Object... objects) {
        log(LogLevel.WARN, s, objects)
    }

    @Override
    void warn(String s, Object o, Object o1) {
        log(LogLevel.WARN, s, o, o1)
    }

    @Override
    void warn(String s, Throwable throwable) {
        log(LogLevel.WARN, s, throwable)
    }

    @Override
    boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker)
    }

    @Override
    void warn(Marker marker, String s) {
        delegate.warn(marker, s)
    }

    @Override
    void warn(Marker marker, String s, Object o) {
        delegate.warn(marker, s, o)
    }

    @Override
    void warn(Marker marker, String s, Object o, Object o1) {
        delegate.warn(marker, s, o, o1)
    }

    @Override
    void warn(Marker marker, String s, Object... objects) {
        delegate.warn(marker, s, objects)
    }

    @Override
    void warn(Marker marker, String s, Throwable throwable) {
        delegate.warn(marker, s, throwable)
    }

    @Override
    boolean isErrorEnabled() {
        return delegate.errorEnabled
    }

    @Override
    void error(String s) {
        log(LogLevel.ERROR, s)
    }

    @Override
    void error(String s, Object o) {
        log(LogLevel.ERROR, s, o)
    }

    @Override
    void error(String s, Object o, Object o1) {
        log(LogLevel.ERROR, s, o, o1)
    }

    @Override
    void error(String s, Object... objects) {
        log(LogLevel.ERROR, s, objects)
    }

    @Override
    void error(String s, Throwable throwable) {
        log(LogLevel.ERROR, s, throwable)
    }

    @Override
    boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker)
    }

    @Override
    void error(Marker marker, String s) {
        delegate.error(marker, s)
    }

    @Override
    void error(Marker marker, String s, Object o) {
        delegate.error(marker, s, o)
    }

    @Override
    void error(Marker marker, String s, Object o, Object o1) {
        delegate.error(marker, s, o, o1)
    }

    @Override
    void error(Marker marker, String s, Object... objects) {
        delegate.error(marker, s, objects)
    }

    @Override
    void error(Marker marker, String s, Throwable throwable) {
        delegate.error(marker, s, throwable)
    }

    @Override
    void quiet(String s, Throwable throwable) {
        log(LogLevel.QUIET, s, throwable)
    }

    @Override
    boolean isEnabled(LogLevel logLevel) {
        return delegate.isEnabled(logLevel)
    }

    @Override
    void log(LogLevel logLevel, String s) {
        if (isEnabled(logLevel)) {
            delegate.log(logLevel, getMessagePattern(logLevel, s))
        }
    }

    @Override
    void log(LogLevel logLevel, String s, Object... objects) {
        if (isEnabled(logLevel)) {
            delegate.log(logLevel, getMessagePattern(logLevel, s), objects)
        }
    }

    @Override
    void log(LogLevel logLevel, String s, Throwable throwable) {
        if (isEnabled(logLevel)) {
            delegate.log(logLevel, getMessagePattern(logLevel, s), throwable)
        }
    }

    /**
     * <p>
     * Prepends log level to message.
     * </p>
     * <b>Exceptions:</b>
     * <ul>
     * <li>Gradle applies pattern "<TIMESTAMP> [LOGLEVEL] [<LOGGING CLASS FQN>] <MSG>" to debug messages. So no extra level here.</li>
     * </ul>
     */
    private String getMessagePattern(LogLevel logLevel, String msg) {
        return isDebugEnabled() ? msg : "[${logLevel.name()}] ${msg}"
    }
}