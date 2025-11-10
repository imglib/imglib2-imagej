package net.imglib2.imagej;

import ij.process.LUT;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Tests conversions from {@link ColorTable}s to ImageJ {@link LUT}s.
 *
 * @author Gabriel Selzer
 */
public class ColorTableToLUTTest {

    @Test
    public void testConvertColorTable() {
        Random r = new Random(0xdeadbeefL);
        byte[] reds = new byte[256];
        r.nextBytes(reds);
        byte[] greens = new byte[256];
        r.nextBytes(greens);
        byte[] blues = new byte[256];
        r.nextBytes(blues);
        ColorTable table = new ColorTable8(reds, greens, blues);
        LUT actual = ColorTableToLUT.convert(table);
        for(int i = 0; i < 256; i++) {
            // Note ColorTable.get unsigned bytes as ints
            assertEquals(reds[i], (byte) actual.getRed(i));
            assertEquals(greens[i], (byte) actual.getGreen(i));
            assertEquals(blues[i], (byte) actual.getBlue(i));
            assertEquals((byte) 255, (byte) actual.getAlpha(i));
        }
    }
}
