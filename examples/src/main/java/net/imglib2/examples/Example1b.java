package net.imglib2.examples;

import java.io.File;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.io.ImgOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import mpicbg.imglib.container.cell.CellContainerFactory;
import ij.ImageJ;

/**
 * Opens a file with ImgOpener Bioformats as an ImgLib {@link Image}.
 *
 * @author Stephan Preibisch &amp; Stephan Saalfeld
 *
 */
public class Example1b
{
	// within this method we define <T> to be a RealType
	public < T extends RealType< T > & NativeType< T > > Example1b()
	{
		// define the file to open
		File file = new File( "DrosophilaWing.tif" );

		// open with ImgOpener using an ArrayImgFactory, here the return type will be defined by the opener
		Img< T > image = new ImgOpener().openImg( file.getAbsolutePath(), new ArrayImgFactory< T >() );

		// display it via ImgLib using ImageJ
		ImageJFunctions.show( image );

		// open with ImgOpener as Float using an ArrayImgFactory
		Img< FloatType > imageFloat = new ImgOpener().openLOCIFloatType( file.getAbsolutePath(), new CellContainerFactory( 10 ) );

		// display it via ImgLib using ImageJ
		ImageJFunctions.show( imageFloat );
	}

	public static void main( String[] args )
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new Example1b();
	}
}