/*
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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.imagej.ImagePlusToImg;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class PlanarImgToImagePlusTest
{
	private void fill( final RandomAccessibleInterval< ? extends IntegerType<?>> img )
	{
		final AtomicInteger i = new AtomicInteger();
		Views.flatIterable( img ).forEach( pixel -> pixel.setInteger( i.incrementAndGet() ) );
	}

	private PlanarImg< UnsignedByteType, ? > example()
	{
		final PlanarImg< UnsignedByteType, ? > img = new PlanarImgFactory<>(new UnsignedByteType()).create(3, 1, 2);
		fill( img );
		return img;
	}

	@Test
	public void testStorageArray()
	{
		final PlanarImg< UnsignedByteType, ? > img = example();
		final ImagePlus imp = PlanarImgToImagePlus.wrap( img, "test");
		assertSame( img.getPlane( 1 ).getCurrentStorageArray(), imp.getStack().getPixels( 2 ) );
	}

	@Test
	public void testPixelValues()
	{
		final PlanarImg< UnsignedByteType, ? > img = example();
		final ImagePlus imp = PlanarImgToImagePlus.wrap( img, "test" );
		assertArrayEquals( new byte[] { 1, 2, 3 }, ( byte[] ) imp.getStack().getPixels( 1 ) );
		assertArrayEquals( new byte[] { 4, 5, 6 }, ( byte[] ) imp.getStack().getPixels( 2 ) );
	}

	@Test
	public void testImgPlus()
	{
		// setup
		final PlanarImg< UnsignedByteType, ? > img = example();
		final String title = "test image";
		// process
		final ImagePlus imagePlus = PlanarImgToImagePlus.wrap( img, title );
		// test
		assertEquals( title, imagePlus.getTitle() );
		assertEquals( 3, imagePlus.getWidth() );
		assertEquals( 1, imagePlus.getHeight() );
		assertEquals( 2, imagePlus.getNChannels() );
		assertEquals( 1, imagePlus.getNSlices() );
		assertEquals( 1, imagePlus.getNFrames() );
	}

	@Test
	public void testAxisOrder()
	{
		final PlanarImg< UnsignedByteType, ? > img = new PlanarImgFactory<>(new UnsignedByteType()).create(1, 1, 2, 3, 4);
		fill( img );
		final ImagePlus imagePlus = PlanarImgToImagePlus.wrap( img, "title" );
		assertEquals( 7, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 1, 1, 2 ) ).get( 0, 0 ) );
		assertEquals( 3, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 1, 2, 1 ) ).get( 0, 0 ) );
		assertEquals( 2, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 2, 1, 1 ) ).get( 0, 0 ) );
	}

	@Test
	public void testPersistence()
	{
		// setup
		final PlanarImg<FloatType, FloatArray> img = PlanarImgs.floats( 1, 1 );
//		final ImgPlus< FloatType > imgPlus = new ImgPlus< FloatType >( img, "title", new AxisType[] { Axes.X, Axes.Y } );
		final ImagePlus imagePlus = PlanarImgToImagePlus.wrap( img,"title" );
		final float expected = 42.0f;
		// process
		imagePlus.getProcessor().setf( 0, 0, expected );
		// test
		assertEquals( expected, img.cursor().next().get(), 0.0f );
	}

	@Test
	public void testSetPixels() {
		// setup
		final PlanarImg< FloatType, FloatArray > img = PlanarImgs.floats( 1, 1 );
		final ImagePlus imagePlus = PlanarImgToImagePlus.wrap( img, "title" );
		final float expected = 42.0f;
		// process
		imagePlus.getStack().setPixels( new float[] { expected }, 1 );
		// test
		assertEquals( expected, img.cursor().next().get(), 0.0f );
	}

	@Test
	public void testGetProcessorForColorProcessor() {
		// NB: Test that the underlying data does not change, when a ColorProcessor is created.
		// To achieve this min and max must not be set for ColorProcessor.
		final PlanarImg< ARGBType, IntArray > argbs = PlanarImgs.argbs( 1, 1, 1 );
		argbs.randomAccess().get().set( 0xff010203 );
		final ImageStack stack = PlanarImgToImagePlus.wrap( argbs, "title" ).getImageStack();
		assertArrayEquals( new int[] { 0xff010203 }, (int[]) stack.getPixels( 1 ) );
		stack.getProcessor( 1 );
		assertArrayEquals( new int[] { 0xff010203 }, (int[]) stack.getPixels( 1 ) );
	}

	@Test
	public void testConvertingBackAndForth() {
		ImagePlus imagePlus = IJ.createImage( "test", "8-bit ramp", 3, 3, 3 );
		PlanarImg<UnsignedByteType, ?> convertedImg = ImagePlusToImg.wrapByteDirect( imagePlus );
		ImagePlus twiceConvertedImagePlus = PlanarImgToImagePlus.wrap( convertedImg, "title" );
		twiceConvertedImagePlus.getStack().getProcessor( 1 ).set( 0, 0, 5 );
		assertEquals( 5, imagePlus.getStack().getProcessor( 1 ).get( 0, 0 ) );
	}
}
