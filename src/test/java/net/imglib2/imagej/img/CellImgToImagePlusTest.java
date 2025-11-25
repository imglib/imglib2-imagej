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

package net.imglib2.imagej.img;

import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link CellImgToImagePlus}.
 */
public class CellImgToImagePlusTest
{

	@Test
	public void test3D() {
		CellImg< UnsignedByteType, ? > image = new CellImgFactory<>( new UnsignedByteType(), 3, 1, 1 ).create( 3, 1, 2 );
		fill( image );
		ImagePlus imp = wrap( image );
		assertArrayEquals( new byte[]{ 1, 2, 3 }, (byte[]) imp.getStack().getPixels( 1 ) );
		assertArrayEquals( new byte[]{ 4, 5, 6 }, (byte[]) imp.getStack().getPixels( 2 ) );
	}

	private static <T extends NativeType<T>, A extends ArrayDataAccess<A>> ImagePlus wrap(CellImg< T, ?> image )
	{
		return CellImgToImagePlus.wrap( (CellImg<T, ? extends ArrayDataAccess>) image, "title" );
	}

	@Test
	public void test5D() {
		CellImg< UnsignedByteType, ? > image = new CellImgFactory<>( new UnsignedByteType(), 2, 1, 1, 1, 1 ).create( 2, 1, 2, 3, 4 );
		fill( image );
		ImagePlus imp = wrap( image );
		assertArrayEquals( new byte[]{ 1, 2 }, (byte[]) imp.getStack().getPixels( 1 ) );
		assertArrayEquals( new byte[]{ 3, 4 }, (byte[]) imp.getStack().getPixels( 2 ) );
		assertArrayEquals( new byte[]{ 5, 6 }, (byte[]) imp.getStack().getPixels( 3 ) );
	}

	@Test
	public void testFloatImg() {
		CellImg< FloatType, ? > image = new CellImgFactory<>( new FloatType(), 1, 1, 1 ).create( 1, 1, 1 );
		image.firstElement().set( 42 );
		ImagePlus imp = wrap( image );
		assertArrayEquals( new float[]{ 42 }, (float[]) imp.getStack().getPixels( 1 ), 0 );
	}

	@Test
	public void testAxisOrder()
	{
		final CellImg< UnsignedByteType, ? > img = new CellImgFactory<>( new UnsignedByteType(), 1, 1, 1, 1, 1).create( new long[] { 1, 1, 2, 3, 4 } );
		fill( img );
		final ImagePlus imp = wrap(img);
		assertEquals( 7, imp.getStack().getProcessor( imp.getStackIndex( 1, 1, 2 ) ).get( 0, 0 ) );
		assertEquals( 3, imp.getStack().getProcessor( imp.getStackIndex( 1, 2, 1 ) ).get( 0, 0 ) );
		assertEquals( 2, imp.getStack().getProcessor( imp.getStackIndex( 2, 1, 1 ) ).get( 0, 0 ) );
	}

	@Test
	public void testIsSupported()
	{
		assertTrue( CellImgToImagePlus.isSupported( new CellImgFactory<>( new UnsignedByteType(), 2, 2 ).create( 2, 2 ) ) );
	}

	@Test
	public void testIsSupported_FloatType()
	{
		assertTrue( CellImgToImagePlus.isSupported( new CellImgFactory<>( new FloatType(), 2, 2 ).create( 2, 2 ) ) );
	}

	@Test
	public void testIsSupported_UnsupportedType()
	{
		assertFalse( CellImgToImagePlus.isSupported( new CellImgFactory<>( new DoubleType(), 2, 2 ).create( 2, 2 ) ) );
	}

	@Test
	public void testIsSupported_PlanarCells()
	{
		assertTrue( CellImgToImagePlus.isSupported( new CellImgFactory<>( new FloatType(), 7, 2 ).create( 2, 2 ) ) );
		assertTrue( CellImgToImagePlus.isSupported( new CellImgFactory<>( new FloatType(), 2, 2, 3 ).create( 2, 2, 1 ) ) );
	}

	@Test
	public void testIsSupported_NoPlanarCells() {
		assertFalse( CellImgToImagePlus.isSupported( new CellImgFactory<>( new FloatType(), 1, 2 ).create( 2, 2 ) ) );
		assertFalse( CellImgToImagePlus.isSupported( new CellImgFactory<>( new FloatType(), 2, 2, 3 ).create( 2, 2, 3 ) ) );
	}

	private void fill( RandomAccessibleInterval< ? extends IntegerType< ? > > image )
	{
		int i = 1;
		for( IntegerType< ? > pixel : Views.flatIterable( image ) )
			pixel.setInteger( i++ );
	}

	@Test
	public void testPersistence()
	{
		// setup
		final CellImg< FloatType, ? > img = new CellImgFactory<>( new FloatType() ).create( 1, 1, 1 );
		final ImagePlus imagePlus = wrap( img );
		final float expected = 42.0f;
		// process
		imagePlus.getProcessor().setf( 0, 0, expected );
		// test
		assertEquals( expected, img.cursor().next().get(), 0.0f );
	}

	@Test
	public void testSetPixels() {
		// setup
		final CellImg< FloatType, ? > img = new CellImgFactory<>( new FloatType() ).create( 1, 1, 1 );
		final ImagePlus imagePlus = wrap( img );
		final float expected = 42.0f;
		// process
		imagePlus.getStack().setPixels( new float[] { expected }, 1 );
		// test
		assertEquals( expected, img.cursor().next().get(), 0.0f );
	}
}
