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

package net.imglib2.imagej;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RAIToImagePlusTest
{
	@Test
	public void testAxisOrder()
	{
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 1, 1, 2, 3, 4 );
		fill( img );
		final ImagePlus imagePlus = RAIToImagePlus.wrap( img, "title" );
		assertEquals( 7, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 1, 1, 2 ) ).get( 0, 0 ) );
		assertEquals( 3, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 1, 2, 1 ) ).get( 0, 0 ) );
		assertEquals( 2, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 2, 1, 1 ) ).get( 0, 0 ) );
	}

	@Test
	public void testColoredAxisOrder()
	{
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 1, 1, 3, 2, 4 );
		fill( img );
		final ImagePlus imagePlus = RAIToImagePlus.convertRGB( img, "title" );
		// img[0, 0, :, 0, 0] is [1, 2, 3]
		assertEquals( 0xff010203, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 1, 1, 1 ) ).get( 0, 0 ) );
		// img[0, 0, :, 1, 0] is [4, 5, 6]
		assertEquals( 0xff040506, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 1, 2, 1 ) ).get( 0, 0 ) );
		// img[0, 0, :, 0, 1] is [7, 8, 9]
		assertEquals( 0xff070809, imagePlus.getStack().getProcessor( imagePlus.getStackIndex( 1, 1, 2 ) ).get( 0, 0 ) );
	}

	@Test
	public void testBitType()
	{
		testTypeConversion( ByteProcessor.class, 1, new BitType( true ) );
	}

	@Test
	public void testBoolType()
	{
		testTypeConversion( ByteProcessor.class, 1, new BoolType( true ) );
	}

	@Test
	public void testUnsigned2BitType()
	{
		testTypeConversion( ByteProcessor.class, 3, new Unsigned2BitType( 3 ) );
	}

	@Test
	public void testUnsigned4BitTypeType()
	{
		testTypeConversion( ByteProcessor.class, 15, new Unsigned4BitType( 15 ) );
	}

	@Test
	public void testUnsignedByteType()
	{
		testTypeConversion( ByteProcessor.class, 42f, new UnsignedByteType( 42 ) );
	}

	@Test
	public void testUnsigned12BitType()
	{
		testTypeConversion( ShortProcessor.class, 420, new Unsigned12BitType( 420 ) );
	}

	@Test
	public void testUnsignedShortType()
	{
		testTypeConversion( ShortProcessor.class, 42f, new UnsignedShortType( 42 ) );
	}

	@Test
	public void testUnsignedIntType()
	{
		testTypeConversion( FloatProcessor.class, 42, new UnsignedIntType( 42 ) );
	}

	@Test
	public void testUnsignedLongType()
	{
		testTypeConversion( FloatProcessor.class, 42, new UnsignedLongType( 42 ) );
	}

	@Test
	public void testUnsigned128BitType()
	{
		testTypeConversion( FloatProcessor.class, 1000000, new Unsigned128BitType( 1000000, 0 ) );
	}

	@Test
	public void testByteType()
	{
		testTypeConversion( FloatProcessor.class, -42, new ByteType( ( byte ) -42 ) );
	}

	@Test
	public void testShortType()
	{
		testTypeConversion( FloatProcessor.class, -42, new ShortType( ( short ) -42 ) );
	}

	@Test
	public void testIntType()
	{
		testTypeConversion( FloatProcessor.class, -42, new IntType( -42 ) );
	}

	@Test
	public void testLongType()
	{
		testTypeConversion( FloatProcessor.class, -42, new LongType( -42 ) );
	}

	@Test
	public void testFloatType()
	{
		testTypeConversion( FloatProcessor.class, -42f, new FloatType( -42 ) );
	}

	@Test
	public void testDoubleType()
	{
		testTypeConversion( FloatProcessor.class, -42f, new DoubleType( -42 ) );
	}

	private < T extends RealType< T > > void testTypeConversion( final Class< ? extends ImageProcessor > processorClass, final float expected, final T input )
	{
		final RandomAccessibleInterval< T > rai = ConstantUtils.constantRandomAccessibleInterval( input, new FinalInterval( 1, 1 ) );
		// process
		final ImagePlus imagePlus = RAIToImagePlus.wrap( rai, "title" );
		// test
		final ImageProcessor processor = imagePlus.getProcessor();
		Assert.assertTrue( processorClass.isInstance( processor ) );
		Assert.assertEquals( expected, processor.getPixelValue( 0, 0 ), 0f );
	}

	private void fill( final RandomAccessibleInterval< ? extends IntegerType<?> > img )
	{
		final AtomicInteger i = new AtomicInteger();
		Views.flatIterable( img ).forEach( pixel -> pixel.setInteger( i.incrementAndGet() ) );
	}

	@Test
	public void testUnknownAxes()
	{
		final byte[] array = { 1, 2, 3, 4, 5, 6 };
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( array, 1, 1, 2, 3 );
		final ImagePlus result = RAIToImagePlus.wrap( img, "title" );
		assertEquals( 2, result.getNChannels() );
		assertEquals( 3, result.getNSlices() );
	}

	@Test
	public void testPersistence()
	{
		// setup
		final Img< FloatType > img = ArrayImgs.floats( 1, 1 );
		final ImagePlus imagePlus = RAIToImagePlus.wrap( img, "title" );
		final float expected = 42;
		// process
		final ImageProcessor processor = imagePlus.getStack().getProcessor( 1 );
		processor.setf( 0, 0, expected );
		imagePlus.getStack().setPixels( processor.getPixels(), 1 ); // NB:
																	// required
																	// to signal
																	// data
																	// changed
		// test
		assertEquals( expected, img.cursor().next().get(), 0.0f );
	}

	@Test
	public void testPersistenceBits()
	{
		// setup
		final Img< BitType > img = ArrayImgs.bits( 1, 1 );
		final ImagePlus imagePlus = RAIToImagePlus.wrapAndScaleBoolean( img, "test" );
		// process
		final ImageProcessor processor = imagePlus.getStack().getProcessor( 1 );
		processor.setf( 0, 0, 255 );
		imagePlus.getStack().setPixels( processor.getPixels(), 1 ); // NB:
																	// required
																	// to signal
																	// data
																	// changed
		// test
        assertTrue(img.cursor().next().get());
	}
}
