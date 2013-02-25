/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2013 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
 * Stephan Saalfeld, Albert Cardona, Curtis Rueden, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Lee Kamentsky, Larry Lindsey, Grant Harris,
 * Mark Hiner, Aivar Grislis, Martin Horn, Nick Perry, Michael Zinsmaier,
 * Steffen Jaensch, Jan Funke, Mark Longair, and Dimiter Prodanov.
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

package net.imglib2.histogram;

import net.imglib2.type.numeric.RealType;

/**
 * @author Barry DeZonia
 * @param <T>
 */
public class Real1dBinMapper<T extends RealType<T>> implements BinMapper<T> {

	// -- instance variables --

	private final long bins;
	private final long[] binDimensions;
	private final double minVal, maxVal;
	private final boolean tailBins;

	// -- constructor --

	public Real1dBinMapper(double minVal, double maxVal, long numBins,
		boolean tailBins)
	{
		this.bins = numBins;
		this.binDimensions = new long[] { numBins };
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.tailBins = tailBins;
		if (bins <= 0) {
			throw new IllegalArgumentException(
				"invalid RealBinMapper: nonpositive dimension");
		}
		if (minVal >= maxVal) {
			throw new IllegalArgumentException(
				"invalid RealBinMapper: nonpositive bin width");
		}
	}

	// -- BinMapper methods --

	@Override
	public int numDimensions() {
		return binDimensions.length;
	}
	
	@Override
	public void getBinDimensions(long[] dims) {
		for (int i = 0; i < binDimensions.length; i++)
			dims[i] = binDimensions[i];
	}

	@Override
	public void getBinPosition(T value, long[] binPos) {
		long pos;
		double val = value.getRealDouble();
		if (val < minVal) pos = 0;
		else if (val > maxVal) pos = bins - 1;
		else {
			double relPos = (val - minVal) / (maxVal - minVal);
			if (tailBins) {
				pos = 1 + Math.round(relPos * (bins - 2));
			}
			else {
				pos = Math.round(relPos * bins);
			}
		}
		binPos[0] = pos;
	}

	@Override
	public void getCenterValue(long[] binPos, T value) {
		value.setReal(center(binPos[0]));
	}

	@Override
	public void getMinValue(long[] binPos, T value) {
		value.setReal(min(binPos[0]));
	}

	@Override
	public void getMaxValue(long[] binPos, T value) {
		value.setReal(max(binPos[0]));
	}

	@Override
	public boolean includesMinValue(long[] binPos) {
		if (tailBins && binPos[0] == bins - 1) return false;
		return true;
	}

	@Override
	public boolean includesMaxValue(long[] binPos) {
		if (tailBins) {
			if (binPos[0] >= bins - 2) return true;
		}
		else { // no tail bins
			if (binPos[0] == bins - 1) return true;
		}
		return false;
	}

	// -- helpers --

	private double min(long pos) {
		if (pos < 0 || pos > bins - 1) {
			throw new IllegalArgumentException("invalid bin position specified");
		}
		if (tailBins) {
			if (pos == 0) return Double.NEGATIVE_INFINITY;
			if (pos == bins - 1) return maxVal;
			return minVal + (1.0 * (pos - 1) / (bins - 2)) * (maxVal - minVal);
		}
		return minVal + (1.0 * pos / (bins)) * (maxVal - minVal);
	}

	private double max(long pos) {
		if (pos < 0 || pos > bins - 1) {
			throw new IllegalArgumentException("invalid bin position specified");
		}
		if (tailBins) {
			if (pos == 0) return minVal;
			if (pos == bins - 1) return Double.POSITIVE_INFINITY;
			return minVal + (1.0 * pos / (bins - 2)) * (maxVal - minVal);
		}
		return minVal + (1.0 * (pos + 1) / (bins)) * (maxVal - minVal);
	}
	
	private double center(long pos) {
		return (min(pos) + max(pos)) / 2;
	}

}
