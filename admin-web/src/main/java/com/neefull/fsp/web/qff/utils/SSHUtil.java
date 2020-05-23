package com.neefull.fsp.web.qff.utils;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 远程命令/sftp工具类
 *
 * @author lijiang
 * @date 2015/8/12 11:31
 */
public class SSHUtil {
    private static final String SFTP = "sftp";
    private static final String SHELL = "exec";
    private static final Logger logger = LoggerFactory.getLogger(SSHUtil.class);

    /**
     * 执行远程命令
     *
     * @param ip
     * @param user
     * @param psw
     * @throws Exception
     */
    public static String execCmd(String ip, String user, String psw, String cmd) throws Exception {
        //连接服务器，采用默认端口
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, ip);
        Channel channel = connect(session, psw, SHELL);
        String result = null;

        try {
            ChannelExec channelExec = (ChannelExec) channel;
            //获取输入流和输出流
            InputStream in = channel.getInputStream();
            channelExec.setCommand(cmd);
            channelExec.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    result = new String(tmp, 0, i);
                }
                if (channel.isClosed()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    result = e.toString();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            session.disconnect();
            channel.disconnect();
        }

        return result;
    }

    /**
     * 执行sftp传输
     *
     * @param ip
     * @param user
     * @param psw
     * @param localFile 本地文件
     * @param dstDir
     * @throws Exception
     */
    public static void sftp(String ip, String user, String psw, File localFile, String dstDir) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, ip);
        //连接服务器，采用默认端口
        Channel channel = connect(session, psw, SFTP);

        try {
            ChannelSftp sftp = (ChannelSftp) channel;
            sftp.connect();

            //本地文件
            if (!localFile.isFile())
                return;
            InputStream in = new FileInputStream(localFile);

            // 目的文件
            OutputStream out = sftp.put(dstDir + "/" + localFile.getName());

            byte b[] = new byte[1024];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
            out.flush();
            out.close();
            in.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            session.disconnect();
            channel.disconnect();
        }
    }

    /**
     * 连接服务器
     *
     * @param session
     * @param psw
     * @param type
     * @return
     * @throws Exception
     */
    private static Channel connect(Session session, String psw, String type) throws Exception {
        //如果服务器连接不上，则抛出异常
        if (session == null) {
            throw new Exception("session is null");
        }

        //设置登陆主机的密码
        session.setPassword(psw);//设置密码

        //设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");

        //设置登陆超时时间
        session.connect(30000);

        //创建通信通道
        return session.openChannel(type);
    }
}
