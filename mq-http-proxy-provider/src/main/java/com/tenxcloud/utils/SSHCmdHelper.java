package com.tenxcloud.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import org.apache.log4j.Logger;



public class SSHCmdHelper {
	private static final Logger s_logger = Logger.getLogger(SSHCmdHelper.class);
	
	private static String  DEFAULTCHART="UTF-8";
	public static Connection acquireAuthorizedConnection(String ip, String username, String password) {
		return acquireAuthorizedConnection(ip, 22, username, password);
	}
	public static boolean isEmpty(String s) {
		int length;
		if ((s == null) || ((length = s.length()) == 0) || s.equals("null")) {
			return true;
		}
		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static Connection acquireAuthorizedConnection(String ip, int port, String username, String password) {
		Connection sshConnection = new Connection(ip, port);
		try {
			sshConnection.connect(null, 60000, 60000);
			if (!sshConnection.authenticateWithPassword(username, password)) {
				String[] methods = sshConnection.getRemainingAuthMethods(username);
				StringBuffer mStr = new StringBuffer();
				for (int i=0; i<methods.length; i++) {
					mStr.append(methods[i]);
				}
				s_logger.warn("SSH authorizes failed, support authorized methods are " + mStr);
				return null;
			}
			return sshConnection;
		} catch (IOException e) {
			s_logger.warn("Get SSH connection failed", e);
			return null;
		}
	}

	public static void releaseSshConnection(Connection sshConnection) {
		if (sshConnection != null) {
			sshConnection.close();
		}
	}
	
	public static boolean sshExecuteCmd(Connection sshConnection, String cmd, int nTimes) {
		for (int i = 0; i < nTimes; i ++) {
			try {
				if (sshExecuteCmdOneShot(sshConnection, cmd)) {
					return true;
				}
			} catch (Exception e) {
				continue;
			}
		}
		return false;
	}
	
	public static int sshExecuteCmdWithExitCode(Connection sshConnection, String cmd) {
		return sshExecuteCmdWithExitCode(sshConnection, cmd, 3);
	}
	
	public static int sshExecuteCmdWithExitCode(Connection sshConnection, String cmd, int nTimes) {
		for (int i = 0; i < nTimes; i ++) { 
			try {
				return sshExecuteCmdOneShotWithExitCode(sshConnection, cmd);
			} catch (Exception e) {
				continue;
			}
		}
		return -1;
	}
	
	public static boolean sshExecuteCmd(Connection sshConnection, String cmd) {
		return sshExecuteCmd(sshConnection, cmd, 3);
	}
	
	public static int sshExecuteCmdOneShotWithExitCode(Connection sshConnection, String cmd) throws Exception {
		s_logger.info("Executing cmd: " + cmd);
		Session sshSession = null;
		InputStream stdout=null;
		InputStream stderr=null;
		try {
			sshSession = sshConnection.openSession();
			// There is a bug in Trilead library, wait a second before
			// starting a shell and executing commands, from http://spci.st.ewi.tudelft.nl/chiron/xref/nl/tudelft/swerl/util/SSHConnection.html
//			Thread.sleep(1000);

			if (sshSession == null) {
				throw new Exception("Cannot open ssh session");
			}
			
			sshSession.execCommand(cmd);
			
			stdout = sshSession.getStdout();
			stderr = sshSession.getStderr();
			
	
			byte[] buffer = new byte[8192];
			while (true) {
				if (stdout == null || stderr == null) {
					throw new Exception("stdout or stderr of ssh session is null");
				}
				
				if ((stdout.available() == 0) && (stderr.available() == 0)) {
					int conditions = sshSession.waitForCondition(
							ChannelCondition.STDOUT_DATA
							| ChannelCondition.STDERR_DATA
							| ChannelCondition.EOF, 120000);
					
					if ((conditions & ChannelCondition.TIMEOUT) != 0) {
						s_logger.info("Timeout while waiting for data from peer.");
						break;
					}

					if ((conditions & ChannelCondition.EOF) != 0) {
						if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {							
							break;
						}
					}
				}
							
				while (stdout.available() > 0) {
					stdout.read(buffer);
				}
			
				while (stderr.available() > 0) {
					stderr.read(buffer);
				}
			}
			
			if (buffer[0] != 0) {
				s_logger.info(cmd + " output:" + new String(buffer));
			}
//			Thread.sleep(1000);
			return sshSession.getExitStatus();
		}  catch (Exception e) {
			s_logger.info("Ssh executed failed", e);
			throw new Exception("Ssh executed failed " + e.getMessage());
		}	finally {
			if (sshSession != null)
				sshSession.close();
            if (stdout!=null)
                try {
                    stdout.close();
                } catch (IOException e) {
                    s_logger.info("stdout close error ", e);
                }
            if(stderr!=null)
                try {
                    stderr.close();
                } catch (IOException e) {
                    s_logger.info("stdout close error ", e);
                }
		}
	}
    private static String processStdout(InputStream in, String charset){
        InputStream    stdout = new StreamGobbler(in);
        StringBuffer buffer = new StringBuffer();;
        try {
            @SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout,charset));
            String line=null;
            while((line=br.readLine()) != null){
//            	s_logger.info("processStdout(InputStream in, String charset) line= "+line);
                buffer.append(line+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
        	if(stdout!=null)
				try {
					stdout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
        return buffer.toString();
    }
    public static String sshExecuteCmdOneShotWithExitMsg(Connection sshConnection, String cmd) throws Exception {
        Session sshSession = null;
        InputStream stdout=null;
        InputStream stderr=null;
        String result="";  
        try {
            sshSession = sshConnection.openSession();

            if (sshSession == null) {
                throw new Exception("Cannot open ssh session");
            }
            sshSession.execCommand(cmd);

            result=processStdout(sshSession.getStdout(),DEFAULTCHART);
            if(SSHCmdHelper.isEmpty(result)){
                result=processStdout(sshSession.getStderr(),DEFAULTCHART);  
            }  
            return result;
        }  catch (Exception e) {
            s_logger.info("Ssh executed failed", e);
            throw new Exception("Ssh executed failed " + e.getMessage());
        }   finally {
            if (sshSession != null)
                sshSession.close();
            if (stdout!=null)
                try {
                    stdout.close();
                } catch (IOException e) {
                    s_logger.info("stdout close error ", e);
                }
            if(stderr!=null)
                try {
                    stderr.close();
                } catch (IOException e) {
                    s_logger.info("stdout close error ", e);
                }
        }
    }
	
	public static boolean sshExecuteCmdOneShot(Connection sshConnection, String cmd) throws Exception {
		return sshExecuteCmdOneShotWithExitCode(sshConnection, cmd) == 0;
	}
}
