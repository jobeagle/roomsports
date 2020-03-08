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
 *
 *****************************************************************************
 * AntDefines.java: Definitionen für die ANT+ Schnittstelle
 *****************************************************************************
 *
 * Diese Klasse beinhaltet alle wichtigen ANT-Definitionen der Klasse LibAnt 
 * und wurde abgeleitet von ant.h aus ANT_Windows_Library_Package.
 *  
 */

public class AntDefines {
	public static final byte MESG_TX_SYNC = (byte) 0xa4;
	public static final byte MESG_RX_SYNC = 0xa;
	public static final byte MESG_SYNC_SIZE = 1;
	public static final byte MESG_SIZE_SIZE = 1;
	public static final byte MESG_ID_SIZE = 1;
	public static final byte MESG_CHANNEL_NUM_SIZE = 1;
	public static final byte MESG_EXT_MESG_BF_SIZE = 1;  // NOTE: this could increase in the future
	public static final byte MESG_CHECKSUM_SIZE = 1;
	public static final byte MESG_DATA_SIZE = 9;

	// The largest serial message is an ANT data message with all of the extended fields
	public static final byte MESG_ANT_MAX_PAYLOAD_SIZE = AntDefines.ANT_STANDARD_DATA_PAYLOAD_SIZE;

	public static final byte MESG_MAX_EXT_DATA_SIZE = AntDefines.ANT_EXT_MESG_DEVICE_ID_FIELD_SIZE + 4 + 2; // ANT device ID (4 public ints; +  (4 public ints; +  (2 public ints;

	public static final byte MESG_MAX_DATA_SIZE = MESG_ANT_MAX_PAYLOAD_SIZE + MESG_EXT_MESG_BF_SIZE + MESG_MAX_EXT_DATA_SIZE; // ANT data payload (8 public ints; + extended bitfield (1 public int; + extended data (10 public ints;
	public static final byte MESG_MAX_SIZE_VALUE = MESG_MAX_DATA_SIZE + MESG_CHANNEL_NUM_SIZE;  // this is the maximum value that the serial message size value is allowed to be
	public static final byte MESG_BUFFER_SIZE = MESG_SIZE_SIZE + MESG_ID_SIZE + MESG_CHANNEL_NUM_SIZE + MESG_MAX_DATA_SIZE + MESG_CHECKSUM_SIZE;
	public static final byte MESG_FRAMED_SIZE = MESG_ID_SIZE + MESG_CHANNEL_NUM_SIZE + MESG_MAX_DATA_SIZE;
	public static final byte MESG_HEADER_SIZE = MESG_SYNC_SIZE + MESG_SIZE_SIZE + MESG_ID_SIZE;
	public static final byte MESG_FRAME_SIZE = MESG_HEADER_SIZE + MESG_CHECKSUM_SIZE;
	public static final byte MESG_MAX_SIZE = MESG_MAX_DATA_SIZE + MESG_FRAME_SIZE;

	public static final byte MESG_SIZE_OFFSET = MESG_SYNC_SIZE;
	public static final byte MESG_ID_OFFSET = MESG_SYNC_SIZE + MESG_SIZE_SIZE;
	public static final byte MESG_DATA_OFFSET = MESG_HEADER_SIZE;
	public static final byte MESG_RECOMMENDED_BUFFER_SIZE =  64;                         // This is the recommended size for serial message buffers if there are no RAM restrictions on the system

	//////////////////////////////////////////////
	// Message ID's
	//////////////////////////////////////////////
	public static final byte MESG_INVALID_ID = 0x00;
	public static final byte MESG_EVENT_ID = 0x01;

	public static final byte MESG_VERSION_ID = 0x3E;
	public static final byte MESG_RESPONSE_EVENT_ID = 0x40;

	public static final byte MESG_UNASSIGN_CHANNEL_ID = 0x41;
	public static final byte MESG_ASSIGN_CHANNEL_ID = 0x42;
	public static final byte MESG_CHANNEL_MESG_PERIOD_ID = 0x43;
	public static final byte MESG_CHANNEL_SEARCH_TIMEOUT_ID = 0x44;
	public static final byte MESG_CHANNEL_RADIO_FREQ_ID = 0x45;
	public static final byte MESG_NETWORK_KEY_ID = 0x46;
	public static final byte MESG_RADIO_TX_POWER_ID = 0x47;
	public static final byte MESG_RADIO_CW_MODE_ID = 0x48;
	public static final byte MESG_SYSTEM_RESET_ID = 0x4A;
	public static final byte MESG_OPEN_CHANNEL_ID = 0x4B;
	public static final byte MESG_CLOSE_CHANNEL_ID = 0x4C;
	public static final byte MESG_REQUEST_ID = 0x4D;

	public static final byte MESG_BROADCAST_DATA_ID = 0x4E;
	public static final byte MESG_ACKNOWLEDGED_DATA_ID = 0x4F;
	public static final byte MESG_BURST_DATA_ID = 0x50;

	public static final byte MESG_CHANNEL_ID_ID = 0x51;
	public static final byte MESG_CHANNEL_STATUS_ID = 0x52;
	public static final byte MESG_RADIO_CW_INIT_ID = 0x53;
	public static final byte MESG_CAPABILITIES_ID = 0x54;

	public static final byte MESG_STACKLIMIT_ID = 0x55;

	public static final byte MESG_SCRIPT_DATA_ID = 0x56;
	public static final byte MESG_SCRIPT_CMD_ID = 0x57;

	public static final byte MESG_ID_LIST_ADD_ID = 0x59;
	public static final byte MESG_ID_LIST_CONFIG_ID = 0x5A;
	public static final byte MESG_OPEN_RX_SCAN_ID = 0x5B;

	public static final byte MESG_EXT_CHANNEL_RADIO_FREQ_ID = 0x5C;  // OBSOLETE: (for 905 radio;
	public static final byte MESG_EXT_BROADCAST_DATA_ID = 0x5D;
	public static final byte MESG_EXT_ACKNOWLEDGED_DATA_ID = 0x5E;
	public static final byte MESG_EXT_BURST_DATA_ID = 0x5F;

	public static final byte MESG_CHANNEL_RADIO_TX_POWER_ID = 0x60;
	public static final byte MESG_GET_SERIAL_NUM_ID = 0x61;
	public static final byte MESG_GET_TEMP_CAL_ID = 0x62;
	public static final byte MESG_SET_LP_SEARCH_TIMEOUT_ID = 0x63;
	public static final byte MESG_SET_TX_SEARCH_ON_NEXT_ID = 0x64;
	public static final byte MESG_SERIAL_NUM_SET_CHANNEL_ID_ID = 0x65;
	public static final byte MESG_RX_EXT_MESGS_ENABLE_ID = 0x66;
	public static final byte MESG_RADIO_CONFIG_ALWAYS_ID = 0x67;
	public static final byte MESG_ENABLE_LED_FLASH_ID = 0x68;
	public static final byte MESG_XTAL_ENABLE_ID = 0x6D;
	public static final byte MESG_STARTUP_MESG_ID = 0x6F;
	public static final byte MESG_AUTO_FREQ_CONFIG_ID = 0x70;
	public static final byte MESG_PROX_SEARCH_CONFIG_ID = 0x71;

	public static final byte MESG_CUBE_CMD_ID = (byte) 0x80;

	public static final byte MESG_GET_PIN_DIODE_CONTROL_ID = (byte) 0x8D;
	public static final byte MESG_PIN_DIODE_CONTROL_ID = (byte) 0x8E;
	public static final byte MESG_FIT1_SET_AGC_ID = (byte) 0x8F;

	public static final byte MESG_FIT1_SET_EQUIP_STATE_ID = (byte) 0x91;

	// Sensrcore Messages
	public static final byte MESG_SET_CHANNEL_INPUT_MASK_ID = (byte) (0x90);
	public static final byte MESG_SET_CHANNEL_DATA_TYPE_ID = (byte) (0x91);
	public static final byte MESG_READ_PINS_FOR_SECT_ID = (byte) (0x92);
	public static final byte MESG_TIMER_SELECT_ID = (byte) (0x93);
	public static final byte MESG_ATOD_SETTINGS_ID = (byte) (0x94);
	public static final byte MESG_SET_SHARED_ADDRESS_ID = (byte) (0x95);
	public static final byte MESG_ATOD_EXTERNAL_ENABLE_ID = (byte) (0x96);
	public static final byte MESG_ATOD_PIN_SETUP_ID = (byte) (0x97);
	public static final byte MESG_SETUP_ALARM_ID = (byte) (0x98);
	public static final byte MESG_ALARM_VARIABLE_MODIFY_TEST_ID = (byte) (0x99);
	public static final byte MESG_PARTIAL_RESET_ID = (byte) (0x9A);
	public static final byte MESG_OVERWRITE_TEMP_CAL_ID = (byte) (0x9B);
	public static final byte MESG_SERIAL_PASSTHRU_SETTINGS_ID = (byte) (0x9C);

	public static final byte MESG_READ_SEGA_ID = (byte) (0xA0);
	public static final byte MESG_SEGA_CMD_ID = (byte) (0xA1);
	public static final byte MESG_SEGA_DATA_ID = (byte) (0xA2);
	public static final byte MESG_SEGA_ERASE_ID = (byte) (0xA3);
	public static final byte MESG_SEGA_WRITE_ID = (byte) (0xA4);
	public static final byte AVOID_USING_SYNC_BYTES_FOR_MESG_IDS = (byte) (0xA5);

	public static final byte MESG_SEGA_LOCK_ID = (byte) (0xA6);
	public static final byte MESG_FLASH_PROTECTION_CHECK_ID = (byte) (0xA7);
	public static final byte MESG_UARTREG_ID = (byte) (0xA8);
	public static final byte MESG_MAN_TEMP_ID = (byte) (0xA9);
	public static final byte MESG_BIST_ID = (byte) (0xAA);
	public static final byte MESG_SELFERASE_ID = (byte) (0xAB);
	public static final byte MESG_SET_MFG_BITS_ID = (byte) (0xAC);
	public static final byte MESG_UNLOCK_INTERFACE_ID = (byte) (0xAD);
	public static final byte MESG_SERIAL_ERROR_ID = (byte) (0xAE);
	public static final byte MESG_SET_ID_STRING_ID = (byte) (0xAF);

	public static final byte MESG_IO_STATE_ID = (byte) (0xB0);
	public static final byte MESG_CFG_STATE_ID = (byte) (0xB1);
	public static final byte MESG_BLOWFUSE_ID = (byte) (0xB2);
	public static final byte MESG_MASTERIOCTRL_ID = (byte) (0xB3);
	public static final byte MESG_PORT_GET_IO_STATE_ID = (byte) (0xB4);
	public static final byte MESG_PORT_SET_IO_STATE_ID = (byte) (0xB5);



	public static final byte MESG_SLEEP_ID = (byte) (0xC5);
	public static final byte MESG_GET_GRMN_ESN_ID = (byte) (0xC6);

	public static final byte MESG_DEBUG_ID = (byte) (0xF0);              // use 2 public static final byte sub-index identifier

	//////////////////////////////////////////////
	// Message Sizes
	//////////////////////////////////////////////
	public static final byte MESG_INVALID_SIZE = 0;

	public static final byte MESG_VERSION_SIZE = 13;
	public static final byte MESG_RESPONSE_EVENT_SIZE = 3;
	public static final byte MESG_CHANNEL_STATUS_SIZE = 2;

	public static final byte MESG_UNASSIGN_CHANNEL_SIZE = 1;
	public static final byte MESG_ASSIGN_CHANNEL_SIZE = 3;
	public static final byte MESG_CHANNEL_ID_SIZE = 5;
	public static final byte MESG_CHANNEL_MESG_PERIOD_SIZE = 3;
	public static final byte MESG_CHANNEL_SEARCH_TIMEOUT_SIZE = 2;
	public static final byte MESG_CHANNEL_RADIO_FREQ_SIZE = 2;
	public static final byte MESG_CHANNEL_RADIO_TX_POWER_SIZE = 2;
	public static final byte MESG_NETWORK_KEY_SIZE = 9;
	public static final byte MESG_RADIO_TX_POWER_SIZE = 2;
	public static final byte MESG_RADIO_CW_MODE_SIZE = 3;
	public static final byte MESG_RADIO_CW_INIT_SIZE = 1;
	public static final byte MESG_SYSTEM_RESET_SIZE = 1;
	public static final byte MESG_OPEN_CHANNEL_SIZE = 1;
	public static final byte MESG_CLOSE_CHANNEL_SIZE = 1;
	public static final byte MESG_REQUEST_SIZE = 2;

	public static final byte MESG_CAPABILITIES_SIZE = 6;
	public static final byte MESG_STACKLIMIT_SIZE = 2;

	public static final byte MESG_SCRIPT_DATA_SIZE = 10;
	public static final byte MESG_SCRIPT_CMD_SIZE = 3;

	public static final byte MESG_ID_LIST_ADD_SIZE = 6;
	public static final byte MESG_ID_LIST_CONFIG_SIZE = 3;
	public static final byte MESG_OPEN_RX_SCAN_SIZE = 1;
	public static final byte MESG_EXT_CHANNEL_RADIO_FREQ_SIZE = 3;

	public static final byte MESG_RADIO_CONFIG_ALWAYS_SIZE = 2;
	public static final byte MESG_RX_EXT_MESGS_ENABLE_SIZE = 2;
	public static final byte MESG_SET_TX_SEARCH_ON_NEXT_SIZE = 2;
	public static final byte MESG_SET_LP_SEARCH_TIMEOUT_SIZE = 2;

	public static final byte MESG_SERIAL_NUM_SET_CHANNEL_ID_SIZE = 3;
	public static final byte MESG_ENABLE_LED_FLASH_SIZE = 2;
	public static final byte MESG_GET_SERIAL_NUM_SIZE = 4;
	public static final byte MESG_GET_TEMP_CAL_SIZE = 4;
	public static final byte MESG_CLOCK_DRIFT_DATA_SIZE = 9;

	public static final byte MESG_AGC_CONFIG_SIZE = 2;
	public static final byte MESG_RUN_SCRIPT_SIZE = 2;
	public static final byte MESG_ANTLIB_CONFIG_SIZE = 2;
	public static final byte MESG_XTAL_ENABLE_SIZE = 1;
	public static final byte MESG_STARTUP_MESG_SIZE = 1;
	public static final byte MESG_AUTO_FREQ_CONFIG_SIZE = 4;
	public static final byte MESG_PROX_SEARCH_CONFIG_SIZE = 2;

	public static final byte MESG_GET_PIN_DIODE_CONTROL_SIZE = 1;
	public static final byte MESG_PIN_DIODE_CONTROL_ID_SIZE = 2;
	public static final byte MESG_FIT1_SET_EQUIP_STATE_SIZE = 2;
	public static final byte MESG_FIT1_SET_AGC_SIZE = 3;

	public static final byte MESG_READ_SEGA_SIZE = 2;
	public static final byte MESG_SEGA_CMD_SIZE = 3;
	public static final byte MESG_SEGA_DATA_SIZE = 10;
	public static final byte MESG_SEGA_ERASE_SIZE = 0;
	public static final byte MESG_SEGA_WRITE_SIZE = 3;
	public static final byte MESG_SEGA_LOCK_SIZE = 1;
	public static final byte MESG_FLASH_PROTECTION_CHECK_SIZE = 1;
	public static final byte MESG_UARTREG_SIZE = 2;
	public static final byte MESG_MAN_TEMP_SIZE = 2;
	public static final byte MESG_BIST_SIZE = 6;
	public static final byte MESG_SELFERASE_SIZE = 2;
	public static final byte MESG_SET_MFG_BITS_SIZE = 2;
	public static final byte MESG_UNLOCK_INTERFACE_SIZE = 1;
	public static final byte MESG_SET_SHARED_ADDRESS_SIZE = 3;

	public static final byte MESG_GET_GRMN_ESN_SIZE = 5;

	public static final byte MESG_IO_STATE_SIZE = 2;
	public static final byte MESG_CFG_STATE_SIZE = 2;
	public static final byte MESG_BLOWFUSE_SIZE = 1;
	public static final byte MESG_MASTERIOCTRL_SIZE = 1;
	public static final byte MESG_PORT_SET_IO_STATE_SIZE = 5;


	public static final byte MESG_SLEEP_SIZE = 1;


	public static final byte MESG_EXT_DATA_SIZE = 13;
	public static final byte CHANNEL_TYPE_MASTER = 0;
	public static final byte CHANNEL_TYPE_SLAVE = 1;
	public static final byte CHANNEL_TYPE_INVALID = 2;

	public static final byte CHANNEL_0 = 0;
	public static final byte CHANNEL_1 = 1;
	public static final byte CHANNEL_2 = 2;
	public static final byte CHANNEL_3 = 3;

	public static final byte TOGGLE_MASK = (byte) 0x80;

	// Pages Garminpuls
	public static final byte GARMINPULS_PAGE_1 = 1;
	public static final byte GARMINPULS_PAGE_2 = 2;
	public static final byte GARMINPULS_PAGE_3 = 3;
	public static final byte GARMINPULS_PAGE_4 = 4;
	// Pages GarminSPEEDCadence = Combined Bike Speed and Cadence Data
	public static final byte GARMINSPEEDCADENCE_PAGE_0 = 0;
	// Pages Wahoo KICKR
	public static final byte WK_PAGE_CALIBRATION = 0x01;
	public static final byte WK_PAGE_GET_SET_PARAMETERS = 0x02;
	public static final byte WK_PAGE_POWER = 0x10;
	public static final byte WK_PAGE_WHEEL_TORQUE = 0x11;
	public static final byte WK_PAGE_CRANK_TORQUE = 0x12;
	public static final byte WK_PAGE_TEPS = 0x13;
	public static final byte WK_PAGE_CTF = 0x20;
	public static final byte WK_PAGE_KICKR_81 = 0x51;		// SW-Version und SNR
	public static final byte WK_PAGE_KICKR_RESPONSE = (byte) 0xF0;
	// Pages ANT+ FE-C
	public static final byte ANTFEC_PAGE_GENERAL = 0x10;
	public static final byte ANTFEC_PAGE_GENSET = 0x11;
	public static final byte ANTFEC_PAGE_BIKE_STAT = 0x15;
	public static final byte ANTFEC_PAGE_SPECIFIC = 0x19;
	public static final byte ANTFEC_PAGE_TARGET_POWER = 0x31;
	public static final byte ANTFEC_PAGE_MANUID = 0x50;
	public static final byte ANTFEC_PAGE_PRODID = 0x51;
	public static final byte ANTFEC_PAGE_REQUEST_DATA = 0x46;
	
	public static final byte ANT_DATA_SIZE = 8;                    // ANT message payload size.
	//////////////////////////////////////////////
	// ANT Clock Frequency
	//////////////////////////////////////////////
	public static final int ANT_CLOCK_FREQUENCY = 32768; //ulong   // ANT system clock frequency

	//////////////////////////////////////////////
	// ANT Message Payload Size
	//////////////////////////////////////////////
	public static final byte ANT_STANDARD_DATA_PAYLOAD_SIZE = 8;

	//////////////////////////////////////////////
	// ANT LIBRARY Extended Data Message Fields
	// NOTE: You must check the extended message
	// bitfield first to find out which fields
	// are present before accessing them!
	//////////////////////////////////////////////
	public static final byte ANT_EXT_MESG_DEVICE_ID_FIELD_SIZE = 4;

	//////////////////////////////////////////////
	// ANT Extended Data Message Bifield Definitions
	//////////////////////////////////////////////
	public static final byte ANT_EXT_MESG_BITFIELD_DEVICE_ID = (byte) (0x80); 

	// 4 bits free reserved set to 0
	public static final byte ANT_EXT_MESG_BIFIELD_EXTENSION = 0x01;

	public static final byte ANT_EXT_MESG_BITFIELD_OVERWRITE_SHARED_ADR = 0x10; 
	public static final byte ANT_EXT_MESG_BITFIELD_TRANSMISSION_TYPE = 0x08; 



	//////////////////////////////////////////////
	// ID Definitions
	//////////////////////////////////////////////
	public static final byte ANT_ID_SIZE = 4;
	public static final byte ANT_ID_TRANS_TYPE_OFFSET = 3;
	public static final byte ANT_ID_DEVICE_TYPE_OFFSET = 2;
	public static final byte ANT_ID_DEVICE_NUMBER_HIGH_OFFSET = 1;
	public static final byte ANT_ID_DEVICE_NUMBER_LOW_OFFSET = 0;
	public static final byte ANT_ID_DEVICE_TYPE_PAIRING_FLAG = (byte) (0x80);

	public static final byte ANT_TRANS_TYPE_SHARED_ADDR_MASK = 0x03;
	public static final byte ANT_TRANS_TYPE_1_BYTE_SHARED_ADDRESS = 0x02;
	public static final byte ANT_TRANS_TYPE_2_BYTE_SHARED_ADDRESS = 0x03;


	//////////////////////////////////////////////
	// Assign Channel Parameters
	//////////////////////////////////////////////
	public static final byte PARAMETER_RX_NOT_TX  = 0x00;
	public static final byte PARAMETER_TX_NOT_RX = 0x10;
	public static final byte PARAMETER_SHARED_CHANNEL = 0x20;
	public static final byte PARAMETER_NO_TX_GUARD_BAND = 0x40;
	public static final byte PARAMETER_ALWAYS_RX_WILD_CARD_SEARCH_ID = 0x40; 
	public static final byte PARAMETER_RX_ONLY = 0x40;

	//////////////////////////////////////////////
	// Ext. Assign Channel Parameters
	//////////////////////////////////////////////
	public static final byte EXT_PARAM_ALWAYS_SEARCH = 0x01;
	public static final byte EXT_PARAM_FREQUENCY_AGILITY = 0x04;

	//////////////////////////////////////////////
	// Radio TX Power Definitions
	//////////////////////////////////////////////
	public static final byte RADIO_TX_POWER_MASK = 0x03;
	public static final byte RADIO_TX_POWER_MINUS20DB = 0x00;
	public static final byte RADIO_TX_POWER_MINUS10DB = 0x01;
	public static final byte RADIO_TX_POWER_MINUS5DB = 0x02;
	public static final byte RADIO_TX_POWER_0DB = 0x03;

	//////////////////////////////////////////////
	// Channel Status
	//////////////////////////////////////////////
	public static final byte STATUS_CHANNEL_STATE_MASK = 0x03;
	public static final byte STATUS_UNASSIGNED_CHANNEL = 0x00;
	public static final byte STATUS_ASSIGNED_CHANNEL = 0x01;
	public static final byte STATUS_SEARCHING_CHANNEL = 0x02;
	public static final byte STATUS_TRACKING_CHANNEL = 0x03;

	//////////////////////////////////////////////
	// Standard capabilities defines
	//////////////////////////////////////////////
	public static final byte CAPABILITIES_NO_RX_CHANNELS = 0x01;
	public static final byte CAPABILITIES_NO_TX_CHANNELS = 0x02;
	public static final byte CAPABILITIES_NO_RX_MESSAGES = 0x04;
	public static final byte CAPABILITIES_NO_TX_MESSAGES = 0x08;
	public static final byte CAPABILITIES_NO_ACKD_MESSAGES = 0x10;
	public static final byte CAPABILITIES_NO_BURST_TRANSFER = 0x20;

	//////////////////////////////////////////////
	// Advanced capabilities defines
	//////////////////////////////////////////////
	public static final byte CAPABILITIES_OVERUN_UNDERRUN = 0x01;  
	public static final byte CAPABILITIES_NETWORK_ENABLED = 0x02;
	public static final byte CAPABILITIES_AP1_VERSION_2 = 0x04;    
	public static final byte CAPABILITIES_SERIAL_NUMBER_ENABLED = 0x08;
	public static final byte CAPABILITIES_PER_CHANNEL_TX_POWER_ENABLED = 0x10;
	public static final byte CAPABILITIES_LOW_PRIORITY_SEARCH_ENABLED = 0x20;
	public static final byte CAPABILITIES_SCRIPT_ENABLED = 0x40;
	public static final byte CAPABILITIES_SEARCH_LIST_ENABLED = (byte) 0x80;

	//////////////////////////////////////////////
	// Advanced capabilities 2 defines
	//////////////////////////////////////////////
	public static final byte CAPABILITIES_LED_ENABLED = 0x01;
	public static final byte CAPABILITIES_EXT_MESSAGE_ENABLED = 0x02;
	public static final byte CAPABILITIES_SCAN_MODE_ENABLED  = 0x04;
	public static final byte CAPABILITIES_RESERVED = 0x08;
	public static final byte CAPABILITIES_PROX_SEARCH_ENABLED = 0x10;
	public static final byte CAPABILITIES_EXT_ASSIGN_ENABLED = 0x20;
	public static final byte CAPABILITIES_FREE_1 = 0x40;
	public static final byte CAPABILITIES_FIT1_ENABLED = (byte) (0x80);

	//////////////////////////////////////////////
	// Advanced capabilities 3 defines
	//////////////////////////////////////////////
	public static final byte CAPABILITIES_SENSRCORE_ENABLED = 0x01;
	public static final byte CAPABILITIES_RESERVED_1 = 0x02;
	public static final byte CAPABILITIES_RESERVED_2 = 0x04;
	public static final byte CAPABILITIES_RESERVED_3 = 0x08;


	//////////////////////////////////////////////
	// Burst Message Sequence
	//////////////////////////////////////////////
	public static final byte CHANNEL_NUMBER_MASK = 0x1F;
	public static final byte SEQUENCE_NUMBER_MASK = (byte) (0xE0);
	public static final byte SEQUENCE_NUMBER_ROLLOVER = 0x60;
	public static final byte SEQUENCE_FIRST_MESSAGE = 0x00;
	public static final byte SEQUENCE_LAST_MESSAGE = (byte) (0x80);
	public static final byte SEQUENCE_NUMBER_INC = 0x20;

	//////////////////////////////////////////////
	// Control Message Flags
	//////////////////////////////////////////////
	public static final byte BROADCAST_CONTROL_BYTE = 0x00;
	public static final byte ACKNOWLEDGED_CONTROL_BYTE = (byte) (0xA0);

	//////////////////////////////////////////////
	// Response / Event Codes
	//////////////////////////////////////////////
	public static final byte RESPONSE_NO_ERROR = 0x00;
	public static final byte NO_EVENT = 0x00;

	public static final byte EVENT_RX_SEARCH_TIMEOUT = 0x01;
	public static final byte EVENT_RX_FAIL = 0x02;
	public static final byte EVENT_TX = 0x03;
	public static final byte EVENT_TRANSFER_RX_FAILED = 0x04;
	public static final byte EVENT_TRANSFER_TX_COMPLETED = 0x05;
	public static final byte EVENT_TRANSFER_TX_FAILED = 0x06;
	public static final byte EVENT_CHANNEL_CLOSED = 0x07;
	public static final byte EVENT_RX_FAIL_GO_TO_SEARCH = 0x08;
	public static final byte EVENT_CHANNEL_COLLISION = 0x09;
	public static final byte EVENT_TRANSFER_TX_START = 0x0A;               // a pending transmit transfer has begun

	public static final byte EVENT_CHANNEL_ACTIVE = 0x0F;

	public static final byte EVENT_TRANSFER_TX_NEXT_MESSAGE = 0x11;        // only enabled in FIT1

	public static final byte CHANNEL_IN_WRONG_STATE = 0x15;                // returned on attempt to perform an action from the wrong channel state
	public static final byte CHANNEL_NOT_OPENED = 0x16;                    // returned on attempt to communicate on a channel that is not open
	public static final byte CHANNEL_ID_NOT_SET = 0x18;                    // returned on attempt to open a channel without setting the channel ID
	public static final byte CLOSE_ALL_CHANNELS = 0x19;                    // returned when attempting to start scanning mode, when channels are still open

	public static final byte TRANSFER_IN_PROGRESS = 0x1F;                  // returned on attempt to communicate on a channel with a TX transfer in progress
	public static final byte TRANSFER_SEQUENCE_NUMBER_ERROR = 0x20;        // returned when sequence number is out of order on a Burst transfer
	public static final byte TRANSFER_IN_ERROR = 0x21;
	public static final byte TRANSFER_BUSY = 0x22;

	public static final byte MESSAGE_SIZE_EXCEEDS_LIMIT = 0x27;            // returned if a data message is provided that is too large
	public static final byte INVALID_MESSAGE = 0x28;                       // returned when the message has an invalid parameter
	public static final byte INVALID_NETWORK_NUMBER = 0x29;                // returned when an invalid network number is provided
	public static final byte INVALID_LIST_ID = 0x30;                       // returned when the provided list ID or size exceeds the limit
	public static final byte INVALID_SCAN_TX_CHANNEL = 0x31;               // returned when attempting to transmit on channel 0 when in scan mode
	public static final byte INVALID_PARAMETER_PROVIDED = 0x33;            // returned when an invalid parameter is specified in a configuration message

	public static final byte EVENT_QUE_OVERFLOW = 0x35;                    // ANT event que has overflowed and lost 1 or more events

	public static final byte EVENT_CLK_ERROR = 0x36;                       

	public static final byte SCRIPT_FULL_ERROR = 0x40;                     // error writing to script, memory is full
	public static final byte SCRIPT_WRITE_ERROR = 0x41;                    // error writing to script, public ints not written correctly
	public static final byte SCRIPT_INVALID_PAGE_ERROR = 0x42;             // error accessing script page
	public static final byte SCRIPT_LOCKED_ERROR = 0x43;                   // the scripts are locked and can't be dumped

	public static final byte NO_RESPONSE_MESSAGE = 0x50;                   // returned to the Command_SerialMessageProcess function, so no reply message is generated
	public static final byte RETURN_TO_MFG = 0x51;                         // default return to any mesg when the module determines that the mfg procedure has not been fully completed

	public static final byte FIT_ACTIVE_SEARCH_TIMEOUT = 0x60;             // Fit1 only event added for timeout of the pairing state after the Fit module becomes active
	public static final byte FIT_WATCH_PAIR = 0x61;                        // Fit1 only
	public static final byte FIT_WATCH_UNPAIR = 0x62;                      // Fit1 only

	// Internal only events below this point
	public static final byte INTERNAL_ONLY_EVENTS = (byte) (0x80);
	public static final byte EVENT_RX = (byte) (0x80);                     // INTERNAL: Event for a receive message
	public static final byte EVENT_NEW_CHANNEL = (byte) (0x81);            // INTERNAL: EVENT for a new active channel
	public static final byte EVENT_PASS_THRU = (byte) (0x82);              // INTERNAL: Event to allow an upper stack events to pass through lower stacks
	public static final byte EVENT_TRANSFER_RX_COMPLETED = (byte) (0x83);  // INTERNAL: Event for RX completed that indicates ANT library is ready for new messasges

	public static final byte EVENT_BLOCKED = (byte) (0xFF);                // INTERNAL: Event to replace any event we do not wish to go out, will also zero the size of the Tx message

	///////////////////////////////////////////////////////
	// Script Command Codes
	///////////////////////////////////////////////////////
	public static final byte SCRIPT_CMD_FORMAT = 0x00;
	public static final byte SCRIPT_CMD_DUMP = 0x01;
	public static final byte SCRIPT_CMD_SET_DEFAULT_SECTOR = 0x02;
	public static final byte SCRIPT_CMD_END_SECTOR = 0x03;
	public static final byte SCRIPT_CMD_END_DUMP = 0x04;
	public static final byte SCRIPT_CMD_LOCK = 0x05;

	///////////////////////////////////////////////////////
	// Reset Mesg Codes
	///////////////////////////////////////////////////////
	public static final byte RESET_FLAGS_MASK = (byte) (0xE0);
	public static final byte RESET_SUSPEND = (byte) (0x80); 
	public static final byte RESET_SYNC = 0x40;    
	public static final byte RESET_CMD = 0x20;  
	public static final byte RESET_WDT = 0x02;
	public static final byte RESET_RST = 0x01;
	public static final byte RESET_POR = 0x00;

	//////////////////////////////////////////////
	// PC Application Event Codes
	//////////////////////////////////////////////
	//NOTE: These events are not generated by the embedded ANT module

	public static final byte EVENT_RX_BROADCAST = (byte) 0x9A;                    // returned when module receives broadcast data
	public static final byte EVENT_RX_ACKNOWLEDGED = (byte) 0x9B;                 // returned when module receives acknowledged data
	public static final byte EVENT_RX_BURST_PACKET = (byte) 0x9C;                 // returned when module receives burst data

	public static final byte EVENT_RX_EXT_BROADCAST = (byte) 0x9D;                // returned when module receives broadcast data
	public static final byte EVENT_RX_EXT_ACKNOWLEDGED = (byte) 0x9E;             // returned when module receives acknowledged data
	public static final byte EVENT_RX_EXT_BURST_PACKET = (byte) 0x9F;             // returned when module receives burst data

	public static final byte EVENT_RX_FLAG_BROADCAST = (byte) 0xA3;               // returned when module receives broadcast data with flag attached
	public static final byte EVENT_RX_FLAG_ACKNOWLEDGED = (byte) 0xA4;            // returned when module receives acknowledged data with flag attached
	public static final byte EVENT_RX_FLAG_BURST_PACKET = (byte) 0xA5;            // returned when module receives burst data with flag attached

	// public static final byte ENABLE_EXTENDED_MESSAGES;

	public static final int USER_BAUDRATE = 50000;  // For AP1, use 50000; for AT3/AP2, use 57600
	public static final int USER_BAUDRATE2 = 57600;
	
	public static final byte USER_RADIOFREQ = 57;     	// RF Frequency + 2400 MHz  Garmin Pulser
	public static final byte USER_RFGENBRAKE = 60;    	// Tacx Genius Brake
	public static final byte USER_RFGENDISP = 60;     	// Tacx Genius Display
	public static final byte USER_RFVORBRAKE = 72;     	// Tacx Vortex Brake
	public static final byte USER_RFVORDISP = 78; 		// Tacx Vortex Display
	public static final byte USER_RFBUSHBRAKE = 60;    	// Tacx Bushido Brake
	public static final byte USER_RFBUSHDISP = 60;     	// Tacx Bushido Display
	public static final byte USER_RFKICKR = 57;     	// RF Frequency Wahoo KICKR
	public static final byte USER_RFANTFEC = 57;     	// RF Frequency ANT+ FE-C
	
	// Channelperiods
	public static final int USER_CHPERGARPULS = 8070;	// Message/Channel Period Garmin Pulser
	public static final int USER_CHPERGENBRAKE = 4096;	// Genius Brake
	public static final int USER_CHPERGENDISP = 4096;	// Genius Display
	public static final int USER_CHPERVORBRAKE = 4096;	// Vortex Brake
	public static final int USER_CHPERVORDISP = 3840;	// Vortex Display
	public static final int USER_CHPERBUSHBRAKE = 4096;	// Bushido Brake
	public static final int USER_CHPERBUSHDISP = 4096;	// Bushido Display
	public static final int USER_CHPERKICKR = 8182;		// Wahoo KICKR
	public static final int USER_CHPERCADENCESPEED = 8086;	// Message/Channel Period Combined Speed and Cadence
	public static final int USER_CHPERCADENCE = 8102;	// Message/Channel Period Cadence
	public static final int USER_CHPERANTFEC = 8192;	// ANT+ FE-C
	
	public static final byte USER_ANTCHANNEL0 = 0;     	// ANT channel 0 Garmin Pulser / TACX Display
	public static final byte USER_ANTCHANNEL1 = 1;     	// Wahoo Kickr
	public static final byte USER_ANTCHANNEL2 = 2;     	// not used
	public static final byte USER_ANTCHANNEL3 = 3;     	// not used
	public static final int  USER_DEVICENUM = 0;     	// Device number (19515)
	public static final byte USER_DEVICETYPE0 = 0;      // Device type
	public static final byte USER_DEVICETYPEPULS = 120; // Device type Pulser
	public static final byte USER_DEVICETYPECADENCESPEED = 121;// Device type Cadence & Speed
	public static final byte USER_DEVICETYPECADENCE = 122;// Device type Cadence
	public static final byte USER_TRANSTYPE = 1;      	// Transmission type

	// ANT+: 0xB9, 0xA5, 0x21, 0xFB, 0xBD, 0x72, 0xC3, 0x45
	// Public: 0xE8, 0xE4, 0x21, 0x3B, 0x55, 0x7A, 0x67, 0xC1
	//  Garmin: A8A423B9F55E63C1
	public static final byte[] NULL_NETWORK_KEY  = {(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};	// TACX
	public static final byte[] GARMIN_NETWORK_KEY  = {(byte) 0xA8, (byte) 0xA4, 0x23, (byte) 0xB9, (byte) 0xF5, 0x5E, (byte) 0x63, (byte) 0xC1};
	public static final byte[] USER_NETWORK_KEY  = {(byte) 0xB9, (byte) 0xA5, 0x21, (byte) 0xFB, (byte) 0xBD, 0x72, (byte) 0xC3, 0x45};	// Garmin, Wahoo KICKR
	public static final byte[] TACX_NETWORK_KEY  = {(byte) 0xE8, (byte) 0xE4, (byte) 0x21, (byte) 0x3B, (byte) 0x55, (byte) 0x7A, (byte) 0x67, (byte) 0xC1};
	// Gerätezuordnung:
	// TACX: PUBLIC_ oder NULL_ 
	// Garmin Puls: USER_
	
	public static final byte USER_NETWORK_NUM = 0;      // The network key is assigned to this network number

	public static final byte MAX_CHANNEL_EVENT_SIZE = AntDefines.MESG_MAX_SIZE_VALUE;     // Channel event buffer size, assumes worst case extended message size
	public static final byte MAX_RESPONSE_SIZE  = AntDefines.MESG_MAX_SIZE_VALUE ;    // Protocol response buffer size

	// WahooKICKR Opcodes
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_RESISTANCE_MODE = 0x40;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_STANDARD_MODE = 0x41;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_ERG_MODE = 0x42;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_SIM_MODE = 0x43;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_CRR = 0x44;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_C = 0x45;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_GRADE = 0x46;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_WIND_SPEED = 0x47;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_WHEEL_CIRCUMFERENCE = 0x48;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_INIT_SPINDOWN = 0x49;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_READ_MODE = 0x4A;
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SET_FTP_MODE = 0x4B;
    // 0x4C-0x4E reserved.
	public static final byte WF_WCPMCP_OPCODE_TRAINER_CONNECT_ANT_SENSOR = 0x4F;
    // 0x51-0x59 reserved.
	public static final byte WF_WCPMCP_OPCODE_TRAINER_SPINDOWN_RESULT = 0x5A;
	// zusätzliche Definitionen für den KICKR
	public static final byte ANT_COMMAND_BURST = 0x48;
	public static final byte ANTPLUS_RESERVED = (byte) 0xFF;
	public static final byte WF_ANT_MANUFACTURER_ID = 32;
	public static final int  WF_API_PRODUCT_ID = 0xAAAA;
	public static final byte WF_KICKR_DEVICE_TYPE = 0x0B;
	public static final byte WF_KICKR_TRANSMISSION_TYPE = (byte) 0xA5;
	// Definitionen für ANT+ FE-C
	public static final byte ANTFEC_DEVICE_TYPE = 0x11;
//	public static final byte ANTFEC_DEVICE_NUM = 0x1;
//	public static final byte ANTFEC_TRANSMISSION_TYPE = 0x05;
	public static final byte ANTFEC_DEVICE_NUM = 0x0;
	public static final byte ANTFEC_TRANSMISSION_TYPE = 0x0;
	

	// Indexes into message recieved from ANT
	public static final byte MESSAGE_BUFFER_DATA1_INDEX = 0;
	public static final byte MESSAGE_BUFFER_DATA2_INDEX = 1;
	public static final byte MESSAGE_BUFFER_DATA3_INDEX = 2;
	public static final byte MESSAGE_BUFFER_DATA4_INDEX = 3;
	public static final byte MESSAGE_BUFFER_DATA5_INDEX = 4;
	public static final byte MESSAGE_BUFFER_DATA6_INDEX = 5;
	public static final byte MESSAGE_BUFFER_DATA7_INDEX = 6;
	public static final byte MESSAGE_BUFFER_DATA8_INDEX = 7;
	public static final byte MESSAGE_BUFFER_DATA9_INDEX = 8;
	public static final byte MESSAGE_BUFFER_DATA10_INDEX = 9;
	public static final byte MESSAGE_BUFFER_DATA11_INDEX = 10;
	public static final byte MESSAGE_BUFFER_DATA12_INDEX = 11;
	public static final byte MESSAGE_BUFFER_DATA13_INDEX = 12;
	public static final byte MESSAGE_BUFFER_DATA14_INDEX = 13;
}