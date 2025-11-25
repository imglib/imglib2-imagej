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
        byte[] greens = new byte[256];
        r.nextBytes(greens);
        byte[] blues = new byte[256];
        r.nextBytes(blues);
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
