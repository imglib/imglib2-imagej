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
import ij.process.ImageProcessor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

public class ArrayImgToImagePlus
{
	private ArrayImgToImagePlus()
	{
		// prevent from instantiation
	}

	/**
	 * Indicates if {@link #wrap(net.imglib2.img.array.ArrayImg, String)} wrap} supports the image.
	 *
	 * @param obj an {@link Object} that may be supported by {@code wrap}
	 * @return {@code true} iff {@code obj} can be converted into an {@link ImagePlus}.
	 * @see PlanarImgToImagePlus
	 * @see CellImgToImagePlus
	 */
	public static boolean isSupported( Object obj )
	{
		if (! (obj instanceof ArrayImg))
			return false;
		ArrayImg<?, ?> img = (ArrayImg<?, ?>) obj;
		Object storageArray = img.update(null);
		if (! (storageArray instanceof ArrayDataAccess))
			return false;
		return img.numDimensions() == 2 &&
				ImageProcessorUtils.isSupported( img.getType() );
	}

	/**
	 * Takes an {@link ArrayImg} and wraps it into an {@link ImagePlus}
	 * (IJ1). This only works when {@link ArrayImg} is backed by a two
	 * dimensional {@link ArrayImg}. Type of the image must be
	 * {@link UnsignedByteType}, {@link UnsignedShortType}, {@link ARGBType} or
	 * {@link FloatType}.
	 * <p>
	 * The returned {@link ImagePlus} uses the same pixel buffer as the given
	 * image. Changes to the {@link ImagePlus} are therefore correctly reflected
	 * in the {@link ArrayImg}. The title and calibration are derived from the
	 * given image.
	 * <p>
	 * Use {@link #isSupported(Object)} to check if an {@link ImagePlus} is
	 * supported.
	 *
	 * @param img the {@link ArrayImg} to convert
	 * @param name the {@link String} title to assign to the result
	 * @return an {@link ImagePlus} wrapping the data in {@code img}
	 * @see PlanarImgToImagePlus
	 * @see CellImgToImagePlus
	 */
	public static ImagePlus wrap(ArrayImg< ?, ? extends ArrayDataAccess<?> > img, String name)
	{
		final int sizeX = ( int ) img.dimension( 0 );
		final int sizeY = ( int ) img.dimension( 1 );
		final Object pixels = img.update( null ).getCurrentStorageArray();
		final ImageProcessor processor = ImageProcessorUtils.createImageProcessor( pixels, sizeX, sizeY, null );
        return new ImagePlus( name, processor );
	}

}
