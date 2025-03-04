package net.imglib2.imagej;

import ij.ImagePlus;
import ij.VirtualStack;
import net.imagej.ImgPlus;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.ComplexPowerGLogFloatConverter;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.imagej.img.*;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.*;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;

import java.util.concurrent.ExecutorService;

/**
 * Utilities for <b>wrapping</b> {@link RandomAccessibleInterval}s
 * into {@link ImagePlus}es.
 * <p>
 * Under the hood, conversion utilizes {@link ImageJVirtualStack},
 * meaning the resulting objects are <b>read-only</b> unless
 * {@link ImageJVirtualStack#setWritable(boolean)} is called.
 * </p>
 *
 * @author Tobis Pietzsch
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class RAIToImagePlus {

    // Prevent instantiation of utility class
    private RAIToImagePlus() {}

    /**
     * Create a single channel {@link ImagePlus} from a
     * {@link RandomAccessibleInterval}. The image type of the result
     * (ImagePlus.GRAY8, ImagePlus.GRAY16, ImagePlus.GRAY32, ImagePlus.COLOR_256
     * or ImagePlus.COLOR_RGB) is inferred from the generic type of the input
     * {@link RandomAccessibleInterval}.
     *
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name to assign to the wrapped {@link ImagePlus}
     * @param service an {@link ExecutorService} to manage asynchronous tasks
     * @return an {@link ImagePlus} wrapping {@code img} named {@code title}
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public static < T extends NumericType< T > > ImagePlus wrap( final RandomAccessibleInterval< T > img, final String title,
                                                                 final ExecutorService service )
    {
        ImagePlus target;
        final T t = img.getType();

        if ( t instanceof ARGBType )
            target = wrapRGB( Cast.unchecked( img ), title, service );
        else if ( t instanceof UnsignedByteType )
            target = wrapUnsignedByte( Cast.unchecked( img ), title, service );
        else if ( t instanceof BitType )
            target = wrapBit( Cast.unchecked( img ), title, service );
        else if ( t instanceof IntegerType )
            target = wrapUnsignedShort( Cast.unchecked( img ), title, service );
        else if ( t instanceof RealType )
            target = wrapFloat( Cast.unchecked( img ), title, service );
        else if ( t instanceof ComplexType )
            target = wrapFloat( Cast.unchecked( img ), new ComplexPowerGLogFloatConverter(), title, service );
        else
        {
            System.out.println( "Do not know how to display Type " + t.getClass().getSimpleName() );
            target = null;
        }

        // Retrieve and set calibration if we can. ImgPlus has calibration and
        // axis types
        if ( null != target && img instanceof ImgPlus )
        {

            final ImgPlus< T > imgplus = ( ImgPlus< T > ) img;
            CalibrationUtils.copyCalibrationToImagePlus( imgplus, target );
            target.setTitle( imgplus.getName() );
        }

        return target;
    }

    /**
     * Create a single channel {@link ImagePlus} from a
     * {@link RandomAccessibleInterval}. The image type of the result
     * (ImagePlus.GRAY8, ImagePlus.GRAY16, ImagePlus.GRAY32, ImagePlus.COLOR_256
     * or ImagePlus.COLOR_RGB) is inferred from the generic type of the input
     * {@link RandomAccessibleInterval}.
     *
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name to assign to the wrapped {@link ImagePlus}
     * @return an {@link ImagePlus} wrapping {@code img} named {@code title}
     */
    public static < T extends NumericType< T > > ImagePlus wrap( final RandomAccessibleInterval< T > img, final String title )
    {
        return wrap( img, title, null );
    }

    /**
     * Create a single channel 32-bit float {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     */
    public static < T extends RealType< T > > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackFloat stack = ImageJVirtualStackFloat.wrap( img );
        stack.setExecutorService( service );
        return makeImagePlus( img, stack, title );
    }

    public static < T extends RealType< T > > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final String title )
    {
        return wrapFloat( img, title, null );
    }

    /**
     * Create a single channel 32-bit float {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     */
    public static < T > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final Converter< T, FloatType> converter,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackFloat stack = new ImageJVirtualStackFloat( img, converter, service );
        return makeImagePlus( img, stack, title );
    }

    public static < T > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final Converter< T, FloatType > converter,
            final String title )
    {
        return wrapFloat( img, converter, title, null );
    }

    /**
     * Create a 24bit RGB {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} a using a default (identity)
     * {@link Converter}.
     */
    public static ImagePlus wrapRGB( final RandomAccessibleInterval< ARGBType > img, final String title,
                                     final ExecutorService service )
    {
        final ImageJVirtualStackARGB stack = ImageJVirtualStackARGB.wrap( img );
        stack.setExecutorService(service);
        return makeImagePlus( img, stack, title );
    }

    public static ImagePlus wrapRGB( final RandomAccessibleInterval< ARGBType > img, final String title )
    {
        return wrapRGB( img, title, null );
    }

    /**
     * Create a 24bit RGB {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} a using a custom {@link Converter}.
     */
    public static < T > ImagePlus wrapRGB( final RandomAccessibleInterval< T > img, final Converter< T, ARGBType > converter, final String title,
                                           final ExecutorService service )
    {
        return wrapRGB( Converters.convert( img, converter, new ARGBType() ), title, service );
    }

    public static < T > ImagePlus wrapRGB( final RandomAccessibleInterval< T > img, final Converter< T, ARGBType > converter, final String title )
    {
        return wrapRGB( img, converter, title, null );
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     */
    public static < T extends RealType< T > > ImagePlus wrapUnsignedByte(
            final RandomAccessibleInterval< T > img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackUnsignedByte stack = ImageJVirtualStackUnsignedByte.wrap( img );
        stack.setExecutorService( service );
        return makeImagePlus( img, stack, title );
    }

    public static < T extends RealType< T > > ImagePlus wrapUnsignedByte(
            final RandomAccessibleInterval< T > img,
            final String title )
    {
        return wrapUnsignedByte( img, title, null );
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * BitType {@link RandomAccessibleInterval} using a custom {@link Converter}
     * .
     */
    public static < T extends RealType< T > > ImagePlus wrapBit(
            final RandomAccessibleInterval< T > img,
            final String title,
            final ExecutorService service )
    {
        return wrapUnsignedByte( img, new RealUnsignedByteConverter< T >( 0, 1 ), title, service );
    }

    public static < T extends RealType< T > > ImagePlus wrapBit(
            final RandomAccessibleInterval< T > img,
            final String title )
    {
        return wrapBit( img, title, null );
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     */
    public static < T > ImagePlus wrapUnsignedByte(
            final RandomAccessibleInterval< T > img,
            final Converter< T, UnsignedByteType > converter,
            final String title,
            final ExecutorService service )
    {
        return wrapUnsignedByte( Converters.convert( img, converter, new UnsignedByteType() ), title, service );
    }

    public static < T > ImagePlus wrapUnsignedByte(
            final RandomAccessibleInterval< T > img,
            final Converter< T, UnsignedByteType > converter,
            final String title )
    {
        return wrapUnsignedByte( img, converter, title, null );
    }

    /**
     * Create a single channel 16-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a default {@link Converter} (clamp
     * values to range [0, 65535]).
     */
    public static < T extends RealType< T > > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackUnsignedShort stack = ImageJVirtualStackUnsignedShort.wrap( img );
        stack.setExecutorService( service );
        return makeImagePlus( img, stack, title );
    }

    public static < T extends RealType< T > > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final String title )
    {
        return wrapUnsignedShort( img, title, null );
    }

    /**
     * Create a single channel 16-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     */
    public static < T > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final Converter< T, UnsignedShortType> converter,
            final String title,
            final ExecutorService service )
    {
        return wrapUnsignedShort( Converters.convert( img, converter, new UnsignedShortType() ), title, service );
    }

    public static < T > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final Converter< T, UnsignedShortType > converter,
            final String title )
    {
        return wrapUnsignedShort( img, converter, title, null );
    }

    private static ImagePlus makeImagePlus( final Dimensions dims, final VirtualStack stack, final String title )
    {
        final ImagePlus imp = new ImagePlus( title, stack );
        final int n = dims.numDimensions();
        if ( n > 2 )
        {
            imp.setOpenAsHyperStack( true );
            final int c = ( int ) dims.dimension( 2 ), s, f;
            if ( n > 3 )
            {
                s = ( int ) dims.dimension( 3 );
                if ( n > 4 )
                    f = ( int ) dims.dimension( 4 );
                else
                    f = 1;
            }
            else
            {
                s = 1;
                f = 1;
            }
            imp.setDimensions( c, s, f );
        }
        return imp;
    }

}
