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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A class for displaying exception information in a pop-up frame. As a convenience, an instance exists as a static
 * member of the <code>Grappa</code> class.
 *
 * @see Grappa#displayException(java.lang.Exception)
 * @see Grappa#displayException(java.lang.Exception,java.lang.String)
 * @version $Id$
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a href="http://www.research.att.com">Research @
 *         AT&T Labs</a>
 */
public class AwtExceptionDisplay
    implements ExceptionDisplay
{
    private String title = null;

    Exception exception = null;

    Display display = null;

    /**
     * Creates an instance of the class for displaying exceptions.
     *
     * @param title the title for the pop-up frame
     */
    public AwtExceptionDisplay(String title)
    {
        this.title = title;
    }

    /**
     * Pops up the frame and displays information on the supplied exception. Initially, a text area displays the message
     * associated with the exception. By pressing a button, an end-user can view a stack trace as well.
     *
     * @param ex the exception about which information is to be displayed.
     */
    public void displayException(Exception ex)
    {
        displayException(ex, null);
    }

    /**
     * Pops up the frame and displays information on the supplied exception. Initially, a text area displays the
     * supplied string followed on the next line by the message associated with the exception. By pressing a button, an
     * end-user can view a stack trace as well.
     *
     * @param ex the exception about which information is to be displayed.
     */
    public void displayException(Exception ex, String msg)
    {
        if (this.display == null) {
            this.display = new Display(this.title);
        }
        this.exception = ex;
        if (ex == null && msg == null) {
            return;
        }
        if (msg != null) {
            if (ex == null) {
                this.display.setText(msg);
            } else {
                this.display.setText(msg + GrappaConstants.NEW_LINE + ex.getMessage());
            }
        } else {
            this.display.setText(ex.getMessage());
        }
        this.display.setVisible(true);
    }

    // TODO: re-do this using JFrame (not a big deal)
    class Display extends Frame
    {
        private static final long serialVersionUID = 4155196593331479095L;

        private TextArea textarea = null;

        private Panel buttonPanel = null;

        private Button trace = null;

        private Button dismiss = null;

        private WindowObserver observer = null;

        Display(String title)
        {
            super(title);

            this.observer = new WindowObserver();

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            setLayout(gbl);

            this.textarea = new TextArea("", 7, 80);
            this.textarea.setEditable(false);

            this.buttonPanel = new Panel();
            this.buttonPanel.setLayout(new BorderLayout());

            this.trace = new Button("Stack Trace");
            this.trace.addActionListener(this.observer);
            this.dismiss = new Button("Dismiss");
            this.dismiss.addActionListener(this.observer);

            this.buttonPanel.add("West", this.trace);
            this.buttonPanel.add("East", this.dismiss);

            gbc.fill = GridBagConstraints.BOTH;
            gbl.setConstraints(this.textarea, gbc);
            add(this.textarea);
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbl.setConstraints(this.buttonPanel, gbc);
            add(this.buttonPanel);

            addWindowListener(this.observer);
            pack();
        }

        void setText(String text)
        {
            if (text == null) {
                text = "No message to display, try stack trace.";
            }
            this.textarea.setText(text);
        }

        Exception getException()
        {
            return AwtExceptionDisplay.this.exception;
        }

        class WindowObserver extends WindowAdapter implements ActionListener
        {

            @Override
            public void windowClosing(WindowEvent evt)
            {
                dismiss();
            }

            private void dismiss()
            {
                setVisible(false);
                dispose();
                AwtExceptionDisplay.this.display = null;
            }

            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Object src = evt.getSource();
                if (src instanceof Button) {
                    Button btn = (Button) src;
                    if (btn.getLabel().equals("Dismiss")) {
                        setVisible(false);
                    } else if (btn.getLabel().equals("Stack Trace")) {
                        if (getException() == null) {
                            setText("No stack trace available (exception is null).");
                        } else {
                            StringWriter swriter = new StringWriter();
                            PrintWriter pwriter = new PrintWriter(swriter);
                            getException().printStackTrace(pwriter);
                            pwriter.flush();
                            setText(swriter.toString());
                            pwriter.close();
                        }
                    }
                }
            }
        }
    }
}
