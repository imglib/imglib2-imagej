package net.imglib2.imagej;

import ij.ImagePlus;
import ij.VirtualStack;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.converter.ComplexPowerGLogFloatConverter;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.converter.readwrite.SamplerConverter;
import net.imglib2.imagej.img.*;
import net.imglib2.img.basictypeaccess.IntAccess;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.*;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.GenericComposite;

import java.util.concurrent.ExecutorService;

/**
 * Utilities for <b>wrapping</b> {@link RandomAccessibleInterval}s
 * into {@link ImagePlus}es.
 * <p>
 * Under the hood, conversion utilizes {@link ImageJVirtualStack}, allowing
 * read/write operations to the backing {@link RandomAccessibleInterval}.
 * </p>
 *
 * @author Tobis Pietzsch
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Gabriel Selzer
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
     * @param <T> element type in source image
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
        else if ( t instanceof IntegerType ) {
            final int bitDepth = ((IntegerType<?>) t).getBitsPerPixel();
            final boolean isSigned = ((IntegerType<?>) t).getMinValue() < 0;
            if (bitDepth <= 8 && !isSigned)
                target = wrapUnsignedByte(Cast.unchecked(img), title, service);
            else if (bitDepth <= 16 && !isSigned)
                target = wrapUnsignedShort(Cast.unchecked(img), title, service);
            else
                target = wrapFloat( Cast.unchecked( img ), title, service );
        }
        else if ( t instanceof RealType )
            target = wrapFloat( Cast.unchecked( img ), title, service );
        else if ( t instanceof ComplexType )
            target = wrapFloat( Cast.unchecked( img ), new ComplexPowerGLogFloatConverter(), title, service );
        else
        {
            System.out.println( "Do not know how to display Type " + t.getClass().getSimpleName() );
            target = null;
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
     * @param <T> element type in source image
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
     * {@link RandomAccessibleInterval} using a default {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the data to wrap.
     * @param title the title to set on the resulting {@link ImagePlus}.
     * @param service an {@link ExecutorService} used for processing.
     * @return an RGB {@link ImagePlus} wrapping {@code img}
     */
    public static < T extends RealType< T > > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackFloat stack = ImageJVirtualStackFloat.wrap( img );
        stack.setExecutorService( service );
        final ImagePlus imp = makeImagePlus( img, stack, title );
        // NB: setWritable after the ImagePlus is created. Otherwise a useless stack.setPixels(...) call would be performed.
        stack.setWritable( true );
        return imp;
    }

    /**
     * Create a single channel 32-bit float {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a default {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the data to wrap.
     * @param title the title to set on the resulting {@link ImagePlus}.
     * @return an RGB {@link ImagePlus} wrapping {@code img}
     */
    public static < T extends RealType< T > > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final String title )
    {
        return wrapFloat( img, title, null );
    }

    /**
     * Create a single channel 32-bit float {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the data to wrap.
     * @param converter a {@link Converter} turning {@link T} into a an {@link FloatType}
     * @param title the title to set on the resulting {@link ImagePlus}.
     * @param service an {@link ExecutorService} used for processing.
     * @return an RGB {@link ImagePlus} wrapping {@code img}
     */
    public static < T > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final Converter< T, FloatType> converter,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackFloat stack = new ImageJVirtualStackFloat( img, converter, service );
        final ImagePlus imp = makeImagePlus( img, stack, title );
        // NB: setWritable after the ImagePlus is created. Otherwise a useless stack.setPixels(...) call would be performed.
        stack.setWritable( true );
        return imp;
    }

    /**
     * Create a single channel 32-bit float {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the data to wrap.
     * @param converter a {@link Converter} turning {@link T} into a an {@link FloatType}
     * @param title the title to set on the resulting {@link ImagePlus}.
     * @return an RGB {@link ImagePlus} wrapping {@code img}
     */
    public static < T > ImagePlus wrapFloat(
            final RandomAccessibleInterval< T > img,
            final Converter< T, FloatType > converter,
            final String title )
    {
        return wrapFloat( img, converter, title, null );
    }

    /**
     * Create a 24bit RGB {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} with {@link ARGBType} elements.
     *
     * @param img the data to wrap.
     * @param title the title to set on the resulting {@link ImagePlus}.
     * @param service an {@link ExecutorService} used for processing.
     * @return an RGB {@link ImagePlus} wrapping {@code img}
     */
    public static ImagePlus wrapRGB( final RandomAccessibleInterval< ARGBType > img, final String title,
                                     final ExecutorService service)
    {
        final ImageJVirtualStackARGB stack = ImageJVirtualStackARGB.wrap( img );
        stack.setExecutorService(service);
        final ImagePlus imp = makeImagePlus( img, stack, title );
        // NB: setWritable after the ImagePlus is created. Otherwise a useless stack.setPixels(...) call would be performed.
        stack.setWritable( true );
        return imp;
    }

    /**
     * Create a 24bit RGB {@link ImagePlus} wrapping a
     * {@link RandomAccessibleInterval} with {@link ARGBType} elements.
     *
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an RGB {@link ImagePlus} wrapping {@code img}
     */
    public static ImagePlus wrapRGB( final RandomAccessibleInterval< ARGBType > img, final String title) {
        return wrapRGB(img, title, null);
    }

    /**
     * Create a 24bit RGB {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} a using a default {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an RGB {@link ImagePlus} wrapping {@code img}
     */
    public static < T extends RealType<T>> ImagePlus convertRGB( final RandomAccessibleInterval< T > img, final String title )
    {
        // Currently assumes 5d, XYCZT
        final boolean hasAlpha = img.dimension(2) != 3;
        RandomAccessibleInterval<T> permuted = Views.moveAxis(img, 2, img.numDimensions() - 1);

        RandomAccessibleInterval<GenericComposite<T>> collapsed = Cast.unchecked(Views.collapse(permuted));
        RandomAccessibleInterval<ARGBType> rgbRAI = Converters.convert(
            collapsed, //
            new RAIToImagePlus.RGBAConverter<>(hasAlpha) //
        );

        final ImagePlus imp = wrapRGB( rgbRAI, title, null );

        final int c = 1;
        final int z = img.numDimensions() > 3 ? (int) img.dimension(3) : 1;
        final int t = img.numDimensions() > 4 ? (int) img.dimension(4) : 1;
        imp.setDimensions(c, z, t);

        return imp;
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @param service the {@link ExecutorService} used for processing.
     * @return an {@link ImagePlus} wrapping {@code img}
     */
    public static < T extends RealType< T > > ImagePlus wrapUnsignedByte(
            final RandomAccessibleInterval< T > img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackUnsignedByte stack = ImageJVirtualStackUnsignedByte.wrap( img );
        stack.setExecutorService( service );
        final ImagePlus imp = makeImagePlus( img, stack, title );
        // NB: setWritable after the ImagePlus is created. Otherwise a useless stack.setPixels(...) call would be performed.
        stack.setWritable( true );
        return imp;
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an {@link ImagePlus} wrapping {@code img}
     */
    public static < T extends RealType< T > > ImagePlus wrapUnsignedByte(
            final RandomAccessibleInterval< T > img,
            final String title )
    {
        return wrapUnsignedByte( img, title, null );
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param converter a {@link Converter} turning {@link T} into a an {@link UnsignedByteType}
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @param service the {@link ExecutorService} used for processing.
     * @return an {@link ImagePlus} wrapping {@code img}
     */
    public static < T > ImagePlus wrapUnsignedByte(
            final RandomAccessibleInterval< T > img,
            final Converter< T, UnsignedByteType > converter,
            final String title,
            final ExecutorService service )
    {
        return wrapUnsignedByte( Converters.convert( img, converter, new UnsignedByteType() ), title, service );
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param converter a {@link Converter} turning {@link T} into a an {@link UnsignedByteType}
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an {@link ImagePlus} wrapping {@code img}
     */
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
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @param service the {@link ExecutorService} used for processing.
     * @return an {@link ImagePlus} wrapping {@code img}
     */
    public static < T extends RealType< T > > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackUnsignedShort stack = ImageJVirtualStackUnsignedShort.wrap( img );
        stack.setExecutorService( service );
        final ImagePlus imp = makeImagePlus( img, stack, title );
        // NB: setWritable after the ImagePlus is created. Otherwise a useless stack.setPixels(...) call would be performed.
        stack.setWritable( true );
        return imp;
    }

    /**
     * Create a single channel 16-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a default {@link Converter} (clamp
     * values to range [0, 65535]).
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an {@link ImagePlus} wrapping {@code img}
     */
    public static < T extends RealType< T > > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final String title )
    {
        return wrapUnsignedShort( img, title, null );
    }

    /**
     * Create a single channel 16-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param converter a {@link Converter} turning {@link T} into a an {@link UnsignedShortType}
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @param service the {@link ExecutorService} used for processing.
     * @return an {@link ImagePlus} wrapping {@code img}
     */
    public static < T > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final Converter< T, UnsignedShortType> converter,
            final String title,
            final ExecutorService service )
    {
        return wrapUnsignedShort( Converters.convert( img, converter, new UnsignedShortType() ), title, service );
    }

    /**
     * Create a single channel 16-bit unsigned integer {@link ImagePlus} from a
     * {@link RandomAccessibleInterval} using a custom {@link Converter}.
     *
     * @param <T> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param converter a {@link Converter} turning {@link T} into a an {@link UnsignedShortType}
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an {@link ImagePlus} wrapping {@code img}
     */
    public static < T > ImagePlus wrapUnsignedShort(
            final RandomAccessibleInterval< T > img,
            final Converter< T, UnsignedShortType > converter,
            final String title )
    {
        return wrapUnsignedShort( img, converter, title, null );
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link BooleanType} {@link RandomAccessibleInterval} using a custom
     * {@link Converter}.
     *
     * @param <B> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @param service the {@link ExecutorService} used for processing.
     * @return an {@link ImagePlus} wrapping {@code img}
     * @see #wrapAndScaleBit(RandomAccessibleInterval, String, ExecutorService) for wrapping with scaling.
     */
    public static < B extends BooleanType< B >> ImagePlus wrapBit(
            final RandomAccessibleInterval< B > img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackUnsignedByte stack = ImageJVirtualStackUnsignedByte.wrap( img );
        stack.setExecutorService( service );
        final ImagePlus imp = makeImagePlus( img, stack, title );
        // NB: setWritable after the ImagePlus is created. Otherwise a useless stack.setPixels(...) call would be performed.
        stack.setWritable( true );
        return imp;
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link BooleanType} {@link RandomAccessibleInterval} using a custom
     * {@link Converter}.
     *
     * @param <B> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an {@link ImagePlus} wrapping {@code img}
     * @see #wrapAndScaleBit(RandomAccessibleInterval, String) for wrapping with scaling.
     */
    public static < B extends BooleanType< B >> ImagePlus wrapBit(
            final RandomAccessibleInterval< B > img,
            final String title )
    {
        return wrapBit( img, title, null );
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link BooleanType} {@link RandomAccessibleInterval} using a custom
     * {@link Converter}.
     * <p>
     * Note that this method scales the result such that {@code true} values in {@code img} are mapped to {@code 255} in the resulting {@code ImagePlus}.
     * </p>
     *
     * @param <B> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @param service the {@link ExecutorService} used for processing.
     * @return an {@link ImagePlus} wrapping {@code img}
     * @see #wrapBit(RandomAccessibleInterval, String, ExecutorService) for wrapping without scaling.
     */
    public static < B extends BooleanType<B> > ImagePlus wrapAndScaleBit(
            final RandomAccessibleInterval<B> img,
            final String title,
            final ExecutorService service )
    {
        final ImageJVirtualStackUnsignedByte stack = ImageJVirtualStackUnsignedByte.wrapAndScaleBitType( img );
        stack.setExecutorService( service );
        final ImagePlus imp = makeImagePlus( img, stack, title );
        // NB: setWritable after the ImagePlus is created. Otherwise a useless stack.setPixels(...) call would be performed.
        stack.setWritable( true );
        return imp;
    }

    /**
     * Create a single channel 8-bit unsigned integer {@link ImagePlus} from a
     * {@link BooleanType} {@link RandomAccessibleInterval} using a custom
     * {@link Converter}.
     * <p>
     * Note that this method scales the result such that {@code true} values in {@code img} are mapped to {@code 255} in the resulting {@code ImagePlus}.
     * </p>
     *
     * @param <B> element type in source image
     * @param img the {@link RandomAccessibleInterval} to wrap
     * @param title the name assigned to the resulting {@link ImagePlus}
     * @return an {@link ImagePlus} wrapping {@code img}
     * @see #wrapBit(RandomAccessibleInterval, String) for wrapping without scaling.
     */
    public static < B extends BooleanType<B> > ImagePlus wrapAndScaleBit(
            final RandomAccessibleInterval<B> img,
            final String title )
    {
        return wrapAndScaleBit( img, title, null );
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

    private static class RGBAConverter<T extends RealType<T>, C extends Composite<T>> implements SamplerConverter<C, ARGBType> {

        final boolean hasAlpha;

        public RGBAConverter(final boolean hasAlpha) {
            this.hasAlpha = hasAlpha;
        }

        @Override
        public ARGBType convert(Sampler<? extends C> sampler) {
            return new ARGBType(new IntAccess() {
                @Override
                public int getValue(int index) {
                    Composite<T> in = sampler.get();
                    return ARGBType.rgba( //
                            toInt( in.get( 0 ) ), //
                            toInt( in.get( 1 ) ), //
                            toInt( in.get( 2 ) ), //
                            hasAlpha ? toInt( in.get( 3 ) ) : 255 //
                    );
                }

                @Override
                public void setValue(int index, int value) {
                    Composite<T> in = sampler.get();
                    in.get(0).setReal(ARGBType.red(value));
                    in.get(1).setReal(ARGBType.green(value));
                    in.get(2).setReal(ARGBType.blue(value));
                    if (hasAlpha) {
                        in.get(3).setReal(ARGBType.alpha(value));
                    }
                }
            });
        }

        private int toInt( final RealType< ? > realType )
        {
            return ( int ) realType.getRealFloat();
        }
    }


}
