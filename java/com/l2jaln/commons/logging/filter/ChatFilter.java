package com.l2jaln.commons.logging.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ChatFilter implements Filter
{
	@Override
	public boolean isLoggable(LogRecord record)
	{
		return record.getLoggerName().equals("chat");
	}
}