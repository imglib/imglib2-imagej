package net.imglib2.imagej;

import ij.process.LUT;
import net.imglib2.util.Binning;
import net.imglib2.display.ColorTable;

import java.awt.*;

/**
 * Provides convenience functions to wrap ImageJ {@link ij.process.LUT} objects into ImgLib2
 * {@link net.imglib2.display.ColorTable}s.
 *
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Matthias Arzt
 * @author Gabriel Selzer
 */
public class LUTToColorTable {

    public static ColorTable wrap(final LUT lut) {
        return new ColorTable() {
            @Override
            public int lookupARGB(double min, double max, double value) {
                final int bins = getLength();
                final int bin = Binning.valueToBin( bins, min, max, value );
                return lut.getRGB( bin );
            }

            @Override
            public int getComponentCount() {
                return 4;
            }

            @Override
            public int getLength() {
                return 256;
            }

            @Override
            public int get(int comp, int bin) {
                int rgb = lut.getRGB(bin);
                int shift = comp == ColorTable.RED ? 16 :
                        comp == ColorTable.GREEN ? 8 :
                        comp == ColorTable.BLUE ? 0 :
                        24;
                return (rgb >> shift) & 0xff;
            }

            @Override
            public int getResampled(int comp, int bins, int bin) {
                final int newBin = ( int ) ( ( long ) getLength() * bin / bins );
                return get( comp, newBin );
            }
        };
    }
}
