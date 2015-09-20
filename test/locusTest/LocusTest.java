/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package locusTest;

import java.util.Arrays;
import java.util.List;
import locus.Locus;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fran
 */
public class LocusTest {

    public LocusTest() {
    }

    /**
     * Test of parseLine method, of class Locus.
     */
    @Test
    public void parseLine() {
        String linea = "$PMTKLOX,1,1,0ADE5552,04A58920,42926996,C21D002B*24";
        List parseLine = Locus.parseLine(linea);
        int result = parseLine.size();
        assertEquals(1, result);

    }

    /**
     * Test of checksum method, of class Locus.
     */
    @Test
    public void checksum() {
        String checksum = Locus.checksum("$PMTKLOX,1,1,0ADE5552,04A58920,42926996,C21D002B");
        assertEquals(checksum, "24");
    }

    /**
     * Test of hexStringToIntArray method, of class Locus.
     */
    @Test
    public void hexStringToIntArray() {
        String data = "$PMTKLOX,1,1,0ADE5552,04A58920,42926996,C21D002B";
        String[] parts = data.split(",");
        String dataFields = "";
        for (int i = 3; i < parts.length; i++) {
            dataFields = dataFields + parts[i];
        }
        int[] bytes = Locus.hexStringToIntArray(dataFields);
        int[] expectec ={10,222,85,82,4,165,137,32,66,146,105,150,194,29,0,43};
        assertArrayEquals(expectec, bytes);
        long timestamp = Locus.parseLong(Arrays.copyOfRange(bytes, 0, 4));
    }
     /**
     * Test of parseLong method, of class Locus.
     */
    @Test
    public void parseLong() {
        int[] val = {218,50,79,82};
        assertEquals(1380922074, Locus.parseLong(val));  
    }
}
