package att.grappa;

/**
 * Implement this interface to register your special exception-displaying
 * solution. See Grappa.setExceptionDisplay()
 */
public interface ExceptionDisplay
{
    /**
     * Called when an exception occurred, that should be displayed.
     *
     * @param ex The exception about which information is to be displayed.
     */
    void displayException(Exception ex);

    /**
     * Called when an exception occurred, that should be displayed.
     *
     * @param ex The exception about which information is to be displayed.
     * @param msg Additional information.
     */
    void displayException(Exception ex, String msg);
}
