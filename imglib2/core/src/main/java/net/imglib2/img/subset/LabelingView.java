/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   29 Apr 2011 (hornm): created
 */
package net.imglib2.img.subset;

import java.util.Collection;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labeling.DefaultROIStrategy;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingFactory;
import net.imglib2.labeling.LabelingROIStrategy;
import net.imglib2.labeling.LabelingType;
import net.imglib2.roi.IterableRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * Helper class to create a sub image.
 * 
 * @author dietzc, University of Konstanz
 */
public class LabelingView< L extends Comparable< L >> extends IterableRandomAccessibleInterval< LabelingType< L >> implements Labeling< L >
{

	protected LabelingROIStrategy< L, ? extends Labeling< L >> m_strategy;

	private final LabelingFactory< L > m_fac;

	private final IterableInterval< LabelingType< L >> m_ii;

	/**
	 * TODO: No metadata is saved here..
	 * 
	 * @see SubImg
	 * 
	 */
	public LabelingView( RandomAccessibleInterval< LabelingType< L >> in, LabelingFactory< L > fac )
	{
		super( in );
		m_fac = fac;
		m_strategy = new DefaultROIStrategy< L, Labeling< L >>( this );
		m_ii = Views.iterable( in );
	}

	@Override
	public boolean getExtents( L label, long[] minExtents, long[] maxExtents )
	{
		return m_strategy.getExtents( label, minExtents, maxExtents );
	}

	@Override
	public boolean getRasterStart( L label, long[] start )
	{
		return m_strategy.getRasterStart( label, start );
	}

	@Override
	public long getArea( L label )
	{
		return m_strategy.getArea( label );
	}

	@Override
	public Collection< L > getLabels()
	{
		return m_strategy.getLabels();
	}

	@Override
	public Cursor< LabelingType< L >> cursor()
	{
		return m_ii.cursor();
	}

	@Override
	public Cursor< LabelingType< L >> localizingCursor()
	{
		return m_ii.localizingCursor();
	}

	@Override
	public RegionOfInterest getRegionOfInterest( L label )
	{
		return m_strategy.createRegionOfInterest( label );
	}

	@Override
	public IterableRegionOfInterest getIterableRegionOfInterest( L label )
	{
		return m_strategy.createIterableRegionOfInterest( label );
	}

	@Override
	public Labeling< L > copy()
	{
		throw new UnsupportedOperationException( "TODO" );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public < LL extends Comparable< LL >> LabelingFactory< LL > factory()
	{
		return ( LabelingFactory< LL > ) m_fac;
	}
}