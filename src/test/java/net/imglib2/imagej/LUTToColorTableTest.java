package net.imglib2.imagej;

import ij.process.LUT;
import net.imglib2.display.ColorTable;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Tests utilities in {@link LUTToColorTable}
 *
 * @author Gabriel Selzer
 */
public class LUTToColorTableTest {

    @Test
    public void testWrapLUT() {
        Random r = new Random(0xdeadbeefL);
        byte[] reds = new byte[256];
        r.nextBytes(reds);
        byte[] blues = new byte[256];
        r.nextBytes(blues);
        byte[] greens = new byte[256];
        r.nextBytes(greens);
        LUT lut = new LUT(reds, greens, blues);
        ColorTable actual = LUTToColorTable.wrap(lut);
        assertEquals(256, actual.getLength());
        assertEquals(4, actual.getComponentCount());
        for(int i = 0; i < actual.getLength(); i++) {
            // Note ColorTable.get unsigned bytes as ints
            assertEquals(reds[i], (byte) actual.get(ColorTable.RED, i));
            assertEquals(greens[i], (byte) actual.get(ColorTable.GREEN, i));
            assertEquals(blues[i], (byte) actual.get(ColorTable.BLUE, i));
            assertEquals((byte) 255, (byte) actual.get(ColorTable.ALPHA, i));
        }
    }
}
