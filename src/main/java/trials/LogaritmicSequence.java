package trials;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is used to generate a logaritmic sequence.
 * The iterator it returns doesn't support remove operation.
 * @author sergio
 */
public class LogaritmicSequence implements Iterable<Long> {

	private long start;
	private long end;
	private int module;

	/**
	 * Construct a logarithmic sequence beginning from start and ending in end
	 * with 9 steps (10 -> 20...90 -> 100).
	 *
	 * @see #LogaritmicSequence(long, long, int)
	 */
	public LogaritmicSequence(long start, long end) {

		this(start, end, 9);
	}

	/**
	 * Construct a logarithmic sequence beginning from start and ending in end
	 * inclusive, using the given number of steps from one order of magnitude
	 * (OoM) to the other.
	 * E.g.
	 * <pre>
	 * 10 -> 100              is 1 step
	 * 10 -> 20 -> 100       are 2 steps
	 * 10 -> 20 -> 30 -> 100 are 3 steps
	 * </pre>
	 * Sequence is chosen incrementally because its distance on the log-scale
	 * and the usefulness of having integer numbers.
	 *
	 * @param start sequence start number
	 * @param end   sequence end number
	 * @param steps the number of steps from one OoM to the next [1..9].
	 * @throws IllegalArgumentException if start isn't positive or if it is
	 *	       greater than end.
	 */
	public LogaritmicSequence(long start, long end, int steps) {

		if (start <= 0) {

			throw new IllegalArgumentException("sequence must start with a "
											+ "positive number: start=" + start);
		} else if (start > end) {

			throw new IllegalArgumentException("start can't be greater than end:"
											+ " start=" + start + ", end=" + end);
		}

		if (steps < 1 || steps > 9) {

			throw new IllegalArgumentException("steps must be in range [1..9]: " + steps);
		}

		this.start = start;
		this.end = end;
		this.module = steps;
	}

	@Override
	public Iterator<Long> iterator() {

		return new LogIter();
	}

	private class LogIter implements Iterator<Long> {

		private long lastRet = -1L;		// last value returned.
		private long current = start;	// next value to return.

		private int unit;				// from 1 to 9
		private long step;				// to add to current value.

		public LogIter() {

			long tmp = start;
			step = 1L;

			while ((tmp / 10) != 0) {

				tmp /= 10;
				step *= 10L;
			}

			unit = (int) tmp;
		}

		@Override
		public boolean hasNext() {

			return lastRet < end;
		}

		@Override
		public Long next() {

			if (current <= end) {

				lastRet = current;
				unit = (unit % module) + 1;

				if (unit == 1) {

					step *= 10L;
				}

				current = unit * step;

			} else if (hasNext()) { // returns "end" if not already done.

				lastRet = end;

			} else {
				throw new NoSuchElementException();
			}

			return lastRet;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("No mutable sequence");
		}
	}
}
