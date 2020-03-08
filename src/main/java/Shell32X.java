import java.util.Arrays;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.win32.W32APIOptions;

/* 
 * This file is part of the RoomSports distribution 
 * (https://github.com/jobeagle/roomsports/roomsports.git).
 * 
 * Copyright (c) 2020 Bruno Schmidt (mail@roomsports.de).
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 * Shell32X.java: 
 * Diese Klasse beinhaltet das JNA-Interface für den ShellExecuteEx-Aufruf.
 * Er wird benötigt um in Windows UAC-Rechte zu bekommen (für Update).
 ******************************************************************************
 */
public interface Shell32X extends Shell32
{
    Shell32X INSTANCE = (Shell32X)Native.loadLibrary("shell32", Shell32X.class, W32APIOptions.UNICODE_OPTIONS);

    int SW_HIDE = 0;
    int SW_MAXIMIZE = 3;
    int SW_MINIMIZE = 6;
    int SW_RESTORE = 9;
    int SW_SHOW = 5;
    int SW_SHOWDEFAULT = 10;
    int SW_SHOWMAXIMIZED = 3;
    int SW_SHOWMINIMIZED = 2;
    int SW_SHOWMINNOACTIVE = 7;
    int SW_SHOWNA = 8;
    int SW_SHOWNOACTIVATE = 4;
    int SW_SHOWNORMAL = 1;

    /** File not found. */
    int SE_ERR_FNF = 2;
    /** Path not found. */
    int SE_ERR_PNF = 3;
    /** Access denied. */
    int SE_ERR_ACCESSDENIED = 5;
    /** Out of memory. */
    int SE_ERR_OOM = 8;
    /** DLL not found. */
    int SE_ERR_DLLNOTFOUND = 32;
    /** Cannot share an open file. */
    int SE_ERR_SHARE = 26;
    int SEE_MASK_NOCLOSEPROCESS = 0x00000040;

    int ShellExecute(int i, String lpVerb, String lpFile, String lpParameters, String lpDirectory, int nShow);
    boolean ShellExecuteEx(SHELLEXECUTEINFO lpExecInfo);



    public static class SHELLEXECUTEINFO extends Structure
    {
        public int cbSize = size();
        public int fMask;
        public HWND hwnd;
        public WString lpVerb;
        public WString lpFile;
        public WString lpParameters;
        public WString lpDirectory;
        public int nShow;
        public HINSTANCE hInstApp;
        public Pointer lpIDList;
        public WString lpClass;
        public HKEY hKeyClass;
        public int dwHotKey;
        public HANDLE hMonitor;
        public HANDLE hProcess;

        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {
                "cbSize", "fMask", "hwnd", "lpVerb", "lpFile", "lpParameters",
                "lpDirectory", "nShow", "hInstApp", "lpIDList", "lpClass",
                "hKeyClass", "dwHotKey", "hMonitor", "hProcess",
            });
        }
    }

}
