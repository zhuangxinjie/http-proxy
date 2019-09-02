package com.tenxcloud.utils;

import org.apache.log4j.Logger;

public class SSH {
	private static Logger logger = Logger.getLogger(SSH.class);
	public String sshcmd_str(String host,String user,String passwd, String cmd) throws Exception {
		com.trilead.ssh2.Connection sshConnection = null;
		String tempString ="";
		try {
				sshConnection = new com.trilead.ssh2.Connection(host, 22);
				sshConnection.connect(null, 60000, 60000);
				logger.info(this.getClass().getSimpleName()+"  host="+host+" cmd="+cmd);
				if (!sshConnection.authenticateWithPassword(user, passwd)) {
					throw new Exception("sshConnection login error");
				}
				 tempString = SSHCmdHelper.sshExecuteCmdOneShotWithExitMsg(sshConnection, cmd).trim();
		} catch (Exception e) {
			logger.error(e);
			throw new Exception(e);
		}finally{
			if(sshConnection!=null) {
                sshConnection.close();
            }
		}
		return tempString;
	}
}
