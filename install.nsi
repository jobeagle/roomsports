; Installation RoomSports mit autom. JRE installation mittels NSIS
; siehe: https://nsis.sourceforge.io/Download
; und wahlweise VLC-Installation.
; siehe https://www.videolan.org
; Bruno Schmidt 2009...2020
;-----------------------------------------------
 
Name "RoomSports"
Caption "RoomSports"
; Icon "Java Launcher.ico"
OutFile "setup.exe"
SetCompressor /SOLID lzma

!define COMPANYNAME "Bruno Schmidt"

VIAddVersionKey "ProductName" "RoomSports Installation"
VIAddVersionKey "CompanyName" "Bruno Schmidt"
VIAddVersionKey "LegalCopyright" "Lizenz: GNU GPL-V3"
VIAddVersionKey "FileDescription" "RoomSports Installation"
VIAddVersionKey "FileVersion" "4.33"
VIProductVersion "4.33.0.0"
 
!define PRODUCT_NAME "RoomSports"

; Definitionen für Java
!define JRE_VERSION "8.0"
!define JAVAEXE "java.exe"

; Definitionen für VLC
!define VLC_URL http://download.videolan.org/pub/videolan/vlc/2.2.4/win32/vlc-2.2.4-win32.exe
;!define VLC_URL http://download.videolan.org/pub/videolan/vlc/2.2.0/win32/vlc-2.2.0-win32.exe
!define VLC_INSTALLER "vlc-2.2.4-win32.exe"
!define VLC_VERSION "2.2.4"
!define VLC_64_URL http://download.videolan.org/pub/videolan/vlc/2.2.4/win64/vlc-2.2.4-win64.exe
;!define VLC_64_URL http://download.videolan.org/pub/videolan/vlc/2.2.0/win64/vlc-2.2.0-win64.exe
!define VLC_64_INSTALLER "vlc-2.2.4-win64.exe"
!define VLC_64_VERSION "2.2.4"

; Definitionen für Kettler-Silaps Treiber: (veraltet!)
!define VCP_INSTALLER "CP210x_VCP_Win_XP_S2K3_Vista_7.exe"

RequestExecutionLevel admin
AutoCloseWindow true
ShowInstDetails nevershow

InstallDir $PROGRAMFILES\roomsports

!include "FileFunc.nsh"
!insertmacro GetFileVersion
!insertmacro GetParameters
!include "WordFunc.nsh"
!insertmacro VersionCompare
!include "StrFunc.nsh"
${StrLoc}
${StrCase}
!include "EnvVarUpdate.nsh"
!include "nsDialogs.nsh"
!include "x64.nsh"
!include "XML.nsh"
!include "dialogs.nsh"
!include "MUI2.nsh"

;!define MUI_ABORTWARNING
;!define MUI_ICON "logo.ico"
;!define MUI_UNICON "logo.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "logo_h40.bmp"

!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "English" ;first language is the default language

!include "messages.nsh"

LicenseLangString license ${LANG_ENGLISH} licence
LicenseLangString license ${LANG_GERMAN} lizenz_ansi.txt
LicenseData $(license)

LicenseText "$(STR_LIZENZ)"
LicenseForceSelection checkbox "$(STR_LIZENZ_FS)"

MiscButtonText "$(STR_BUT_ZUR)" "$(STR_BUT_WEI)" "$(STR_BUT_ABB)" "$(STR_BUT_SCH)"

Var Dialog
Var CbJava
Var CbJava_State
Var CbVLC
Var CbVLC_State
Var CbDaum
Var CbDaum_State
Var CbKettler
Var CbKettler_State
Var CbTacx
Var CbTacx_State
Var CbTour
Var CbTour_State
Var CbGEReg
Var CbGEReg_State
Var JAVA64
Var TourenVZ
Var JavaExePath

Section "Closeall"
  MessageBox MB_ICONINFORMATION "$(STR_CLOSE_ANW)"
  SetShellVarContext current
SectionEnd

DirText "$(STR_ZIELORDNER_TEXT)" "$(STR_ZIELORDNER)" "$(STR_SUCHEN)" "$(STR_VERZ_AUSWAHL)"

;--------------------------------
; Pages

Page license Lizenzheader
;Page Custom SerialPageShow SerialPageLeave
Page directory Dirheader
Page custom nsDialogsPage nsDialogsPageLeave
Page instfiles

;--------------------------------
; Hauptprogramm
;--------------------------------
Section "" 
  ;MessageBox MB_ICONINFORMATION  $LANGUAGE
  ;MessageBox MB_ICONINFORMATION  ${LANG_ENGLISH}

;  Call CheckJRE64
  
;  Messagebox MB_OK $CbJava_State
	
;  StrCmp $CbJava_State 0 NOJAVA
  Call GetJRE
  Pop $R0
  StrCpy $JavaExePath $R0
  
  ; hier gehts weiter...
  ${GetParameters} $1
  MessageBox MB_ICONINFORMATION  '"$(STR_JAVA_OK)" $R0'

NOJAVA:  
  StrCmp $CbVLC_State 0 NOVLC
  Call GetVLC
  Pop $R0

NOVLC:  
  StrCmp $CbKettler_State 0 NOVCP  
  Call GetVCP
  Pop $R0

NOVCP:
  SetOutPath $INSTDIR\touren
  File touren\*.*
  
  SetOutPath $INSTDIR
  
  ; Dateien kopieren...
  File *.jar
  File *.dll
  File mtbs.sqlite
;  File logo.ico
  File install*.exe
  File *.png
  File *.gif
  File *.sys
  File vcredist*.exe
  File *.pdf

  File ${VCP_INSTALLER}

  ${if} $JAVA64 != ""
     ; die 64 Bit Versionen verwenden:
     CopyFiles "$INSTDIR\RXTXcomm64.jar" "$INSTDIR\RXTXcomm.jar"
     CopyFiles "$INSTDIR\rxtxserial64.dll" "$INSTDIR\rxtxserial.dll"
     CopyFiles "$INSTDIR\swt64.jar" "$INSTDIR\swt.jar"
     CopyFiles "$INSTDIR\LibusbJava64.dll" "$INSTDIR\LibusbJava.dll"
     CopyFiles "$INSTDIR\platform64.jar" "$INSTDIR\platform.jar"
     CopyFiles "$INSTDIR\ANT_DLL_64.dll" "$INSTDIR\ANT_DLL.dll"
     CopyFiles "$INSTDIR\DSI_CP210xManufacturing_3_1_64.dll" "$INSTDIR\DSI_CP210xManufacturing_3_1.dll"
     CopyFiles "$INSTDIR\DSI_SiUSBXp_3_1_64.dll" "$INSTDIR\DSI_SiUSBXp_3_1.dll"
  ${endif}
  Delete "$INSTDIR\RXTXcomm64.jar" 
  Delete "$INSTDIR\rxtxserial64.dll" 
  Delete "$INSTDIR\swt64.jar" 
  Delete "$INSTDIR\LibusbJava64.dll" 
  Delete "$INSTDIR\platform64.jar" 
  Delete "$INSTDIR\ANT_DLL_64.dll" 
  Delete "$INSTDIR\DSI_CP210xManufacturing_3_1_64.dll" 
  Delete "$INSTDIR\DSI_SiUSBXp_3_1_64.dll" 
  
  ; abh. von der gew. Sprache die Propertydatei, .exe und Rahmen kopieren:
  StrCmp $LANGUAGE ${LANG_ENGLISH} EnglishCmp
  File start_rs.bat

  Goto EndLanguageCmp
  EnglishCmp:
  File start_rs_e.bat
   
  CopyFiles "$INSTDIR\start_rs_e.bat" "$INSTDIR\start_rs.bat"

  Delete "$INSTDIR\start_rs_e.bat" 

  EndLanguageCmp:
  ; start_rs.bat mit Aufruf von RoomSports ergänzen
  FileOpen $4 "$INSTDIR\start_rs.bat" a
  FileSeek $4 0 END
  FileWrite $4 "$\r$\n" ; we write a new line
  FileWrite $4 "$\"$JavaExePath$\" -jar rs.jar -d -g 5"
  FileWrite $4 "$\r$\n" ; we write an extra line
  FileClose $4 ; and close the file

  StrCmp $CbTacx_State 0 NOLIBUSB
  ; bei 64 Bit Windows generell die 64-Bit Version von LibUSB uebernehmen:
  ${If} ${RunningX64}
    CopyFiles "$INSTDIR\libusb0_64.sys" "$INSTDIR\libusb0.sys"
    CopyFiles "$INSTDIR\install-filter-win_64.exe" "$INSTDIR\install-filter-win.exe"
  ${EndIf}
  CopyFiles "$INSTDIR\libusb0.sys" "$SYSDIR\drivers"
  MessageBox MB_ICONINFORMATION  "$(STR_ROLLE_CONNECT)"
  
  ExecWait $INSTDIR\install-filter-win.exe
  
  MessageBox MB_ICONINFORMATION "$(STR_ROLLE_DISCONNECT)"
   
NOLIBUSB:
  ; Visual C++ Redist. Set (für ANT_DLL)
  ; vorher prüfen siehe: forums.winamp.com/archive/index.php/t-323052.html
  ${If} ${RunningX64}
    CopyFiles "$INSTDIR\vcredist_x64.exe" "$INSTDIR\vcredist.exe"  
    Goto InstallVcredist
  ${EndIf}
  CopyFiles "$INSTDIR\vcredist_x86.exe" "$INSTDIR\vcredist.exe"  
  
  InstallVcredist:  
  ExecWait '$INSTDIR\vcredist.exe /q:a /c:"VCREDI~1.EXE /q:a /c""msiexec /i vcrredist.msi /qb!"" "' 
  Delete "$INSTDIR\vcredist*.exe" 
  
  StrCmp $CbTour_State 0 NOTOUR
  CreateDirectory "$INSTDIR\touren"

  MessageBox MB_ICONINFORMATION "$(STR_DIALOG_TVERZ)"

  # Params:
  # 1) Title: "Looking for something"
  # 2) Caption: none
  # 3) InitDir: "$EXEDIR"
  # 4) Return: $R0
  ${ModernFolderBox} "$(STR_TVERZ_WAHL)" "" $INSTDIR\touren ${VAR_R0}
  # See if the user selects a file:
  ${if} $R0 == "${NULL}"
  Goto NOTOUR
  ${endif}

  CopyFiles "$INSTDIR\touren\*.m?v" "$R0"
  CopyFiles "$INSTDIR\touren\*.gpx" "$R0"
  CreateShortCut "$SMPROGRAMS\roomsports\touren.lnk" "$R0"
  StrCpy $TourenVZ $R0

  SetOutPath $APPDATA\roomsports
  SetOverwrite off
  File settings.xml
  ; ---> wieder umstellen, so dass die DB nicht mehr ueberschrieben wird
   SetOverwrite on
  File rsserver.sqlite

  ; Tourpfad in settings.xml schreiben
  ${xml::LoadFile} "$APPDATA\roomsports\settings.xml" $0
  ${xml::GotoPath} "/konfig" $0
  ${xml::SetAttribute} "tourenpfad" $R0 $0
  ${xml::SaveFile} "$APPDATA\roomsports\settings.xml" $0
  Goto DOKUCOPY
  
NOTOUR:
  SetOutPath $APPDATA\roomsports
  SetOverwrite off
  File settings.xml
  File rsserver.sqlite
  SetOverwrite on

DOKUCOPY:  
  ; Pfad ändern wegen des Applications-Links:
  SetOutPath $INSTDIR

  CreateDirectory "$SMPROGRAMS\roomsports"
  CreateShortCut "$SMPROGRAMS\roomsports\${PRODUCT_NAME}.lnk" "$INSTDIR\start_rs.bat"
  CreateShortCut "$DESKTOP\${PRODUCT_NAME}.lnk" "$INSTDIR\start_rs.bat"

  CreateShortCut "$SMPROGRAMS\roomsports\Dokumentation.lnk" "$INSTDIR\roomsports.pdf"
  CreateShortCut "$SMPROGRAMS\roomsports\roomsportswork.lnk" "$APPDATA\roomsports"
  WriteUninstaller $INSTDIR\uninstall.exe
  CreateShortCut "$SMPROGRAMS\roomsports\RS-Uninstall.lnk" "$INSTDIR\uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\roomsports ${PRODUCT_NAME}" "DisplayName" "${PRODUCT_NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\roomsports ${PRODUCT_NAME}" "UninstallString" "$INSTDIR\uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\roomsports ${PRODUCT_NAME}" "InstallLocation" "$INSTDIR"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\roomsports ${PRODUCT_NAME}" "Publisher" "${COMPANYNAME}"

  ; PATH erweitern, wenn VLC nicht gesetzt
  ReadEnvStr $2 PATH
  ${StrCase} $1 $2 "U"
  ${StrLoc} $0 $1 "VLC" ">"  ; Pfad in der Pathvariable gesetzt?
  IntCmp $0 0 is0 lessthan0 morethan0
is0:  
lessthan0:
  ${EnvVarUpdate} $0 "PATH" "A" "HKCU" "$PROGRAMFILES\VideoLAN\VLC" ; Append      
  goto Ende1
morethan0:
    ;MessageBox MB_ICONINFORMATION "$(STR_VLC_PATH)"  
Ende1:  
  MessageBox MB_ICONINFORMATION  "$(STR_ENDE)"
SectionEnd

;--------------------------------
;Installer Functions

Function .onInit
  !define MUI_LANGDLL_INFO "Bitte wählen sie eine Sprache / Please select a language:"
  !insertmacro MUI_LANGDLL_DISPLAY
;  Call CheckJRE64
FunctionEnd

;--------------------------------
; nsDialogsPages
;--------------------------------
Function nsDialogsPage
  nsDialogs::Create 1018
  Pop $Dialog

  ${If} $Dialog == error
	Abort
  ${EndIf}
	
  !insertmacro MUI_HEADER_TEXT "$(STR_OPT_HEADER1)" "$(STR_OPT_HEADER)"
  StrCpy $CbVLC_State "1"
  StrCpy $CbJava_State "0"
  StrCpy $CbDaum_State "1"
  StrCpy $CbKettler_State "1"
  StrCpy $CbTacx_State "1"
  StrCpy $CbTour_State "1"

  ${NSD_CreateCheckbox} 30 30u 100% 10u "$(STR_OPT_VLC)"
  Pop $CbVLC
;  ${NSD_CreateCheckbox} 30 40u 100% 10u "$(STR_OPT_JAVA)"
;  Pop $CbJava
  ${NSD_CreateCheckbox} 30 50u 100% 10u "$(STR_OPT_DAUM)"
  Pop $CbDaum
  ${NSD_CreateCheckbox} 30 60u 100% 10u "$(STR_OPT_KETT)"
  Pop $CbKettler
  ${NSD_CreateCheckbox} 30 70u 100% 10u "$(STR_OPT_TACX)"
  Pop $CbTacx
  ${NSD_CreateCheckbox} 30 80u 100% 10u "$(STR_OPT_TOUR)"
  Pop $CbTour

  ${NSD_SetState} $CbVLC $CbVLC_State
  ${NSD_SetState} $CbJava $CbJava_State
  ${NSD_SetState} $CbDaum $CbDaum_State
  ${NSD_SetState} $CbKettler $CbKettler_State
  ${NSD_SetState} $CbTacx $CbTacx_State
  ${NSD_SetState} $CbTour $CbTour_State

  nsDialogs::Show
FunctionEnd

Function nsDialogsPageLeave
  !insertmacro MUI_HEADER_TEXT "$(STR_INST_HEADER1)" "$(STR_INST_HEADER)"
  ${NSD_GetState} $CbVLC $CbVLC_State
  ${NSD_GetState} $CbJava $CbJava_State
  ${NSD_GetState} $CbDaum $CbDaum_State
  ${NSD_GetState} $CbKettler $CbKettler_State
  ${NSD_GetState} $CbTacx $CbTacx_State
  ${NSD_GetState} $CbTour $CbTour_State
	
  ;MessageBox MB_ICONINFORMATION  '"$(STR_AUSWAHL)" $Checkbox_State'
FunctionEnd


;--------------------------------
; GetVLC
;--------------------------------
;  Prüft, ob ein VLC installiert ist. Ist ein VLC vorhanden, wird die Versionsnummer ausgegeben 
;  und rückgefragt, ob der VLC aus VLC_URL drüberinstalliert werden soll.
Function GetVLC
    Push $R0
    Push $1
    Push $2

; ist ein VLC vorhanden?
    ClearErrors
    StrCpy $R0 "$PROGRAMFILES\VIDEOLAN\VLC\vlc.exe"
    IfFileExists $R0 VLCFound

DownloadVLC:
    Call ElevateToAdmin
    MessageBox MB_ICONINFORMATION "$(STR_VLC_DOWNLD)"
    ${if} $JAVA64 != ""
       StrCpy $2 "$TEMP\${VLC_64_INSTALLER}"
       nsisdl::download /TIMEOUT=30000 ${VLC_64_URL} $2
    ${else}
       StrCpy $2 "$TEMP\${VLC_INSTALLER}"
       nsisdl::download /TIMEOUT=30000 ${VLC_URL} $2
    ${endif}
    Pop $R0 ;Get the return value
    StrCmp $R0 "success" +3
     MessageBox MB_ICONSTOP "$(STR_DOWNLD_FAIL)"
     Abort

; InstallVLC:
    ${if} $JAVA64 != ""
       ExecWait $TEMP\${VLC_64_INSTALLER}
    ${else}
       ExecWait $TEMP\${VLC_INSTALLER}
    ${endif}
    Goto VLCEnde

VLCFound:    
    MessageBox MB_YESNO "$(STR_VLC_DOWNLD_QUEST)" IDNO DownloadVLC

VLCEnde:
;    ReadEnvStr $2 PATH
;    ${StrCase} $1 $2 "U"
;    ${StrLoc} $0 $1 "VLC" ">"  ; Pfad in der Pathvariable gesetzt?
;    IntCmp $0 0 is0 lessthan0 morethan0
;is0:  
;lessthan0:
;    ;MessageBox MB_ICONINFORMATION "$(STR_VLC_NO_PATH)"
;    ${EnvVarUpdate} $0 "PATH" "A" "HKCU" "$PROGRAMFILES\VideoLAN\VLC" ; Append      
;    goto VLCEnde1
;morethan0:
;    ;MessageBox MB_ICONINFORMATION "$(STR_VLC_PATH)"  
;VLCEnde1:
    Pop $2
    Pop $1
    Exch $R0        
FunctionEnd
 
;--------------------------------
; GetVCP
;--------------------------------
;  Fragt nach, ob der VCP-Treiber fuer CP210x Chip installiert werden soll.
Function GetVCP
    Push $R0
    Push $2

    MessageBox MB_YESNO "$(STR_KETT_QUEST)" IDNO VCPEnde

; InstallVCP:
    ExecWait $INSTDIR\${VCP_INSTALLER}

VCPEnde:
    Pop $2
    Exch $R0        
FunctionEnd

;--------------------------------
; GetJRE
;--------------------------------
;  returns the full path of a valid java.exe
;  looks in:
;  1 - .\jre directory (JRE Installed with application)
;  2 - JAVA_HOME environment variable
;  3 - the registry
;  4 - hopes it is in current dir or PATH
Function GetJRE
    Push $R0
    Push $R1
    Push $1
    Push $2
    Push $3
    Push $4

  ; 1) Check local JRE
    ClearErrors

    ${If} ${RunningX64}
        ;check for 64bit JRE on 64bit
        ${if} $JAVA64 != ""
            ;Messagebox MB_OK "$(STR_64BITJAVA_BREAK)"
            ;Abort
            StrCpy $R0 "$JAVA64\bin\${JAVAEXE}"
            IfFileExists $R0 JreFound
        ${endif}
        ;check for 32bit JRE on 64bit
        ReadRegStr $3 HKLM "SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment" "CurrentVersion"
        ReadRegStr $4 HKLM "SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment\$3" "JavaHome"
        ;Messagebox MB_OK "32Bit on 64Bit: 3: $3 / 4: $4"
        StrCpy $R0 "$4\bin\${JAVAEXE}"
        IfFileExists $R0 JreFound
    ${else}
        StrCpy $R0 "$EXEDIR\jre\bin\${JAVAEXE}"
        IfFileExists $R0 JreFound
    ${endif}  
 
  ; 2) Check for JAVA_HOME
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckRegistry     
    IfFileExists $R0 0 CheckRegistry
    Call CheckJREVersion
    IfErrors CheckRegistry JreFound
 
  ; 3) Check for registry
  CheckRegistry:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors DownloadJRE
    IfFileExists $R0 0 DownloadJRE
    Call CheckJREVersion
    IfErrors DownloadJRE JreFound
 
  DownloadJRE:
;    Call ElevateToAdmin
;    MessageBox MB_ICONINFORMATION "$(STR_INST_JAVA)"
;    StrCpy $2 "$TEMP\Java Runtime Environment.exe"
;    nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
;    Pop $R0 ;Get the return value
;    StrCmp $R0 "success" +3
;      MessageBox MB_ICONSTOP "$(STR_DOWNLD_FAIL)"
;    ExecWait $2
;    Delete $2	
;    inetc::get ${JRE_URL} $2 /END
 
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
;	MessageBox MB_OK "$R0"

    IfFileExists $R0 0 GoodLuck
    Call CheckJREVersion
    IfErrors GoodLuck JreFound
 
  ; 4) wishing you good luck
  GoodLuck:
    StrCpy $R0 "${JAVAEXE}"
    MessageBox MB_ICONSTOP "$(STR_INST_JAVA)"
    Abort

 
  JreFound:
    Pop $4
    Pop $3
    Pop $2
    Pop $1
    Pop $R1
    Exch $R0
FunctionEnd

;--------------------------------
; CheckJRE64
;-------------------------------- 
; kopiert den "javaw.exe" path nach $JAVA64 wenn ein 64 Bit Java installiert ist
; und kopiert die 64 Bit Version von RoomSports und der dlls.
Function CheckJRE64
    Push $1
    Push $2
 
    ${If} ${RunningX64}
        ;check for 64bit JRE on 64bit
        SetRegView 64
        ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
        ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
        ;Messagebox MB_OK "64Bit: 1: $1 / 2: $2"
        ${if} $2 != ""
            Messagebox MB_OK "$(STR_64BITJAVA)"
            StrCpy $JAVA64 "$2"
            ;Messagebox MB_OK $JAVA64
            StrCpy $INSTDIR "$PROGRAMFILES64\roomsports"
        ${endif}
    ${endif}  

    Pop $2
    Pop $1
FunctionEnd

;--------------------------------
; CheckJREVersion
;-------------------------------- 
; Pass the "javaw.exe" path by $R0
Function CheckJREVersion
    Push $R1

    ; Get the file version of javaw.exe
    ${GetFileVersion} $R0 $R1
    ${VersionCompare} ${JRE_VERSION} $R1 $R1
 
    ; Check whether $R1 != "1"
    ClearErrors
    StrCmp $R1 "1" 0 CheckDone
    SetErrors
 
  CheckDone:
    Pop $R1
FunctionEnd
 
;--------------------------------
; ElevateToAdmin
;--------------------------------
; Attempt to give the UAC plug-in a user process and an admin process.
Function ElevateToAdmin
  UAC_Elevate:
    UAC::RunElevated
    StrCmp 1223 $0 UAC_ElevationAborted ; UAC Dialog abgeborochen durch Benutzer?
    StrCmp 0 $0 0 UAC_Err ; Error?
    StrCmp 1 $1 0 UAC_Success ;Are we the real deal or just the wrapper?
    Quit
 
  UAC_ElevationAborted:
    # elevation was aborted, run as normal?
    MessageBox MB_ICONSTOP "$(STR_INST_ADMIN_ABBRUCH)"
    Abort
 
  UAC_Err:
    MessageBox MB_ICONSTOP "Unmï¿½glich fortzufahren, Error $0"
    Abort
 
  UAC_Success:
    StrCmp 1 $3 +4 ;Admin?
    StrCmp 3 $1 0 UAC_ElevationAborted ;Try again?
    MessageBox MB_ICONSTOP "$(STR_INST_ADMIN_RETRY)"
    goto UAC_Elevate 
FunctionEnd

;--------------------------------
; Lizenzheader zeigt die Überschrift 
; für die Lizenz an
;--------------------------------
Function Lizenzheader
  !insertmacro MUI_HEADER_TEXT "$(STR_LIZENZ_HEADER1)" "$(STR_LIZENZ_HEADER)"
FunctionEnd

;--------------------------------
; Dirheader zeigt die Überschrift 
; für das Zielverzeichnis an
;--------------------------------
Function Dirheader
  !insertmacro MUI_HEADER_TEXT "$(STR_DIR_HEADER1)" "$(STR_DIR_HEADER)"
FunctionEnd

Section "Uninstall"
  Messagebox MB_OK "$(STR_NODELETETOUREN)"
  Delete $INSTDIR\*.*
  RMDir $INSTDIR
  Delete $SMPROGRAMS\roomsports\*.*
  RMDir $SMPROGRAMS\roomsports
  Delete $APPDATA\roomsports\*.*
  RMDir $APPDATA\roomsports
  Delete "$DESKTOP\${PRODUCT_NAME}.lnk"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\roomsports ${PRODUCT_NAME}"
SectionEnd