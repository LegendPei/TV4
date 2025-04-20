package com.peitianbao.www.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author leg
 */
public class LogFormatter extends Formatter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("日期：").append(dateFormat.format(new Date(record.getMillis()))).append("\n");
        sb.append("等级：").append(record.getLevel()).append("\n");
        sb.append("具体信息：").append(formatMessage(record)).append("\n\n");
        return sb.toString();
    }
}
