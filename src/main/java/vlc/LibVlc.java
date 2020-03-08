package vlc;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.ptr.IntByReference;

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
 *****************************************************************************
 * LibVlc.java: Java-Bindings zur libvlc.dll
 *****************************************************************************
 *
 * Diese Klasse bildet die Schnittstelle zum VLC-Player.
 * Es wurden nur die benötigten Interfaces implementiert.  
 */
public interface LibVlc extends Library
{
	LibVlc INSTANCE = (LibVlc) Native.loadLibrary(Platform.isWindows()? "libvlc" : Platform.isMac()? "libvlc.dylib" : "libvlc.so.5", LibVlc.class);
    LibVlc SYNC_INSTANCE = (LibVlc) Native.synchronizedLibrary(INSTANCE);
    public int  libvlc_marquee_Enable = 0;
    public int  libvlc_marquee_Text = 1;
    public int  libvlc_marquee_Color = 2;
    public int  libvlc_marquee_Opacity = 3;
    public int  libvlc_marquee_Position = 4;
    public int  libvlc_marquee_Refresh = 5;
    public int  libvlc_marquee_Size = 6;
    public int  libvlc_marquee_Timeout = 7;
    public int  libvlc_marquee_X = 8;
    public int  libvlc_marquee_Y = 9;

    public int  libvlc_logo_Enable = 0;
    public int  libvlc_logo_File = 1;
    public int  libvlc_logo_X = 2;
    public int  libvlc_logo_Y = 3;
    public int  libvlc_logo_Delay = 4;
    public int  libvlc_logo_Repeat = 5;
    public int  libvlc_logo_Opacity = 6;
    public int  libvlc_logo_Position = 7;

    public static class libvlc_exception_t extends Structure
    {
        public int raised;
        public int code;
        public String message;
    }
  
    public static class libvlc_log_message_t extends Structure
    {
        public int sizeof_msg; /* sizeof() of message structure, must be filled in by user */
        public int i_severity; /* 0=INFO, 1=ERR, 2=WARN, 3=DBG */
        public String psz_type; /* module type */
        public String psz_name; /* module name */
        public String psz_header; /* optional header */
        public String psz_message; /* message */
    }

   
    public static class libvlc_event_t extends Structure
    {
        public int type;
        public Pointer obj;
        public event_type_specific event_type_specific;
    }

    public class media_meta_changed extends Structure
    {
        public Pointer meta_type;
    }

    public class media_subitem_added extends Structure
    {
        public LibVlcMediaDescriptor new_child;
    }

    public class media_duration_changed extends Structure
    {
        public NativeLong new_duration;
    }

    public class media_preparsed_changed extends Structure
    {
        public int new_status;
    }

    public class media_freed extends Structure
    {
        public LibVlcMediaDescriptor md;
    }

    public class media_state_changed extends Structure
    {
        public int new_state;
    }

    /* media instance */
    public class media_player_position_changed extends Structure
    {
        public float new_position;
    }

    public class media_player_time_changed extends Structure
    {
        public long new_time;
    }

    /* media list */
    public class media_list_item_added extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    public class media_list_will_add_item extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    public class media_list_item_deleted extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    public class media_list_will_delete_item extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    /* media list view */    
    public class media_list_view_item_added extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    public class media_list_view_will_add_item extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    public class media_list_view_item_deleted extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    public class media_list_view_will_delete_item extends Structure
    {
        public LibVlcMediaDescriptor item;
        public int index;
    }

    /* media discoverer */
    public class media_media_discoverer_started extends Structure
    {
        public Pointer unused;
    }

    public class media_media_discoverer_ended extends Structure
    {
        public Pointer unused;
    }

   public class event_type_specific extends Union
    {
        public media_meta_changed media_meta_changed;
        public media_subitem_added media_subitem_added;
        public media_duration_changed media_duration_changed;
        public media_preparsed_changed media_preparsed_changed;
        public media_freed media_freed;
        public media_state_changed media_state_changed;
        
        public media_player_position_changed media_player_position_changed;
        public media_player_time_changed media_player_time_changed;
        public media_list_item_added media_list_item_added;
        public media_list_will_add_item media_list_will_add_item;
        public media_list_item_deleted media_list_item_deleted;
        public media_list_will_delete_item media_list_will_delete_item;
    }

    public class LibVlcInstance extends PointerType
    {
    }

    public class LibVlcMediaDescriptor extends PointerType
    {
    }

    public class LibVlcMediaInstance extends PointerType
    {
    }

    public class LibVlcMediaList extends PointerType
    {
    }

    public class LibVlcMediaListPlayer extends PointerType
    {
    }

    public class LibVlcEventManager extends PointerType
    {
    }

    public class LibVlcLog extends PointerType
    {
    }

    public class LibVlcLogIterator extends PointerType
    {
    }

    // event manager
    public static interface LibVlcCallback extends Callback
    {
        void callback(libvlc_event_t libvlc_event, Pointer userData);
    }

    LibVlcInstance libvlc_new(int argc, String[] argv);
    void libvlc_release(LibVlcInstance instance);
    void libvlc_media_player_play(LibVlcMediaInstance mediaInstance);
    void libvlc_media_player_stop(LibVlcMediaInstance mediaInstance);
    void libvlc_media_player_pause(LibVlcMediaInstance mediaInstance);
    LibVlcMediaInstance libvlc_media_player_new(LibVlcInstance instance);
    void libvlc_media_player_release(LibVlcMediaInstance instance);
    LibVlcMediaInstance libvlc_media_player_new_from_media(LibVlcMediaDescriptor mediaDescriptor);
    LibVlcMediaDescriptor libvlc_media_new_location(LibVlcInstance instance, String mrl);
    LibVlcMediaDescriptor libvlc_media_new_path(LibVlcInstance instance, String mrl);
    void libvlc_media_player_set_media(LibVlcMediaInstance mediaInstance, LibVlcMediaDescriptor mediaDescriptor);
    void libvlc_media_release(LibVlcMediaDescriptor mediaDescriptor);
    LibVlcEventManager libvlc_media_player_event_manager(LibVlcMediaInstance mediaInstance);
    void libvlc_event_attach(LibVlcEventManager eventManager, int eventType, LibVlcCallback callback, Pointer userData);
    void libvlc_event_detach(LibVlcEventManager eventManager, int eventType, LibVlcCallback callback, Pointer userData);
    float libvlc_media_player_get_position(LibVlcMediaInstance instance);
    void libvlc_media_player_set_position(LibVlcMediaInstance instance, float position);
    
    float libvlc_media_player_get_rate(LibVlcMediaInstance mediaInstance);
    float libvlc_media_player_set_rate(LibVlcMediaInstance mediaInstance, float rate);
    
    void libvlc_video_set_marquee_string(LibVlcMediaInstance mediaInstance, int option, String text);
    void libvlc_video_set_marquee_int(LibVlcMediaInstance mediaInstance, int option, int wert);
    
    void libvlc_video_set_logo_int(LibVlcMediaInstance mediaInstance, int option, int wert);
    void libvlc_video_set_logo_string(LibVlcMediaInstance mediaInstance, int option, String text);
    
    void libvlc_media_player_set_hwnd(LibVlcMediaInstance mediaInstance, int drawable);
    int  libvlc_video_get_height(LibVlcMediaInstance mediaInstance); 
    int  libvlc_video_get_width(LibVlcMediaInstance mediaInstance); 
    int libvlc_video_get_size(LibVlcMediaInstance mediaInstance, int num, IntByReference px, IntByReference py);

    long libvlc_media_player_get_xwindow(LibVlcMediaInstance mediaInstance);
    void libvlc_media_player_set_xwindow(LibVlcMediaInstance mediaInstance, int drawable);
    void libvlc_media_player_set_nsobject(LibVlcMediaInstance mediaInstance, long drawable);
}
