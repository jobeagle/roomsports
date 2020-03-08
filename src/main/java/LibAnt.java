import java.text.DecimalFormat;
import com.sun.jna.Callback;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

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
 * LibAnt.java: Die ANT+ Schnittstelle von RoomSports
 *****************************************************************************
 *
 * Diese Klasse beinhaltet die ANT+ Schnittstelle. Es wird per jna auf
 * ANT_DLL.dll zugegriffen.
 * mögliche Kanalkonfiguration (nur ein Gerät pro Kanal! = oder!):
 * Kanal 0:	Puls
 *          Trittfrequenz + Speed (GSC10) 
 *          TACX (bei TACX wird der Garminpuls über das Display ermittelt)
 *          Wahoo KICKR
 *          FE-C
 * Kanal 1: Puls
 *          Trittfrequenz + Speed (GSC10) 
 * Kanal 2: Trittfrequenz + Speed (GSC10) 
 * Kanal 3..4/8: aktuell nicht verwendet
 *  
 */

public class LibAnt extends Structure{
	public interface AntDll extends Library {
		interface Caller extends Callback {
			void callback (int channel, int msgId);
		}
		public interface CallerChannel extends Callback {
			void callback (byte channel, byte msgId);
		}
		public boolean ANT_AssignChannel(int channelNumber, int channelType, int networkNumber);
		public boolean ANT_Init1(int deviceNumber, int baudRate);
		public boolean ANT_SetChannelId(int channelNumber, int deviceNumber, int deviceType, int transmissionType);
		public boolean ANT_SetChannelPeriod(int channelNumber, int messagePeriod);
		public boolean ANT_SetChannelRFFreq(int channelNumber, int rfFreq);
		public boolean ANT_SetChannelSearchTimeout(int channelNumber, int searchTimeout);
		public boolean ANT_SetNetworkKey(int networkNumber, byte[] key);
		public boolean ANT_OpenChannel(int channelNumber);
		public boolean ANT_CloseChannel(int channelNumber);
		public boolean ANT_RequestMessage(int channelNumber, int messageId);
		public boolean ANT_ResetSystem();
		public boolean ANT_SendBroadcastData(int channelNumber, byte[] paket);
		public boolean ANT_SendAcknowledgedData(int channelNumber, byte[] paket);
		public boolean ANT_SendBurstTransfer(int channelNumber, byte[] paket, int anzPakete);
		public boolean ANT_AssignResponseFunction(Caller caller, PointerByReference buffer);
		public boolean ANT_AssignChannelEventFunction(int channel, CallerChannel caller, PointerByReference buffer);
		public void ANT_GetDeviceUSBPID();
		public void ANT_Close();
		public String ANT_LibVersion();
		//public boolean ANT_SendBurstTransferPacket();
		////////////////////////////////////////////

		public void ANT_GetDeviceUSBInfo();

		public void ANT_AddChannelID();
		public void ANT_ConfigList();
		public void ANT_SleepMessage();
		public void ANT_Nap();
		public boolean ANT_UnAssignChannel(byte channel);
	}

	public  AntDll lib = (AntDll) Native.loadLibrary("ANT_DLL", AntDll.class);
	public  byte ucChannelType = AntDefines.CHANNEL_TYPE_SLAVE;	// Slave

	private boolean debug = false;		// erweiterte Messageanzeige über Mlog.debug
	private boolean connectAP1 = false;
	private boolean connectAP2 = false;
	private boolean timeout = false;	// ANT-Kommunikation Timout?
	private boolean stop = false;		// beendet Thread
	public  boolean ein = false;		// ANT+ Protokoll ist eingeschaltet
	public  boolean response = false;	// ANT-Kommunikation Response - klappt Ansprechen vom USB-Stick?
	private boolean antWertChg = false;	// wenn true, dann muss neuer Wert per ANT+ (z. B.: Steigung, Watt) übermittelt werden!
	
	public  PointerByReference response_ptr = new PointerByReference();
	public  PointerByReference channel_ptr = new PointerByReference();	// Channel 0
	public  PointerByReference channel_ptr1 = new PointerByReference();	// Channel 1
	public  PointerByReference channel_ptr2 = new PointerByReference();	// Channel 2
	public  byte[] responseBuffer = new byte[AntDefines.MAX_RESPONSE_SIZE];
	public  byte[] channelBuffer = new byte[AntDefines.MAX_CHANNEL_EVENT_SIZE];
	//public  byte[] channelBuffer1 = new byte[AntDefines.MAX_CHANNEL_EVENT_SIZE];
	private int puls;
	//private int cadence;
	private byte page;
	private long optime;
	private int manId;
	private int serialNumberPuls;
	private int hwVersion;
	private int swVersion;
	private int modelNum;
	private int previousBeat;
	private int antMsgPeriod;
	private int currCadenceTime;
	private int prevCadenceTime;
	private int currCadRevCount;
	private int prevCadRevCount;
	private int currSpeedTime;
	private int prevSpeedTime;
	private int currSpeedRevCount;
	private int prevSpeedRevCount;
	private int countRpmTimer = 0;
	private int maxcountRpmTimer = 10;		// nach max. 10 Aufrufen der Trittauswertung mit gleichen Werten wird RPM auf 0 gesetzt
	private byte antFreq;
	private int	dispDevType;	
    public static enum TDev { notset, tacxvortex, tacxbushido, manuell, wahookickr, antfec };	// ANT+ Trainingsdevice
    private TDev aktTDev = TDev.notset;
    public static enum THr { notset, antppuls };											// Pulser
    private THr  aktTHr = THr.notset;
    public static enum TCadence { notset, antpcadence, antpcadencespeed };					// Trittfrequenz-Sensor
    private TCadence aktTCadence = TCadence.notset;
    private byte dispCmd;
    private byte dispMode;
    private byte dispStatus;
    private int ispeed;
    private int power;
    private int rpm;
    private double speed;
    private int slope;
    private String devSNR = new String("0");
	private DecimalFormat zf00 = new DecimalFormat("00"); 
	private String errorMsg;
	private double radumfang = 2.22;	// Radumfang bei 27.5" - 57-584
	
	// zusätzliche Variablendefinitionen für Wahoo KICKR
	private int lastwheelEventCount = 0;
	private int lastaccumPeriod = 0;
	private int diffwheelEventCount;
	private int diffaccumPeriod;
	private byte antCmdIndex = 0;
	private int vorgabePower = 0;

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}


	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}


	/**
	 * @return the timeout
	 */
	public  boolean isTimeout() {
		return timeout;
	}


	/**
	 * @param timeout to set
	 */
	public  void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * @return the response
	 */
	public boolean isResponse() {
		return response;
	}


	/**
	 * @param response the response to set
	 */
	public void setResponse(boolean response) {
		this.response = response;
	}

	/**
	 * @return the ein
	 */
	public boolean isEin() {
		return ein;
	}


	/**
	 * @param ein the ein to set
	 */
	public void setEin(boolean ein) {
		this.ein = ein;
	}

	/**
	 * @return the stop
	 */
	public boolean isStop() {
		return stop;
	}


	/**
	 * @param stop the stop to set
	 */
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	/**
	 * @return the puls
	 */
	public int getPuls() {
		return puls;
	}


	/**
	 * @param puls the puls to set
	 */
	public void setPuls(int puls) {
		this.puls = puls;
	}


	/**
	 * @return the page
	 */
	public byte getPage() {
		return page;
	}


	/**
	 * @param page the page to set
	 */
	public void setPage(byte page) {
		this.page = page;
	}


	/**
	 * @return the optime
	 */
	public long getOptime() {
		return optime;
	}


	/**
	 * @param optime the optime to set
	 */
	public void setOptime(long optime) {
		this.optime = optime;
	}


	/**
	 * @return the manId
	 */
	public int getManId() {
		return manId;
	}


	/**
	 * @param manId the manId to set
	 */
	public void setManId(int manId) {
		this.manId = manId;
	}


	/**
	 * @return the serialNumber
	 */
	public int getSerialNumber() {
		return serialNumberPuls;
	}


	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(int serialNumber) {
		this.serialNumberPuls = serialNumber;
	}


	/**
	 * @return the hwVersion
	 */
	public int getHwVersion() {
		return hwVersion;
	}


	/**
	 * @param hwVersion the hwVersion to set
	 */
	public void setHwVersion(int hwVersion) {
		this.hwVersion = hwVersion;
	}


	/**
	 * @return the swVersion
	 */
	public int getSwVersion() {
		return swVersion;
	}


	/**
	 * @param swVersion the swVersion to set
	 */
	public void setSwVersion(int swVersion) {
		this.swVersion = swVersion;
	}


	/**
	 * @return the modelNum
	 */
	public int getModelNum() {
		return modelNum;
	}


	/**
	 * @param modelNum the modelNum to set
	 */
	public void setModelNum(int modelNum) {
		this.modelNum = modelNum;
	}


	/**
	 * @return the previousBeat
	 */
	public int getPreviousBeat() {
		return previousBeat;
	}


	/**
	 * @param previousBeat the previousBeat to set
	 */
	public void setPreviousBeat(int previousBeat) {
		this.previousBeat = previousBeat;
	}

	/**
	 * @return the antMsgPeriod
	 */
	public int getAntMsgPeriod() {
		return antMsgPeriod;
	}


	/**
	 * @param antMsgPeriod the antMsgPeriod to set
	 */
	public void setANTMsgPeriod(int antMsgPeriod) {
		this.antMsgPeriod = antMsgPeriod;
	}


	/**
	 * @return the antFreq
	 */
	public byte getANTFreq() {
		return antFreq;
	}


	/**
	 * @param antFreq the antFreq to set
	 */
	public void setANTFreq(byte antFreq) {
		this.antFreq = antFreq;
	}


	/**
	 * @return the dispDevType
	 */
	public int getDispDevType() {
		return dispDevType;
	}


	/**
	 * @param dispDevType the dispDevType to set
	 */
	public void setDispDevType(int dispDevType) {
		this.dispDevType = dispDevType;
	}

	/**
	 * @return the aktdevice
	 */
	public TDev getaktTDev() {
		return aktTDev;
	}


	/**
	 * @param aktdevice the aktdevice to set
	 */
	public void setaktTDev(TDev aktdevice) {
		this.aktTDev = aktdevice;
	}


	/**
	 * @return the aktHr
	 */
	public THr getaktTHr() {
		return aktTHr;
	}


	/**
	 * @param aktTHr the aktTHr to set
	 */
	public void setaktTHr(THr aktTHr) {
		this.aktTHr = aktTHr;
	}

	/**
	 * @return the aktTCadence
	 */
	public TCadence getaktTCadence() {
		return aktTCadence;
	}


	/**
	 * @param aktTCadence the aktTCadence to set
	 */
	public void setaktTCadence(TCadence aktTCadence) {
		this.aktTCadence = aktTCadence;
	}


	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}


	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}


	/**
	 * @return the power
	 */
	public int getPower() {
		return power;
	}


	/**
	 * @param power the power to set
	 */
	public void setPower(int power) {
		this.power = power;
	}


	/**
	 * @return the rpm
	 */
	public int getRpm() {
		return rpm;
	}


	/**
	 * @param rpm the rpm to set
	 */
	public void setRpm(int rpm) {
		this.rpm = rpm;
	}


	/**
	 * @return antWertChg
	 */
	public boolean isantWertChg() {
		return antWertChg;
	}


	/**
	 * @param antWertChg der gesetzt werden soll
	 */
	public void setantWertChg(boolean antWertChg) {
		this.antWertChg = antWertChg;
	}


	/**
	 * @return the slope
	 */
	public int getSlope() {
		return slope;
	}


	/**
	 * @param slope the slope to set
	 */
	public void setSlope(int slope) {
		this.slope = slope;
	}


	/**
	 * @return the devSNR
	 */
	public String getDevSNR() {
		return devSNR;
	}


	/**
	 * @param devSNR the devSNR to set
	 */
	public void setDevSNR(String devSNR) {
		this.devSNR = devSNR;
	}


	/**
	 * @return the vorgabePower
	 */
	public int getVorgabePower() {
		return vorgabePower;
	}


	/**
	 * @param vorgabePower the vorgabePower to set
	 */
	public void setVorgabePower(int vorgabePower) {
		this.vorgabePower = vorgabePower;
	}


	/**
	 * Callback für die Kanaldaten - Auswertung kanalunabhängig
	 */
	private void fnChannelAuswertung(byte channel, byte ucEvent_) {
		byte b1,b2,b3,b4;
		// benötigt für asynchronen Aufruf der Fehlermeldung
		Runnable rError = new Runnable() {
			public void run() {
				if (errorMsg != null)
					Messages.errormessage(errorMsg);	
				// TODO: testen, ob das Open hier wirklich nötig ist!
				Rsmain.thisTrainer.antopen();
			}
		};

		if(debug) {
			Mlog.debug("*** Data Dump: (" +
					channel + "): " +
					Integer.toHexString(channelBuffer[0] & 0xff)+" "+
					Integer.toHexString(channelBuffer[1] & 0xff)+" "+
					Integer.toHexString(channelBuffer[2] & 0xff)+" "+
					Integer.toHexString(channelBuffer[3] & 0xff)+" "+
					Integer.toHexString(channelBuffer[4] & 0xff)+" "+
					Integer.toHexString(channelBuffer[5] & 0xff)+" "+
					Integer.toHexString(channelBuffer[6] & 0xff)+" "+
					Integer.toHexString(channelBuffer[7] & 0xff)+" "+
					Integer.toHexString(channelBuffer[8] & 0xff));
		} 

		switch(ucEvent_) {
		case AntDefines.EVENT_TX: {
			// This event indicates that a message has just been
			// sent over the air. We take advantage of this event to set
			// up the data for the next message period.
			byte ucIncrement = 0;      // Increment the first byte of the buffer

			channelBuffer[0] = ucIncrement++;

			if(debug) {
				// Echo what the data will be over the air on the next message period.
				Mlog.debug(">>> Tx: - wird anschl. gesendet!(" +
						channel + "): " +
						Integer.toHexString(channelBuffer[0] & 0xff)+" "+
						Integer.toHexString(channelBuffer[1] & 0xff)+" "+
						Integer.toHexString(channelBuffer[2] & 0xff)+" "+
						Integer.toHexString(channelBuffer[3] & 0xff)+" "+
						Integer.toHexString(channelBuffer[4] & 0xff)+" "+
						Integer.toHexString(channelBuffer[5] & 0xff)+" "+
						Integer.toHexString(channelBuffer[6] & 0xff)+" "+
						Integer.toHexString(channelBuffer[7] & 0xff)+" "+
						Integer.toHexString(channelBuffer[8] & 0xff)+"\n");
			}
			break;
		}
		case AntDefines.EVENT_RX_FLAG_ACKNOWLEDGED:
		case AntDefines.EVENT_RX_FLAG_BURST_PACKET:
		case AntDefines.EVENT_RX_FLAG_BROADCAST: {
			// This is an event generated by the DLL. It is not strictly
			// returned by the ANT part. To enable, must call ANT_RxExtMesgsEnable first.
			// This event has a flag at the end of the data payload
			// that indicates additional information. Process this here
			// and then fall-through to process the data payload.
			//byte ucFlag = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA10_INDEX];
		}
		case AntDefines.EVENT_RX_ACKNOWLEDGED:
		case AntDefines.EVENT_RX_BURST_PACKET:
		case AntDefines.EVENT_RX_BROADCAST: {
			switch(channel) {
			case AntDefines.CHANNEL_0: { // Channel 0 fuer HRM Daten / ANT+ Trainingsgerät / Cadence
				if (debug)
					Mlog.debug("Channel 0 Broadcast!");
				if (aktTHr == THr.antppuls && aktTDev == TDev.notset) {	// GARMIN Pulser auswerten
					garminPulsauswertung();
					break;
				} 
				if (aktTCadence != TCadence.notset && aktTDev == TDev.notset) {	// ANT+ Cadence oder Cadence/Speed
					garminTrittauswertung();
					break;
				} 
				if (aktTDev == TDev.tacxvortex || aktTDev == TDev.tacxbushido) {
						dispCmd = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX];
						dispMode = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX];
						dispStatus = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
						//Mlog.debug("TACX Kommando = " + dispCmd);
						//Mlog.debug("TACX Mode = " + dispMode);
						if (dispCmd == (byte) 0xAD) {
							// TACX-SNR vom Display ablegen:
							b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
							b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
							int hSnr = (int) ((b2 < 0) ? 256 + b2 : b2) | ((b1 < 0) ? 256 + b1 : b1) << 8;
							setDevSNR("" + channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX] + 
									channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX] +
									zf00.format(channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX])+hSnr);
						}
						if (dispCmd == (byte) 0xAD && dispStatus == 4) {
							// send Switch from pause to acquisition (mode 2): AC 03 02 00 00 00 00 00 
							byte [] paket = { (byte) 0xAC, 0x03, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00};
							lib.ANT_SendBroadcastData(0, paket);
						} else if (dispCmd == (byte) 0xAD && dispStatus != 4 && dispStatus != 2) {
							// send init PC-Connection: AC 03 04 00 00 00 00 00
							byte [] paket = { (byte) 0xAC, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00};
							lib.ANT_SendBroadcastData(0, paket);
						} else if (antWertChg) {
							// sende Slope (Steigung): DC 01 00 ZZ XX 4d 00 00
							// If ZZ = 00 then: SLOPE = XX/10; If ZZ = FF then: SLOPE = (XX-256)/10 (Not 255!)
							slope = (slope < 0) ? 0 : slope;
							slope = (slope > 200) ? 200 : slope;
							byte bslope = (byte) slope;
							if (bslope < 0)
								bslope = (byte) (256 + bslope);
							byte [] paket = { (byte) 0xDC, 0x01, 0x00, 0x00, bslope, 0x4d, 0x00, 0x00}; // evtl. muss ZZ auf FF gesetzt werden?
							
							lib.ANT_SendBroadcastData(0, paket);
							antWertChg = false;
						} else {
							// sende immer DC 02 00 99 00 00 00 00
							byte [] paket = { (byte) 0xDC, 0x02, 0x00, (byte) 0x99, 0x00, 0x00, 0x00, 0x00};
							lib.ANT_SendBroadcastData(0, paket);
						}
						if (dispCmd == (byte) 0xDD) {	
							switch (dispMode) {
							case (1): {	// DD 01 SS SS PP PP CC UU: (S) = Speed (kmh) * 10, (P) = Power (W), (C) = Cadence (RPM) 
								b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
								b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];
								ispeed = (int) ((b2 < 0) ? 256 + b2 : b2) | ((b1 < 0) ? 256 + b1 : b1) << 8;
								setSpeed(ispeed / 10.0);
								b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
								b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
								power = (int) ((b2 < 0) ? 256 + b2 : b2) | ((b1 < 0) ? 256 + b1 : b1) << 8;
								rpm = (int) channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
								rpm = (rpm < 0) ? 256 + rpm : rpm;
								break;
							}
							case (2): { // DD 02 DD DD DD DD HH UU: D = Distance (m), H = Heart Rate (bpm)
								puls = (int) channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
								//Mlog.debug("Puls Tacx = " + puls);
								puls = (puls < 0) ? 256 + puls : puls;
								break;
							}
							case (3): { // DD 03 00 00 TT 00 00 00: T = Brake temperature (degrees C)? 
								break;
							}
							}
						}
				} else
						if (aktTDev == TDev.wahookickr) {
							page = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX];
							switch (page) {
							case AntDefines.WK_PAGE_POWER:
								//Mlog.debug("*** wk: Power = " + page);
			                    int powEventCount = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX];		// Power event count
			                    powEventCount = (powEventCount < 0) ? 256 + powEventCount : powEventCount;
			                    //byte pedalPower = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];			// Pedal Power
			                    //byte cadence = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];			// Cadence
			                    //usAccumPower = (ushort)(pucBuffer[4] | pucBuffer[5] << 8);	// Cumulative power (W)
								b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
								b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
								//int accumPower = (int) (((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8);	// Cumulative power (W)
			                    //usPower = (ushort)(pucBuffer[6] | pucBuffer[7] << 8);		// Instantaneous power (W)
								b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
								b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
								power = (int) (((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8);	// Instantaneous power (W)
								break;

							case AntDefines.WK_PAGE_WHEEL_TORQUE:
								//Mlog.debug("*** wk: Wheel = " + page);
			                    int wheelEventCount = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX];		// Wheel Event count
			                    wheelEventCount = (wheelEventCount < 0) ? 256 + wheelEventCount : wheelEventCount;
			                    //byte wheelTicks = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];			// Wheel revolutions?
			                    //byte cadence1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];			// Cadence
								b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
								b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
								int accumPeriod = (int) (((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8);	// Cumulative wheel period (1/2048 s)
								b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
								b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
								if (lastwheelEventCount != wheelEventCount) {
									// auf Überlauf prüfen:
									wheelEventCount = (wheelEventCount < lastwheelEventCount) ? wheelEventCount + 256 : wheelEventCount;
									accumPeriod = (accumPeriod < lastaccumPeriod) ? accumPeriod + 65536 : accumPeriod;
									diffaccumPeriod = accumPeriod - lastaccumPeriod;
									diffwheelEventCount = wheelEventCount - lastwheelEventCount;
									lastwheelEventCount = wheelEventCount;
									lastaccumPeriod = accumPeriod;
								}
								if (diffaccumPeriod > 0) {
									if (power > 0) {
										// Raddurchmesser = 2090 mm:
										// speed = 3.6 * 2048 * 2.09 * diffwheelEventCount / diffaccumPeriod;
										setSpeed(15409.152 * diffwheelEventCount / diffaccumPeriod);
										//Mlog.debug("wk: speed = " + speed);
									} else
										setSpeed(0.0);
								}
								break;

							case AntDefines.WK_PAGE_KICKR_RESPONSE:
								//Mlog.debug("*** Response! ");
								break;

							case AntDefines.WK_PAGE_KICKR_81:
								//int sWVersion = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];
								b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
								b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
								b3 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
								b4 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
								int sNr = (int) (((b1 < 0) ? 256 + b1 : b1) | 
												(((b2 < 0) ? 256 + b2 : b2) << 8) |
												(((b3 < 0) ? 256 + b3 : b3) << 16) |
												(((b4 < 0) ? 256 + b4 : b4) << 24));
								setDevSNR(new Integer(sNr).toString());
			                    break;

							default:
								Mlog.debug("KICKR: nicht dekodiert: page = " + page);
								break;
							}
							if (antWertChg) {
								if (getVorgabePower() > 0) {
									sendKICKRErgModePower(getVorgabePower());
								}								
								antWertChg = false;
							}
				} else
							if (aktTDev == TDev.antfec) {
								page = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX];
								switch (page) {
								case AntDefines.ANTFEC_PAGE_GENERAL:
									b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
									b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
									double spd = (int) (((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8);
									setSpeed(spd * 0.0036);
									// Meldung "Rollentrainer meldet sich nicht" unterdrücken
									if (getDevSNR().equals("0"))
										setDevSNR("ANT+FEC");
									break;
									
								case AntDefines.ANTFEC_PAGE_SPECIFIC:
									if (aktTCadence == TCadence.notset) {
										rpm = (int) channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
										rpm = (rpm < 0) ? 256 + rpm : rpm;
										rpm = (rpm == 255) ? 0 : rpm;
									}
									b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];					// LSB
									b2 = (byte) (channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX] & 0x0F);	// MSB nur Bit 0..3
									power = (int) (((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8);	// Instantaneous power (W)
									break;
									
								case AntDefines.ANTFEC_PAGE_PRODID:
									b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
									b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
									b3 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
									b4 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
									long sNr = (long) (((b1 < 0) ? 256 + b1 : b1) | 
													(((b2 < 0) ? 256 + b2 : b2) << 8) |
													(((b3 < 0) ? 256 + b3 : b3) << 16) |
													(((b4 < 0) ? 256 + b4 : b4) << 24));
									sNr = (sNr < 0) ? 4294967296l + sNr : sNr;	// 256^4
									setDevSNR(new Long(sNr).toString());
									if (debug)
										Mlog.debug("ANT+FE-C Produkt Info(0x51)(" +
											channel + "): " +
											Integer.toHexString(channelBuffer[1] & 0xff)+" "+
											Integer.toHexString(channelBuffer[2] & 0xff)+" "+
											Integer.toHexString(channelBuffer[3] & 0xff)+" "+
											Integer.toHexString(channelBuffer[4] & 0xff)+" "+
											Integer.toHexString(channelBuffer[5] & 0xff)+" "+
											Integer.toHexString(channelBuffer[6] & 0xff)+" "+
											Integer.toHexString(channelBuffer[7] & 0xff)+" "+
											Integer.toHexString(channelBuffer[8] & 0xff)+"\n");
									break;
									
//								case AntDefines.ANTFEC_PAGE_BIKE_STAT:
//									b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
//									b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
//									power = (int) (((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8);	// Instantaneous power (W)
//									break;
								
								case AntDefines.ANTFEC_PAGE_GENSET:
									// aktuell nur Debugausgabe
									if (debug)
										Mlog.debug("ANT+FE-C General Settings (0x11)(" +
											channel + "): " +
											Integer.toHexString(channelBuffer[1] & 0xff)+" "+
											Integer.toHexString(channelBuffer[2] & 0xff)+" "+
											Integer.toHexString(channelBuffer[3] & 0xff)+" "+
											Integer.toHexString(channelBuffer[4] & 0xff)+" "+
											Integer.toHexString(channelBuffer[5] & 0xff)+" "+
											Integer.toHexString(channelBuffer[6] & 0xff)+" "+
											Integer.toHexString(channelBuffer[7] & 0xff)+" "+
											Integer.toHexString(channelBuffer[8] & 0xff)+"\n");
									
									break;
									
								default:
									Mlog.debug("ANT+ FE-C: nicht dekodiert: page = " + page);
									break;
								}
							}
				break;
			}
			case AntDefines.CHANNEL_1: { // Channel 1 aktuell nur für Garmin Pulser und Trittfrequenz
				if (debug)
					Mlog.debug("Channel 1 Broadcast!");
				if (aktTHr == THr.antppuls && aktTDev != TDev.notset) {
					garminPulsauswertung();
					break;
				} 
				if (aktTCadence != TCadence.notset) {
					// Ant+ Cadence oder Combined Cadence and speed Sensor
					garminTrittauswertung();
				} 
				break;
			}
			case AntDefines.CHANNEL_2: { // Channel 2 aktuell nur für Garmin Trittfrequenz
				if (debug)
					Mlog.debug("Channel 2 Broadcast!");
				if (aktTCadence != TCadence.notset) {
					// Ant+ Cadence oder Combined Cadence and speed Sensor
					garminTrittauswertung();
				} 
				break;				
			}
			case AntDefines.CHANNEL_3: { // Channel 3 aktuell nicht benutzt.
				if (debug)
					Mlog.debug("Channel 3 Broadcast!");
				break;				
			}
			}
			break;
		}
		case -98:
		case -97:
		case -96: {
			// This is an event generated by the DLL. It is not strictly
			// returned by the ANT part. To enable, must call ANT_RxExtMesgsEnable first.

			// The "extended" part of this message is the 4-byte channel
			// id of the device that we recieved this message from. This event
			// is only available on the AT3. The AP2 uses the EVENT_RX_FLAG_BROADCAST
			// as shown above.

			// Channel ID of the device that we just recieved a message from.
			int usDeviceNumber = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX] | (channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX] << 8);
			byte ucDeviceType =  channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
			byte ucTransmissionType = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];

			//bPrintBuffer = true;
			//ucDataOffset = AntDefines.MESSAGE_BUFFER_DATA6_INDEX;   // For most data messages

			if(debug) {
				// Display the channel id
				Mlog.debug("ANT+ Channel ID: "+ usDeviceNumber+"/"+ucDeviceType+"/"+ucTransmissionType );

				if(ucEvent_ == AntDefines.EVENT_RX_EXT_ACKNOWLEDGED)
					Mlog.debug("ANT+  -Acked Rx: "+ channelBuffer[AntDefines.MESSAGE_BUFFER_DATA1_INDEX]);
				else if(ucEvent_ == AntDefines.EVENT_RX_EXT_BURST_PACKET)
					;// Mlog.debug("- Burst(0x%02x) Rx:(%d): ", ((aucChannelBuffer[AntDefines.MESSAGE_BUFFER_DATA1_INDEX] & 0xE0) >> 5), aucChannelBuffer[AntDefines.MESSAGE_BUFFER_DATA1_INDEX] & 0x1F );
				else
					Mlog.debug("ANT+  -Rx: "+ channelBuffer[AntDefines.MESSAGE_BUFFER_DATA1_INDEX]);
			}

			break;
		}
		case AntDefines.EVENT_RX_SEARCH_TIMEOUT: {
			if (debug)
				Mlog.debug("ANT+ Search Timeout! channel: "+channel);
			// Nur auf Channel 0 haben wir ein Trainingsgerät zur Prüfung:
			if (channel == AntDefines.CHANNEL_0 && aktTHr != THr.antppuls)
				errorMsg = Messages.getString("Rsmain.ant_meldet_nicht");											
			else
				errorMsg = null;
			Rsmain.display.asyncExec(rError);
			break;
		}
		case AntDefines.EVENT_RX_FAIL: {
			if (debug)
				Mlog.debug("ANT+ Rx Fail! channel: "+ channel);
			break;
		}
		case AntDefines.EVENT_TRANSFER_RX_FAILED: {
			if (debug)
				Mlog.debug("ANT+ Burst receive failed! channel: "+ channel);
			break;
		}
		case AntDefines.EVENT_TRANSFER_TX_COMPLETED: {
			if (debug)
				Mlog.debug("ANT+ Transfer abgeschlossen");
			break;
		}
		case AntDefines.EVENT_TRANSFER_TX_FAILED: {
			if (debug)
				Mlog.debug("ANT+ Transferfehler");
			break;
		}
		case AntDefines.EVENT_CHANNEL_CLOSED: {
			// This event should be used to determine that the channel is closed.
			if (debug)
				Mlog.debug("ANT+ Channel closed, unassign channel");
			lib.ANT_UnAssignChannel(channel /*AntDefines.USER_ANTCHANNEL*/);
			break;
		}
		case AntDefines.EVENT_RX_FAIL_GO_TO_SEARCH: {
			if (debug)
				Mlog.debug("ANT+ Goto search.");
			break;
		}
		case AntDefines.EVENT_CHANNEL_COLLISION: {
			if (debug)
				Mlog.debug("ANT+ Channel Collision!");
			break;
		}
		case AntDefines.EVENT_TRANSFER_TX_START: {
			if (debug)
				Mlog.debug("ANT+ Burst started");
			break;
		}
		default: {
			Mlog.error("ANT+ Unknown channel: "+channel+" - event: "+ucEvent_);
			break;
		}
		}
	}

	/**
	 * Auswertung des Garmin Pulsers
	 */
	private void garminPulsauswertung() {
		byte b1,b2,b3;
		page = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX];
		if (debug)
			Mlog.debug("Pulsauswertung...");
		switch (page & ~AntDefines.TOGGLE_MASK) { //check the new pages and remove the toggle bit, TOGGLE_MASK = 0x80
		case AntDefines.GARMINPULS_PAGE_1: {
			if (debug)
				Mlog.debug("Puls Page 1");
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
			b3 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];
			optime = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8  | ((b3 < 0) ? 256 + b3 : b3) << 16;
			optime *= 2;	// Ink.rement alle 2 Sekunden
			break;
		}
		case AntDefines.GARMINPULS_PAGE_2: {
			if (debug)
				Mlog.debug("Puls Page 2");
			manId = (int) channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX];
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];
			serialNumberPuls = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
			break;
		}
		case AntDefines.GARMINPULS_PAGE_3: {
			if (debug)
				Mlog.debug("Puls Page 3");
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX];
			hwVersion = (int) ((b1 < 0) ? 256 + b1 : b1);
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
			swVersion = (int) ((b1 < 0) ? 256 + b1 : b1);
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];									
			modelNum = (int) ((b1 < 0) ? 256 + b1 : b1);
			break;
		}
		case AntDefines.GARMINPULS_PAGE_4: {
			if (debug)
				Mlog.debug("Puls Page 4");
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];
			previousBeat = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
			break;
		}
		}
		// siehe http://www.thisisant.com/images/Resources/PDF/ANT_Device_Profile_Heart_Rate_Monitor.pdf
		// (am Ende des pdf)
		// Bei HR, wenn sich erstes Bit ändert, soll Puls ermittelt werden!
		// 8.7.2014 Bit-Toggle passiert nicht mehr bei meinem Pulse ?!
		//if ((lastpage & AntDefines.TOGGLE_MASK) != (page & AntDefines.TOGGLE_MASK)) {
			puls = (int) channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
			puls = (puls < 0) ? 256 + puls : puls;
		//}
		//lastpage = page;	
	}
	
	/**
	 * Auswertung der Garmin Trittfrequenz
	 * The combined bike speed and cadence data format was one of the first defined ANT+ message formats and 
	 * does not conform to the standard ANT+ message definition. The combined bike speed and cadence sensor does 
	 * not have the ability to use a data paging system and therefore only has one data format (i.e. page) 
	 * that can be used.
	 * Später erweitert um reinen Cadence-Sensor (TCadence.antpcadence)
	 */
	private void garminTrittauswertung() {
		byte b1,b2;
		int divisor;

		if (debug)
			Mlog.debug("Trittauswertung...");
		if (aktTCadence == TCadence.antpcadencespeed) {
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX];
			currCadenceTime = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];
			currCadRevCount = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
			currSpeedTime = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
			currSpeedRevCount = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
		}
		if (aktTCadence == TCadence.antpcadence) {
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA6_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA7_INDEX];
			currCadenceTime = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
			b1 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA8_INDEX];
			b2 = channelBuffer[AntDefines.MESSAGE_BUFFER_DATA9_INDEX];
			currCadRevCount = (int) ((b1 < 0) ? 256 + b1 : b1) | ((b2 < 0) ? 256 + b2 : b2) << 8;
		}
		//Mlog.debug("prevCadRevCount: "+prevCadRevCount);
		//Mlog.debug("currCadRevCount: "+currCadRevCount);
		//Mlog.debug("prevCadenceTime: "+prevCadenceTime);
		//Mlog.debug("currCadenceTime: "+currCadenceTime);
		if (prevCadRevCount > 0 && currCadenceTime > prevCadenceTime) {
			divisor = currCadenceTime - prevCadenceTime;
			setRpm((int) (currCadRevCount - prevCadRevCount) * 61440 / divisor);
			if (debug)
				Mlog.debug("getRPM: "+getRpm());
			countRpmTimer = 0;
		} else {
			countRpmTimer++;
			if (countRpmTimer > maxcountRpmTimer) {
				countRpmTimer = 0;
				setRpm(0);
			}
		}
		prevCadRevCount = currCadRevCount;
		prevCadenceTime = currCadenceTime;
			//Mlog.debug("prevSpeedRevCount: "+prevSpeedRevCount);
			//Mlog.debug("currSpeedRevCount: "+currSpeedRevCount);
			//Mlog.debug("prevSpeedTime: "+prevSpeedTime);
			//Mlog.debug("currSpeedTime: "+currSpeedTime);
		if (aktTCadence == TCadence.antpcadencespeed) {
			if (aktTDev != TDev.tacxbushido && aktTDev != TDev.tacxvortex && aktTDev != TDev.wahookickr) {
				if (prevSpeedRevCount > 0 && currSpeedTime > prevSpeedTime) {
					divisor = currSpeedTime - prevSpeedTime;
					setSpeed((currSpeedRevCount - prevSpeedRevCount) * 3686.4 * radumfang / divisor);		// 3.6 * 1024 = 3686,4	
				} else {
					if (countRpmTimer > maxcountRpmTimer) 
						setSpeed(0.0);
				}
				prevSpeedRevCount = currSpeedRevCount;
				prevSpeedTime = currSpeedTime;	
			}
		}
	}

	/**
	 * Callback für die Kanaldaten Channel 0
	 */
	public   AntDll.CallerChannel fnChannel = new AntDll.CallerChannel(){
		public void callback(byte channel, byte ucEvent_) {	
			channelBuffer = channel_ptr.getPointer().getByteArray(0,AntDefines.MAX_CHANNEL_EVENT_SIZE);
			if (debug)
				Mlog.debug("Channel-0 callback, ucEvent = " + "0x" + Integer.toHexString(ucEvent_ & 0xff) + " Channel: " + channel);
			fnChannelAuswertung(channel, ucEvent_);
		}
	};

	public static CallbackThreadInitializer callbackThreadInitializer = new  CallbackThreadInitializer();

	/**
	 * Callback für die Kanaldaten Channel 1
	 */
	public   AntDll.CallerChannel fnChannel1 = new AntDll.CallerChannel(){
		public void callback(byte channel, byte ucEvent_) {			
			channelBuffer = channel_ptr1.getPointer().getByteArray(0,AntDefines.MAX_CHANNEL_EVENT_SIZE);
			if (debug)
				Mlog.debug("Channel-1 callback, ucEvent = " + "0x" + Integer.toHexString(ucEvent_ & 0xff) + " Channel: " + channel);
			fnChannelAuswertung(channel, ucEvent_);
		}
	};

	/**
	 * Callback für die Kanaldaten Channel 2
	 */
	public   AntDll.CallerChannel fnChannel2 = new AntDll.CallerChannel(){
		public void callback(byte channel, byte ucEvent_) {			
			channelBuffer = channel_ptr2.getPointer().getByteArray(0,AntDefines.MAX_CHANNEL_EVENT_SIZE);
			if (debug)
				Mlog.debug("Channel-2 callback, ucEvent = " + "0x" + Integer.toHexString(ucEvent_ & 0xff) + " Channel: " + channel);
			fnChannelAuswertung(channel, ucEvent_);
		}
	};
	
	/**
	 * Callback für Response (Antworten auf Befehle)
	 */
	public  AntDll.Caller fn = new AntDll.Caller() {
		public void callback (int channel, int msgId){
			byte mgId = 0;
			mgId = (byte) (msgId | mgId);
			//boolean bSuccess = false;
			responseBuffer = response_ptr.getPointer().getByteArray(0,AntDefines.MAX_RESPONSE_SIZE);

			switch(mgId){
			case AntDefines.MESG_STARTUP_MESG_ID: {
				int ucReasonInt = responseBuffer[AntDefines.MESSAGE_BUFFER_DATA1_INDEX];
				byte ucReason = 0;
				String grund = "UNKNOWN";
				ucReason = (byte)(ucReasonInt | ucReason);

				if(ucReason == AntDefines.RESET_POR)
					grund = "RESET_POR";
				if(ucReason == AntDefines.RESET_SUSPEND)
					grund = "RESET_SUSPEND";
				if(ucReason == AntDefines.RESET_SYNC)
					grund = "RESET_SYNC";
				if(ucReason == AntDefines.RESET_CMD)
					grund = "RESET_CMD";
				if(ucReason == AntDefines.RESET_WDT)
					grund = "RESET_WDT";
				if(ucReason == AntDefines.RESET_RST)
					grund = "RESET_RST";
				if (debug)
					Mlog.debug("ANT+ RESET! Grund: " + grund);

				break;
			}
			case AntDefines.MESG_CAPABILITIES_ID: {
				byte ucAdvanced2 = 0;
				byte ucStandardOptions = 0;
				byte ucAdvanced = 0;

				if (debug) {
					Mlog.debug("ANT+ CAPABILITIES:");
					Mlog.debug("max. ANT channels: " + responseBuffer[AntDefines.MESSAGE_BUFFER_DATA1_INDEX]);
					Mlog.debug("max ANT networks: " + responseBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX]);
				}
				ucStandardOptions = (byte) (ucStandardOptions | responseBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX]);
				ucAdvanced = (byte) (ucAdvanced | responseBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX]);
				ucAdvanced2 = (byte) (ucAdvanced2 | responseBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX]);
				
				if (debug) {
					Mlog.debug("ANT+ Standard options:");

					if( ucStandardOptions == AntDefines.CAPABILITIES_NO_RX_CHANNELS )
						Mlog.debug("CAPABILITIES_NO_RX_CHANNELS");
					if( ucStandardOptions == AntDefines.CAPABILITIES_NO_TX_CHANNELS )
						Mlog.debug("CAPABILITIES_NO_TX_CHANNELS");
					if( ucStandardOptions == AntDefines.CAPABILITIES_NO_RX_MESSAGES )
						Mlog.debug("CAPABILITIES_NO_RX_MESSAGES");
					if( ucStandardOptions == AntDefines.CAPABILITIES_NO_TX_MESSAGES )
						Mlog.debug("CAPABILITIES_NO_TX_MESSAGES");
					if( ucStandardOptions == AntDefines.CAPABILITIES_NO_ACKD_MESSAGES )
						Mlog.debug("CAPABILITIES_NO_ACKD_MESSAGES");
					if( ucStandardOptions == AntDefines.CAPABILITIES_NO_BURST_TRANSFER )
						Mlog.debug("CAPABILITIES_NO_BURST_TRANSFER");

					Mlog.debug("ANT+ Advanced options:");

					if( ucAdvanced == AntDefines.CAPABILITIES_OVERUN_UNDERRUN )
						Mlog.debug("CAPABILITIES_OVERUN_UNDERRUN");
					if( ucAdvanced == AntDefines.CAPABILITIES_NETWORK_ENABLED )
						Mlog.debug("CAPABILITIES_NETWORK_ENABLED");
					if( ucAdvanced == AntDefines.CAPABILITIES_AP1_VERSION_2 )
						Mlog.debug("CAPABILITIES_AP1_VERSION_2");
					if( ucAdvanced == AntDefines.CAPABILITIES_SERIAL_NUMBER_ENABLED )
						Mlog.debug("CAPABILITIES_SERIAL_NUMBER_ENABLED");
					if( ucAdvanced == AntDefines.CAPABILITIES_PER_CHANNEL_TX_POWER_ENABLED )
						Mlog.debug("CAPABILITIES_PER_CHANNEL_TX_POWER_ENABLED");
					if( ucAdvanced == AntDefines.CAPABILITIES_LOW_PRIORITY_SEARCH_ENABLED )
						Mlog.debug("CAPABILITIES_LOW_PRIORITY_SEARCH_ENABLED");
					if( ucAdvanced == AntDefines.CAPABILITIES_SCRIPT_ENABLED )
						Mlog.debug("CAPABILITIES_SCRIPT_ENABLED");
					if( ucAdvanced == AntDefines.CAPABILITIES_SEARCH_LIST_ENABLED )
						Mlog.debug("CAPABILITIES_SEARCH_LIST_ENABLED");

					Mlog.debug("ANT+ Advanced Options 2:");

					if( ucAdvanced2 == AntDefines.CAPABILITIES_LED_ENABLED )
						Mlog.debug("CAPABILITIES_LED_ENABLED");
					if( ucAdvanced2 == AntDefines.CAPABILITIES_EXT_MESSAGE_ENABLED )
						Mlog.debug("CAPABILITIES_EXT_MESSAGE_ENABLED");
					if( ucAdvanced2 == AntDefines.CAPABILITIES_SCAN_MODE_ENABLED )
						Mlog.debug("CAPABILITIES_SCAN_MODE_ENABLED");
					if( ucAdvanced2 == AntDefines.CAPABILITIES_RESERVED )
						Mlog.debug("CAPABILITIES_RESERVED");
					if( ucAdvanced2 == AntDefines.CAPABILITIES_PROX_SEARCH_ENABLED )
						Mlog.debug("CAPABILITIES_PROX_SEARCH_ENABLED");
					if( ucAdvanced2 == AntDefines.CAPABILITIES_EXT_ASSIGN_ENABLED )
						Mlog.debug("CAPABILITIES_EXT_ASSIGN_ENABLED");
					if( ucAdvanced2 == AntDefines.CAPABILITIES_FREE_1 )
						Mlog.debug("CAPABILITIES_FREE_1");
					if( ucAdvanced2 == AntDefines.CAPABILITIES_FIT1_ENABLED )
						Mlog.debug("CAPABILITIES_FIT1_ENABLED");
				}
				break;
			}
			case AntDefines.MESG_CHANNEL_STATUS_ID: {
				String astrStatus[] = {  
						"STATUS_UNASSIGNED_CHANNEL",
						"STATUS_ASSIGNED_CHANNEL",
						"STATUS_SEARCHING_CHANNEL",
						"STATUS_TRACKING_CHANNEL"   };
				int ucStatusByte = responseBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX] & AntDefines.STATUS_CHANNEL_STATE_MASK; // MUST MASK OFF THE RESERVED BITS
				Mlog.debug("ANT+ STATUS: "+astrStatus[ucStatusByte]);

				break;
			}
			case AntDefines.MESG_CHANNEL_ID_ID: {
				// Channel ID of the device that we just recieved a message from.
				int usDeviceNumber = responseBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX] | (responseBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX] << 8);
				int ucDeviceType =  responseBuffer[AntDefines.MESSAGE_BUFFER_DATA4_INDEX];
				int ucTransmissionType = responseBuffer[AntDefines.MESSAGE_BUFFER_DATA5_INDEX];
				Mlog.debug("ANT+ CHANNEL ID: "+ usDeviceNumber+"/"+ ucDeviceType + "/" +ucTransmissionType);

				break;
			}
			case AntDefines.MESG_VERSION_ID: {
				Mlog.debug("ANT+ VERSIONID: " + responseBuffer[AntDefines.MESSAGE_BUFFER_DATA1_INDEX]);
				break;
			}
			case AntDefines.MESG_RESPONSE_EVENT_ID: {
				byte respevents = 0;
				respevents = (byte) (respevents | responseBuffer[2]);
				//Mlog.debug("respevents "+respevents);
				switch(respevents){
				case AntDefines.RESPONSE_NO_ERROR:{
					setResponse(true);
					byte msgid = (byte)responseBuffer[1];
					byte antChannel = (byte)responseBuffer[0];
					switch (msgid){
					case AntDefines.MESG_NETWORK_KEY_ID:{
						Mlog.debug("ANT+ Network Key Set, assigning channel: " + antChannel + " Type: " + ucChannelType);
						//Find out channel type
						do {
							if(ucChannelType == AntDefines.CHANNEL_TYPE_MASTER){
								lib.ANT_AssignChannel(antChannel, AntDefines.PARAMETER_TX_NOT_RX, AntDefines.USER_NETWORK_NUM);
							}
							else if(ucChannelType == AntDefines.CHANNEL_TYPE_SLAVE){
								lib.ANT_AssignChannel(antChannel, AntDefines.PARAMETER_RX_NOT_TX, AntDefines.USER_NETWORK_NUM);
							}
							else{
								ucChannelType = AntDefines.CHANNEL_TYPE_INVALID;
								Mlog.error("ANT+ Invalid channel type.");
							}
						} while(ucChannelType == AntDefines.CHANNEL_TYPE_INVALID);
						break;
					}
					case AntDefines.MESG_ASSIGN_CHANNEL_ID: {
						Mlog.info("ANT+ Channel assigned, setting channel ID, Channel: " + antChannel);
						switch (antChannel) {
						case AntDefines.USER_ANTCHANNEL0:			// Channel 0: Pulser oder Trittfrequenz bzw. Display Rollentrainer/Wahoo KICKR
							if (aktTDev == TDev.wahookickr) {		// Wahoo KICKR
								if (debug)
									Mlog.debug("SetChannelId: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.WF_KICKR_DEVICE_TYPE+"/"+AntDefines.WF_KICKR_TRANSMISSION_TYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.WF_KICKR_DEVICE_TYPE, AntDefines.WF_KICKR_TRANSMISSION_TYPE);
								break;
							} 
							if (aktTDev == TDev.antfec) {			// ANT+ FE-C
								if (debug)
									Mlog.debug("SetChannelId_1: "+antChannel+"/"+AntDefines.ANTFEC_DEVICE_NUM+"/"+AntDefines.ANTFEC_DEVICE_TYPE+"/"+AntDefines.ANTFEC_TRANSMISSION_TYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.ANTFEC_DEVICE_NUM, AntDefines.ANTFEC_DEVICE_TYPE, AntDefines.ANTFEC_TRANSMISSION_TYPE);
								break;
							} 
							if (aktTDev == TDev.notset && aktTHr == THr.antppuls) {	// Pulse
								if (debug)
									Mlog.debug("SetChannelId_2: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPEPULS+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPEPULS, AntDefines.USER_TRANSTYPE);
								break;
							}
							if (aktTDev == TDev.notset && aktTCadence == TCadence.antpcadencespeed) {	// Cadence/Speed
								if (debug)
									Mlog.debug("SetChannelId_3: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCESPEED+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCESPEED, AntDefines.USER_TRANSTYPE);
								break;
							}
							if (aktTDev == TDev.notset && aktTCadence == TCadence.antpcadence) {		// Cadence
								if (debug)
									Mlog.debug("SetChannelId_4: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCE+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCE, AntDefines.USER_TRANSTYPE);
								break;
							}
							// Andere z.B. TACX Vortex
							if (debug)
								Mlog.debug("SetChannelId_5: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPE0+"/"+AntDefines.USER_TRANSTYPE);
							lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPE0, AntDefines.USER_TRANSTYPE);
							break;
						case AntDefines.USER_ANTCHANNEL1:			// Channel 1: Pulser oder Cadence/Speed
							if (aktTHr == THr.antppuls && aktTCadence != TCadence.notset) {	// Beide Geräte? -> Puls bevorzugt
								if (aktTDev == TDev.notset)	{		// dann ist der Pulser bereits auf Channel 0 
									if (aktTCadence == TCadence.antpcadencespeed) {
										if (debug)
											Mlog.debug("SetChannelId_6: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCESPEED+"/"+AntDefines.USER_TRANSTYPE);
										lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCESPEED, AntDefines.USER_TRANSTYPE);										
									} else {
										if (aktTCadence == TCadence.antpcadence) {
											if (debug)
												Mlog.debug("SetChannelId_6a: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCE+"/"+AntDefines.USER_TRANSTYPE);
											lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCE, AntDefines.USER_TRANSTYPE);										
										} 										
									}
								} else {
									if (debug)
										Mlog.debug("SetChannelId_6b: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPEPULS+"/"+AntDefines.USER_TRANSTYPE);
									lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPEPULS, AntDefines.USER_TRANSTYPE);
								}
								break;
							} 
							if (aktTHr == THr.antppuls) {
								if (debug)
									Mlog.debug("SetChannelId_7: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPEPULS+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPEPULS, AntDefines.USER_TRANSTYPE);
								break;									
							} 
							if (aktTCadence == TCadence.antpcadencespeed) {
								if (debug)
									Mlog.debug("SetChannelId_8: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCESPEED+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCESPEED, AntDefines.USER_TRANSTYPE);
								break;
							}
							if (aktTCadence == TCadence.antpcadence) {
								if (debug)
									Mlog.debug("SetChannelId_9: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCE+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCE, AntDefines.USER_TRANSTYPE);
								break;
							}
							break;
						case AntDefines.USER_ANTCHANNEL2:			// Channel 2: kann aktuell nur Cadence/Speed oder Cadence sein
							if (aktTCadence == TCadence.antpcadencespeed) {
								if (debug)
									Mlog.debug("SetChannelId_10: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCESPEED+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCESPEED, AntDefines.USER_TRANSTYPE);
								break;
							}
							if (aktTCadence == TCadence.antpcadence) {
								if (debug)
									Mlog.debug("SetChannelId_11: "+antChannel+"/"+AntDefines.USER_DEVICENUM+"/"+AntDefines.USER_DEVICETYPECADENCE+"/"+AntDefines.USER_TRANSTYPE);
								lib.ANT_SetChannelId(antChannel, AntDefines.USER_DEVICENUM, AntDefines.USER_DEVICETYPECADENCE, AntDefines.USER_TRANSTYPE);
								break;
							}
							break;
						}
					}
					case AntDefines.MESG_CHANNEL_ID_ID: {
						int mesgPeriod = 0;
						Mlog.debug("ANT+ Channel ID set, Channel: " + antChannel);
						switch (antChannel) {
						case AntDefines.USER_ANTCHANNEL0:			// Channel 0: Pulser oder Cadence/Speed bzw. Display Rollentrainer/Wahoo KICKR/ANT+FEC
							if (aktTDev == TDev.notset) {
								if (aktTHr == THr.antppuls) {
									mesgPeriod = AntDefines.USER_CHPERGARPULS;
									break;
								}
								if (aktTCadence == TCadence.antpcadencespeed) { 
									mesgPeriod = AntDefines.USER_CHPERCADENCESPEED;
									break;
								}
								if (aktTCadence == TCadence.antpcadence) { 
									mesgPeriod = AntDefines.USER_CHPERCADENCE;
									break;
								}
								mesgPeriod = getAntMsgPeriod();
								break;
							} 
							mesgPeriod = getAntMsgPeriod();
							break;
						case AntDefines.USER_ANTCHANNEL1:			// Channel 1: kann aktuell nur Pulser oder Trittfrequenz sein
							if (aktTHr == THr.antppuls && aktTCadence != TCadence.notset) { // Beide Geräte? -> Puls bevorzugt
								if (aktTDev == TDev.notset) {	// dann ist der Pulser auf Channel 0!
									if (aktTCadence == TCadence.antpcadencespeed)
										mesgPeriod = AntDefines.USER_CHPERCADENCESPEED;
									if (aktTCadence == TCadence.antpcadence)
										mesgPeriod = AntDefines.USER_CHPERCADENCE;
								} else
									mesgPeriod = AntDefines.USER_CHPERGARPULS;
								break;
							}	
							if (aktTHr == THr.antppuls) {
								mesgPeriod = AntDefines.USER_CHPERGARPULS;
								break;
							} 
							if (aktTCadence == TCadence.antpcadencespeed) { 
								mesgPeriod = AntDefines.USER_CHPERCADENCESPEED;
								break;
							}
							if (aktTCadence == TCadence.antpcadence) { 
								mesgPeriod = AntDefines.USER_CHPERCADENCE;
								break;
							}
							break;
						case AntDefines.USER_ANTCHANNEL2:			// Channel 2: kann aktuell nur Trittfrequenz sein
							if (aktTCadence == TCadence.antpcadencespeed) 
								mesgPeriod = AntDefines.USER_CHPERCADENCESPEED;
							if (aktTCadence == TCadence.antpcadence) 
								mesgPeriod = AntDefines.USER_CHPERCADENCE;
							break;
						}
						Mlog.info("ANT+ Channel: "+antChannel+" Setting period to: " + mesgPeriod);
						lib.ANT_SetChannelPeriod(antChannel, mesgPeriod);
						break;
					}
					case AntDefines.MESG_CHANNEL_MESG_PERIOD_ID: {
						int rfFreq = 0;
						Mlog.debug("ANT+ Period set, Channel: " + antChannel);
						switch (antChannel) {
						case AntDefines.USER_ANTCHANNEL0:			// Channel 0: Pulser oder Trittfrequenz bzw. Display Rollentrainer/Wahoo KICKR/ANT+FEC
							if (aktTDev == TDev.notset) {
								if (aktTHr == THr.antppuls || aktTCadence != TCadence.notset) 
									rfFreq = AntDefines.USER_RADIOFREQ;
								else 
									rfFreq = getANTFreq();
							} else 
								rfFreq = getANTFreq();
							break;
						case AntDefines.USER_ANTCHANNEL1:			// Channel 1: kann aktuell nur Pulser oder Trittfrequenz sein
							if (aktTHr == THr.antppuls || aktTCadence != TCadence.notset) 
								rfFreq = AntDefines.USER_RADIOFREQ;
							break;
						case AntDefines.USER_ANTCHANNEL2:			// Channel 2: kann aktuell nur Trittfrequenz sein
							if (aktTCadence != TCadence.notset) 
								rfFreq = AntDefines.USER_RADIOFREQ;
							break;
						}
						Mlog.info("ANT+ Channel: "+antChannel+" Setting RF frequency to: " + rfFreq);
						lib.ANT_SetChannelRFFreq(antChannel, rfFreq);
						break;
					}
					case AntDefines.MESG_CHANNEL_RADIO_FREQ_ID: {
						Mlog.debug("ANT+ Radio frequency set, Channel: " + antChannel);
						Mlog.debug("ANT+ Opening channel: " + antChannel);
						lib.ANT_OpenChannel(antChannel);
						break;
					}
					case AntDefines.MESG_OPEN_CHANNEL_ID: {
						Mlog.debug("ANT+ Channel opened, Channel: " + antChannel);
						break;
					}
					case AntDefines.MESG_UNASSIGN_CHANNEL_ID: {
						Mlog.debug("ANT+ Unassigned channel: " + antChannel);
						//bDone = true;
						break;
					}
					case AntDefines.MESG_CLOSE_CHANNEL_ID: {
						Mlog.debug("ANT+ channel closed: " + antChannel);
						break;
					}

					case AntDefines.MESG_RX_EXT_MESGS_ENABLE_ID: {
						Mlog.debug("ANT+ Extended messages enabled");
						break;
					}

					default: {
						break;
					}
					}
					break;
				}
				case AntDefines.CHANNEL_IN_WRONG_STATE: {
					Mlog.error("ANT+ Channel in wrong state");
					break;
				}
				case AntDefines.CHANNEL_NOT_OPENED: {
					Mlog.error("ANT+ Channel not opened");
					break;
				}
				case AntDefines.CHANNEL_ID_NOT_SET: { // ?
					Mlog.error("ANT+ Channel ID not set");
					break;
				}
				case AntDefines.CLOSE_ALL_CHANNELS: { // Start RX Scan mode
					Mlog.debug("ANT+ Close all channels");
					break;
				}
				case AntDefines.TRANSFER_IN_PROGRESS: { // TO ack message ID
					Mlog.debug("ANT+ Tranfer in progress");
					break;
				}
				case AntDefines.TRANSFER_SEQUENCE_NUMBER_ERROR: {
					Mlog.debug("ANT+ Transfer sequence number error");
					break;
				}
				case AntDefines.TRANSFER_IN_ERROR: {
					Mlog.error("ANT+ Transfer in error");
					break;
				}
				case AntDefines.INVALID_MESSAGE: {
					Mlog.debug("ANT+ Invalid message");
					break;
				}
				case AntDefines.INVALID_NETWORK_NUMBER: {
					Mlog.error("ANT+ Invalid network number");
					break;
				}
				case AntDefines.INVALID_LIST_ID: {
					Mlog.error("ANT+ Invalid list ID");
					break;
				}
				case AntDefines.INVALID_SCAN_TX_CHANNEL: {
					Mlog.error("ANT+ Invalid Scanning transmit channel");
					break;
				}
				case AntDefines.INVALID_PARAMETER_PROVIDED: {
					Mlog.error("ANT+ Invalid parameter provided");
					break;
				}
				case AntDefines.EVENT_QUE_OVERFLOW: {
					Mlog.error("ANT+ Queue overflow");
					break;
				}
				default: {
					Mlog.error("ANT+ Unknown error code: "+responseBuffer[AntDefines.MESSAGE_BUFFER_DATA3_INDEX]+" to message: "+responseBuffer[AntDefines.MESSAGE_BUFFER_DATA2_INDEX]);
					break;
				}
				}
			}
			}
			if(debug) {
				Mlog.debug("Antwort vom Geraet: "+
					Integer.toHexString(responseBuffer[0] & 0xff)+" "+
					Integer.toHexString(responseBuffer[1] & 0xff)+" "+
					Integer.toHexString(responseBuffer[2] & 0xff)+" "+
					Integer.toHexString(responseBuffer[3] & 0xff)+" "+
					Integer.toHexString(responseBuffer[4] & 0xff)+" "+
					Integer.toHexString(responseBuffer[5] & 0xff)+" "+
					Integer.toHexString(responseBuffer[6] & 0xff)+" "+
					Integer.toHexString(responseBuffer[7] & 0xff)+" "+
					Integer.toHexString(responseBuffer[8] & 0xff));
			}
		}
	};

	/**
	 * Pausenfunktion
	 * @param howLong	Pause in Millisekunden
	 */
/*	public  void sleeper(int howLong){
		try {
			Thread.currentThread();
			Thread.sleep(howLong);
		} catch(InterruptedException e){
			Mlog.debug("ANT+ sleeping" + e.getMessage());
		}
	}
*/
	
	/**
	 * Mittels Burst-Transfer wird die Leistung an den KICKR übertragen.
	 * Aktuell wird nicht auf ein bestimmtes Gerät eingeschränkt.
	 * Die Paketdefinition wurde übernommen aus: KICKR_ANT_commands.m (Wahoo-API)
	 * @param power	Leistung als int
	 */
    private void sendKICKRErgModePower(int power)
    {
		//Mlog.debug("KICKR, sende Power: " + power);
		// Burst-Transfer:
		byte [] paket = { (byte) AntDefines.ANT_COMMAND_BURST,
				AntDefines.WF_WCPMCP_OPCODE_TRAINER_SET_ERG_MODE,
				antCmdIndex++, 
				3, 						// ucPacketCount
				AntDefines.ANTPLUS_RESERVED,
				AntDefines.ANTPLUS_RESERVED,
				AntDefines.ANTPLUS_RESERVED,
				AntDefines.ANTPLUS_RESERVED,
				
				(byte) (AntDefines.WF_ANT_MANUFACTURER_ID & 0xFF), 
				(byte) (AntDefines.WF_ANT_MANUFACTURER_ID >> 8),
				(byte) (AntDefines.WF_API_PRODUCT_ID & 0xFF),
				(byte) (AntDefines.WF_API_PRODUCT_ID >> 8),
				0,0,0,0,				// Slave Serialnumber?
				(byte) (power & 0xFF), 	// Watts
				(byte) (power >> 8) 	// Watts
				};
		try {
			lib.ANT_SendBurstTransfer(AntDefines.USER_ANTCHANNEL0, paket, 5);
			//Mlog.debug("KICKR, nach BurstTransfer! ");			
		} catch (Exception e) {
			Mlog.debug("KICKR, BurstTransfer Ex in ANT_DLL!");	
		}
    }

    /**
     * sendet Command Page 49 (verwende TargetPower Mode) an ANT+ FE-C Device
     */
    private void sendTargetPowerPage(int pwr4) {
		byte b1,b2;

		b1 = (byte) (pwr4 & 0xFF);
		b2 = (byte) ((pwr4 & 0xFF00) >> 8);
		byte [] paket = { AntDefines.ANTFEC_PAGE_TARGET_POWER, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, b1, b2}; 						
		lib.ANT_SendAcknowledgedData(0, paket);    	
    }
      
    /**
	 * Verbindung zum USB-Stick (Garmin AP1, AP2) aufbauen, Reset und Network-Key setzen.
	 * @param 	dev		ANT+ Devicenr.
	 * @param 	baud	Baudrate
	 * @return			erfolgreich oder nicht
	 */
	private boolean antConnect(int dev, int baud, byte[] netkey) {
		boolean flag;
		int sleeptimekurz = 100;	// 100
		
		flag = lib.ANT_Init1(dev, baud);
		Mlog.debug("ANT+ Init("+baud+"): " + flag);//
		Global.sleep(sleeptimekurz);	
		if (!flag) {
			Mlog.info("ANT+ kein passender USB-Stick vorhanden!");
			return false;
		} 
		
		flag = lib.ANT_ResetSystem();
		Mlog.debug("ANT+ RESET system: " + flag);
		Global.sleep(sleeptimekurz);	

		flag = lib.ANT_SetNetworkKey(AntDefines.USER_NETWORK_NUM, netkey);	// Garmin: AntDefines.USER_NETWORK_KEY
		Global.sleep(sleeptimekurz);
		if ((aktTHr == THr.antppuls && (aktTDev == TDev.wahookickr || aktTDev == TDev.antfec)) || 
			(aktTCadence != TCadence.notset && (aktTDev == TDev.wahookickr || aktTDev == TDev.antfec)) ||
			(aktTHr == THr.antppuls && aktTCadence != TCadence.notset)) {	// Wahoo KICKR/ANT+FEC + Pulser / Wahoo KICKR/ANT+FEC + Trittfreq. / Pulser + Trittfrequenz ? 2. Kanal öffnen
				Mlog.debug("ANT+ setze Kanal 1");
				lib.ANT_AssignChannel(AntDefines.USER_ANTCHANNEL1, AntDefines.PARAMETER_RX_NOT_TX, AntDefines.USER_NETWORK_NUM);
				if (aktTHr == THr.antppuls && aktTCadence != TCadence.notset && (aktTDev == TDev.wahookickr || aktTDev == TDev.antfec)) { // Wahoo KICKR/ANT+FEC + Pulser + Trittfrequenz ? 3. Kanal öffnen
					Mlog.debug("ANT+ setze Kanal 2");
					lib.ANT_AssignChannel(AntDefines.USER_ANTCHANNEL2, AntDefines.PARAMETER_RX_NOT_TX, AntDefines.USER_NETWORK_NUM);
				}
		}
		Global.sleep(1000);
		
		return true;
	}

	/**
	 * Initialisierung der ANT+ Kommunikation. Es wird die version der DLL ausgegeben und 
	 * anschliessend die Calllbackfunctionen mittels JNA gesetzt.
	 */
	private void antInit() {
		Mlog.debug("ANT+ Version: " + lib.ANT_LibVersion());

		if (Rsmain.thisTrainer.isDeepdebug())
			setDebug(true);

		Pointer responseP = new Memory(AntDefines.MAX_RESPONSE_SIZE);
		response_ptr.setPointer(responseP);
		lib.ANT_AssignResponseFunction(fn, response_ptr);

		Pointer channelP = new Memory(AntDefines.MAX_CHANNEL_EVENT_SIZE);
		channel_ptr.setPointer(channelP);
		lib.ANT_AssignChannelEventFunction(AntDefines.USER_ANTCHANNEL0, fnChannel, channel_ptr);

		Pointer channelP1 = new Memory(AntDefines.MAX_CHANNEL_EVENT_SIZE);
		channel_ptr1.setPointer(channelP1);
		lib.ANT_AssignChannelEventFunction(AntDefines.USER_ANTCHANNEL1, fnChannel1, channel_ptr1);

		Pointer channelP2 = new Memory(AntDefines.MAX_CHANNEL_EVENT_SIZE);
		channel_ptr2.setPointer(channelP2);
		lib.ANT_AssignChannelEventFunction(AntDefines.USER_ANTCHANNEL2, fnChannel2, channel_ptr2);
		// TODO ergänzen, wenn mehr als drei Kanaäle verwendet werden sollen!
	}
	
	/**
	 * Extra Thread (Runnable) für die ANT+ Kommunikation.
	 * Am Anfang wird initialisiert (Callbacks etc.), dann wird
	 * abhängig von Statusflags die Verbindung aufgebaut oder gehalten.
	 */
	private Thread thread = new Thread(new Runnable()  {
		byte[] netkey;
		public void run() {
			// benötigt für asynchronen Aufruf der Fehlermeldung
			Runnable rError = new Runnable() {
				public void run() {
					Messages.errormessage(errorMsg);											
				}
			};
			// Pulsanzeige im Hauptthread
			Runnable rPuls = new Runnable() {
				public void run() {
					// Puls nur direkt anzeigen, wenn Video nicht läuft!
					if (Rsmain.aktstatus != Rsmain.Status.laeuft) {
						if (aktTHr == THr.antppuls)
							Rsmain.atPuls.show(0, "" + getPuls(), 0);	
						if (aktTCadence != TCadence.notset)
							Rsmain.atKurbel.show(0, "" + getRpm(), 0);	
					}
				}
			};
			
			antInit();

			while (true) {
				//Mlog.debug("ANT+ Thread, isEin: "+isEin()+" - isResponse: "+isResponse());
				if (aktTHr == THr.antppuls || aktTCadence != TCadence.notset || aktTDev == TDev.wahookickr || aktTDev == TDev.antfec)
					netkey = AntDefines.USER_NETWORK_KEY;
				else
					if (aktTDev == TDev.tacxvortex || aktTDev == TDev.tacxbushido)
						netkey = AntDefines.TACX_NETWORK_KEY;
				
				if (isEin() && !isResponse()) {	// eingeschaltet, aber keine Kommunikation ? Dann connecten:
					connectAP2 = antConnect(0,AntDefines.USER_BAUDRATE2, netkey);
					if (isResponse()) {		
						setEin(true);
					} else {					// Stick vorhanden aber es konnte keine Kommunikation aufgebaut werden!
						lib.ANT_Close();
						connectAP1 = antConnect(0, AntDefines.USER_BAUDRATE, netkey);	// jetzt auf Garmin AP1 abfragen
						if (connectAP2 || connectAP1) {
							setEin(true);
						} else {				// Es ist kein passender ANT+ USB-Stick vorhanden!
							setEin(false);
						}
						if (isResponse()) {	
							setEin(true);
						} else {				// Es konnte trotzdem keine Kommunikation aufgebaut werden! -> Fehlermeldung (asynchron) ausgeben
							setEin(false);
							lib.ANT_Close();
							if (connectAP2 || connectAP1)
								errorMsg = Messages.getString("libant.keinekom");	
							else
								errorMsg = Messages.getString("libant.keinstick");											
							Rsmain.display.asyncExec(rError);
						}
					}
				}

				while (isEin()) {
					// hier können jetzt noch regelmäsige Überwachungen und Berechnungen rein...
					if (!isStop()) {
						if (aktTDev == TDev.antfec) {
							sendTargetPowerPage(getVorgabePower() * 4);
						}
						setEin(true);
						Rsmain.display.asyncExec(rPuls);		// Anzeige von Puls, evtl. RPM, evtl. Speed
						if (debug) {
							Mlog.debug("ANT+ Puls = " + getPuls());
							Mlog.debug("ANT+ RPM = " + getRpm());
							//Mlog.debug("ANT+ Speed = " + speed);
							//Mlog.debug("ANT+ Power = " + getPower());
							//Mlog.debug("optime = " + optime);
							//Mlog.debug("manId = " + manId);
							//Mlog.debug("serialNumber = " + serialNumber);
							//Mlog.debug("hwVersion = " + hwVersion);
							//Mlog.debug("swVersion = " + swVersion);
							//Mlog.debug("modelNum = " + modelNum);
							//Mlog.debug("previousBeat = " + previousBeat + "\n");
						}
					} else {
						Mlog.info("ANT+ deaktiviert");
						setEin(false);
					}
					Global.sleep(1000);
				}
				Global.sleep(500);
			}
		};		
	});
	
	/**
	 * Starten der ANT+ Kommunikation. Falls der Thread schon läuft, werden nur die Flags gesetzt.
	 */
	public void start() {
		if (debug)
			Mlog.debug("ANT+ Start!");
		// Damit nicht ständig neue Threads für jeden Callback gemacht werden:
		Native.setCallbackThreadInitializer(fnChannel, callbackThreadInitializer);
		Native.setCallbackThreadInitializer(fnChannel1, callbackThreadInitializer);
		Native.setCallbackThreadInitializer(fnChannel2, callbackThreadInitializer);
		Native.setProtected(true);
		if (debug)
			Mlog.debug("ANT+ isprotected: " + Native.isProtected());
		switch (aktTDev) {
		case tacxvortex:
			aktTHr = THr.notset;		// Puls wird über TACX-Device ermittelt
			setANTFreq(AntDefines.USER_RFVORDISP);				
			setANTMsgPeriod(AntDefines.USER_CHPERVORDISP);				
			break;
		case tacxbushido:
			aktTHr = THr.notset;		// Puls wird über TACX-Device ermittelt
			setANTFreq(AntDefines.USER_RFBUSHDISP);			
			setANTMsgPeriod(AntDefines.USER_CHPERBUSHDISP);			
			break;
		case wahookickr:
			setANTFreq(AntDefines.USER_RFKICKR);	
			setANTMsgPeriod(AntDefines.USER_CHPERKICKR);	
			break;
		case antfec:					// ANT+ FE-C
			setANTFreq(AntDefines.USER_RFANTFEC);	
			setANTMsgPeriod(AntDefines.USER_CHPERANTFEC);	
			break;
		default:
			break;
		}
		setEin(true);
		setStop(false);
		if (!(thread.isAlive()))
			thread.start();
	}
	
	/**
	 * Stoppen der ANT-Kommunikation und schliessen der Kanäle (Thread läuft weiter!)
	 */
	public void stop() {
		setStop(true);
		setEin(false);
		setResponse(false);
		lib.ANT_Close();
		Mlog.info("ANT+ Channels closed");
		Global.sleep(1000);
	}
}


