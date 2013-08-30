package net.imglib2.ops.sandbox;

import java.util.Arrays;
import java.util.List;

/**
 * Another Axis experiment. Without generics. And support for nonlinear
 * scalings.
 * 
 * @author Barry DeZonia
 */
public class AxisIdeas {

	private interface Axis {

		/**
		 * Constant used for labeling unknown axes.
		 */
		public static final String UNKNOWN = "Unknown";

		/**
		 * Convert a point along the axis (in domain space like 0, 1, 2, etc.) into
		 * a calibrated value (in range space like 47.3, 98.32, etc.).
		 * 
		 * @param domainValue The value to convert to range space.
		 */
		double calibratedValue(double domainValue);

		/**
		 * Convert a point along the axis (in range space like 47.3, 98.32, etc.)
		 * into an uncalibrated value (in domain space like 0, 1, 2, etc.).
		 * 
		 * @param rangeValue The value to convert to domain space.
		 */
		double rawValue(double rangeValue);

		/**
		 * Returns the unit associated with the axis. Can be null for none.
		 */
		String unit();

		/**
		 * Sets the unit associated with the axis. Can be null for none.
		 */
		void setUnit(String unit);

		/**
		 * Gets the axis label. It is never null. The label is the primary key of an
		 * axis. Two axes are compatible if their label matches.
		 */
		String label();

		/**
		 * Sets the axis label. Null is an illegal input. The label is the primary
		 * key of an axis. Two axes are compatible if their label matches.
		 * 
		 * @param label
		 */
		void setLabel(String label);

		/**
		 * Checks if two axes are compatible (i.e. they have the same label). Label
		 * comparison is done in a case insensitive way. Note that axes labeled
		 * UNKNOWN are never compatible with another Axis.
		 */
		boolean isCompatibleWith(Axis other);

		// NB - notice that axes do not have an explicit type. This is very
		// flexible. One could have two time axes with different labels. We could
		// have an Axes static helper that has some common labels like "x", "y",
		// "z", "channel", "time", etc. Users can use the helper to reason about
		// types if needed. We also do not distinguish between space, time, and
		// other.
	}

	/*
	private enum VariableName {

		A("a"), B("b"), C("c"), D("d"), E("e"), F("f"), G("g"), H("h"), I("i"), J(
			"j"), K("k"), L("l"), M("m"), N("n"), O("o"), P("p"), Q("q"), R("r"), S(
			"s"), T("t"), U("u"), V("v"), W("w"), X("x"), Y("y"), Z("z");

		private String name;

		VariableName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	*/

	private static abstract class AbstractAxis implements Axis {

		private String unit = null;
		private String label = UNKNOWN;

		@Override
		public String unit() {
			return unit;
		}

		@Override
		public void setUnit(String unit) {
			this.unit = unit;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public void setLabel(String label) {
			if (label == null) {
				throw new IllegalArgumentException("label cannot be null");
			}
			this.label = label;
		}

		@Override
		public boolean isCompatibleWith(Axis a) {
			if (a == this) return true;
			if (label.equals(UNKNOWN)) return false;
			if (a.label().equals(UNKNOWN)) return false;
			return label.equalsIgnoreCase(a.label());
		}
	}

	private interface VariableAxis extends Axis {

		String name(); // "Linear mapping"

		String equation(); // "y = a + b * x"

		String calibratedEquation(); // "y = (0.4) + (-3.2) * x"

		List<String> variables(); // {"a", "b"}

		double getValue(String variable);

		void setValue(String variable, double value);
	}

	// Each variable axis stores some constants for computing calibrated values.
	// They are named with Strings. Any Axis class can have custom strings that
	// simplify the concepts within the equation. Like a LinearAxis can have
	// variables "slope" and "y intercept" perhaps.

	private static abstract class AbstractVariableAxis extends AbstractAxis
		implements VariableAxis
	{

		protected String[] variables;
		protected double[] values;

		public AbstractVariableAxis(int varCount) {
			super();
			variables = new String[varCount];
			values = new double[varCount];
		}

		@Override
		public List<String> variables() {
			return Arrays.asList(variables);
		}

		@Override
		public double getValue(String variable) {
			for (int i = 0; i < variables.length; i++) {
				if (variable.equals(variables[i])) return values[i];
			}
			throw new IllegalArgumentException("unknown variable " + variable);
		}

		@Override
		public void setValue(String variable, double value) {
			for (int i = 0; i < variables.length; i++) {
				if (variable.equals(variables[i])) {
					values[i] = value;
					return;
				}
			}
			throw new IllegalArgumentException("unknown variable " + variable);
		}
	}

	// NB - might not need this class

	private static class UncalibratedAxis extends AbstractVariableAxis {

		// -- constants --

		public static final String NAME = "Uncalibrated axis";

		// -- constructors --

		public UncalibratedAxis() {
			super(0);
		}

		@Override
		public double calibratedValue(double domainValue) {
			return domainValue;
		}

		@Override
		public double rawValue(double rangeValue) {
			return rangeValue;
		}

		@Override
		public String name() {
			return NAME;
		}

		@Override
		public String equation() {
			return "y = x";
		}

		@Override
		public String calibratedEquation() {
			return "y = x";
		}

	}

	private static class LinearAxis extends AbstractVariableAxis {

		// -- constants --

		public static final String NAME = "Linear axis";
		public static final String INTERCEPT = "intercept";
		public static final String SLOPE = "slope";

		// -- constructors --

		public LinearAxis() {
			this(0, 1);
		}

		public LinearAxis(double intercept, double slope) {
			super(2);
			variables[0] = INTERCEPT;
			variables[1] = SLOPE;
			values[0] = intercept;
			values[1] = slope;
		}

		public LinearAxis(double x1, double y1, double x2, double y2) {
			super(2);
			variables[0] = INTERCEPT;
			variables[1] = SLOPE;
			values[0] = (y1 + y2 + (((y1 - y2) * (x1 + x2)) / (x2 - x1))) / 2;
			values[1] = (y2 - y1) / (x2 - x1);
		}

		// -- Function methods --

		@Override
		public double calibratedValue(double domainValue) {
			return values[1] * domainValue + values[0];
		}

		@Override
		public double rawValue(double rangeValue) {
			return (rangeValue - values[0]) / values[1];
		}

		// -- VariableFunction methods --

		@Override
		public String name() {
			return NAME;
		}

		@Override
		public String equation() {
			return "y = a + b*x";
		}

		@Override
		public String calibratedEquation() {
			return "y = (" + values[0] + ") + (" + values[1] + ") * x";
		}

	}

	private static class LogAxis extends AbstractVariableAxis {

		// -- constants --

		public static final String NAME = "Log axis (loglinear?)";
		public static final String A = "a"; // TODO - name these better
		public static final String B = "b";
		public static final String C = "c";
		public static final String D = "d";

		// -- constructors --

		public LogAxis(double a, double b, double c, double d) {
			super(4);
			variables[0] = A;
			variables[1] = B;
			variables[2] = C;
			variables[3] = D;
			values[0] = a;
			values[1] = b;
			values[2] = c;
			values[3] = d;
		}

		public LogAxis() {
			this(0, 1, 1, 1);
		}

		// -- Axis methods --

		@Override
		public double calibratedValue(double domainValue) {
			return values[0] + values[1] *
				Math.log(values[2] * domainValue + values[3]);
		}

		@Override
		public double rawValue(double rangeValue) {
			return (Math.exp((rangeValue - values[0]) / values[1]) - values[3]) /
				values[2];
		}

		// -- VariableAxis methods --

		@Override
		public String name() {
			return NAME;
		}

		@Override
		public String equation() {
			return "y = a + b * ln(c * x + d)";
		}

		@Override
		public String calibratedEquation() {
			return "y = (" + values[0] + ") + (" + values[1] + ") * ln((" +
				values[2] + ") * x + (" + values[3] + "))";
		}

	}

	/*
	 * Exponential: y = a + b * exp(c * x + d);
	 * Power: y = a + b * pow(c * x + d);
	 * Gaussian: y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))
	 * others can be taken from IJ1's CurveFitter code
	 * Note that users can provide additional ones
	 */

	// NB - in the limit this is the derivative at the rawValue point

	public static double scaleOverInterval(Axis f, double rawValue1,
		double rawValue2)
	{
		return (f.calibratedValue(rawValue2) - f.calibratedValue(rawValue1)) /
			(rawValue2 - rawValue1);
	}

	public static void main(String[] args) {

		Axis xAxis = new LinearAxis();
		Axis yAxis = new LogAxis();

		// to get origin value (domain point 0,0)
		double originX = xAxis.calibratedValue(0);
		double originY = yAxis.calibratedValue(0);
		System.out.println("Origin " + originX + "," + originY);

		// to get value at domain point (4,9)
		double valX = xAxis.calibratedValue(4);
		double valY = yAxis.calibratedValue(9);
		System.out.println("Value(4,9) = " + valX + "," + valY);

		// get domain values from range point
		double rawX = xAxis.rawValue(97.4);
		double rawY = yAxis.rawValue(13.7);
		System.out.println("Raw(97.4,13.7) = " + rawX + "," + rawY);

		// units support
		String unit = xAxis.unit();
		if (unit == null) System.out.println("xAxis unit defaulted to null");
		yAxis.setUnit("meter");
		System.out.println("yAxis unit = " + yAxis.unit());

		// label support
		System.out.println("xAxis label = " + xAxis.label());
		xAxis.setLabel("V");
		yAxis.setLabel("v");

		// axis comparison
		if (xAxis.isCompatibleWith(yAxis)) {
			System.out.println("xAxis and yAxis are compatible");
		}

		// one way to interact with axes
		if (xAxis instanceof LinearAxis) { // test and cast not great
			LinearAxis axis = (LinearAxis) xAxis;
			axis.setValue(LinearAxis.SLOPE, 7.4);
			System.out.println("slope set to " + axis.getValue(LinearAxis.SLOPE));
		}

		// or can do this
		VariableAxis axis = new LinearAxis(5, -3.2);
		if (axis.name().equals(LinearAxis.NAME)) {
			// can cast
			// can know scale is constant
			// can tweak variables
			// etc.
			axis.setValue(LinearAxis.INTERCEPT, 9);
			System.out.println("intercept set to " +
				axis.getValue(LinearAxis.INTERCEPT));
		}
		System.out.println("axis eqn = " + axis.calibratedEquation());
		axis = new LogAxis(1, 2, 3, 4);
		if (axis.name().equals(LogAxis.NAME)) {
			axis.setValue(LogAxis.D, 405);
		}
		System.out.println("axis eqn = " + axis.calibratedEquation());

		System.out.println("Finished");

		/*
		 * The question is whether to use Axis or VariableAxis as the class we
		 * usually work with. VariableAxis is the most full featured.
		 */
	}
}
