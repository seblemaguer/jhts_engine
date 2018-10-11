package jhts_engine;


/**
 *  The dedicate HTS engine exception class
 *
 * @author <a href="mailto:slemaguer@tcd.ie">Sébastien Le Maguer</a>
 */
public class HTSEngineException extends Exception {

    /** Serial ID needed for serializable classes */
	private static final long serialVersionUID = -7439529628623918148L;

	/**
     *  Constructor with message
     *
     *  @param message the message describing the reason of the exception
     */
    public HTSEngineException(String message) {
        super(message);
    }
}
