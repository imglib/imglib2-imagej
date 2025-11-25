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
import net.imglib2.cache.Cache;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.imagej.imageplus.ByteImagePlus;
import net.imglib2.imagej.imageplus.FloatImagePlus;
import net.imglib2.imagej.imageplus.IntImagePlus;
import net.imglib2.imagej.imageplus.ShortImagePlus;
import net.imglib2.img.basictypeaccess.array.*;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.NativeTypeFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Fraction;

import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.LongStream;

/**
 * Provides convenience functions to wrap ImageJ 1.x data structures as ImgLib2
 * ones.
 *
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Matthias Arzt
 * @author Gabriel Selzer
 */
public class ImagePlusToImg
{

	/**
	 * Wraps an {@link ImagePlus} into a {@link PlanarImg}.
	 * <p>
	 * Under the hood, each {@link ij.process.ImageProcessor}'s backing array is
	 * wrapped into an {@link ArrayDataAccess}. The resulting {@link List} of
	 * {@link ArrayDataAccess}es is used to form a {@link PlanarImg}.
	 * </p>
	 *
	 * @param imp the {@link ImagePlus} to wrap
	 * @return a {@link PlanarImg} directly wrapping {@code imp}
	 */
	public static PlanarImg< ?, ? > wrapDirect(final ImagePlus imp )
	{
		switch ( imp.getType() )
		{
		case ImagePlus.GRAY8:
			return wrapByteDirect( imp );
		case ImagePlus.GRAY16:
			return wrapShortDirect( imp );
		case ImagePlus.GRAY32:
			return wrapFloatDirect( imp );
		case ImagePlus.COLOR_RGB:
			return wrapRGBADirect( imp );
		default:
			throw new RuntimeException( "Only 8, 16, 32-bit and RGB supported!" );
		}
	}

	/**
	 * Wraps an {@link ImagePlus} into a {@link PlanarImg} of unsigned bytes.
	 * <p>
	 * Under the hood, each {@link ij.process.ImageProcessor}'s backing array is
	 * wrapped into an {@link ByteArray}. The resulting {@link List} of
	 * {@link ArrayDataAccess}es is used to form a {@link PlanarImg}.
	 * </p>
	 *
	 * @param imp the {@link ImagePlus} to wrap
	 * @return a {@link PlanarImg} of unsigned bytes directly wrapping {@code imp}
	 */
	public static PlanarImg< UnsignedByteType, ? > wrapByteDirect(final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.GRAY8 )
			throw new IllegalArgumentException(imp + " does not contain unsigned bytes!");

		final ByteImagePlus< UnsignedByteType > container = new ByteImagePlus<>( imp );

		// create a Type that is linked to the container
		final UnsignedByteType linkedType = new UnsignedByteType( container );

		// pass it to the NativeContainer
		container.setLinkedType( linkedType );

		return container;
	}

	/**
	 * Wraps an {@link ImagePlus} into a {@link PlanarImg} of unsigned shorts.
	 * <p>
	 * Under the hood, each {@link ij.process.ImageProcessor}'s backing array is
	 * wrapped into an {@link ShortArray}. The resulting {@link List} of
	 * {@link ArrayDataAccess}es is used to form a {@link PlanarImg}.
	 * </p>
	 *
	 * @param imp the {@link ImagePlus} to wrap
	 * @return a {@link PlanarImg} of unsigned shorts directly wrapping {@code imp}
	 */
	public static PlanarImg< UnsignedShortType, ? > wrapShortDirect(final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.GRAY16 )
			throw new IllegalArgumentException(imp + " does not contain unsigned shorts!");

		final ShortImagePlus< UnsignedShortType > container = new ShortImagePlus<>( imp );

		// create a Type that is linked to the container
		final UnsignedShortType linkedType = new UnsignedShortType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;
	}

	/**
	 * Wraps an {@link ImagePlus} into a {@link PlanarImg} of RGBA tuples.
	 * <p>
	 * Under the hood, each {@link ij.process.ImageProcessor}'s backing array is
	 * wrapped into an {@link IntArray}. The resulting {@link List} of
	 * {@link ArrayDataAccess}es is used to form a {@link PlanarImg}.
	 * </p>
	 *
	 * @param imp the {@link ImagePlus} to wrap
	 * @return a {@link PlanarImg} of ARGB tuples directly wrapping {@code imp}
	 */
	public static PlanarImg< ARGBType, ? > wrapRGBADirect(final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.COLOR_RGB )
			throw new IllegalArgumentException(imp + " does not contain RGB tuples!");

		final IntImagePlus< ARGBType > container = new IntImagePlus<>( imp );

		// create a Type that is linked to the container
		final ARGBType linkedType = new ARGBType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;
	}

	/**
	 * Wraps an {@link ImagePlus} into a {@link PlanarImg} of floats.
	 * <p>
	 * Under the hood, each {@link ij.process.ImageProcessor}'s backing array is
	 * wrapped into an {@link FloatArray}. The resulting {@link List} of
	 * {@link ArrayDataAccess}es is used to form a {@link PlanarImg}.
	 * </p>
	 *
	 * @param imp the {@link ImagePlus} to wrap
	 * @return a {@link PlanarImg} of floats directly wrapping {@code imp}
	 */
	public static PlanarImg< FloatType, ? > wrapFloatDirect(final ImagePlus imp )
	{
		if ( imp.getType() != ImagePlus.GRAY32 )
			throw new IllegalArgumentException(imp + " does not contain floats!");

		final FloatImagePlus< FloatType > container = new FloatImagePlus<>( imp );

		// create a Type that is linked to the container
		final FloatType linkedType = new FloatType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;
	}

	/**
	 * Wraps an 8 bit {@link ImagePlus}, into an {@link PlanarImg}, that is backed
	 * by a {@link PlanarImg}. The {@link PlanarImg} loads the planes only if
	 * needed, and caches them.
	 * @param image the {@link ImagePlus} to wrap. Must contain unsigned bytes.
	 * @return a {@link PlanarImg} wrapping {@code image}.
	 */
	public static PlanarImg< UnsignedByteType, ByteArray > wrapByteCached(final ImagePlus image )
	{
		return internWrap( image, ImagePlus.GRAY8, new UnsignedByteType(), array -> new ByteArray( ( byte[] ) array ) );
	}

	/**
	 * Wraps a 16 bit {@link ImagePlus}, into an {@link PlanarImg}, that is backed
	 * by a {@link PlanarImg}. The {@link PlanarImg} loads the planes only if
	 * needed, and caches them.
	 * @param image the {@link ImagePlus} to wrap. Must contain unsigned shorts.
	 * @return a {@link PlanarImg} wrapping {@code image}.
	 */
	public static PlanarImg< UnsignedShortType, ShortArray > wrapShortCached(final ImagePlus image )
	{
		return internWrap( image, ImagePlus.GRAY16, new UnsignedShortType(), array -> new ShortArray( ( short[] ) array ) );
	}

	/**
	 * Wraps a 32 bit {@link ImagePlus}, into an {@link PlanarImg}, that is backed
	 * by a {@link PlanarImg}. The {@link PlanarImg} loads the planes only if
	 * needed, and caches them.
	 * @param image the {@link ImagePlus} to wrap. Must contain floats.
	 * @return a {@link PlanarImg} wrapping {@code image}.
	 */
	public static PlanarImg< FloatType, FloatArray > wrapFloatCached(final ImagePlus image )
	{
		return internWrap( image, ImagePlus.GRAY32, new FloatType(), array -> new FloatArray( ( float[] ) array ) );
	}

	/**
	 * Wraps a 24 bit {@link ImagePlus}, into an {@link PlanarImg}, that is backed
	 * by a {@link PlanarImg}. The {@link PlanarImg} loads the planes only if
	 * needed, and caches them.
	 * @param image the {@link ImagePlus} to wrap. Must contain RGB tuples.
	 * @return a {@link PlanarImg} wrapping {@code image}.
	 */
	public static PlanarImg< ARGBType, IntArray > wrapRGBACached(final ImagePlus image )
	{
		return internWrap( image, ImagePlus.COLOR_RGB, new ARGBType(), array -> new IntArray( ( int[] ) array ) );
	}

	/**
	 * Wraps an {@link ImagePlus}, into an {@link PlanarImg}, that is backed by a
	 * {@link PlanarImg}. The {@link PlanarImg} loads the planes only if needed,
	 * and caches them. The pixel type of the returned image depends on the type
	 * of the ImagePlus.
	 * @param image the {@link ImagePlus} to wrap
	 * @return a {@link PlanarImg} wrapping {@code image}.
	 */
	public static PlanarImg< ?, ? > wrapCached(final ImagePlus image )
	{
		switch ( image.getType() )
		{
			case ImagePlus.GRAY8:
				return wrapByteCached( image );
			case ImagePlus.GRAY16:
				return wrapShortCached( image );
			case ImagePlus.GRAY32:
				return wrapFloatCached( image );
			case ImagePlus.COLOR_RGB:
				return wrapRGBACached( image );
		}
		throw new RuntimeException( "Only 8, 16, 32-bit and RGB supported!" );
	}

	private static < T extends NativeType< T >, A extends ArrayDataAccess< A > > PlanarImg< T, A > internWrap(
			final ImagePlus image,
			final int expectedType,
			final T type,
			final Function< Object, A > createArrayAccess
	) {
		if ( image.getType() != expectedType )
			throw new IllegalArgumentException();
		final ImagePlusLoader< A > loader = new ImagePlusLoader<>( image, createArrayAccess );
		final long[] dimensions = getNonTrivialDimensions( image );
		final PlanarImg< T, A > cached = new PlanarImg<>( loader, dimensions, new Fraction() );
        //noinspection unchecked
        cached.setLinkedType( ( (NativeTypeFactory< T, A >) type.getNativeTypeFactory() ).createLinkedType( cached ) );
		// TODO: Preserve metadata
		return cached;
	}

	private static long[] getNonTrivialDimensions(final ImagePlus image )
	{
		final LongStream xy = LongStream.of( image.getWidth(), image.getHeight() );
		final LongStream czt = LongStream.of( image.getNChannels(), image.getNSlices(), image.getNFrames() );
		return LongStream.concat( xy, czt.filter( x -> x > 1 ) ).toArray();
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
}
