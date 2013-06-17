package lhf.launcher.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 서버에 업로드/다운로드 하는 간단한 FTP 구현체
 *
 * @author
 */
public class FtpUtil {

    private Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    private String serverIp;
    private int port;
    private String userId;
    private String passwd;
    private String encoding;
    private String rootPath;
    
    private Properties appProperties = new Properties();

    public Properties getProperties() {
        try{
            InputStream in = this.getClass().getResourceAsStream("/properties/environment-config.properties");
            Properties props = new Properties();
            props.load(in);
            for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                String value = props.getProperty(key);
                appProperties.setProperty(key, value);
            }
        }catch(Exception e){
            logger.debug(e.toString());
            e.printStackTrace();
        }
        return appProperties;
    }
    
    public void init(String spec) {
        this.serverIp = getProperties().getProperty("lhf.ftp." + spec + ".serverIp");
        this.port = Integer.parseInt(getProperties().getProperty("lhf.ftp." + spec + ".port", "21"));
        this.userId = getProperties().getProperty("lhf.ftp." + spec + ".user");
        this.passwd = getProperties().getProperty("lhf.ftp." + spec + ".pwd");
        this.encoding = getProperties().getProperty("lhf.ftp." + spec + ".encode", "UTF-8");
        this.rootPath = getProperties().getProperty("lhf.ftp." + spec + ".rootPath", "");
        logger.debug("this.userId : " + this.userId + "/spec :" + spec);
        logger.debug("this.passwd : " + this.passwd);
    }

    /**
     * 파일 다운로드
     * @param  remoteFileFullPath 서버 저장 패스
     * @param  localFileFullPath  로컬 리소스 경로
     * @return 다운로드 성공 여부
     */
    public boolean download(String remoteFileFullPath, String localFileFullPath) {
        FTPClient ftpClient = new FTPClient();
//        ftpClient.setControlEncoding(encoding);
        OutputStream stream = null;
        try {
            // ftp 접속
            if(!connect(ftpClient)) return false;

            // ftp 로그인
            if(!login(ftpClient)) return false;

            // 업로드 타입 지정
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 기본 디렉토리로 이동
            if(!"".equals(rootPath)){
                ftpClient.changeWorkingDirectory(rootPath);
            }

            String localFileFullPath2 = localFileFullPath.substring(0, localFileFullPath.lastIndexOf("/"));
            new File(localFileFullPath2).mkdirs();

            stream = new FileOutputStream(new File(localFileFullPath));

            // download 시도
            if(!ftpClient.retrieveFile(remoteFileFullPath, stream)) {
                logger.error("ftpClient retrieveFile fail - " + remoteFileFullPath);
                throw new RuntimeException("ftpClient retrieveFile fail - " + remoteFileFullPath);
            }
            logger.debug("download finished: {}", rootPath + "/" + remoteFileFullPath);
        } catch(IOException e) {
            logger.error("download fail!", e);
        } catch(Exception e) {
            logger.error("download fail!", e);
        } finally {
            try {
                if(stream != null) stream.close();
            } catch (IOException e) {
                logger.error("connection fail!", e);
            }
            disconnect(ftpClient);
        }
        return true;
    }

    /**
     * 파일 업로드
     *
     * @param localFileFullPath 업로드할 파일의 절대 경로
     * @param remoteFileFullPath 서버에 저장할 절대 경로
     * @return 업로드 성공 여부
     */
    public boolean upload(String localFileFullPath, String remoteFileFullPath) {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding(encoding);

        FileInputStream stream = null;

        try {
            stream = new FileInputStream(localFileFullPath);

            // ftp 접속
            if(!connect(ftpClient)) return false;

            // ftp 로그인
            if(!login(ftpClient)) return false;

            // 전송 준비
            String[] splitPathAndName = splitPathAndName(remoteFileFullPath, "/");
            String remoteFilePath = splitPathAndName[0] + "/";
            String remoteFileName = splitPathAndName[1];

            // 업로드 타입 지정
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 기본 디렉토리로 이동
            if(!"".equals(rootPath)){
                ftpClient.changeWorkingDirectory(rootPath);
            }
            // 업로드 디렉토리 변경
            changeWorkingDirectory(ftpClient, remoteFilePath);

            // 업로드 시도
            if(!ftpClient.storeFile(remoteFileName, stream)) {
                logger.error("ftpClient storeFile fail - " + remoteFilePath + remoteFileName);
                throw new RuntimeException("ftpClient storeFile fail - " + remoteFilePath + remoteFileName);
            }

            logger.debug("upload finished: {}", ("".equals(rootPath)?"":(rootPath + "/")) + remoteFilePath + remoteFileName);
        } catch(IOException e) {
            logger.error("upload fail!", e);
        } finally {
            try {
                if(stream != null) stream.close();
            } catch (IOException e) {
                logger.error("connection fail!", e);
            }

            disconnect(ftpClient);
        }
        return true;
    }


    /**
     * 파일 삭제
     *
     * @param 삭제할파일의경로
     * @return 삭제 성공 여부
     */
    public boolean delete(String deleteTargetPath) {

        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding(encoding);

        FileInputStream stream = null;

        try {
            // ftp 접속
            if(!connect(ftpClient)) return false;

            // ftp 로그인
            if(!login(ftpClient)) return false;

            if(!("").equals(deleteTargetPath)){
                ftpClient.deleteFile(deleteTargetPath);
            }

        } catch(IOException e) {
            logger.error("delete fail!", e);
        } finally {
            try {
                if(stream != null) stream.close();
            } catch (IOException e) {
                logger.error("connection fail!", e);
            }

            disconnect(ftpClient);
        }

        return true;
    }


    /* FTP 접속 */
    private boolean connect(FTPClient ftpClient) {
        try {
            ftpClient.connect( serverIp, port );
            int replyCode = ftpClient.getReplyCode();
            logger.debug("server ftp connect: {}", replyCode);

            if(!FTPReply.isPositiveCompletion(replyCode)) {
                disconnect(ftpClient);
                return false;
            }

            logger.debug("server ftp connect success - {}:{}", new Object[]{serverIp, port});

            return true;
        } catch(Exception e) {
            logger.error("server ftp connect fail!", e);

            return false;
        }
    }

    /* FTP 접속해제 */
    private void disconnect(FTPClient ftpClient) {
        try {
            if(ftpClient.isConnected())
                ftpClient.disconnect();
        } catch(Exception e) {
            logger.error("disconnection fail!", e);
        }
        logger.debug("server ftp disconnect!");
    }

    /* 로그인 */
    private boolean login(FTPClient ftpClient) {
        try {
            boolean result = ftpClient.login(userId, passwd);
            if(!result)
                logger.error("server ftp login fail!");

            logger.debug("server ftp login success - {}", userId);

            return result;
        } catch (IOException e) {
            logger.error("server ftp login fail!", e);
            return false;
        }
    }

    /**
     * FTP 서버의 디렉토리로 이동하는 기능
     *
     * @param remoteFilePath 이동할 디렉토리
     * @return
    */
    public void changeWorkingDirectory(FTPClient ftpClient, String remoteFilePath) {
        try {
            if (ftpClient.changeWorkingDirectory(remoteFilePath)) {
                logger.debug("ftpClient changeWorkingDirectory success - {}", ftpClient.printWorkingDirectory());
            } else {
                // 디렉토리가 없는 경우, 디렉토리를 만든다.
                logger.debug("ftpClient makeDirectory: {}", remoteFilePath);

                String[] paths = remoteFilePath.trim().split("/");
                for(String path : paths) {
                    if(path == null || path.trim().equals("")) continue;

                    // 디렉토리 변경 시도 후 없으면 생성
                    if(!ftpClient.changeWorkingDirectory(path)) {
                        ftpClient.makeDirectory(path);

                        // 디렉토리 변경 재시도 후 없으면 예외
                        if(!ftpClient.changeWorkingDirectory(path))
                            throw new RuntimeException("ftpClient changeWorkingDirectory fail - " + path);
                    }
                }
                logger.debug("ftpClient changeWorkingDirectory success - {}", ftpClient.printWorkingDirectory());
            }

        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 파일명이 포함된 전체경로를 주면 파일경로와 파일명으로 분리
     *
     * @param fullpath 전체경로
     * @return String[] - 파일경로[0], 파일명[1]
    */
    public String [] splitPathAndName(String path, String fileSep) {

        String[] split = new String[2];
        if (path == null || "".equals(path)) {
            split[0] = "";
            split[1] = "";
        } else {
            int lastIndex = path.lastIndexOf(fileSep);
            if ( lastIndex >= 0 ) {
                split[0] = path.substring(0,lastIndex);
                split[1] = path.substring(lastIndex+1);
            } else {
                split[0] = "";
                split[1] = path;
            }
        }
        return split;
    }

}
