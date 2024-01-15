package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.slf4j.Marker

/**
 * <p>
 * Logger that builds upon standard Gradle Logger. Evaluates log messages lazily, i.e. message closure is called only
 * when log level is enabled. This facilitates the use of method calls as log arguments.
 * </p>
 * <p>
 * Tries to apply the custom log message pattern of "[<LOGLEVEL>] <MSG>" if possible, i.e. when not otherwise
 * dictated by Gradle (see DEBUG level).
 * </p>
 */

@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
// This class may be eliminated once the new Slf4J API (>= 2.0) is shipped with Gradle.
class LazyLogger implements Logger {

    final Logger delegate

    static LazyLogger fromClazz(final Class<?> clazz) {
        return new LazyLogger(Logging.getLogger(clazz))
    }

    void log(final LogLevel logLevel, final Closure<String> msg) {
        log(logLevel, msg, null)
    }

    void log(final LogLevel logLevel, final Closure<String> msg, final Throwable throwable) {

        if (isEnabled(logLevel)) {

            if (throwable) {
                log(logLevel, msg.call(), throwable)
            } else {
                log(logLevel, msg.call())
            }
        }
    }

    void error(final Closure<String> msg) {
        log(LogLevel.ERROR, msg)
    }

    void error(final Closure<String> msg, final Throwable throwable) {
        log(LogLevel.ERROR, msg, throwable)
    }

    void quiet(final Closure<String> msg) {
        log(LogLevel.QUIET, msg)
    }

    void quiet(final Closure<String> msg, final Throwable throwable) {
        log(LogLevel.QUIET, msg, throwable)
    }

    void warn(final Closure<String> msg) {
        log(LogLevel.WARN, msg)
    }

    void warn(final Closure<String> msg, final Throwable throwable) {
        log(LogLevel.WARN, msg, throwable)
    }

    void lifecycle(final Closure<String> msg) {
        log(LogLevel.LIFECYCLE, msg)
    }

    void lifecycle(final Closure<String> msg, final Throwable throwable) {
        log(LogLevel.LIFECYCLE, msg, throwable)
    }

    void info(final Closure<String> msg) {
        log(LogLevel.INFO, msg)
    }

    void info(final Closure<String> msg, final Throwable throwable) {
        log(LogLevel.INFO, msg, throwable)
    }

    void debug(final Closure<String> msg) {
        log(LogLevel.DEBUG, msg)
    }

    void debug(final Closure<String> msg, final Throwable throwable) {
        log(LogLevel.DEBUG, msg, throwable)
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
    void trace(final String s) {
        delegate.trace(s)
    }

    @Override
    void trace(final String s, final Object o) {
        delegate.trace(s, o)
    }

    @Override
    void trace(final String s, final Object o, final Object o1) {
        delegate.trace(s, o, o1)
    }

    @Override
    void trace(final String s, final Object... objects) {
        delegate.trace(s, objects)
    }

    @Override
    void trace(final String s, final Throwable throwable) {
        delegate.trace(s, throwable)
    }

    @Override
    boolean isTraceEnabled(final Marker marker) {
        return delegate.isTraceEnabled(marker)
    }

    @Override
    void trace(final Marker marker, final String s) {
        delegate.trace(marker, s)
    }

    @Override
    void trace(final Marker marker, final String s, final Object o) {
        delegate.trace(marker, s, o)
    }

    @Override
    void trace(final Marker marker, final String s, final Object o, final Object o1) {
        delegate.trace(marker, s, o, o1)
    }

    @Override
    void trace(final Marker marker, final String s, final Object... objects) {
        delegate.trace(marker, s, objects)
    }

    @Override
    void trace(final Marker marker, final String s, final Throwable throwable) {
        delegate.trace(marker, s, throwable)
    }

    @Override
    boolean isDebugEnabled() {
        return delegate.debugEnabled
    }

    @Override
    void debug(final String s) {
        log(LogLevel.DEBUG, s)
    }

    @Override
    void debug(final String s, final Object o) {
        log(LogLevel.DEBUG, s, o)
    }

    @Override
    void debug(final String s, final Object o, final Object o1) {
        log(LogLevel.DEBUG, s, o, o1)
    }

    @Override
    void debug(final String s, final Object... objects) {
        log(LogLevel.DEBUG, s, objects)
    }

    @Override
    void debug(final String s, final Throwable throwable) {
        log(LogLevel.DEBUG, s, throwable)
    }

    @Override
    boolean isDebugEnabled(final Marker marker) {
        return delegate.isDebugEnabled(marker)
    }

    @Override
    void debug(final Marker marker, final String s) {
        delegate.debug(marker, s)
    }

    @Override
    void debug(final Marker marker, final String s, final Object o) {
        delegate.debug(marker, s, o)
    }

    @Override
    void debug(final Marker marker, final String s, final Object o, final Object o1) {
        delegate.debug(marker, s, o, o1)
    }

    @Override
    void debug(final Marker marker, final String s, final Object... objects) {
        delegate.debug(marker, s, objects)
    }

    @Override
    void debug(final Marker marker, final String s, final Throwable throwable) {
        delegate.debug(marker, s, throwable)
    }

    @Override
    boolean isInfoEnabled() {
        return delegate.infoEnabled
    }

    @Override
    void info(final String s) {
        log(LogLevel.INFO, s)
    }

    @Override
    void info(final String s, final Object o) {
        log(LogLevel.INFO, s, o)
    }

    @Override
    void info(final String s, final Object o, final Object o1) {
        log(LogLevel.INFO, s, o, o1)
    }

    @Override
    void lifecycle(final String s) {
        log(LogLevel.LIFECYCLE, s)
    }

    @Override
    void lifecycle(final String s, final Object... objects) {
        log(LogLevel.LIFECYCLE, s, objects)
    }

    @Override
    void lifecycle(final String s, final Throwable throwable) {
        log(LogLevel.LIFECYCLE, s, throwable)
    }

    @Override
    boolean isQuietEnabled() {
        return delegate.quietEnabled
    }

    @Override
    void quiet(final String s) {
        log(LogLevel.QUIET, s)
    }

    @Override
    void quiet(final String s, final Object... objects) {
        log(LogLevel.QUIET, s, objects)
    }

    @Override
    void info(final String s, final Object... objects) {
        log(LogLevel.INFO, s, objects)
    }

    @Override
    void info(final String s, final Throwable throwable) {
        log(LogLevel.INFO, s, throwable)
    }

    @Override
    boolean isInfoEnabled(final Marker marker) {
        return delegate.isInfoEnabled(marker)
    }

    @Override
    void info(final Marker marker, final String s) {
        delegate.info(marker, s)
    }

    @Override
    void info(final Marker marker, final String s, final Object o) {
        delegate.info(marker, s, o)
    }

    @Override
    void info(final Marker marker, final String s, final Object o, final Object o1) {
        delegate.info(marker, s, o, o1)
    }

    @Override
    void info(final Marker marker, final String s, final Object... objects) {
        delegate.info(marker, s, objects)
    }

    @Override
    void info(final Marker marker, final String s, final Throwable throwable) {
        delegate.info(marker, s, throwable)
    }

    @Override
    boolean isWarnEnabled() {
        return delegate.warnEnabled
    }

    @Override
    void warn(final String s) {
        log(LogLevel.WARN, s)
    }

    @Override
    void warn(final String s, final Object o) {
        log(LogLevel.WARN, s, o)
    }

    @Override
    void warn(final String s, final Object... objects) {
        log(LogLevel.WARN, s, objects)
    }

    @Override
    void warn(final String s, final Object o, final Object o1) {
        log(LogLevel.WARN, s, o, o1)
    }

    @Override
    void warn(final String s, final Throwable throwable) {
        log(LogLevel.WARN, s, throwable)
    }

    @Override
    boolean isWarnEnabled(final Marker marker) {
        return delegate.isWarnEnabled(marker)
    }

    @Override
    void warn(final Marker marker, final String s) {
        delegate.warn(marker, s)
    }

    @Override
    void warn(final Marker marker, final String s, final Object o) {
        delegate.warn(marker, s, o)
    }

    @Override
    void warn(final Marker marker, final String s, final Object o, final Object o1) {
        delegate.warn(marker, s, o, o1)
    }

    @Override
    void warn(final Marker marker, final String s, final Object... objects) {
        delegate.warn(marker, s, objects)
    }

    @Override
    void warn(final Marker marker, final String s, final Throwable throwable) {
        delegate.warn(marker, s, throwable)
    }

    @Override
    boolean isErrorEnabled() {
        return delegate.errorEnabled
    }

    @Override
    void error(final String s) {
        log(LogLevel.ERROR, s)
    }

    @Override
    void error(final String s, final Object o) {
        log(LogLevel.ERROR, s, o)
    }

    @Override
    void error(final String s, final Object o, final Object o1) {
        log(LogLevel.ERROR, s, o, o1)
    }

    @Override
    void error(final String s, final Object... objects) {
        log(LogLevel.ERROR, s, objects)
    }

    @Override
    void error(final String s, final Throwable throwable) {
        log(LogLevel.ERROR, s, throwable)
    }

    @Override
    boolean isErrorEnabled(final Marker marker) {
        return delegate.isErrorEnabled(marker)
    }

    @Override
    void error(final Marker marker, final String s) {
        delegate.error(marker, s)
    }

    @Override
    void error(final Marker marker, final String s, final Object o) {
        delegate.error(marker, s, o)
    }

    @Override
    void error(final Marker marker, final String s, final Object o, final Object o1) {
        delegate.error(marker, s, o, o1)
    }

    @Override
    void error(final Marker marker, final String s, final Object... objects) {
        delegate.error(marker, s, objects)
    }

    @Override
    void error(final Marker marker, final String s, final Throwable throwable) {
        delegate.error(marker, s, throwable)
    }

    @Override
    void quiet(final String s, final Throwable throwable) {
        log(LogLevel.QUIET, s, throwable)
    }

    @Override
    boolean isEnabled(final LogLevel logLevel) {
        return delegate.isEnabled(logLevel)
    }

    @Override
    void log(final LogLevel logLevel, final String s) {
        if (isEnabled(logLevel)) {
            delegate.log(logLevel, getMessagePattern(logLevel, s))
        }
    }

    @Override
    void log(final LogLevel logLevel, final String s, final Object... objects) {
        if (isEnabled(logLevel)) {
            delegate.log(logLevel, getMessagePattern(logLevel, s), objects)
        }
    }

    @Override
    void log(final LogLevel logLevel, final String s, final Throwable throwable) {
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
    private String getMessagePattern(final LogLevel logLevel, final String msg) {
        return isDebugEnabled() ? msg : "[${logLevel.name()}] ${msg}"
    }
}