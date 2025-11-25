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

import ij.ImagePlus;
import ij.VirtualStack;
import net.imglib2.Interval;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.*;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * Utility class to convert a {@link PlanarImg} to an {@link ImagePlus}
 * without copying data. It is restricted to certain pixel types:
 * UnsignedByteType, UnsignedShortType, ARGBType and FloatType.
 *
 * @see ArrayImgToImagePlus
 * @see CellImgToImagePlus
 */
public class PlanarImgToImagePlus extends AbstractVirtualStack
{

	// static

	/**
	 * Returns true, if {@link #wrap(PlanarImg, String)} supports the given image.
	 * @param obj an {@link Object} that may be supported by {@code wrap}
	 * @return {@code true} iff {@code obj} can be converted into an {@link ImagePlus}.
	 */
	public static boolean isSupported( Object obj )
	{
		if (!(obj instanceof PlanarImg))
			return false;
		PlanarImg<?, ?> img = (PlanarImg<?, ?>) obj;
		return ImageProcessorUtils.isSupported(img.getType());
	}

	/**
	 * Wraps a {@link PlanarImg} into an {@link ImagePlus}. The returned
	 * {@link ImagePlus} uses the same pixel buffer as the given image.
	 * Changes to the {@link ImagePlus} are therefore reflected in the
	 * {@link PlanarImg}.
	 * <p>
	 * The image must be {@link UnsignedByteType}, {@link UnsignedShortType},
	 * {@link ARGBType} or {@link FloatType}. Only up to five dimensions are
	 * supported. Axes are presumed to start with X, Y. Channel, Z, and Time
	 * axes are assumed to map to any following dimensions, in that order.
	 * <p>
	 * Use {@link #isSupported(Object)} to check if the {@link PlanarImg} is
	 * supported.
	 *
	 * @param img the {@link PlanarImg} to convert
	 * @param name the {@link String} title to assign to the result
	 * @return an {@link ImagePlus} wrapping the data in {@code img}
	 * @see #isSupported(Object)
	 */
	public static ImagePlus wrap(PlanarImg< ?, ? > img, String name )
	{
		final VirtualStack stack = new PlanarImgToImagePlus( img, x -> x );
		final ImagePlus imp = new ImagePlus(name, stack);
		final int c = img.numDimensions() > 2 ? (int) img.dimension(2) : 1;
		final int z = img.numDimensions() > 3 ? (int) img.dimension(3) : 1;
		final int t = img.numDimensions() > 4 ? (int) img.dimension(4) : 1;
		imp.setDimensions(c, z, t);
		return imp;
	}

	// fields

	private final PlanarAccess< ? extends ArrayDataAccess< ? > > img;

	private final IntUnaryOperator indexer;

	// constructor

	private PlanarImgToImagePlus(final PlanarImg< ?, ? > img, final IntUnaryOperator indexer )
	{
		super( ( int ) img.dimension( 0 ), ( int ) img.dimension( 1 ), initSize( img ), getBitDepth( img.randomAccess().get() ) );
		this.img = img;
		this.indexer = indexer;
	}

	private static int initSize( final Interval interval )
	{
		return IntStream.range( 2, interval.numDimensions() ).map( x -> ( int ) interval.dimension( x ) ).reduce( 1, ( a, b ) -> a * b );
	}

	// public methods

	@Override
	protected Object getPixelsZeroBasedIndex( final int index )
	{
		return img.getPlane( indexer.applyAsInt( index ) ).getCurrentStorageArray();
	}

	@Override
	protected void setPixelsZeroBasedIndex( int index, Object pixels )
	{
		try
		{
			img.setPlane( indexer.applyAsInt( index ), Cast.unchecked( wrapPixelsToAccess( pixels ) ) );
		}
		catch ( UnsupportedOperationException e )
		{
			// intentionally do nothing
			// NB: Both methods VirtualStack.setPixels(...) and
			// VirtualStackAdapter.wrap( imagePlus ).setPlane(...) are not
			// implemented. But they handle this with different strategies:
			//   * VirtualStack.setPixels(...) -> does nothing
			//   * VirtualStackAdapter.wrap( imagePlus ).setPlane(...)
			//       -> throws an UnsupportedOperationException
			// This try-catch-block adapts between the two strategies by
			// doing nothing (like VirtualStack.setPixels(...)),
			// if setPlane(...) throws an UnsupportedOperationException.
		}
	}

	private ArrayDataAccess< ? > wrapPixelsToAccess( Object pixels )
	{
		if ( pixels instanceof byte[] )
			return new ByteArray( ( byte[] ) pixels );
		if ( pixels instanceof short[] )
			return new ShortArray( ( short[] ) pixels );
		if ( pixels instanceof int[] )
			return new IntArray( ( int[] ) pixels );
		if ( pixels instanceof float[] )
			return new FloatArray( ( float[] ) pixels );
		throw new UnsupportedOperationException();
	}

	// Helper methods

	private static int getBitDepth( final Type< ? > type )
	{
		if ( type instanceof UnsignedByteType )
			return 8;
		if ( type instanceof UnsignedShortType )
			return 16;
		if ( type instanceof ARGBType )
			return 24;
		if ( type instanceof FloatType )
			return 32;
		throw new IllegalArgumentException( "unsupported type" );
	}

}
