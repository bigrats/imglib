package net.imglib2.img.array;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.IntervalIndexer;

import org.junit.Before;
import org.junit.Test;

public class IterableSubIntervalCursorTest
{
	long[] dimensions;

	Interval intervalA, intervalB, intervalC;

	int numValues, fastintervalsize, shiftedintervalsize;

	int[] intData;

	long intDataSum;

	ArrayImg< IntType, ? > intImg;

	@Before
	public void createSourceData()
	{
		dimensions = new long[] { 23, 31, 11, 7, 3 };

		intervalA = new FinalInterval( new long[] { 23, 31, 5, 1, 1 } );

		intervalB = new FinalInterval( new long[] { 23, 2, 3, 1, 1 } );

		intervalC = new FinalInterval( new long[] { 0, 0, 3, 5, 1 }, new long[] { 22, 30, 4, 5, 1 } );

		// Size of the planes which can be iterated fast
		shiftedintervalsize = 23 * 31 * 2;

		fastintervalsize = 23 * 31 * 5;

		numValues = 1;
		for ( int d = 0; d < dimensions.length; ++d )
			numValues *= dimensions[ d ];

		intData = new int[ numValues ];
		intDataSum = 0;
		Random random = new Random( 0 );
		for ( int i = 0; i < numValues; ++i )
		{
			intData[ i ] = random.nextInt();
			intDataSum += intData[ i ];
		}

		intImg = ( ArrayImg< IntType, ? > ) new ArrayImgFactory< IntType >().create( dimensions, new IntType() );

		long[] pos = new long[ dimensions.length ];
		RandomAccess< IntType > a = intImg.randomAccess();

		for ( int i = 0; i < numValues; ++i )
		{
			IntervalIndexer.indexToPosition( i, dimensions, pos );
			a.setPosition( pos );
			a.get().set( intData[ i ] );
		}
	}

	@Test
	public void testOptimizable()
	{

		// Testing Cursor
		assertTrue( ( intImg.cursor( intervalA ) instanceof AbstractArrayCursor ) );

		// Testing Localizing Cursor
		assertTrue( ( intImg.localizingCursor( intervalA ) instanceof AbstractArrayLocalizingCursor ) );

		// Testing Cursor
		assertFalse( ( intImg.cursor( intervalB ) instanceof AbstractArrayCursor ) );

		// Testing Localizing Cursor
		assertFalse( ( intImg.localizingCursor( intervalB ) instanceof AbstractArrayLocalizingCursor ) );

		// Testing Cursor
		assertTrue( ( intImg.cursor( intervalC ) instanceof AbstractArrayCursor ) );

		// Testing Localizing Cursor
		assertTrue( ( intImg.localizingCursor( intervalC ) instanceof AbstractArrayLocalizingCursor ) );
	}

	@Test
	public void testIterationFast()
	{
		Cursor< IntType > cursor = intImg.cursor( intervalA );

		long[] position = new long[ cursor.numDimensions() ];
		long[] max = new long[ cursor.numDimensions() ];

		int ctr = 0;

		while ( cursor.hasNext() )
		{
			cursor.next();
			cursor.localize( position );
			ctr++;
		}

		intervalA.max( max );

		assertTrue( Arrays.equals( max, position ) );
		assertTrue( ctr == fastintervalsize );

	}

	@Test
	public void testIterationShifted()
	{
		Cursor< IntType > cursor = intImg.cursor( intervalC );

		long[] position = new long[ cursor.numDimensions() ];
		long[] tmp = new long[ cursor.numDimensions() ];

		intervalC.min( tmp );

		cursor.fwd();
		cursor.localize( position );
		assertTrue( Arrays.equals( tmp, position ) );

		cursor.reset();
		int ctr = 0;
		while ( cursor.hasNext() )
		{
			cursor.next();
			cursor.localize( position );
			ctr++;
		}

		intervalC.max( tmp );

		assertTrue( Arrays.equals( tmp, position ) );
		assertTrue( ctr == shiftedintervalsize );
	}

	@Test
	public void testJumpFwdFast()
	{
		Cursor< IntType > cursor = intImg.cursor( intervalA );

		long[] position = new long[ cursor.numDimensions() ];
		long[] ref = new long[ cursor.numDimensions() ];

		intervalA.min( ref );

		ref[ 0 ] += 17;
		cursor.jumpFwd( 18 );
		cursor.localize( position );

		assertTrue( Arrays.equals( ref, position ) );
	}

	@Test
	public void testJumpFwdShifted()
	{
		Cursor< IntType > cursor = intImg.cursor( intervalC );

		long[] position = new long[ cursor.numDimensions() ];
		long[] ref = new long[ cursor.numDimensions() ];

		intervalC.min( ref );

		ref[ 0 ] += 17;
		cursor.jumpFwd( 18 );
		cursor.localize( position );

		assertTrue( Arrays.equals( ref, position ) );
	}
}