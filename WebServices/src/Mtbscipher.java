import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

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
 * Mtbscipher.java: Verschlüsselungsfunktionen
 *****************************************************************************
 *
 * Wurde verwendet zum codieren/decodieren der "Seriennummer" des MTB-Simulators.
 * Für RoomSports wird es nur noch zur Teilnahme am MTBS Onlinetraining benötigt.
 * 
 */

public class Mtbscipher {
    private static String skey = "hzqsaI9g";
    private static String coding = "DES";
    //private static boolean filemode = false;
    //private static boolean decode = false;
    
	/**
	 * verschlüsseln mittels DES-Verschlüsselung
	 * @param bytes
	 * @param out
	 * @throws Exception
	 */
	static void encode(byte[] bytes, OutputStream out) throws Exception { 
		Cipher c = Cipher.getInstance(coding); 
		Key k = new SecretKeySpec(skey.getBytes(), coding); 
		c.init(Cipher.ENCRYPT_MODE, k); 

		OutputStream cos = new CipherOutputStream(out, c); 
		cos.write(bytes); 
		cos.close(); 
	} 
	 
	/**
	 * dekodieren mittels DES Verschlüsselung
	 * @param is
	 * @return
	 * @throws Exception
	 */
	static byte[] decode(InputStream is) throws Exception { 
	    Cipher c = Cipher.getInstance(coding); 
	    Key k = new SecretKeySpec(skey.getBytes(), coding); 
	    c.init(Cipher.DECRYPT_MODE, k); 
	 
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
	    CipherInputStream cis = new CipherInputStream(is, c); 
	 
	    for (int b; (b = cis.read()) != -1;) 
	      bos.write( b ); 
	 
	    cis.close(); 
	    return bos.toByteArray(); 
	} 
	
	/**
	 * Mainprogram zum kodieren/dekodieren
	 * 1. Parameter: -s = Stringmode, -f = Filemode
	 * 2. Parameter: -e = encode, -d = decode
	 * 3. Parameter: String bzw. Eingangsdateiname
	 * 4. Parameter: Ausgangsdateiname (nur beim Filemode)
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		if (args.length > 4 || args.length < 3) {
			System.out.println("Aufruf bitte mit: java -jar mtbscipher.jar <-s/-f> <-e/-d> <String/Datei> [<Ausgabedatei> (bei -f)] !");
			return;
		}
		
		if (args[0].equalsIgnoreCase("-s") || args[1].equalsIgnoreCase("-s"))   
			filemode = false;
		else
			filemode = true;
		
		if (args[0].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("-d"))   
			decode = true;
		else
			decode = false;

		try {
			if (!filemode) {
				if (!decode) { // encode
					ByteArrayOutputStream out = new ByteArrayOutputStream(); 
					encode(args[2].getBytes(), out);

					String s = new BASE64Encoder().encode(out.toByteArray()); 
					System.out.println(s);			
				} else {		// decode
					byte[] decode = new BASE64Decoder().decodeBuffer(args[2]); 
					InputStream is = new ByteArrayInputStream(decode); 
					System.out.println(new String(decode(is))); 				
				}
			} else {			// Filemode = ganze Datei verarbeiten
				String zeile = new String();
				String inhalt = new String();
				BufferedReader reader = new BufferedReader(new FileReader(args[2]));
				while ((zeile = reader.readLine()) != null) {
					inhalt += zeile;
				}

				FileWriter fw = new FileWriter(args[3]);
				PrintWriter pw = new PrintWriter(fw, true);
				
				if (!decode) { // encode
					ByteArrayOutputStream out = new ByteArrayOutputStream(); 
					encode(inhalt.getBytes(), out);

					String s = new BASE64Encoder().encode(out.toByteArray()); 
					//System.out.println(s);
					pw.println(s);
				} else {		// decode
					byte[] decode = new BASE64Decoder().decodeBuffer(inhalt); 
					InputStream is = new ByteArrayInputStream(decode); 
					//System.out.println(new String(decode(is, skey))); 
					pw.println(new String(decode(is)));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	*/
}
