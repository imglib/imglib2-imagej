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
import net.imglib2.util.Binning;

/**
 * Provides convenience functions to wrap ImageJ {@link ij.process.LUT} objects into ImgLib2
 * {@link net.imglib2.display.ColorTable}s.
 *
 *
 * @author Gabriel Selzer
 */
public class LUTToColorTable {

    /**
     * Wraps (i.e. copy-free) {@code lut} into a {@link ColorTable}.
     * @param lut the {@link LUT} to convert
     * @return a {@link ColorTable} enclosing {@code lut}
     */
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
