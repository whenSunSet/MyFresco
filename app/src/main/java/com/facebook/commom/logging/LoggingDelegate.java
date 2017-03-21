package com.facebook.commom.logging;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * Logging interface
 */
public interface LoggingDelegate {

    /**
     * 设置日志的最小检测等级
     * Sets a minimum log-level under which the logger will not log regardless of other checks.
     *
     * @param level the minimum level to set
     */
    void setMinimumLoggingLevel(int level);

    /**
     * Gets a minimum log-level under which the logger will not log regardless of other checks.
     *
     * @return the minimum level
     */
    int getMinimumLoggingLevel();

    /**
     * 是否记录日志
     * Gets whether the specified level is loggable.
     *
     * @param level the level to check
     * @return the level
     */
    boolean isLoggable(int level);

    /**
     * 记录一个{@link android.util.Log#VERBOSE}的日志
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void v(String tag, String msg);

    /**
     * 记录一个{@link android.util.Log#VERBOSE}的日志和异常
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void v(String tag, String msg, Throwable tr);

    /**
     * 记录一个{@link android.util.Log#DEBUG}的日志
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void d(String tag, String msg);

    /**
     * 记录一个{@link android.util.Log#DEBUG}的日志和异常
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void d(String tag, String msg, Throwable tr);

    /**
     * 记录一个{@link android.util.Log#INFO}的日志
     * Send an {@link android.util.Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void i(String tag, String msg);

    /**
     * 记录一个{@link android.util.Log#INFO}的日志和异常
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void i(String tag, String msg, Throwable tr);

    /**
     * 记录一个{@link android.util.Log#WARN}的日志
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void w(String tag, String msg);

    /**
     * 记录一个{@link android.util.Log#WARN}的日志和异常
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void w(String tag, String msg, Throwable tr);

    /**
     * 记录一个{@link android.util.Log#ERROR}的日志
     * Send an {@link android.util.Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void e(String tag, String msg);

    /**
     * 记录一个{@link android.util.Log#ERROR}的日志和异常
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void e(String tag, String msg, Throwable tr);

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     * Send wtf soft error report (sampled).
     * Note: This is not equivalent of {@link android.util.Log#wtf}.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void wtf(String tag, String msg);

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     * Send wtf soft error report (sampled).
     * Note: This is not equivalent of {@link android.util.Log#wtf}.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.  May be null.
     */
    void wtf(String tag, String msg, Throwable tr);

    /**
     * Logs a message.
     *
     * @param priority the priority of the message
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void log(int priority, String tag, String msg);
}
