/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Gabriel Selzer, Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.imagej;

import ij.process.LUT;
import net.imglib2.display.ColorTable;

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
        if (table.getLength() != 256) {
            throw new IllegalArgumentException("Can only convert ColorTables of length 256 to LUTs");
        }
        byte[][] data = new byte[3][256];
        for (int bin = 0; bin < 256; bin++) {
            data[ColorTable.RED][bin] = (byte) table.get(ColorTable.RED, bin);
            data[ColorTable.GREEN][bin] = (byte) table.get(ColorTable.GREEN, bin);
            data[ColorTable.BLUE][bin] = (byte) table.get(ColorTable.BLUE, bin);
        }
        return new LUT(data[ColorTable.RED], data[ColorTable.GREEN], data[ColorTable.BLUE]);
    }
}
