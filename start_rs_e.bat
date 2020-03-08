rem start_rs.bat ist Teil der RoomSports-Applikation und freie Software entsprechend GNU GPLv3
rem 
rem Start der RoomSports-Applikation in englischer Sprache
rem 
rem mittels java -jar rs.jar <parameter>
rem 
rem z. B. java -jar rs.jar -d
rem 
rem Es sind folgende Aufrufparameter möglich:
rem
rem     P.  Zusatz  Name        Bedeutung
rem     ----------------------------------------------------------------------------- 
rem     -d          (debug)     erweiterte Debugmeldungen im Logfile
rem     -D          (deepdebug) erweiterte Debugmeldungen + Kommunikationsmeldungen
rem     -q          (demo)      Demomodus ohne Kommunikation zum Trainingsgerät
rem     -e          (english)   Umstellung auf Englisch
rem     -o          (nooverlay) VLC-Overlaymodus abschalten (gut z.B. für Screenshots)
rem     -g 1..9     (gang)      Gang vorgeben bim Start  
rem     -l          (loop)      Endlosbetrieb (z.B. auf Messen etc.)
rem     -f          (tour)      Tourdatei vorgeben
rem     -a          (nostop)    Autostopp abschalten
rem     -s          (start)     Sofort die Tour ohne Rückfrage starten (sinnvoll nur in Verbindung mit "tour")
rem     -m          (nomenu)    ohne Menü und Toolbar (zurück mit ESC)
rem     ----------------------------------------------------------------------------- 
rem Falls java.exe nicht im PATH enthalten, ggf. den absoluten Pfad ergänzen z.B.:
rem C:\Programme\Java\jre1.8.0_121\bin\java.exe -jar rs.jar -d -e -g 5

