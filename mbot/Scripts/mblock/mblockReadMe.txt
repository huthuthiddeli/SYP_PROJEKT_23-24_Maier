V 1.0
2022-11-30
Markus Falkensteiner

damit das Kernel Modul und das Devicde angezeigt wurden musste unter Linux folgendes PAckage deinstalliert werden: 
brltty


Kernelmodul für Serial Adapter laden:
sudo modprobe ch341

nun sollte folgendes Device geladen sein: /dev/ttyUSB0

MBlock-Link starten:
sudo mblock-mlink start


Browser IDE öffnen:
https://ide.mblock.cc/
 
