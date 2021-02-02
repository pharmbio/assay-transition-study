package utils;
public class Stopwatch {
	private long start;
	private long stop;

	public void start() {
		start = System.currentTimeMillis(); // start timing
	}

	public void stop() {
		stop = System.currentTimeMillis(); // stop timing
	}

	public long elapsedTimeMillis() {
		return stop - start;
	}

	//return nice string
	public String toString() {
		long elapsed = elapsedTimeMillis();
		int millis = (int) (elapsed % 1000);
		int seconds = (int) ((elapsed / 1000) % 60);
		int minutes = (int) ((elapsed / 1000) / 60);
		if (minutes == 1)
			return "1 min and " + seconds + " s";
		else if (minutes>0)
			return "" + minutes + " mins and " + seconds + " s";
		else if (seconds>0)
			return "" + seconds + '.'+millis+" s";
		else
			return "" + millis + " ms";
	}

}