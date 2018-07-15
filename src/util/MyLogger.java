package util;

import java.util.logging.Logger;

/**
 * 日志输出器。
 *
 * @author soulike
 */
public class MyLogger
{
    /**
     * 内部使用的 Logger 对象。
     */
    private final Logger logger;

    /**
     * 构造函数。设定 Logger 的名字。
     *
     * @param loggerName Logger 的名字。
     */
    public MyLogger(String loggerName)
    {
        logger = Logger.getLogger(loggerName);
    }

    /**
     * 输出 info 级别的日志。
     *
     * @param msg 要输出的日志。
     */
    public void logInfo(String msg)
    {
        logger.info(format(msg));
    }

    /**
     * 输出 severe 级别的日志。
     *
     * @param msg 要输出的日志。
     */
    public void logError(String msg)
    {
        logger.severe(format(msg));
    }

    /**
     * 输出 warning 级别的日志。
     *
     * @param msg 要输出的日志。
     */
    public void logWarning(String msg)
    {
        logger.warning(format(msg));
    }

    /**
     * 将输入信息格式化成标准形式。
     *
     * @param msg 要输出的日志。
     */
    private String format(String msg)
    {
        return String.format("%s:\n%s\n", logger.getName(), msg);
    }
}
