package lhf.launcher.support

import groovy.util.logging.Slf4j

import java.util.zip.*
import java.util.HashMap ;
import org.slf4j.*

import ant.*
import ch.qos.logback.*

@Slf4j
class LaunchUtil { 
    def config
    def os
    
    // 압축 파일 처리
    def runZip(actionType) {
        try {
            if (System.properties['os.name'].toLowerCase().contains('windows')) {
                os = "win"
            } else {
                os = "linux"
            }
            def fileName = config.zip.fileName
            def ant = new AntBuilder()
            
            if(actionType == 'zip') {
                def targetDir = config.zip.targetDir
                log.debug "[server] basedir: ${targetDir}"
                log.debug "[server] destfile: ${fileName}"
                new File(fileName).delete()
//                ant.zip(destfile: fileName,
//                    basedir: targetDir)
    //                includes: "**/*.txt",
    //                excludes: "**/*.svn")
                ZipUtil.zip(targetDir, fileName);
            } else {
                def workingDir = config.zip.workingDir
                if(!new File(fileName).exists()) {
                    log.error "fileName not exist!!! : ${config.zip.fileName}"
                }
                log.debug "[agent] fileName: ${fileName}"
                log.debug "[agent] destDir: ${workingDir}"
                
                    ant.unzip(  src:fileName,
                                dest:workingDir,
                                overwrite:"true" )
//              rumtime 에서 ant.unzip 의 리턴이 넘어오지않아서 java로 수정
//              ZipUtil.unzip(fileName, workingDir);
                log.debug "[agent] finished unzip"
            }
        } catch (Exception e) {
            log.debug e.toString()
        }
    }
    
    // FTP 파일 전송
    def runFtp(actionType) {
        try {
            log.debug "hostIp: ${config.ftp.hostIp}"
            log.debug "userId: ${config.ftp.userId}"
            log.debug "password: ${config.ftp.password}"
            log.debug "localDir: ${config.ftp.localDir}"
            log.debug "remoteDir: ${config.ftp.remoteDir}"
            log.debug "includeFiles: ${config.ftp.includeFiles}"
			
            def hostIp = config.ftp.hostIp 
            def userId = config.ftp.userId
            def password = config.ftp.password
            def localDir = config.ftp.localDir
            def remoteDir = config.ftp.remoteDir
            def includeFiles = config.ftp.includeFiles
    
            getClass().getClassLoader().loadClass("org.apache.tools.ant.taskdefs.optional.net.FTP")
            def ant = new AntBuilder()
    //        log.debug getClass().getClassLoader() //groovy.lang.GroovyClassLoader$InnerLoader
    //        log.debug ant.getClass().getClassLoader() //org.codehaus.groovy.tools.RootLoader
    
            if(actionType == 'put') {
                log.debug "[server] put ftp file from : " + localDir
                log.debug "[server] put ftp file to : " + remoteDir
                ant.ftp( server:hostIp,
                userid:userId,
                password:password,
                passive:"yes",
                verbose:"yes",
                remotedir:remoteDir,
                binary:"yes" ) {
                    fileset( dir:localDir ) { include( name:includeFiles ) }
                    // fileset( dir:"." ) { include( name:includeFiles ) } // **/*.gz
                }
            } else { // get
                new File(localDir).deleteDir()
                new File(localDir).mkdirs()
                log.debug "[agent] get ftp file from : " + remoteDir
                log.debug "[agent] get ftp file to : " + localDir
                ant.ftp( server:hostIp,
                userid:userId,
                password:password,
                passive:"yes",
                verbose:"yes",
                action:"get",
                remotedir:remoteDir,
                binary:"yes" ) {
                    fileset( dir:localDir ) { include( name:includeFiles ) }
                }
            }
        } catch (Exception e) {
            log.debug e.toString().toString()
        }
    }
    
    // Java FTP 파일 전송
    def runJFtp(actionType) {
        try {
            if (System.properties['os.name'].toLowerCase().contains('windows')) {
                os = "win"
            } else {
                os = "linux"
            }

            def localDir = config.ftp.localDir
            def remoteDir = config.ftp.remoteDir
            def includeFiles = config.ftp.includeFiles
            FtpUtil ftpUtil = new FtpUtil();
            ftpUtil.init(os);
            
            if(actionType == 'put') {
                log.debug "[server] put ftp file from : " + localDir
                log.debug "[server] put ftp file to : " + remoteDir
                ftpUtil.upload(localDir, remoteDir);
            } else { // get
                new File(localDir).deleteDir()
                new File(localDir).mkdirs()
                log.debug "[agent] get ftp file from : " + remoteDir
                log.debug "[agent] get ftp file to : " + localDir
                ftpUtil.download(remoteDir + "/lhf.lorisg.zip", localDir + "/lhf.lorisg.zip");
            }
        } catch (Exception e) {
            log.debug e.toString().toString()
        }
    }

    // command 실행
    def runExec(command, sync) {
        def rtrn
        try {
            if (System.properties['os.name'].toLowerCase().contains('windows')) {
                os = "win"
            } else {
                os = "linux"
            }
            log.debug "[agent] runExec : " + command
    
            // Option 1: executing a string
            command = "" + command + ""
            def proc = command.execute()
            if(sync) {
                proc.waitFor()
                rtrn = "finished"
//                rtrn = proc.exitValue()
//                log.debug "[agent] stderr: ${proc.err.text}"
//                log.debug "[agent] stdout: ${proc.in.text}"
                log.debug "[agent] return code: ${rtrn}"
            }
            
            // Option 2: using ant builder's exec task
    //        def ant = new AntBuilder()   // create an antbuilder
    //        ant.exec(outputproperty:"cmdOut",
    //                     errorproperty: "cmdErr",
    //                     resultproperty:"cmdExit",
    //                     failonerror: "true",
    //                     executable: command) {
    //                         arg(line:""" stop """)
    //             }
    //        log.debug "[agent] return code:  ${ant.project.properties.cmdExit}"
    //        log.debug "[agent] stderr:         ${ant.project.properties.cmdErr}"
    //        log.debug "[agent] stdout:        ${ ant.project.properties.cmdOut}"
        } catch (Exception e) {
            log.debug e.toString()
        }
        return rtrn
    }
    
    // http://extensions.xwiki.org/xwiki/bin/view/Extension/REST+HTTP+Client
    // was 서버 live check
    def checkHttp() {
        def errCode = 200
        try {
            log.debug "[agent] check out 1 : " + config.service.url
            def url = new URL(config.service.url)
            def connection = url.openConnection()
            connection.setRequestMethod("GET")
            log.debug "[agent] check out 2 : " + config.service.url
            connection.connect()
            errCode = connection.responseCode
            log.debug "[agent] errCode : " + errCode
        } catch (Exception e) {
            log.debug e.toString()
            errCode = 0
        }
        log.debug "[agent] return errCode : " + errCode
        return errCode
    }
    
    // command cursor 읽기
    def readUntil( reader, pattern ) {
        def ch
        def sb = new StringBuffer()
        while ((ch = reader.read()) != -1) {
            sb << (char) ch
            if (sb.toString().endsWith(pattern)) {
                def found = sb.toString()
                sb = new StringBuffer()
                return found
            }
        }
        return null
    }

    // 환경 정보 셋팅
    def setConfig( config ){
        this.config = config;
    }
    
    // http get file
    class FileBinaryCategoryTest {
        void testDownloadBinaryFile() {
            def file = new File("logo.gif")
            use (FileBinaryCategory) {
                file << "http://www.google.com/images/logo.gif".toURL()
            }
            assert file.length() > 0
            file.delete()
        }
    }
    
    class FileBinaryCategory {
        def leftShift(File a_file, URL a_url) {
            def input
            def output
            try {
                input = a_url.openStream()
                output = new BufferedOutputStream(new FileOutputStream(a_file))
                output << input
            }
            finally {
                input?.close()
                output?.close()
            }
        }
    }
}
