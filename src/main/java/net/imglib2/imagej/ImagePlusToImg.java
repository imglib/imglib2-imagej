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
import net.imglib2.Cursor;
import net.imglib2.cache.Cache;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.converter.Converter;
import net.imglib2.imagej.imageplus.*;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.AbstractList;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Provides convenience functions to wrap ImageJ 1.x data structures as ImgLib2
 * ones.
 *
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Matthias Arzt
 */
public class ImagePlusToImg
{

	public static PlanarImg< ?, ? > wrap( final ImagePlus imp )
	{
		switch ( imp.getType() )
		{
		case ImagePlus.GRAY8:
			return wrapByte( imp );
		case ImagePlus.GRAY16:
			return wrapShort( imp );
		case ImagePlus.GRAY32:
			return wrapFloat( imp );
		case ImagePlus.COLOR_RGB:
			return wrapRGBA( imp );
		default:
			throw new RuntimeException( "Only 8, 16, 32-bit and RGB supported!" );
		}
	}

	public static PlanarImg< UnsignedByteType, ? > wrapByte(final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.GRAY8 )
			return null;

		final ByteImagePlus< UnsignedByteType > container = new ByteImagePlus<>( imp );

		// create a Type that is linked to the container
		final UnsignedByteType linkedType = new UnsignedByteType( container );

		// pass it to the NativeContainer
		container.setLinkedType( linkedType );

		return container;
	}

	public static PlanarImg< UnsignedShortType, ? > wrapShort(final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.GRAY16 )
			return null;

		final ShortImagePlus< UnsignedShortType > container = new ShortImagePlus<>( imp );

		// create a Type that is linked to the container
		final UnsignedShortType linkedType = new UnsignedShortType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;
	}

	public static PlanarImg< UnsignedIntType, ? > wrapInt(final ImagePlus imp )
	{
		if( imp.getType() != ImagePlus.COLOR_RGB )
			return null;

		final IntImagePlus< UnsignedIntType > container = new IntImagePlus<>( imp );

		// create a Type that is linked to the container
		final UnsignedIntType linkedType = new UnsignedIntType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;
	}

	public static PlanarImg< ARGBType, ? > wrapRGBA( final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.COLOR_RGB )
			return null;

		final IntImagePlus< ARGBType > container = new IntImagePlus<>( imp );

		// create a Type that is linked to the container
		final ARGBType linkedType = new ARGBType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;
	}

	public static PlanarImg< FloatType, ? > wrapFloat(final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.GRAY32 )
			return null;

		final FloatImagePlus< FloatType > container = new FloatImagePlus<>( imp );

		// create a Type that is linked to the container
		final FloatType linkedType = new FloatType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;
	}

	public static PlanarImg< FloatType, ? > convertFloat( final ImagePlus imp )
	{

		switch ( imp.getType() )
		{
		case ImagePlus.GRAY8:
			return convertToFloat( wrapByte( imp ), new NumberToFloatConverter< UnsignedByteType >() );
		case ImagePlus.GRAY16:
			return convertToFloat( wrapShort( imp ), new NumberToFloatConverter< UnsignedShortType >() );
		case ImagePlus.GRAY32:
			return wrapFloat( imp );
		case ImagePlus.COLOR_RGB:
			return convertToFloat( wrapRGBA( imp ), new ARGBtoFloatConverter() );
		default:
			throw new RuntimeException( "Only 8, 16, 32-bit and RGB supported!" );
		}
	}

	static private class ARGBtoFloatConverter implements Converter< ARGBType, FloatType >
	{
		/** Luminance times alpha. */
		@Override
		public void convert( final ARGBType input, final FloatType output )
		{
			final int v = input.get();
			output.setReal( ( ( v >> 24 ) & 0xff ) * ( ( ( v >> 16 ) & 0xff ) * 0.299 + ( ( v >> 8 ) & 0xff ) * 0.587 + ( v & 0xff ) * 0.144 ) );
		}
	}

	static private class NumberToFloatConverter< T extends ComplexType< T > > implements Converter< T, FloatType >
	{
		@Override
		public void convert( final T input, final FloatType output )
		{
			output.setReal( input.getRealFloat() );
		}
	}

	protected static < T extends Type< T > > PlanarImg< FloatType, ?> convertToFloat(
			final Img< T > input, final Converter< T, FloatType > c )
	{
		final ImagePlusImg< FloatType, ? > output = new ImagePlusImgFactory<>( new FloatType() ).create( input );

		final Cursor< T > in = input.cursor();
		final Cursor< FloatType > out = output.cursor();

		while ( in.hasNext() )
		{
			in.fwd();
			out.fwd();

			c.convert( in.get(), out.get() );
		}

		return output;
	}

	private static class ImagePlusLoader< A extends ArrayDataAccess< A >> extends AbstractList< A >
	{
		private final ImagePlus image;

		private final Cache< Integer, A > cache;

		private final Function< Object, A > arrayFactory;

		public ImagePlusLoader( final ImagePlus image, final Function< Object, A > arrayFactory )
		{
			this.arrayFactory = arrayFactory;
			this.image = image;
			cache = new SoftRefLoaderCache< Integer, A >().withLoader( this::load );
		}

		@Override
		public A get( final int key )
		{
			try
			{
				return cache.get( key );
			}
			catch ( final ExecutionException e )
			{
				throw new RuntimeException( e );
			}
		}

		private A load( final Integer key )
		{
			return arrayFactory.apply( image.getStack().getPixels( key + 1 ) );
		}

		@Override
		public int size()
		{
			return image.getStackSize();
		}
	}

	//
}
