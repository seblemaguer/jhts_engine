package jhts_engine;

/**
 *  Class to represent a label data structure (label, start position, duration)
 *
 *  @author <a href="mailto:slemaguer@tcd.ie">Sébastien Le Maguer</a>
 */
public class FilledLabel
{
    /** Helper constant to make the conversion between milliseconds and HTK unit */
    public static final double MS_TO_HTK = 1E4;

    /** The label of the segment*/
    private String label;

    /** Start position of the segment */
    private long start;

    /** Duration of the segment */
    private long duration;

    /**
     *  Constructor of a segment label
     *
     *  @param label the label of the segment
     *  @param start the start position of the segment
     *  @param duration the duration of the segment
     */
    public FilledLabel(String label, long start, long duration) {
        this.start = start;
        this.duration = duration;
        this.label = label;
    }

    /**
     *  Method to get the label of the segment
     *
     *  @return the label
     */
    public String getLabel() {
        return label;
    }


    /**
     *  Method to get the start position of the segment
     *
     *  @return the start position
     */
    public long getStart() {
        return start;
    }

    /**
     *  Method to get the duration of the segment
     *
     *  @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     *  Method to set the label of the segment
     *
     *  @param label the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *  Method to set the start position of the segment
     *
     *  @param start the new start position
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     *  Method to set the duration of the segment
     *
     *  @param duration the new duration
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     *  Convert the filled label into a standard HTK label line
     */
    @Override
    public String toString() {
        return String.format("%d %d %s",
                             getStart(),
                             getStart() + getDuration(),
                             getLabel());
    }
}
