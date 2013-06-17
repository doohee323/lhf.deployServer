package lhf.launcher.support

environments {
    
    execRemote {
        zip {
            fileName = 'C:/share/build/lhf.lorisg.zip'
            workingDir = 'C:/share/build/lhf.lorisg/tmp'
            targetDir = 'C:/share/build/lhf.lorisg'
        }
        all {
            remoteList = 'win'
        }
        win {
            serverIp = '10.135.31.101'
            userId = 'administrator'
            password = 'ghdtjwns!323'
            remoteShell = 'net stop "lorisg.deploy",net start "lorisg.deploy"'
        }
        linux {
            serverIp = '10.135.31.103'
            userId = 'lhf'
            password = 'lhf'
            remoteShell = '/DATA2/lhf.launcher/bat/laucher.sh linux deploy &'
        }
    }
 
    win {
        zip {
            fileName = 'C:/share/work/lhf.lorisg/lhf.lorisg.zip'
            workingDir = 'C:/share/build/lhf.lorisg/tmp'
            targetDir = 'C:/LHF_IDE/was/apache-tomcat-6.0.35/webapps/lhf.lorisg'
        }
        replace {
            sourceDir = 'C:/share/build/lhf.lorisg/tmp/WEB-INF/classes/overwrite/DEV'
            targetDir = 'C:/share/build/lhf.lorisg/tmp'
        }
        ftp {
            hostIp = '10.135.31.101'
            userId = 'lhf'
            password = 'lhf'
            localDir = 'c:/share/work/lhf.lorisg'
            remoteDir = '/build'
            includeFiles = '**/*.zip'
        }
        exec {
            shutdown = 'net stop "lorisg.shutdown",net start "lorisg.shutdown"'
            startup = 'net stop "lorisg.start",net start "lorisg.start"'
        }
        service {
            url = 'http://localhost:7001/lhf.lorisg'
        }
    }
    
    linux {
        zip {
            fileName = '/DATA2/work/lhf.lorisg/lhf.lorisg.zip'
            workingDir = '/DATA2/build/lhf.lorisg/tmp'
            // /DATA1/apache-tomcat-6.0.37/webapps> ln -s /DATA2/lhf.lorisg lhf.lorisg
            // lhf.lorisg -> /DATA2/lhf.lorisg/
            targetDir = '/DATA2/lhf.lorisg'
        }
        replace {
            sourceDir = '/DATA2/lhf.lorisg/WEB-INF/classes/overwrite/DEV'
            targetDir = '/DATA2/lhf.lorisg'
        }
        ftp {
            hostIp = '10.135.31.101'
            userId = 'lhf'
            password = 'lhf'
            localDir = '/DATA2/work/lhf.lorisg'
            remoteDir = '/build'
            includeFiles = '**/*.zip'
        }
        exec {
            startup = '/DATA1/apache-tomcat-6.0.37/bin/startup.sh'
            shutdown = '/DATA1/apache-tomcat-6.0.37/bin/shutdown.sh'
        }
        service {
            url = 'http://10.135.31.103:8080/lhf.lorisg'
        }
    }
    
    development {
        zip {
            fileName = 'C:/share/build/lhf.lorisg/lhf.lorisg.zip'
            workingDir = 'C:/share/build/lhf.lorisg/tmp'
            targetDir = 'C:/LHF_IDE/was/apache-tomcat-6.0.35/webapps/lhf.lorisg'
        }
        ftp {
            hostIp = '10.135.31.102'
            userId = 'administrator'
            password = 'ghdtjwns!323'
            localDir = 'c:/share/build/lhf.lorisg'
            remoteDir = '/build/lhf.lorisg'
            includeFiles = '**/*.zip'
        }
        exec {
            //startup = 'C:/LHF_IDE/was/apache-tomcat-6.0.35/bin/startup.bat'
            startup = 'net start "lorisg.start"'
            shutdown = 'C:/LHF_IDE/was/apache-tomcat-6.0.35/bin/shutdown.bat'
        }
        service {
            url = 'http://localhost:7001/lhf.lorisg'
        }
    }

    production {
        zip {
            fileName = 'C:/share/build/lhf.lorisg/lhf.lorisg.zip'
            workingDir = 'C:/share/build'
            workingDir = 'C:/share/build/lhf.lorisg/tmp'
            targetDir = 'C:/LHF_IDE/was/apache-tomcat-6.0.35/webapps/lhf.lorisg'
        }
        ftp {
            hostIp = 'dev1.lorisg.com'
            userId = 'eduadm'
            password = 'tkawjdKPMG1'
            localDir = 'c:/temp'
            remoteDir = '/dweHome'
            includeFiles = '**/*.pdf'
        }
        exec {
            startup = 'C:/LHF_IDE/workspace/lhf.launcher/bat/test.bat'
            shutdown = 'C:/LHF_IDE/workspace/lhf.launcher/bat/test.bat'
        }
        service {
            url = 'http://localhost:7001/lhf.lorisg'
        }
    }
}
