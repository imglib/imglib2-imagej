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

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.imagej.ImagePlusToImg;
import net.imglib2.imagej.RAIToImagePlus;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * TODO
 *
 */
public class ImageJFunctionBehavior
{

	static private void print(final Img<?> img)
	{
		System.out.println( "img: " + img );
	}

	static private void print(final ImagePlus imp)
	{
		System.out.println( "imp: " + imp );
	}

	static public final void main( final String[] arg )
	{
		final ImagePlus imp = IJ.openImage( "http://imagej.nih.gov/ij/images/bat-cochlea-volume.zip" );

		System.out.println( "Opened image: " + imp );

		// 1. Test ImagePlus -> Img, specific wrappers
		IJ.run( imp, "8-bit", "" );
		final Img< UnsignedByteType > imgb = ImagePlusToImg.wrapByte( imp );
		print( imgb );

		IJ.run( imp, "16-bit", "" );
		final Img< UnsignedShortType > imgs = ImagePlusToImg.wrapShort( imp );
		print( imgs );

		IJ.run( imp, "32-bit", "" );
		final Img< FloatType > imgf = ImagePlusToImg.wrapFloat( imp );
		print( imgf );

		IJ.run( imp, "RGB Color", "" );
		final Img< ARGBType > imgRGB = ImagePlusToImg.wrapRGBA( imp );
		print( imgRGB );

		// 2. Test ImagePlus -> Img, generic wrapper
		IJ.run( imp, "8-bit", "" );
		final Img< UnsignedByteType > g_imgb = (Img<UnsignedByteType>) ImagePlusToImg.wrap( imp );
		print( g_imgb );

		IJ.run( imp, "16-bit", "" );
		final Img< UnsignedShortType > g_imgs = (Img<UnsignedShortType>) ImagePlusToImg.wrap( imp );
		print( g_imgs );

		IJ.run( imp, "32-bit", "" );
		final Img< FloatType > g_imgf = (Img<FloatType>) ImagePlusToImg.wrap( imp );
		print( g_imgf );

		IJ.run( imp, "RGB Color", "" );
		final Img< ARGBType > g_imgRGB = (Img<ARGBType>) ImagePlusToImg.wrap( imp );
		print( g_imgRGB );

		// 3. Test Img -> ImagePlus, specific wrappers
		final ImagePlus impb = RAIToImagePlus.wrapUnsignedByte( imgb, "byte Img" );
		print( impb );

		final ImagePlus imps = RAIToImagePlus.wrapUnsignedShort( imgs, "short Img" );
		print( imps );

		final ImagePlus impf = RAIToImagePlus.wrapFloat( imgf, "float Img" );
		print( impf );

		final ImagePlus impRGB = RAIToImagePlus.wrapRGB( imgRGB, "RGB Img" );
		print( impRGB );

		// 4. Test Img -> ImagePlus, generic wrappers
		final ImagePlus g_impb = RAIToImagePlus.wrap( imgb, "byte Img" );
		print( g_impb );

		final ImagePlus g_imps = RAIToImagePlus.wrap( imgs, "short Img" );
		print( g_imps );

		final ImagePlus g_impf = RAIToImagePlus.wrap( imgf, "float Img" );
		print( g_impf );

		final ImagePlus g_impRGB = RAIToImagePlus.wrap( imgRGB, "RGB Img" );
		print( g_impRGB );
	}
}
