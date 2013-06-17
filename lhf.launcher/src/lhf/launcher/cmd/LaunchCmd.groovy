package lhf.launcher.cmd

import java.util.zip.*

import lhf.launcher.service.Launch

class LaunchCmd {

    static void main(args) {
        def argList = args.toList();
//        log.debug argList
        def service = new Launch()
        // 메인 처리
        def stage
        def cmd

        if(argList.size()  > 0) {
            stage = argList[0]
        }

        if(argList.size() > 1) { 
            cmd = argList[1]
        }
        // 1) 본사 호출 테스트
//         stage = 'execRemote'
//         cmd = 'all'
        // 2) 원격지 호출 테스트
//        stage = 'linux'
//        cmd = 'deploy'
        service.runMe(stage, cmd)
    }
}