package tests.subimg;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.subimg.ImgView;
import net.imglib2.img.subimg.KNIPViews;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class SubImgTest
{

	// Not very general test to check a {@link SubImg}
	@Test
	public void testSubImg()
	{
		// Random source img with pixel at (0,0,0) = 1!
		Img< UnsignedByteType > sourceImg = new ArrayImgFactory< UnsignedByteType >().create( new long[] { 10, 10, 10 }, new UnsignedByteType() );
		sourceImg.firstElement().set( 1 );

		// SubImg from [1,0,0] to [9,9,9] NOT including the first pixel
		ImgView< UnsignedByteType > subImg = new ImgView< UnsignedByteType >( KNIPViews.getSubsetView( sourceImg, new FinalInterval( new long[] { 1, 0, 0 }, new long[] { 9, 9, 9 } ), false ), sourceImg.factory() );

		// Cursor over SUBIMG
		Cursor< UnsignedByteType > subCursor = subImg.cursor();

		long[] pos = new long[ subCursor.numDimensions() ];

		// Cursor position should clearly be 0, as this cursor should only
		// return values from [1,0,0] to [9,9,9] and only the first value is set
		assertTrue( subCursor.next().get() == 0 );

		subCursor.localize( pos );
		// Pos should be at [0,0,0] as the SubImg should act like an Img
		assertArrayEquals( pos, new long[ sourceImg.numDimensions() ] );

	}

	// // Not very general test to check a {@link SubImg}
	// @Test
	// public void testSubImgSuper()
	// {
	// Img< UnsignedByteType > sourceImg = new ArrayImgFactory< UnsignedByteType
	// >().create( new long[] { 10, 10 }, new UnsignedByteType() );
	// sourceImg.firstElement().set( 1 );
	//
	// ImgView< UnsignedByteType > subImg = new ImgView< UnsignedByteType >(
	// sourceImg, new FinalInterval( new long[] { 1, 0, 0 }, new long[] { 10,
	// 10, 100 } ), false );
	//
	// RandomAccess< UnsignedByteType > rndAccess = subImg.randomAccess();
	//
	// rndAccess.setPosition( new long[] { 0, 0, 0 } );
	// assertTrue( rndAccess.get().get() == 0 );
	//
	// // Cursor position should clearly be 0, as this cursor should only
	// // return values from [1,0,0] to [9,9,9] and only the first value is set
	// Cursor< UnsignedByteType > subCursor = subImg.cursor();
	// subCursor.next();
	//
	// long[] pos = new long[ subCursor.numDimensions() ];
	// subCursor.localize( pos );
	// // Pos should be at [0,0,0] as the SubImg should act like an Img
	// assertArrayEquals( pos, new long[ sourceImg.numDimensions() ] );
	//
	// }
}
