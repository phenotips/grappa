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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * A class used for drawing the graph.
 *
 * @version 1.2, 04 Mar 2008; Copyright 1996 - 2008 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a href="http://www.research.att.com">Research @
 *         AT&T Labs</a>
 */
public class GrappaPanel extends javax.swing.JPanel implements att.grappa.GrappaConstants, ComponentListener,
    AncestorListener, PopupMenuListener, MouseListener, MouseMotionListener, Printable, Runnable, Scrollable
{
    private static final long serialVersionUID = 2528562615866907501L;

    Graph graph;

    Subgraph subgraph;

    GrappaBacker backer;

    boolean nodeLabels, edgeLabels, subgLabels;

    AffineTransform transform = null;

    AffineTransform oldTransform = null;

    AffineTransform inverseTransform = null;

    Vector<Element> elementVector = null;

    int nextElement = -1;

    boolean scaleToFit = false;

    GrappaSize scaleToSize = null;

    GrappaListener grappaListener = null;

    private Element pressedElement = null;

    private GrappaPoint pressedPoint = null;

    private int pressedModifiers = 0;

    private GrappaStyle selectionStyle = null;

    private GrappaStyle deletionStyle = null;

    private double scaleFactor = 1;

    private double scaleInfo = 1;

    private GrappaBox outline = null;

    private GrappaBox savedOutline = null;

    private GrappaBox zoomBox = null;

    private boolean inMenu = false;

    private boolean scaleChanged = false;

    private Dimension prevsz = null;

    private Point2D panelcpt = null;

    /**
     * Constructs a new canvas associated with a particular subgraph. Keep in mind that Graph is a sub-class of Subgraph
     * so that usually a Graph object is passed to the constructor.
     *
     * @param subgraph the subgraph to be rendered on the canvas
     */
    public GrappaPanel(Subgraph subgraph)
    {
        this(subgraph, null);
    }

    /**
     * Constructs a new canvas associated with a particular subgraph.
     *
     * @param subgraph the subgraph to be rendered on the canvas.
     * @param backer used to draw a background for the graph.
     */
    public GrappaPanel(Subgraph subgraph, GrappaBacker backer)
    {
        super();
        this.subgraph = subgraph;
        this.backer = backer;
        this.graph = subgraph.getGraph();

        addAncestorListener(this);
        addComponentListener(this);

        this.selectionStyle = (GrappaStyle) (this.graph.getGrappaAttributeValue(GRAPPA_SELECTION_STYLE_ATTR));
        this.deletionStyle = (GrappaStyle) (this.graph.getGrappaAttributeValue(GRAPPA_DELETION_STYLE_ATTR));
    }

    /**
     * Adds the specified listener to receive mouse events from this graph.
     *
     * @param listener the event listener.
     * @return the previous event listener.
     * @see GrappaAdapter
     */
    public GrappaListener addGrappaListener(GrappaListener listener)
    {
        GrappaListener oldGL = this.grappaListener;
        this.grappaListener = listener;
        if (this.grappaListener == null) {
            if (oldGL != null) {
                removeMouseListener(this);
                removeMouseMotionListener(this);
            }
            setToolTipText(null);
        } else {
            if (oldGL == null) {
                addMouseListener(this);
                addMouseMotionListener(this);
            }
            String tip = this.graph.getToolTipText();
            if (tip == null) {
                tip = Grappa.getToolTipText();
            }
            setToolTipText(tip);
        }
        return (oldGL);
    }

    /**
     * Removes the current listener from this graph. Equivalent to <TT>addGrappaListener(null)</TT>.
     *
     * @return the event listener just removed.
     */
    public GrappaListener removeGrappaListener()
    {
        return (addGrappaListener(null));
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pi)
        throws PrinterException
    {
        GrappaSize prevToSize = this.scaleToSize;
        boolean prevToFit = this.scaleToFit;

        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        try {
            this.scaleToFit = false;
            this.scaleToSize = new GrappaSize(pf.getImageableWidth(), pf.getImageableHeight());
            ((Graphics2D) g).translate(pf.getImageableX(), pf.getImageableY());
            paintComponent(g);
        } finally {
            this.scaleToSize = prevToSize;
            this.scaleToFit = prevToFit;
        }
        return Printable.PAGE_EXISTS;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        Point2D cpt = null;

        if (Grappa.synchronizePaint || this.graph.getSynchronizePaint()) {
            if (this.graph.setPaint(true)) {
                cpt = componentPaint(g);
                this.graph.setPaint(false);
            }
        } else {
            cpt = componentPaint(g);
        }

        if (cpt != null) {
            setCPT(cpt);
            EventQueue.invokeLater(this);
        }
    }

    void setCPT(Point2D cpt)
    {
        this.panelcpt = cpt;
    }

    Point2D getCPT()
    {
        return (this.panelcpt);
    }

    private Point2D componentPaint(Graphics g)
    {
        if (this.subgraph == null || !this.subgraph.reserve()) {
            return (null);
        }
        Graphics2D g2d = (Graphics2D) g;
        Container prnt;
        Container tprnt;
        Dimension nsz;

        Point2D cpt = null;

        // Color origBackground = g2d.getBackground();
        // //Composite origComposite = g2d.getComposite();
        // Paint origPaint = g2d.getPaint();
        // RenderingHints origRenderingHints = g2d.getRenderingHints();
        // Stroke origStroke = g2d.getStroke();
        // AffineTransform origAffineTransform = g2d.getTransform();
        // Font origFont = g2d.getFont();

        this.elementVector = null;

        GrappaBox bbox = new GrappaBox(this.subgraph.getBoundingBox());

        GrappaSize margins = (GrappaSize) (this.subgraph.getAttributeValue(MARGIN_ATTR));

        if (margins != null) {
            double x_margin = PointsPerInch * margins.width;
            double y_margin = PointsPerInch * margins.height;

            bbox.x -= x_margin;
            bbox.y -= y_margin;
            bbox.width += 2.0 * x_margin;
            bbox.height += 2.0 * y_margin;
        }

        this.subgLabels = this.subgraph.getShowSubgraphLabels();
        this.nodeLabels = this.subgraph.getShowNodeLabels();
        this.edgeLabels = this.subgraph.getShowEdgeLabels();
        if (Grappa.useAntiAliasing) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        if (Grappa.antiAliasText) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        if (Grappa.useFractionalMetrics) {
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        g2d.setStroke(GrappaStyle.defaultStroke);

        this.oldTransform = this.transform;
        this.transform = new AffineTransform();
        if (this.scaleToFit || this.scaleToSize != null) {
            this.scaleFactor = 1;
            this.zoomBox = null;
            double scaleToWidth = 0;
            double scaleToHeight = 0;
            if (this.scaleToFit) {
                tprnt = prnt = getParent();
                while (tprnt != null && !(tprnt instanceof javax.swing.JViewport)) {
                    tprnt = tprnt.getParent();
                }
                if (tprnt != null) {
                    prnt = tprnt;
                }
                if (prnt instanceof javax.swing.JViewport) {
                    Dimension sz = ((javax.swing.JViewport) prnt).getSize();
                    scaleToWidth = sz.width;
                    scaleToHeight = sz.height;
                } else {
                    Rectangle scaleTo = getVisibleRect();
                    scaleToWidth = scaleTo.width;
                    scaleToHeight = scaleTo.height;
                }
            } else {
                scaleToWidth = this.scaleToSize.width;
                scaleToHeight = this.scaleToSize.height;
            }
            double widthRatio = scaleToWidth / bbox.getWidth();
            double heightRatio = scaleToHeight / bbox.getHeight();
            double xTranslate = 0;
            double yTranslate = 0;
            if (widthRatio < heightRatio) {
                xTranslate = (scaleToWidth - widthRatio * bbox.getWidth()) / (2.0 * widthRatio);
                yTranslate = (scaleToHeight - widthRatio * bbox.getHeight()) / (2.0 * widthRatio);
                this.transform.scale(widthRatio, widthRatio);
                this.scaleInfo = widthRatio;
            } else {
                xTranslate = (scaleToWidth - heightRatio * bbox.getWidth()) / (2.0 * heightRatio);
                yTranslate = (scaleToHeight - heightRatio * bbox.getHeight()) / (2.0 * heightRatio);
                this.transform.scale(heightRatio, heightRatio);
                this.scaleInfo = heightRatio;
            }
            this.transform.translate(xTranslate, yTranslate);
            nsz = new Dimension((int) Math.ceil(scaleToWidth), (int) Math.ceil(scaleToHeight));
            if (this.prevsz == null || this.prevsz.getWidth() != nsz.getWidth()
                || this.prevsz.getHeight() != nsz.getHeight()) {
                setSize(nsz);
                setPreferredSize(nsz);
                this.prevsz = new Dimension(nsz.width, nsz.height);
            }
            this.transform.translate(-bbox.getMinX(), -bbox.getMinY());
            this.scaleFactor = this.scaleInfo;
        } else if (this.zoomBox != null) {
            // System.err.println("zoombox");
            Rectangle r;
            tprnt = prnt = getParent();
            while (tprnt != null && !(tprnt instanceof javax.swing.JViewport)) {
                tprnt = tprnt.getParent();
            }
            if (tprnt != null) {
                prnt = tprnt;
            }
            if (prnt instanceof javax.swing.JViewport) {
                Dimension sz = ((javax.swing.JViewport) prnt).getSize();
                r = new Rectangle(0, 0, sz.width, sz.height);
            } else {
                r = getVisibleRect();
            }
            this.scaleFactor = 1;
            if (this.zoomBox.width != 0 && this.zoomBox.height != 0 && this.oldTransform != null) {
                double scaleToWidth = r.width;
                double scaleToHeight = r.height;
                // System.err.println("zb=("+zb.x+","+zb.y+","+zb.width+","+zb.height+")");
                // System.err.println("zB=("+zoomBox.x+","+zoomBox.y+","+zoomBox.width+","+zoomBox.height+")");
                // System.err.println("vr=("+r.x+","+r.y+","+r.width+","+r.height+")");
                double widthRatio = scaleToWidth / this.zoomBox.width;
                double heightRatio = scaleToHeight / this.zoomBox.height;
                if (widthRatio < heightRatio) {
                    this.scaleFactor = widthRatio;
                } else {
                    this.scaleFactor = heightRatio;
                }
                this.transform.scale(this.scaleFactor, this.scaleFactor);
                this.scaleInfo = this.scaleFactor;
                // transform.translate(xTranslate, yTranslate);
                scaleToWidth = bbox.getWidth() * this.scaleFactor;
                scaleToHeight = bbox.getHeight() * this.scaleFactor;
                nsz = new Dimension((int) Math.ceil(scaleToWidth), (int) Math.ceil(scaleToHeight));
                if (this.prevsz == null || this.prevsz.getWidth() != nsz.getWidth()
                    || this.prevsz.getHeight() != nsz.getHeight()) {
                    setSize(nsz);
                    setPreferredSize(nsz);
                    this.prevsz = new Dimension(nsz.width, nsz.height);
                }
                this.transform.translate(-bbox.getMinX(), -bbox.getMinY());
                cpt = new Point2D.Double(this.zoomBox.getCenterX(), this.zoomBox.getCenterY());
            }
            this.zoomBox = null;
            // System.err.println("scaleFactor="+scaleFactor);
            // transform.scale(scaleFactor, scaleFactor);
            // int w = (int)Math.ceil(bbox.getWidth()*scaleFactor);
            // int h = (int)Math.ceil(bbox.getHeight()*scaleFactor);
            // w = w < r.width ? r.width : w;
            // h = h < r.height ? r.height : h;
            // setSize(new Dimension(w,h));
            // setPreferredSize(new Dimension(w,h));
            this.scaleFactor = this.scaleInfo;
        } else if (this.scaleFactor != 1) {
            Rectangle r = getVisibleRect();

            cpt = null;
            prnt = null;

            if (this.scaleChanged) {
                tprnt = prnt = getParent();
                while (tprnt != null && !(tprnt instanceof javax.swing.JViewport)) {
                    tprnt = tprnt.getParent();
                }
                if (tprnt != null) {
                    prnt = tprnt;
                }
                if (prnt instanceof javax.swing.JViewport && this.inverseTransform != null) {
                    Point2D pt = new Point2D.Double(r.x, r.y);
                    cpt = new Point2D.Double(r.x + r.width, r.y + r.height);
                    this.inverseTransform.transform(pt, pt);
                    this.inverseTransform.transform(cpt, cpt);
                    cpt.setLocation(
                        pt.getX() + (cpt.getX() - pt.getX()) / 2.,
                        pt.getY() + (cpt.getY() - pt.getY()) / 2.
                        );
                } else {
                    // to save checking again below
                    prnt = null;
                }
            }

            this.transform.scale(this.scaleFactor, this.scaleFactor);
            this.scaleInfo = this.scaleFactor;
            int w = (int) Math.ceil(bbox.getWidth() * this.scaleFactor);
            int h = (int) Math.ceil(bbox.getHeight() * this.scaleFactor);
            w = w < r.width ? r.width : w;
            h = h < r.height ? r.height : h;
            nsz = new Dimension(w, h);
            if (this.prevsz == null || this.prevsz.getWidth() != nsz.getWidth()
                || this.prevsz.getHeight() != nsz.getHeight()) {
                setSize(nsz);
                setPreferredSize(nsz);
                this.prevsz = new Dimension(nsz.width, nsz.height);
            }
            this.transform.translate(-bbox.getMinX(), -bbox.getMinY());
        } else {
            cpt = null;
            prnt = null;

            if (this.scaleChanged) {
                tprnt = prnt = getParent();
                while (tprnt != null && !(tprnt instanceof javax.swing.JViewport)) {
                    tprnt = tprnt.getParent();
                }
                if (tprnt != null) {
                    prnt = tprnt;
                }
                if (prnt instanceof javax.swing.JViewport && this.inverseTransform != null) {
                    Rectangle r = getVisibleRect();
                    Point2D pt = new Point2D.Double(r.x, r.y);
                    cpt = new Point2D.Double(r.x + r.width, r.y + r.height);
                    this.inverseTransform.transform(pt, pt);
                    this.inverseTransform.transform(cpt, cpt);
                    cpt.setLocation(
                        pt.getX() + (cpt.getX() - pt.getX()) / 2.,
                        pt.getY() + (cpt.getY() - pt.getY()) / 2.
                        );
                } else {
                    // to save checking again below
                    prnt = null;
                }
            }
            this.scaleInfo = 1;
            nsz = new Dimension((int) Math.ceil(bbox.getWidth()), (int) Math.ceil(bbox.getHeight()));
            if (this.prevsz == null || this.prevsz.getWidth() != nsz.getWidth()
                || this.prevsz.getHeight() != nsz.getHeight()) {
                setSize(nsz);
                setPreferredSize(nsz);
                this.prevsz = new Dimension(nsz.width, nsz.height);
            }
            this.transform.translate(-bbox.getMinX(), -bbox.getMinY());

        }

        this.scaleChanged = false;

        if (this.scaleInfo < Grappa.nodeLabelsScaleCutoff) {
            this.nodeLabels = false;
        }

        if (this.scaleInfo < Grappa.edgeLabelsScaleCutoff) {
            this.edgeLabels = false;
        }

        if (this.scaleInfo < Grappa.subgLabelsScaleCutoff) {
            this.subgLabels = false;
        }

        try {
            this.inverseTransform = this.transform.createInverse();
        } catch (NoninvertibleTransformException nite) {
            this.inverseTransform = null;
        }
        g2d.transform(this.transform);

        Rectangle clip = g2d.getClipBounds();
        // grow bounds to account for Java's frugal definition of what
        // constitutes the intersectable area of a shape
        clip.x--;
        clip.y--;
        clip.width += 2;
        clip.height += 2;

        synchronized (this.graph) {

            GrappaNexus grappaNexus = this.subgraph.grappaNexus;

            if (grappaNexus != null) {

                Color bkgdColor = null;

                // do fill now in case there is a Backer supplied
                g2d.setPaint(bkgdColor = (Color) (this.graph.getGrappaAttributeValue(GRAPPA_BACKGROUND_COLOR_ATTR)));
                g2d.fill(clip);
                if (grappaNexus.style.filled || grappaNexus.image != null) {
                    if (grappaNexus.style.filled) {
                        if (grappaNexus.fillcolor != null) {
                            g2d.setPaint(bkgdColor = grappaNexus.fillcolor);
                            grappaNexus.fill(g2d);
                            if (grappaNexus.color != null) {
                                g2d.setPaint(grappaNexus.color);
                            } else {
                                g2d.setPaint(grappaNexus.style.line_color);
                            }
                        } else {
                            g2d.setPaint(bkgdColor = grappaNexus.color);
                            grappaNexus.fill(g2d);
                            g2d.setPaint(grappaNexus.style.line_color);
                        }
                    }
                    grappaNexus.drawImage(g2d);
                    // for the main graph, only outline when filling/imaging
                    if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                        g2d.setStroke(grappaNexus.style.stroke);
                        grappaNexus.draw(g2d);
                        g2d.setStroke(GrappaStyle.defaultStroke);
                    } else {
                        grappaNexus.draw(g2d);
                    }
                }

                if (this.backer != null && Grappa.backgroundDrawing) {
                    this.backer.drawBackground(g2d, this.graph, bbox, clip);
                }

                paintSubgraph(g2d, this.subgraph, clip, bkgdColor);

            }

        }

        // g2d.setBackground(origBackground);
        // //g2d.setComposite(origComposite);
        // g2d.setPaint(origPaint);
        // g2d.setRenderingHints(origRenderingHints);
        // g2d.setStroke(origStroke);
        // g2d.setTransform(origAffineTransform);
        // g2d.setFont(origFont);

        this.subgraph.release();

        return (cpt);
    }

    /**
     * Centers the panel at the supplied point.
     *
     * @param cpt requested center point
     */
    public void centerPanelAtPoint(Point2D cpt)
    {
        Container prnt, tprnt;
        javax.swing.JViewport viewport;

        // System.err.println("cpt="+cpt);

        tprnt = prnt = getParent();
        while (tprnt != null && !(tprnt instanceof javax.swing.JViewport)) {
            tprnt = tprnt.getParent();
        }
        if (tprnt != null) {
            prnt = tprnt;
        }
        if (prnt instanceof javax.swing.JViewport) {
            viewport = (javax.swing.JViewport) prnt;
            this.transform.transform(cpt, cpt);
            Dimension viewsize = viewport.getExtentSize();
            viewport.setViewPosition(new Point((int) (cpt.getX() - (viewsize.width) / 2.),
                (int) (cpt.getY() - (viewsize.height) / 2.)));
        } // else ...
    }

    /**
     * Get the AffineTransform that applies to this drawing.
     *
     * @return the AffineTransform that applies to this drawing.
     */
    public AffineTransform getTransform()
    {
        return (AffineTransform) (this.transform.clone());
    }

    /**
     * Get the inverse AffineTransform that applies to this drawing.
     *
     * @return the inverse AffineTransform that applies to this drawing.
     */
    public AffineTransform getInverseTransform()
    {
        return this.inverseTransform;
    }

    /**
     * Registers the default text to display in a tool tip. Setting the default text to null turns off tool tips. The
     * default text is displayed when the mouse is outside the graph boundaries, but within the panel.
     *
     * @see Graph#setToolTipText(String)
     */
    @Override
    public void setToolTipText(String tip)
    {
        // System.err.println("tip set to: " + tip);
        super.setToolTipText(tip);
    }

    /**
     * Generate an appropriate tooltip based on the mouse location provided by the given event.
     *
     * @return if a GrappaListener is available, the result of its <TT>grappaTip()</TT> method is returned, otherwise
     *         null.
     * @see GrappaPanel#setToolTipText(String)
     */
    @Override
    public String getToolTipText(MouseEvent mev)
    {
        if (this.inverseTransform == null || this.grappaListener == null)
        {
            return (null);
            // System.err.println("tip requested");
        }

        Point2D pt = this.inverseTransform.transform(mev.getPoint(), null);

        return (this.grappaListener.grappaTip(this.subgraph, findContainingElement(this.subgraph, pt),
            new GrappaPoint(pt.getX(), pt.getY()), mev.getModifiers(), this));
    }

    /**
     * Enable/disable scale-to-fit mode.
     *
     * @param setting if true, the graph drawing is scaled to fit the panel, otherwise the graph is drawn full-size.
     */
    public void setScaleToFit(boolean setting)
    {
        this.prevsz = null;
        this.scaleToFit = setting;
    }

    /**
     * Scale the graph drawing to a specific size.
     */
    public void setScaleToSize(Dimension2D scaleSize)
    {

        this.prevsz = null;
        if (scaleSize == null) {
            this.scaleToSize = null;
        } else {
            this.scaleToSize = new GrappaSize(scaleSize.getWidth(), scaleSize.getHeight());
        }
    }

    /**
     * Get the subgraph being drawn on this panel.
     *
     * @return the subgraph being drawn on this panel.
     */
    public Subgraph getSubgraph()
    {
        return (this.subgraph);
    }

    /**
     * Reset the scale factor to one.
     */
    public void resetZoom()
    {
        this.scaleChanged = this.scaleFactor != 1;
        this.scaleFactor = 1;
        this.zoomBox = null;
    }

    /**
     * Check if a swept outline is still available.
     *
     * @return true if there is an outline available.
     */
    public boolean hasOutline()
    {
        return (this.savedOutline != null);
    }

    /**
     * Clear swept outline, if any.
     */
    public void clearOutline()
    {
        this.savedOutline = null;
    }

    /**
     * Zoom the drawing to the outline just swept with the mouse, if any.
     *
     * @return the box corresponding to the swept outline, or null.
     */
    public GrappaBox zoomToOutline()
    {
        this.zoomBox = null;
        if (this.savedOutline != null) {
            this.scaleFactor = 1;
            this.zoomBox = new GrappaBox(this.savedOutline);
            this.savedOutline = null;
        }
        // System.err.println("zoomBox=" + zoomBox);
        return (this.zoomBox);
    }

    /**
     * Zoom the drawing to the outline just swept with the mouse, if any.
     *
     * @param outline the zoom bounds
     * @return the box corresponding to the swept outline, or null.
     */
    public GrappaBox zoomToOutline(GrappaBox outline)
    {
        this.zoomBox = null;
        if (outline != null) {
            this.scaleFactor = 1;
            this.zoomBox = new GrappaBox(outline);
        }
        return (this.zoomBox);
    }

    /**
     * Adjust the scale factor by the supplied multiplier.
     *
     * @param multiplier multiply the scale factor by this amount.
     * @return the value of the previous scale factor.
     */
    public double multiplyScaleFactor(double multiplier)
    {
        double old = this.scaleFactor;
        this.zoomBox = null;
        this.scaleFactor *= multiplier;
        if (this.scaleFactor == 0) {
            this.scaleFactor = old;
        }
        this.scaleChanged = this.scaleFactor != old;
        return (old);
    }

    // //////////////////////////////////////////////////////////////////////
    //
    // Private methods
    //
    // //////////////////////////////////////////////////////////////////////

    private void paintSubgraph(Graphics2D g2d, Subgraph subg, Shape clipper, Color bkgdColor)
    {
        if (subg != this.subgraph && !subg.reserve()) {
            return;
        }

        Rectangle2D bbox = subg.getBoundingBox();
        GrappaNexus grappaNexus = subg.grappaNexus;

        if (bbox != null && grappaNexus != null && subg.visible && !grappaNexus.style.invis && clipper.intersects(bbox)) {

            Enumeration<? extends Element> enm = null;

            int i;

            if (subg != this.subgraph) {
                g2d.setPaint(grappaNexus.color);
                if (grappaNexus.style.filled) {
                    if (grappaNexus.fillcolor != null) {
                        bkgdColor = grappaNexus.fillcolor;
                        grappaNexus.fill(g2d);
                        if (grappaNexus.color != null) {
                            g2d.setPaint(grappaNexus.color);
                        } else {
                            g2d.setPaint(grappaNexus.style.line_color);
                        }
                    } else {
                        bkgdColor = grappaNexus.color;
                        grappaNexus.fill(g2d);
                        g2d.setPaint(grappaNexus.style.line_color);
                    }
                } else if (grappaNexus.color == bkgdColor) { // using == is OK (caching)
                    g2d.setPaint(grappaNexus.style.line_color);
                }
                grappaNexus.drawImage(g2d);
                if (subg.isCluster() || Grappa.outlineSubgraphs) {
                    if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                        g2d.setStroke(grappaNexus.style.stroke);
                        grappaNexus.draw(g2d);
                        g2d.setStroke(GrappaStyle.defaultStroke);
                    } else {
                        grappaNexus.draw(g2d);
                    }
                }
            }

            if ((subg.highlight & DELETION_MASK) == DELETION_MASK) {
                g2d.setPaint(this.deletionStyle.line_color);
                if (GrappaStyle.defaultStroke != this.deletionStyle.stroke) {
                    g2d.setStroke(this.deletionStyle.stroke);
                    grappaNexus.draw(g2d);
                    g2d.setStroke(GrappaStyle.defaultStroke);
                } else {
                    grappaNexus.draw(g2d);
                }
            } else if ((subg.highlight & SELECTION_MASK) == SELECTION_MASK) {
                g2d.setPaint(this.selectionStyle.line_color);
                if (GrappaStyle.defaultStroke != this.selectionStyle.stroke) {
                    g2d.setStroke(this.selectionStyle.stroke);
                    grappaNexus.draw(g2d);
                    g2d.setStroke(GrappaStyle.defaultStroke);
                } else {
                    grappaNexus.draw(g2d);
                }
            }

            if (grappaNexus.lstr != null && this.subgLabels) {
                g2d.setFont(grappaNexus.font);
                g2d.setPaint(grappaNexus.font_color);
                for (i = 0; i < grappaNexus.lstr.length; i++) {
                    g2d.drawString(grappaNexus.lstr[i], (int) grappaNexus.lpos[i].x, (int) grappaNexus.lpos[i].y);
                }
            }

            enm = subg.subgraphElements();
            Subgraph subsubg = null;
            while (enm.hasMoreElements()) {
                subsubg = (Subgraph) (enm.nextElement());
                if (subsubg != null) {
                    paintSubgraph(g2d, subsubg, clipper, bkgdColor);
                }
            }
            Node node;
            enm = subg.nodeElements();
            while (enm.hasMoreElements()) {
                node = (Node) (enm.nextElement());
                if (node == null || !node.reserve()) {
                    continue;
                }
                if ((grappaNexus = node.grappaNexus) != null && node.visible && !grappaNexus.style.invis
                    && clipper.intersects(grappaNexus.rawBounds2D())) {
                    if (grappaNexus.style.filled) {
                        if (grappaNexus.fillcolor != null) {
                            g2d.setPaint(grappaNexus.fillcolor);
                            grappaNexus.fill(g2d);
                            if (grappaNexus.color != null) {
                                g2d.setPaint(grappaNexus.color);
                            } else {
                                g2d.setPaint(grappaNexus.style.line_color);
                            }
                        } else {
                            g2d.setPaint(grappaNexus.color);
                            grappaNexus.fill(g2d);
                            g2d.setPaint(grappaNexus.style.line_color);
                        }
                    } else {
                        g2d.setPaint(grappaNexus.color);
                    }
                    grappaNexus.drawImage(g2d);
                    if ((node.highlight & DELETION_MASK) == DELETION_MASK) {
                        g2d.setPaint(this.deletionStyle.line_color);
                        if (GrappaStyle.defaultStroke != this.deletionStyle.stroke) {
                            g2d.setStroke(this.deletionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else if ((node.highlight & SELECTION_MASK) == SELECTION_MASK) {
                        g2d.setPaint(this.selectionStyle.line_color);
                        if (GrappaStyle.defaultStroke != this.selectionStyle.stroke) {
                            g2d.setStroke(this.selectionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else {
                        if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                            g2d.setStroke(grappaNexus.style.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    }
                    if (grappaNexus.lstr != null && this.nodeLabels) {
                        g2d.setFont(grappaNexus.font);
                        g2d.setPaint(grappaNexus.font_color);
                        for (i = 0; i < grappaNexus.lstr.length; i++) {
                            g2d.drawString(grappaNexus.lstr[i], (int) grappaNexus.lpos[i].x,
                                (int) grappaNexus.lpos[i].y);
                        }
                    }
                }
                node.release();
            }

            Edge edge;
            enm = subg.edgeElements();
            while (enm.hasMoreElements()) {
                edge = (Edge) (enm.nextElement());
                if (edge == null || !edge.reserve()) {
                    continue;
                }
                if ((grappaNexus = edge.grappaNexus) != null && edge.visible && !grappaNexus.style.invis
                    && clipper.intersects(grappaNexus.rawBounds2D())) {
                    grappaNexus.drawImage(g2d);
                    if ((edge.highlight & DELETION_MASK) == DELETION_MASK) {
                        g2d.setPaint(this.deletionStyle.line_color);
                        grappaNexus.fill(g2d);
                        if (GrappaStyle.defaultStroke != this.deletionStyle.stroke) {
                            g2d.setStroke(this.deletionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else if ((edge.highlight & SELECTION_MASK) == SELECTION_MASK) {
                        g2d.setPaint(this.selectionStyle.line_color);
                        grappaNexus.fill(g2d);
                        if (GrappaStyle.defaultStroke != this.selectionStyle.stroke) {
                            g2d.setStroke(this.selectionStyle.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    } else {
                        g2d.setPaint(grappaNexus.color);
                        grappaNexus.fill(g2d);
                        if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
                            g2d.setStroke(grappaNexus.style.stroke);
                            grappaNexus.draw(g2d);
                            g2d.setStroke(GrappaStyle.defaultStroke);
                        } else {
                            grappaNexus.draw(g2d);
                        }
                    }
                    if (grappaNexus.lstr != null && this.edgeLabels) {
                        g2d.setFont(grappaNexus.font);
                        g2d.setPaint(grappaNexus.font_color);
                        for (i = 0; i < grappaNexus.lstr.length; i++) {
                            g2d.drawString(grappaNexus.lstr[i], (int) grappaNexus.lpos[i].x,
                                (int) grappaNexus.lpos[i].y);
                        }
                    }
                }
                edge.release();
            }
        }
        subg.release();
    }

    private Element findContainingElement(Subgraph subg, Point2D pt)
    {
        return (findContainingElement(subg, pt, null));
    }

    private Element findContainingElement(Subgraph subg, Point2D pt, Element crnt)
    {
        Element elem;
        Element[] stash = new Element[2];

        stash[0] = crnt;
        stash[1] = null;

        if ((elem = reallyFindContainingElement(subg, pt, stash)) == null) {
            elem = stash[1];
        }
        return (elem);
    }

    private Element reallyFindContainingElement(Subgraph subg, Point2D pt, Element[] stash)
    {

        Enumeration<? extends Element> enm;

        Rectangle2D bb = subg.getBoundingBox();

        GrappaNexus grappaNexus = null;

        if (bb.contains(pt)) {

            if ((Grappa.elementSelection & EDGE) == EDGE) {
                enm = subg.edgeElements();
                Edge edge;
                while (enm.hasMoreElements()) {
                    edge = (Edge) enm.nextElement();
                    if ((grappaNexus = edge.grappaNexus) == null || !edge.selectable) {
                        continue;
                    }
                    if (grappaNexus.rawBounds2D().contains(pt)) {
                        if (grappaNexus.contains(pt.getX(), pt.getY())) {
                            if (stash[0] == null) {
                                return (edge);
                            }
                            if (stash[1] == null) {
                                stash[1] = edge;
                            }
                            if (stash[0] == edge) {
                                stash[0] = null;
                            }
                        }
                    }
                }
            }

            if ((Grappa.elementSelection & NODE) == NODE) {
                enm = subg.nodeElements();
                Node node;
                while (enm.hasMoreElements()) {
                    node = (Node) enm.nextElement();
                    if ((grappaNexus = node.grappaNexus) == null || !node.selectable) {
                        continue;
                    }
                    if (grappaNexus.rawBounds2D().contains(pt)) {
                        if (grappaNexus.contains(pt)) {
                            if (stash[0] == null) {
                                return (node);
                            }
                            if (stash[1] == null) {
                                stash[1] = node;
                            }
                            if (stash[0] == node) {
                                stash[0] = null;
                            }
                        }
                    }
                }
            }

            Element subelem = null;

            enm = subg.subgraphElements();
            while (enm.hasMoreElements()) {
                if ((subelem = reallyFindContainingElement((Subgraph) (enm.nextElement()), pt, stash)) != null
                    && subelem.selectable) {
                    if (stash[0] == null) {
                        return (subelem);
                    }
                    if (stash[1] == null) {
                        stash[1] = subelem;
                    }
                    if (stash[0] == subelem) {
                        stash[0] = null;
                    }
                }
            }

            if ((Grappa.elementSelection & SUBGRAPH) == SUBGRAPH && subg.selectable) {
                if (stash[0] == null) {
                    return (subg);
                }
                if (stash[1] == null) {
                    stash[1] = subg;
                }
                if (stash[0] == subg) {
                    stash[0] = null;
                }
            }
        }
        return (null);
    }

    // /////////////////////////////////////////////////////////////////
    //
    // AncestorListener Interface
    //
    // /////////////////////////////////////////////////////////////////

    @Override
    public void ancestorMoved(AncestorEvent aev)
    {
        // don't care
    }

    @Override
    public void ancestorAdded(AncestorEvent aev)
    {
        this.graph.addPanel(this);
    }

    @Override
    public void ancestorRemoved(AncestorEvent aev)
    {
        this.graph.removePanel(this);
    }

    // /////////////////////////////////////////////////////////////////
    //
    // ComponentListener Interface
    //
    // /////////////////////////////////////////////////////////////////

    @Override
    public void componentHidden(ComponentEvent cev)
    {
        // don't care
    }

    @Override
    public void componentMoved(ComponentEvent cev)
    {
        // don't care
    }

    @Override
    public void componentResized(ComponentEvent cev)
    {
        // Needed to reset JScrollPane scrollbars, for example
        revalidate();
    }

    @Override
    public void componentShown(ComponentEvent cev)
    {
        // don't care
    }

    // /////////////////////////////////////////////////////////////////
    //
    // PopupMenuListener Interface
    //
    // /////////////////////////////////////////////////////////////////

    @Override
    public void popupMenuCanceled(PopupMenuEvent pmev)
    {
        // don't care
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent pmev)
    {
        this.inMenu = false;
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent pmev)
    {
        this.inMenu = true;
    }

    // /////////////////////////////////////////////////////////////////
    //
    // MouseListener Interface
    //
    // /////////////////////////////////////////////////////////////////

    @Override
    public void mouseClicked(MouseEvent mev)
    {
        if (this.inverseTransform == null || this.grappaListener == null || this.inMenu) {
            return;
        }

        Point2D pt = this.inverseTransform.transform(mev.getPoint(), null);

        this.grappaListener.grappaClicked(
            this.subgraph,
            findContainingElement(this.subgraph, pt, (this.subgraph.currentSelection != null
                && this.subgraph.currentSelection instanceof Element ? ((Element) this.subgraph.currentSelection)
                : null)),
                new GrappaPoint(pt.getX(), pt.getY()), mev.getModifiers(), mev.getClickCount(), this);
    }

    @Override
    public void mousePressed(MouseEvent mev)
    {
        if (this.inverseTransform == null || this.grappaListener == null || this.inMenu) {
            return;
        }

        Point2D pt = this.inverseTransform.transform(mev.getPoint(), null);

        this.outline = null;

        this.grappaListener.grappaPressed(this.subgraph, (this.pressedElement =
            findContainingElement(this.subgraph, pt)), (this.pressedPoint =
            new GrappaPoint(pt.getX(), pt.getY())), (this.pressedModifiers = mev.getModifiers()), this);
    }

    @Override
    public void mouseReleased(MouseEvent mev)
    {
        if (this.inverseTransform == null || this.grappaListener == null || this.inMenu) {
            return;
        }

        int modifiers = mev.getModifiers();

        Point2D pt = this.inverseTransform.transform(mev.getPoint(), null);

        GrappaPoint gpt = new GrappaPoint(pt.getX(), pt.getY());

        this.grappaListener.grappaReleased(this.subgraph, findContainingElement(this.subgraph, pt), gpt, modifiers,
            this.pressedElement,
            this.pressedPoint, this.pressedModifiers, this.outline, this);

        if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0
            && (modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == modifiers) {
            if (this.outline != null) {
                // System.err.println("saving outline");
                this.savedOutline =
                    GrappaSupport.boxFromCorners(this.outline, this.pressedPoint.x, this.pressedPoint.y, gpt.x, gpt.y);
                this.outline = null;
            } else {
                // System.err.println("clearing outline");
                this.savedOutline = null;
            }
        }

    }

    @Override
    public void mouseEntered(MouseEvent mev)
    {
        // don't care
    }

    @Override
    public void mouseExited(MouseEvent mev)
    {
        // don't care
    }

    // /////////////////////////////////////////////////////////////////
    //
    // MouseMotionListener Interface
    //
    // /////////////////////////////////////////////////////////////////

    @Override
    public void mouseDragged(MouseEvent mev)
    {
        if (this.inverseTransform == null || this.grappaListener == null || this.inMenu) {
            return;
        }

        int modifiers = mev.getModifiers();

        Point2D pt = this.inverseTransform.transform(mev.getPoint(), null);

        GrappaPoint gpt = new GrappaPoint(pt.getX(), pt.getY());

        this.grappaListener.grappaDragged(this.subgraph, gpt, modifiers, this.pressedElement, this.pressedPoint,
            this.pressedModifiers, this.outline,
            this);

        if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0
            && (modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == modifiers) {
            this.outline =
                GrappaSupport.boxFromCorners(this.outline, this.pressedPoint.x, this.pressedPoint.y, gpt.x, gpt.y);
        }
    }

    @Override
    public void mouseMoved(MouseEvent mev)
    {
        // don't care
    }

    // --- Scrollable interface ----------------------------------------

    /**
     * Returns the size of the bounding box of the graph augmented by the margin attribute and any scaling.
     *
     * @return The preferredSize of a JViewport whose view is this Scrollable.
     * @see JViewport#getPreferredSize
     */
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {

        // preferred size is set above as needed, so just return it
        return getPreferredSize();
    }

    /**
     * Always returns 1 since a GrappaPanel has not logical rows or columns.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "unit" increment for scrolling in the specified direction, which in the case of a GrappaPanel is
     *         always 1.
     * @see JScrollBar#setUnitIncrement
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return (1);
    }

    /**
     * Returns 90% of the view area dimension that is in the orientation of the requested scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "unit" increment for scrolling in the specified direction, which in the case of a GrappaPanel is 90%
     *         of the visible width for a horizontal increment or 90% of the visible height for a vertical increment.
     * @see JScrollBar#setBlockIncrement
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        int block;

        if (orientation == javax.swing.SwingConstants.VERTICAL) {
            block = (int) (visibleRect.height * 0.9);
        } else {
            block = (int) (visibleRect.width * 0.9);
        }
        if (block < 1) {
            block = 1;
        }

        return (block);
    }

    /**
     * Always returns false as the viewport should not force the width of this GrappaPanel to match the width of the
     * viewport.
     *
     * @return false
     */
    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return (false);
    }

    /**
     * Always returns false as the viewport should not force the height of this GrappaPanel to match the width of the
     * viewport.
     *
     * @return false
     */
    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return (false);
    }

    @Override
    public void run()
    {
        Point2D cpt = getCPT();

        if (cpt != null) {
            centerPanelAtPoint(cpt);
        }
    }
}
