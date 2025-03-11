/*-
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

import net.imglib2.imagej.RAIToImagePlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State( Scope.Benchmark )
public class ImgLib2ToVirtualStackBenchmark
{

	long[] smallDims = { 10, 10, 10 };
	long[] deepDims = { 10, 10, 1000000 };
	long[] cubicDims = { 1000, 1000, 1000 };

	private final CellImgFactory<UnsignedByteType> fac = new CellImgFactory<>( new UnsignedByteType() );
	private final CellImg< UnsignedByteType, ? > smallCellImage = fac.create( smallDims );
	private final CellImg< UnsignedByteType, ? > deepCellImage = fac.create( deepDims );
	private final CellImg< UnsignedByteType, ? > cubicCellImage = fac.create( cubicDims );
	private final PlanarImg< UnsignedByteType, ByteArray > smallPlanarImg = PlanarImgs.unsignedBytes( smallDims );
	private final PlanarImg< UnsignedByteType, ByteArray > cubicPlanarImg = PlanarImgs.unsignedBytes( cubicDims );
	private final PlanarImg< UnsignedByteType, ByteArray > deepPlanarImg = PlanarImgs.unsignedBytes( deepDims );
	private final ArrayImg< UnsignedByteType, ByteArray > small2dArrayImg = ArrayImgs.unsignedBytes( 10, 10 );
	private final ArrayImg< UnsignedByteType, ByteArray> big2dArrayImg = ArrayImgs.unsignedBytes( 10000, 10000 );

	@Benchmark
	public void testSmallCellImg()
	{
		RAIToImagePlus.wrap( smallCellImage, "test" );
	}

	@Benchmark
	public void testDeepCellImg()
	{
		RAIToImagePlus.wrap(deepCellImage, "test" );
	}

	@Benchmark
	public void testCubicCellImg()
	{
		RAIToImagePlus.wrap(cubicCellImage, "test" );
	}

	@Benchmark
	public void testSmallPlanarImg()
	{
		PlanarImgToImagePlus.wrap( smallPlanarImg, "test" );
	}

	@Benchmark
	public void testCubicPlanarImg()
	{
		PlanarImgToImagePlus.wrap( cubicPlanarImg, "test" );
	}

	@Benchmark
	public void testDeepPlanarImg()
	{
		PlanarImgToImagePlus.wrap( deepPlanarImg, "test" );
	}

	@Benchmark
	public void testSmall2dArrayImg()
	{
		ArrayImgToImagePlus.wrap( small2dArrayImg, "test" );
	}

	@Benchmark
	public void testLarge2dArrayImg()
	{
		ArrayImgToImagePlus.wrap( big2dArrayImg, "test" );
	}

	public static void main( final String... args ) throws RunnerException
	{
		final Options opt = new OptionsBuilder()
				.include( ImgLib2ToVirtualStackBenchmark.class.getSimpleName() )
				.forks( 0 )
				.warmupIterations( 4 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}
}
