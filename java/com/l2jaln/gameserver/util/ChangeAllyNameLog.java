/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jaln.gameserver.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jaln.commons.math.MathUtil;

public class ChangeAllyNameLog
{
	static
	{
		new File("log/Player Log/ChangeAllyNameLog").mkdirs();
	}

	private static final Logger _log = Logger.getLogger(ChangeAllyNameLog.class.getName());

	public static void auditGMAction(int i, String Name, String action, String ip, String params)
	{
		final File file = new File("log/Player Log/ChangeAllyNameLog/" + i + ".txt");
		if (!file.exists())
			try
		{
				file.createNewFile();
		}
		catch (IOException e)
		{
		}

		try (FileWriter save = new FileWriter(file, true))
		{
			save.write(MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + " >>>>> ID: [" + i + "] >> Nome Atual da Ally: [" + Name + "] >> Modificou para: [" + action + "] >> IP: [" + ip + "]\r\n");
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "ChangeAllyNameLog for Player " + Name + " could not be saved: ", e);
		}
	}

	public static void auditGMAction(int i, String Name, String action, String ip)
	{
		auditGMAction(i, Name, action, ip, "");
	}
}