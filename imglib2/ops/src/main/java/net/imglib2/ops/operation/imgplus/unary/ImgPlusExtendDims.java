/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2012 Stephan Preibisch, Stephan Saalfeld, Tobias
 * Pietzsch, Albert Cardona, Barry DeZonia, Curtis Rueden, Lee Kamentsky, Larry
 * Lindsey, Johannes Schindelin, Christian Dietz, Grant Harris, Jean-Yves
 * Tinevez, Steffen Jaensch, Mark Longair, Nick Perry, and Jan Funke.
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package net.imglib2.ops.operation.imgplus.unary;

import java.util.Arrays;
import java.util.BitSet;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.Type;

/**
 * Extends the image by new dimensions, if they don't exist yet.
 * 
 * @author Martin Horn (University of Konstanz)
 */
public class ImgPlusExtendDims< T extends Type< T >> implements UnaryOutputOperation< ImgPlus< T >, ImgPlus< T >>
{

	private final String[] m_newDimensions;

	BitSet m_isNewDim;

	/**
	 * @param newDimensions
	 * @param fillDimension
	 *            if true, the newly added dimensions will be filled with a copy
	 *            of the existing ones
	 */
	public ImgPlusExtendDims( String... newDimensions )
	{
		m_newDimensions = newDimensions;
		m_isNewDim = new BitSet( newDimensions.length );

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UnaryObjectFactory< ImgPlus< T >, ImgPlus< T >> bufferFactory()
	{

		return new UnaryObjectFactory< ImgPlus< T >, ImgPlus< T >>()
		{

			@Override
			public ImgPlus< T > instantiate( ImgPlus< T > a )
			{
				AxisType[] axes = new AxisType[ a.numDimensions() ];
				a.axes( axes );
				m_isNewDim.clear();
				for ( int d = 0; d < m_newDimensions.length; d++ )
				{
					for ( int i = 0; i < axes.length; i++ )
					{
						if ( !axes[ i ].getLabel().equals( m_newDimensions[ d ] ) )
						{
							m_isNewDim.set( d );
						}
					}
				}
				long[] newDims = new long[ a.numDimensions() + m_isNewDim.cardinality() ];
				Arrays.fill( newDims, 1 );
				for ( int i = 0; i < a.numDimensions(); i++ )
				{
					newDims[ i ] = a.dimension( i );
				}
				return new ImgPlus< T >( a.factory().create( newDims, a.firstElement().createVariable() ) );
			}
		};

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImgPlus< T > compute( ImgPlus< T > op, ImgPlus< T > r )
	{
		Cursor< T > srcCur = op.localizingCursor();
		RandomAccess< T > resRA = r.randomAccess();

		// TODO: Copy metadata!
		r.setName( op.getName() );

		for ( int d = 0; d < op.numDimensions(); d++ )
		{
			r.setAxis( Axes.get( op.axis( d ).getLabel() ), d );
		}

		int d = op.numDimensions();
		for ( int i = 0; i < m_newDimensions.length; i++ )
		{
			if ( m_isNewDim.get( i ) )
			{
				r.setAxis( Axes.get( m_newDimensions[ i ] ), d );
				d++;
			}
		}

		while ( srcCur.hasNext() )
		{
			srcCur.fwd();
			for ( int i = 0; i < op.numDimensions(); i++ )
			{
				resRA.setPosition( srcCur.getLongPosition( i ), i );

			}
			resRA.get().set( srcCur.get() );
		}

		return r;

	}

	@Override
	public UnaryOutputOperation< ImgPlus< T >, ImgPlus< T >> copy()
	{
		return new ImgPlusExtendDims< T >( m_newDimensions );
	}
}
