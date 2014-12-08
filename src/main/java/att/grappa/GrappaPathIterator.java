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

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * This class provides a PathIterator for GrappaNexus shapes.
 *
 * @version $Id$
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a href="http://www.research.att.com">Research @
 *         AT&T Labs</a>
 */
public class GrappaPathIterator implements PathIterator
{
    GrappaNexus grappaNexus;

    AffineTransform affine;

    PathIterator shapeIterator = null;

    PathIterator areaIterator = null;

    double[] pts = new double[6];

    int type;

    // //////////////////////////////////////////////////////////////////////
    //
    // Constructors
    //
    // //////////////////////////////////////////////////////////////////////

    /**
     * Constructs a new <code>GrappaPathIterator</code> given a GrappaNexus.
     */
    public GrappaPathIterator(GrappaNexus shape)
    {
        this(shape, null);
    }

    /**
     * Constructs a new <code>GrappaPathIterator</code> given a GrappaNexus and an optional AffineTransform.
     */
    public GrappaPathIterator(GrappaNexus shape, AffineTransform at)
    {
        if (shape == null) {
            throw new IllegalArgumentException("shape cannot be null");
        }
        this.grappaNexus = shape;
        this.affine = at;
        if (shape.shape != null) {
            this.shapeIterator = shape.shape.getPathIterator(this.affine);
            if (this.shapeIterator.isDone()) {
                this.shapeIterator = null;
            }
        }
        if (shape.textArea != null && (Grappa.shapeClearText || shape.clearText)) {
            this.areaIterator = shape.textArea.getPathIterator(this.affine);
            if (this.areaIterator.isDone()) {
                this.areaIterator = null;
            }
        }
        if (this.shapeIterator != null) {
            this.type = this.shapeIterator.currentSegment(this.pts);
        } else if (this.areaIterator != null) {
            this.type = this.areaIterator.currentSegment(this.pts);
        } else {
            throw new RuntimeException("cannot initialize; nothing to iterate over");
        }
    }

    // //////////////////////////////////////////////////////////////////////
    //
    // PathIterator interface
    //
    // //////////////////////////////////////////////////////////////////////

    @Override
    public int currentSegment(double[] coords)
    {
        System.arraycopy(this.pts, 0, coords, 0, 6);
        return (this.type);
    }

    @Override
    public int currentSegment(float[] coords)
    {
        coords[0] = (float) this.pts[0];
        coords[1] = (float) this.pts[1];
        coords[2] = (float) this.pts[2];
        coords[3] = (float) this.pts[3];
        coords[4] = (float) this.pts[4];
        coords[5] = (float) this.pts[5];
        return (this.type);
    }

    /**
     * Return the winding rule for determining the interior of the path.
     */
    @Override
    public int getWindingRule()
    {
        return (this.grappaNexus.getWindingRule());
    }

    @Override
    public boolean isDone()
    {
        return ((this.shapeIterator == null || this.shapeIterator.isDone())
            && (this.areaIterator == null || this.areaIterator.isDone()));
    }

    @Override
    public void next()
    {
        if (this.shapeIterator != null) {
            if (this.shapeIterator.isDone()) {
                this.shapeIterator = null;
            } else {
                this.shapeIterator.next();
                if (this.shapeIterator.isDone()) {
                    this.shapeIterator = null;
                } else {
                    this.type = this.shapeIterator.currentSegment(this.pts);
                }
                return;
            }
        }
        if (this.areaIterator != null) {
            if (this.areaIterator.isDone()) {
                this.areaIterator = null;
            } else {
                this.areaIterator.next();
                if (this.areaIterator.isDone()) {
                    this.areaIterator = null;
                } else {
                    this.type = this.areaIterator.currentSegment(this.pts);
                }
                return;
            }
        }
    }

}
