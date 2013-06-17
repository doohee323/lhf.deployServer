package lhf.launcher.service

import groovy.util.logging.Slf4j

import java.util.zip.*

import org.apache.commons.net.telnet.TelnetClient
import org.slf4j.*
import lhf.launcher.support.LaunchUtil;
import lhf.launcher.support.TelnetUtil;

import ant.*
import ch.qos.logback.*

// http://stackoverflow.com/questions/1641116/groovy-with-grape-and-antbuilder-classloader-problem
// http://snipplr.com/view/10625/
// http://naratalk.com/131
// http://groovy.codehaus.org/Cookbook+Examples

// -Dlogback.configurationFile=/path/to/config.xml
// /GroovyTest/src/logback.xml 에서 로그 레벨 수정 가능

//@Grapes([
//    @Grab(group='org.slf4j', module='slf4j-api', version='1.6.1'),
//    @Grab(group='ch.qos.logback', module='logback-classic', version='0.9.28'),
//    @Grab(group='ant', module='ant', version='[1.6.5,)'),
//    @Grab(group='ant', module='ant-nodeps', version='[1.0,)'),
//    @Grab(group='ant', module='ant-apache-oro', version='[1.0,)'),
//    @Grab(group='ant', module='ant-commons-net', version='[1.0,)'),
//    @Grab(group='commons-net', module='commons-net', version='[1.4,)')
//])

@Slf4j
class Launch { 
    def config
    def util
    def os

    // 메인 함수
    def runMe(stage, cmd) {
        try {
            if(stage == null) {
                stage = 'execRemote'
            }
            if(cmd == null) {
                cmd = 'all'
            }
            if (System.properties['os.name'].toLowerCase().contains('windows')) {
                os = "win"
            } else {
                os = "linux"
            }
            log.debug "param stage : " + stage
            log.debug "param cmd : " + cmd
            if(new File('src/lhf/launcher/support/config.groovy').exists()) {
                config = new ConfigSlurper(stage).parse(new File('src/lhf/launcher/support/config.groovy').toURI().toURL())
            } else {
                config = new ConfigSlurper(stage).parse(new File('lhf/launcher/support/config.groovy').toURI().toURL())
            }
            util = new LaunchUtil()
            util.setConfig(config);
            if(stage == 'execRemote') {
                log.debug "[server] workingDir delete : " + config.zip.workingDir
                new File(config.zip.workingDir).deleteDir()
                util.runZip('zip')
                log.debug "[server] execRemote call : " + cmd
                if(cmd == "all") {
                    def remoteList = config.all.remoteList
                    def lists = remoteList.split(",")
                    for(def i=0;i<lists.length;i++) {
                        execRemote(lists[i])
                        sleep(10000)
                    }
                } else {
                    execRemote()
                }
            } else if(cmd == "deploy") {
                util.runFtp('get'); // to-do 이어받기
//                util.runJFtp('get');  // to-do 이어받기
                util.runZip('unzip')
                def nRtrn = serverExec("shutdown")
                log.debug "[agent] nRtrn : " + nRtrn
                if(nRtrn) {
                    log.debug "[agent] ${config.replace.targetDir} replaced by ${config.replace.sourceDir}"
                    new AntBuilder().copy(todir: config.replace.targetDir, overwrite: true) {
                        fileset(dir:config.replace.sourceDir)
                    }
                    
                    log.debug "[agent] deleteDir : " + config.zip.targetDir
                    log.debug "[agent] " + config.zip.workingDir + " renames to " + config.zip.targetDir
                    if(os == "win") {
                        new File(config.zip.targetDir).deleteDir()
                        new File(config.zip.workingDir).renameTo(config.zip.targetDir)
                    } else {
                        util.runExec("rm -Rf " + config.zip.targetDir, true)
                        util.runExec("mv " + config.zip.workingDir + " " + config.zip.targetDir, true)
                    }
                    serverExec('startup')
                    sleep(10000)
                }
            } else if(cmd == "shutdown") {
                serverExec('shutdown')
            } else if(cmd == "startup") {
                serverExec('startup')
            }
        } catch (Exception e) {
            log.debug e.toString()
        }
    }
    
    // was 서버 기동 실행
    def serverExec(actionType) {
        log.debug "serverExec actionType : " + actionType
        try {
            def command
            if(actionType == 'startup') {
                command = config.exec.startup
                def lists = command.split(",")
                for(def i=0;i<lists.length;i++) {
                    util.runExec(lists[i], false)
                    sleep(10000)
                }
            } else if(actionType == 'shutdown') {
                command = config.exec.shutdown
                log.debug "[agent] shutdown : " + command
                def errCode = 200;
                while (errCode > 0) {
                    errCode = util.checkHttp();
                    if(errCode > 0) {
                        def lists = command.split(",")
                        for(def i=0;i<lists.length;i++) {
                            util.runExec(lists[i], true)
                            sleep(10000)
                        }
                    }
                }
            } else {
                log.debug "[agent] actionType is empty!"
                return false
            }
        } catch (Exception e) {
            log.debug e.toString()
        }
        return true
    }
    
    // http://pleac.sourceforge.net/pleac_groovy/internetservices.html
    // 원격지 서버의 원격 실행
    def reader
    def execRemote(remote) {
        def serverIp = config.getAt(remote).serverIp
        def userId = config.getAt(remote).userId
        def password = config.getAt(remote).password
        def remoteShell = config.getAt(remote).remoteShell
        log.debug "[server] serverIp : " + serverIp
        log.debug "[server] userId : " + userId
        log.debug "[server] password : " + password
        log.debug "[server] remoteShell : " + remoteShell
//
//        TelnetUtil telnet = new TelnetUtil(serverIp, userId, password)
//        log.debug "[server] Got Connection..."
//        telnet.sendCommand(remoteShell, "");
//        telnet.disconnect();
        
        try {
            def telnet = new TelnetClient()
            
            telnet.connect( serverIp, 23 )
            reader = telnet.inputStream.newReader()
            def writer = new PrintWriter(new OutputStreamWriter(telnet.outputStream),true)
            util.readUntil( reader, "login:" )
            writer.println(userId)
            util.readUntil( reader, "assword:" )
            writer.println(password)
            def x = util.readUntil( reader, ">" )
			
			def lists = remoteShell.split(",")
			for(def i=0;i<lists.length;i++) {
				writer.println(lists[i])
	            writer.flush();
				sleep(10000);
			}
			
            def line
//            while((line = reader.readLine())!= null) {
//                if(line.indexOf('Exception') > -1
//                     || line.indexOf('not exist') > -1
//                      || line.indexOf('net start') > -1) {
//                    log.debug "[agent]  ---> " + line + " => telnet.disconnect() and exit!";
//                    telnet.disconnect()
//                    return
//                }
//                log.debug "[agent]  ---> " + line;
//            }
        } catch (Exception e) {
            log.debug e.toString()
        }
        
        // 1) 원격으로 윈도우 서비스 호출 
        // net start "lorisg.deploy"  // 원격지 서버의 deploy 전체 처리
        // net start "lorisg.start" // was 기동
        // net start "lorisg.shutdown" // was 종료
        
        // 본사 서버에서 원격지 서버의 배포쉘을 호출할 때 사용 (본사서버 -> 원격지 서버)        
        // C:\LHF_IDE\workspace\lhf.launcher\bat\laucher.bat
				
		//2) bat파일을 서비스로 등록하기
		//	2-1) resource toolkit 설치
		//		http://www.microsoft.com/en-us/download/details.aspx?id=17657
		//	2-2) srvany.exe, instsrv.exe 를 c:\windows\systems 에 복사
		//	2-3) lorisg.deploy 서비스 등록
		//	     instsrv.exe lorisg.deploy "C:\Program Files (x86)\Windows Resource Kits\Tools\srvany.exe"
        //       instsrv.exe lorisg.start "C:\Program Files (x86)\Windows Resource Kits\Tools\srvany.exe"
        //       instsrv.exe lorisg.shutdown "C:\Program Files (x86)\Windows Resource Kits\Tools\srvany.exe"
		//	2-4) 레지스트리 수정
		//		- HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\lorisg.deploy
		//		- Parameters 키 생성
		//		- String 추가
		//			Application	: C:/LHF_IDE/workspace/lhf.launcher/bat/laucher.bat
		//			AppDirectory	:
		//			AppParameters	: win deploy
        //      - HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\lorisg.start
        //      - Parameters 키 생성
        //      - String 추가
        //          Application : C:\LHF_IDE\was\apache-tomcat-6.0.35\bin\startup.bat
        //          AppDirectory    :
        //          AppParameters   : 
        //      - HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\lorisg.shutdown
        //      - Parameters 키 생성
        //      - String 추가
        //          Application : C:\LHF_IDE\was\apache-tomcat-6.0.35\bin\shutdown.bat
        //          AppDirectory    :
        //          AppParameters   : 

        // 3) 윈도우 서비스 삭제
        // sc delete "lorisg.start"
        // sc delete "lorisg.shutdown"

        // * 참고                
        //          http://www.ischo.net/?mid=board_windows&listStyle=webzine&document_srl=4288
        //          http://publib.boulder.ibm.com/tividd/td/BSM/SC32-9130-00/en_US/HTML/bsmp112.htm
    }
}
