/**
 * Locus log parser.
 *
 * Transcrito a java desde la versión en python de Don Coleman.
 * https://github.com/don/locus/blob/master/locus.py
 *
 * @author Francisco Javier Morón Sánchez
 */
package locus;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dragu
 */
public class Locus {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //
/* Sample log ---
         --- Log Start ---
         $PMTK001,314,3*36
         $PMTKLOX,0,43*6E
         $PMTKLOX,1,0,0100010A,1F000000,0F000000,0000100A,00000000,00000000,0000007F,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,00FC8C1C,288C4850,02760620,42642D9F,C2B90001,378C4850,027D0720,42532D9F,C2CB0051*23
         $PMTKLOX,1,1,468C4850,02B50920,42652D9F,C2E600FD,558C4850,02E30B20,427A2D9F,C2120150,648C4850,02C30D20,42892D9F,C22D018B,738C4850,022A0E20,422E2E9F,C23701C8,828C4850,02880E20,42912E9F,C22D013E,918C4850,02561020,428D2E9F,C22201FE*20
         $PMTKLOX,1,2,A08C4850,02D91120,42CA2E9F,C2270103,AF8C4850,02F01120,42C52E9F,C22D0120,BE8C4850,02F01120,42C72E9F,C22E0130,CD8C4850,02FF1120,423D2F9F,C22901B0,DC8C4850,02D01120,427F309F,C22401DE,EB8C4850,023C1120,42EC319F,C2210192*27
         $PMTKLOX,1,3,FA8C4850,02AA1020,42A8329F,C2200152,098D4850,02930E20,423B329F,C21C0128,188D4850,02E80D20,4285329F,C21501F6,278D4850,02BF0D20,42F5329F,C21401EF,368D4850,02B70D20,4208339F,C2160108,458D4850,021E0D20,421D339F,C21C01CD*22
         $PMTKLOX,1,4,548D4850,02F10A20,42EF329F,C22201F9,638D4850,02E30820,42E2339F,C23201C2,728D4850,02530620,428A349F,C23A010A,818D4850,02680320,4202359F,C2350141,908D4850,028D0120,42A8359F,C23C0114,9F8D4850,02B50020,42F3359F,C23F017A*27
         $PMTKLOX,1,5,AE8D4850,0462FE1F,42DF369F,C244010F,BD8D4850,04FBFB1F,4235389F,C2330113,CC8D4850,0490FA1F,42F8399F,C23101C6,DB8D4850,04BCF91F,42E13B9F,C23501E1,EA8D4850,04E8F81F,42C83D9F,C23D01A2,F98D4850,04EBF71F,42973F9F,C24A0197*22
         $PMTKLOX,1,6,088E4850,04F2F51F,423F419F,C25601B4,178E4850,04FDF21F,426C429F,C26101C4,268E4850,0485EF1F,42E1429F,C257012B,358E4850,0475EC1F,4216449F,C266010B,448E4850,0479E91F,4292459F,C27001E0,538E4850,047FE61F,420C479F,C27A0168*5C
         $PMTKLOX,2*47
         $PMTK001,622,3*36
         */
        List<Coordenada> coordenadas = parseFile("/home/dragu/NetBeansProjects/Locus/src/locus/dump.txt");
        List<Coordenada> coordlimpias = new ArrayList<Coordenada>();
        for (Coordenada c : coordenadas) {
            if (c.getFix() < 5) {
                coordlimpias.add(c);
            }
        }
        for (Coordenada c : coordlimpias) {
            System.out.println(c.toString());
        }

    }

    public static int[] bytearray2intarray(byte[] barray) {
        int[] iarray = new int[barray.length];
        int i = 0;
        for (byte b : barray) {
            iarray[i++] = b & 0xff;
        }
        return iarray;
    }

    private static List<Coordenada> parseFile(String filename) {
        File archivo = new File(filename);
        FileReader fr = null;
        BufferedReader br;
        List<Coordenada> coords = null;
        try {
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);
            String linea;
            coords = new ArrayList<>();

            while ((linea = br.readLine()) != null) {
                List resultados = parseLine(linea);
                if (!resultados.isEmpty()) {
                    coords.addAll(resultados);
                }
            }
            return coords;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Locus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Locus.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //cerramos el fichero salga o no una exception
            try {
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e) {
                Logger.getLogger(Locus.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return coords;
    }

    private static List parseLine(String linea) {

        List<Coordenada> records = new ArrayList<>();
        if (linea.startsWith("$PMTKLOX,1")) {
            String[] split = linea.split("\\*");
            String data = split[0];
            String actual_checksum = split[1];
            String generated_checksum = checksum(data);
            if (actual_checksum.equals(generated_checksum)) {
                /*  remove the first 3 parts - command, type, line_number
                 following this 8 byte hex strings (max 24)
                 */
                String[] parts = data.split(",");
                String dataFields = "";
                for (int i = 3; i < parts.length; i++) {
                    dataFields = dataFields + parts[i];
                }
                int chunksize = 32; //Basic logging
                while (dataFields.length() >= chunksize) {
                    String sub = dataFields.substring(0, chunksize);
                    byte[] bytes = hexStringToByteArray(sub);
                    int[] bytedecimal = bytearray2intarray(bytes);
                    Coordenada record = parseBasicRecord(bytedecimal);
                    records.add(record);
                    dataFields = dataFields.substring(chunksize);
                }
            } else {
                System.out.println("WARNING: Checksum failed. Expected " + actual_checksum + " but calculated " + generated_checksum + " for " + data);
            }
        }
        return records;
    }

    //Cambia de un string de bytes a un byte array
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String checksum(String data) {
        int check = 0;
        //XOR all the chars in the line except leading $
        for (int i = 1; i < data.length(); i++) {
            check = check ^ data.charAt(i);
        }
        String toHexString = Integer.toHexString(check);
        return toHexString.toUpperCase();
    }

    /*
     #
     # Basic Record - 16 bytes
     # 0 - 3 timestamp
     # 4 fix flag
     # 5 - 8 latitude
     # 9 - 12 longitude
     # 13 - 14 height
     */
    private static Coordenada parseBasicRecord(int[] bytes) {

        long timestamp = parseLong(Arrays.copyOfRange(bytes, 0, 4));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        int fix = bytes[4]; // TODO bit flag     unsigned char u1VALID = 0x00;  // 0:NoFix , 1: Fix, 2: DGPS, 6: Estimated
        double latitud = parseDouble(Arrays.copyOfRange(bytes, 5, 9));
        double longitud = parseDouble(Arrays.copyOfRange(bytes, 9, 13));
        int height = parseInt(Arrays.copyOfRange(bytes, 13, 15));

        Coordenada coordenadas = new Coordenada(c.getTime(), fix, latitud, longitud, height);
        return coordenadas;

    }

    private static long parseLong(int[] bytes) {
        if (bytes.length != 4) {
            System.err.println("WARNING: expecting 4 bytes got " + bytes.length + " bytes");
        }
        return ((0xFF & bytes[3]) << 24) | ((0xFF & bytes[2]) << 16) | ((0xFF & bytes[1]) << 8) | (0xFF & bytes[0]);
    }

    private static double parseDouble(int[] bytes) {
        long longValue = parseLong(bytes);
        double exponente = (longValue >> 23) & 0xff;
        exponente -= 127.0;
        exponente = Math.pow(2, exponente);
        double mantissa = (longValue & 0x7fffff);
        mantissa = 1.0 + (mantissa / 8388607.0);
        double doubleValue = mantissa * exponente;
        if ((longValue & 0x80000000) == 0x80000000) {
            doubleValue = -doubleValue;
        }
        return doubleValue;
    }

    private static int parseInt(int[] bytes) {
        if (bytes.length != 2) {
            System.err.println("WARNING: expecting 2 bytes got " + bytes.length + " bytes");
        }
        int number = ((0xFF & bytes[1]) << 8) | (0xFF & bytes[0]);
        return number;
    }

}
