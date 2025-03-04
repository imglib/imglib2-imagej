/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
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

package net.imglib2.imagej.img;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayImgToImagePlusTest
{
	@Test
	public void testSharedBuffer()
	{
		final int width = 2;
		final int height = 3;
		final byte[] buffer = new byte[ width * height ];
		final ArrayImg< UnsignedByteType, ByteArray > img = ArrayImgs.unsignedBytes( buffer, width, height );
		final ImagePlus imagePlus = ArrayImgToImagePlus.wrap( img, "test" );
		assertEquals( width, imagePlus.getWidth() );
		assertEquals( height, imagePlus.getHeight() );
		assertSame( buffer, imagePlus.getProcessor().getPixels() );
	}

	@Test
	public void testIsSupported()
	{
		final Img< UnsignedByteType > supported = ArrayImgs.unsignedBytes( 2, 2 );
		final Img< UnsignedByteType > unsupported1 = ArrayImgs.unsignedBytes( 2, 2, 3 );
		final Img< UnsignedByteType > cellImg = new CellImgFactory<>( new UnsignedByteType() ).create( 2, 2 );
		assertTrue( ArrayImgToImagePlus.isSupported( supported ) );
		assertFalse( ArrayImgToImagePlus.isSupported( unsupported1 ) );
		assertFalse( ArrayImgToImagePlus.isSupported( cellImg ) );
	}

	@Test
	public void testPersistence()
	{
		// setup
		final float expected = 42.0f;
		final ArrayImg<FloatType, FloatArray> img = ArrayImgs.floats( 1, 1 );
		final ImagePlus imagePlus = ArrayImgToImagePlus.wrap( img, "title" );
		// process
		imagePlus.getProcessor().setf( 0, 0, expected );
		// test
		assertEquals( expected, img.cursor().next().getRealFloat(), 0.0f );
	}
}
