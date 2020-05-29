[![Build Status](https://travis-ci.org/TargetProcess/BuildBoard.png?branch=master)](https://travis-ci.org/TargetProcess/BuildBoard)

It should work with:
- [jdk 1.8](https://download.oracle.com/otn/java/jdk/8u251-b08/3d5a2bb8f8d4428bbe94aed7ec7ae784/jdk-8u251-windows-x64.exe) 
- [scala 2.11.11](https://scala-lang.org/download/2.11.11.html)
- [sbt 0.13.18](https://piccolo.link/sbt-0.13.18.msi)
- [play 2.4.10 (use github)](https://www.playframework.com/releases)

Ensure jdk bin, scala bin, sbt bin, playframework-xxx\framework directories are added to env PATH variable.

1. View all/my/per user entities/branches
2. Per branch:
  + create PR
  + merge PR
  + view PR status
  + view comments from PR
  + workflow with rules
  + trigger quick/full/pr builds
  + set build status (also on Github)
  + build visualization
  + toggle
  + abort
  + commit list
3. For entity:
  + show/change status
4. Notifications (HTML5)
5. *TBD* Develop
  + deploy to plan
  + start release
6. *TBD* Master
  + deploy to plan/production
7. *TBD* Logging
8. *TBD* Statistics
9. Github login/TP Login

BuildBoard is hosted on srv-hv3 server. To restart buildboard you should kill buildboard java process and remove RUNNING_PID file from _c:\BuildBoard\bin_. Then simply run _buildboard.bat_. I'm not sure that it is the best way, it's all I know :-).
