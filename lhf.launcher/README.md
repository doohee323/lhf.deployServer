# lhf.deployServer

## Objective

배포서버에서 타겟서버로 자원을 배포하고 서비스를 재기동하는 프로그램

https://sites.google.com/site/pbfsup/library/gong-yusoseuguseong-gwanlibang-an

https://sites.google.com/site/pbfsup/library/gong-yuwongyeogjisoseubaepodogu

## Usage

원격지 소스 배포 도구

ㅇ 사용법
 - Center에서 호출
 - 주로 CI 서버에서 본사 배포 완료 후 호출함
  Execute Windows batch command :
  C:/LHF_IDE/workspace/lhf.launcher/bat/execRemote.bat execRemote all
  
 - execRemote : 본사 CI에 배포 서버 JOB
 - all : win, linux 등 대상 target 서버 (환경 설정에 명시 함)
  - win : window 서버
  - linux : linux 서버

## Developing

- Groovy
