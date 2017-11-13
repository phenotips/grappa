/*
 *  This software may only be used by you under license from AT&T Corp.
 *  ("AT&T").  A copy of AT&T's Source Code Agreement is available at
 *  AT&T's Internet website having the URL:
 *  <http://www.research.att.com/sw/tools/graphviz/license/source.html>
 *  If you received this software without first entering into a license
 *  with AT&T, you have an infringing copy of this software and cannot use
 *  it without violating AT&T's intellectual property rights.
 */
package att.grappa;

/**
 * Implement this interface to register your special exception-displaying solution.
 *
 * @see Grappa#setExceptionDisplay(ExceptionDisplay)xceptionDisplay
 */
public interface ExceptionDisplay
{
    /**
     * Called when an exception occurred, that should be displayed.
     *
     * @param ex the exception about which information is to be displayed.
     */
    void displayException(Exception ex);

    /**
     * Called when an exception occurred, that should be displayed.
     *
     * @param ex the exception about which information is to be displayed.
     * @param msg additional information
     */
    void displayException(Exception ex, String msg);
}
