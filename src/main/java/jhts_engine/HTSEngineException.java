package jhts_engine;


/**
 *  The dedicate HTS engine exception class
 *
 * @author <a href="mailto:slemaguer@tcd.ie">Sébastien Le Maguer</a>
 */
public class HTSEngineException extends Exception {

    /**
     *  Constructor with message
     *
     *  @param message the message describing the reason of the exception
     */
    public HTSEngineException(String message) {
        super(message);
    }
}
