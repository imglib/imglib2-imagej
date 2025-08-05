package net.imglib2.imagej;

import ij.process.LUT;
import net.imglib2.display.ColorTable;
import net.imglib2.util.Binning;

/**
 * Provides convenience functions to convert ImgLib2 {@link ColorTable}s into
 * ImageJ {@link LUT} objects.
 *
 *
 * @author Gabriel Selzer
 */
public class ColorTableToLUT {

    /**
     * Copies the data from {@code table} into a new {@link LUT}
     * @param table the {@link ColorTable} to convert
     * @return a {@link LUT} containing the same color mapping as {@code table}
     */
    public static LUT convert(final ColorTable table) {
        byte[][] data = new byte[3][256];
        for (int bin = 0; bin < 256; bin++) {
            data[ColorTable.RED][bin] = (byte) table.get(ColorTable.RED, bin);
            data[ColorTable.GREEN][bin] = (byte) table.get(ColorTable.GREEN, bin);
            data[ColorTable.BLUE][bin] = (byte) table.get(ColorTable.BLUE, bin);
        }
        return new LUT(data[ColorTable.RED], data[ColorTable.GREEN], data[ColorTable.BLUE]);
    }
}
