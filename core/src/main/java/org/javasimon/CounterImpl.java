package org.javasimon;

import org.javasimon.utils.SimonUtils;

/**
 * Class implements {@link org.javasimon.Counter} interface - see there for how to use Counter.
 *
 * @author <a href="mailto:virgo47@gmail.com">Richard "Virgo" Richter</a>
 * @see org.javasimon.Counter
 */
final class CounterImpl extends AbstractSimon implements Counter {

	/** An internal counter. */
	private long counter;

	/** Sum of all increments. */
	private long incrementSum;

	/** Sum of all decrements. */
	private long decrementSum;

	/** A maximum tracker. */
	private long max = Long.MIN_VALUE;

	private long maxTimestamp;

	/** A minimum tracker - only negative values. */
	private long min = Long.MAX_VALUE;

	private long minTimestamp;

	/**
	 * Constructs Counter Simon with a specified name and for the specified manager.
	 *
	 * @param name Simon's name
	 * @param manager owning manager
	 */
	CounterImpl(String name, Manager manager) {
		super(name, manager);
	}

	@Override
	public Counter set(long val) {
		if (!enabled) {
			return this;
		}

		long now = System.currentTimeMillis();
		CounterSample sample;
		synchronized (this) {
			updateUsages(now);
			counter = val;
			updateMax();
			updateMin();
			sample = sampleIfCallbacksNotEmpty();

		}
		manager.callback().onCounterSet(this, val, sample);
		return this;
	}

	private void updateMin() {
		if (counter <= min) {
			min = counter;
			minTimestamp = getLastUsage();
		}
	}

	private void updateMax() {
		if (counter >= max) {
			max = counter;
			maxTimestamp = getLastUsage();
		}
	}

	@Override
	public Counter increase() {
		return increase(1);
	}

	@Override
	public Counter decrease() {
		return decrease(1);
	}

	@Override
	public Counter increase(long inc) {
		if (!enabled) {
			return this;
		}

		long now = System.currentTimeMillis();
		CounterSample sample;
		synchronized (this) {
			incrementSum += inc;
			updateUsages(now);
			counter += inc;
			if (inc > 0) {
				updateMax();
			} else {
				updateMin();
			}
			sample = sampleIfCallbacksNotEmpty();
		}
		manager.callback().onCounterIncrease(this, inc, sample);
		return this;
	}

	@Override
	public synchronized Counter decrease(long dec) {
		if (!enabled) {
			return this;
		}

		long now = System.currentTimeMillis();
		CounterSample sample;
		synchronized (this) {
			decrementSum += dec;
			updateUsages(now);
			counter -= dec;
			if (dec > 0) {
				updateMin();
			} else {
				updateMax();
			}
			sample = sampleIfCallbacksNotEmpty();
		}
		manager.callback().onCounterDecrease(this, dec, sample);
		return this;
	}

	private CounterSample sampleIfCallbacksNotEmpty() {
		if (!manager.callback().callbacks().isEmpty()) {
			return sample();
		}
		return null;
	}

	@Override
	public synchronized long getCounter() {
		return counter;
	}

	@Override
	public synchronized long getMin() {
		return min;
	}

	@Override
	public synchronized long getMinTimestamp() {
		return minTimestamp;
	}

	@Override
	public synchronized long getMax() {
		return max;
	}

	@Override
	public synchronized long getMaxTimestamp() {
		return maxTimestamp;
	}

	@Override
	public synchronized long getIncrementSum() {
		return incrementSum;
	}

	@Override
	public synchronized long getDecrementSum() {
		return decrementSum;
	}

	@Override
	@Deprecated
	void concreteReset() {
		counter = 0;
		max = Long.MIN_VALUE;
		maxTimestamp = 0;
		min = Long.MAX_VALUE;
		minTimestamp = 0;
		incrementSum = 0;
		decrementSum = 0;
	}

	@Override
	@Deprecated
	public synchronized CounterSample sampleAndReset() {
		CounterSample sample = sample();
		reset();
		return sample;
	}

	@Override
	public synchronized CounterSample sample() {
		CounterSample sample = new CounterSample();
		sample.setCounter(counter);
		sample.setMin(min);
		sample.setMax(max);
		sample.setMinTimestamp(minTimestamp);
		sample.setMaxTimestamp(maxTimestamp);
		sample.setIncrementSum(incrementSum);
		sample.setDecrementSum(decrementSum);
		sampleCommon(sample);
		return sample;
	}

	@Override
	public CounterSample sampleIncrement(Object key) {
		return (CounterSample) sampleIncrementHelper(key, new CounterImpl(null, null));
	}

	/**
	 * Returns Simon basic information, counter, max value and min value as a human readable string.
	 *
	 * @return basic information, counter, max and min values
	 * @see AbstractSimon#toString()
	 */
	@Override
	public synchronized String toString() {
		return "Simon Counter: counter=" + counter +
			", max=" + SimonUtils.presentMinMaxCount(max) +
			", min=" + SimonUtils.presentMinMaxCount(min) +
			super.toString();
	}
}
