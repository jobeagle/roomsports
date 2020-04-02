#!/bin/bash
# Deployment:
# kompiliere rs.jar im rs workspace: in Eclipse rechte Maustaste auf rs.jardesc und "Create JAR"
# dieses Script starten (alles benötige nach rs_export aus dem Workspace kopieren)
# Unter Windows: install.nsi: rechte maustaste: compile nsis
# setup.exe kann direkt verwendet werden
# für autom. Update:
# ggf. rsupdate.zip ins Update-Verzeichnis auf Server kopieren

cp ANT_DLL.dll rs_export/
cp ANT_DLL_64.dll rs_export/
cp Ampel.png rs_export/
cp DSI_CP210xManufacturing_3_1.dll rs_export/
cp DSI_CP210xManufacturing_3_1_64.dll rs_export/
cp DSI_SiUSBXp_3_1.dll rs_export/
cp DSI_SiUSBXp_3_1_64.dll rs_export/
cp JSAP-2.1.jar rs_export/
cp LibusbJava.dll rs_export/
cp LibusbJava64.dll rs_export/
cp RXTXcomm.jar rs_export/
cp RXTXcomm64.jar rs_export/
cp WebServices/lib/XmlSchema-1.4.7.jar rs_export/
cp auswert1.png rs_export/
cp WebServices/lib/axiom-api-1.2.13.jar rs_export/
cp WebServices/lib/axiom-dom-1.2.13.jar rs_export/
cp WebServices/lib/axiom-impl-1.2.13.jar rs_export/
cp WebServices/lib/axis2-adb-1.6.2.jar rs_export/
cp WebServices/lib/axis2-kernel-1.6.2.jar rs_export/
cp WebServices/lib/axis2-transport-http-1.6.2.jar rs_export/
cp WebServices/lib/axis2-transport-local-1.6.2.jar rs_export/
cp com4j-amd64.dll rs_export/
cp ch.ntb.usb-0.5.9.jar rs_export/
cp com4j-amd64.dll rs_export/
cp com4j-x86.dll rs_export/
cp com4j.jar rs_export/
cp WebServices/lib/commons-codec-1.3.jar rs_export/
cp WebServices/lib/commons-httpclient-3.1.jar rs_export/
cp commons-io-1.3.2.jar rs_export/
cp WebServices/lib/commons-logging-1.1.1.jar rs_export/
cp csvrace.png rs_export/
cp ergo.gif rs_export/
cp fragezeichen.png rs_export/
cp g*png rs_export/
cp CP210x_VCP_Win_XP_S2K3_Vista_7.exe rs_export/
cp gson-2.7.jar rs_export/
cp WebServices/lib/httpcore-4.0.jar rs_export/
cp install-filter-win.exe rs_export/
cp install-filter-win_64.exe rs_export/
cp install.nsi rs_export/
cp jfreechart-1.0.16/lib/jcommon-1.0.20.jar rs_export/
cp jfreechart-1.0.16/lib/jfreechart-1.0.16-experimental.jar rs_export/
cp jfreechart-1.0.16/lib/jfreechart-1.0.16-swt.jar rs_export/
cp jfreechart-1.0.16/lib/jfreechart-1.0.16.jar rs_export/
cp jna.jar rs_export/
cp junit-4.4.jar rs_export/
cp konfig.gif rs_export/
cp libusb0.dll rs_export/
cp libusb0.sys rs_export/
cp libusb0_64.sys rs_export/
cp lizenz.txt rs_export/
cp lizenz_ansi.txt rs_export/
cp licence rs_export/
cp log4j-1.2.17.jar rs_export/
cp logo_h40.bmp rs_export/
cp logo_h40.png rs_export/
cp WebServices/lib/mail-1.4.jar rs_export/
cp messages.nsh rs_export/
cp mtbs.sqlite rs_export/
cp rsserver.sqlite rs_export/
cp SQLite/SQLite/SQLite/mtbsplus.sqlite rs_export/
cp WebServices/lib/neethi-3.0.2.jar rs_export/
cp open.png rs_export/
cp openFolder.gif rs_export/
cp org.eclipse.core.commands_3.6.0.I20100512-1500.jar rs_export/
cp org.eclipse.core.runtime_3.6.0.v20100505.jar rs_export/
cp org.eclipse.equinox.common_3.6.0.v20100503.jar rs_export/
cp org.eclipse.jface_3.6.2.M20110210-1200.jar rs_export/
cp osm_gegpos.png rs_export/
cp osm_pos.png rs_export/
cp osm_start.png rs_export/
cp osm_ziel.png rs_export/
cp pause.png rs_export/
cp platform.jar rs_export/
cp platform64.jar rs_export/
cp play.png rs_export/
cp doc/roomsports.pdf rs_export/
cp rs.jar rs_export/
cp rxtxSerial.dll rs_export/
cp rxtxSerial64.dll rs_export/
cp settings.xml rs_export/
cp slf4j-api-1.5.2.jar rs_export/
cp slf4j-log4j12-1.5.2.jar rs_export/
cp spreadsheet.png rs_export/
cp sqlite-jdbc-3.7.2.jar rs_export/
cp start_rs.bat rs_export/
cp start_rs_e.bat rs_export/
cp stop.png rs_export/
cp strava.png rs_export/
cp rs.ico rs_export/
cp swt.jar rs_export/
cp swt64.jar rs_export/
cp uac_w7.png rs_export/
cp uac_xp.png rs_export/
cp updater/update.jar rs_export/
cp vcredist_x64.exe rs_export/
cp vcredist_x86.exe rs_export/
cp viewrefresh.png rs_export/
cp vlc.gif rs_export/
cp WebServices/lib/webservices.jar rs_export/
cp ws-commons-util-1.0.2.jar rs_export/
cp WebServices/lib/wsdl4j-1.6.2.jar rs_export/
cp WebServices/lib/wstx-asl-3.2.9.jar rs_export/
cp xmlrpc-client-3.1.3.jar rs_export/
cp xmlrpc-common-3.1.3.jar rs_export/
mkdir -p rs_export/touren
cp Touren/*.gpx rs_export/touren/

mkdir rs_export/rsupdate
cp rs.jar rs_export/rsupdate/rs.jar
zip -r rs_export/rsupdate.zip rs_export/rsupdate
rm -r rs_export/rsupdate

