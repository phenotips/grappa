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
 * A class providing <I>sprintf</I> support.
 *
 * @version 1.2, 04 Mar 2008; Copyright 1996 - 2008 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a> and Rich Drechsler, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public class GrappaSupportPrintf implements GrappaConstants
{
    // /////////////////////////////////////////////////////////////////////////
    //
    // GrappaSupportPrintf
    //
    // /////////////////////////////////////////////////////////////////////////

    /**
     * The familiar C-language sprintf rendered in Java and extended for some Grappa types.
     *
     * @param args the first element of this array is a string giving the format of the returned string, the remaining
     *            elements are object to be formatted according to the format.
     * @return a string giving a formatted representation of the arguments.
     */
    public final static String sprintf(Object args[])
    {
        PrintfParser cvt;
        StringBuilder prtbuf;
        char format[];
        int flen;
        int argn;
        int n;
        char ch;
        boolean flag;

        if (!(args[0] instanceof String)) {
            throw new RuntimeException("initial argument must be format String");
        }

        argn = 0;
        format = ((String) args[argn++]).toCharArray();

        flen = format.length;
        prtbuf = new StringBuilder(2 * flen);
        cvt = new PrintfParser();

        for (n = 0; n < flen;) {
            if ((ch = format[n++]) == '%') {
                if ((n = cvt.parse(format, n)) < flen) {
                    switch (ch = format[n++]) {
                        case 'b':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof GrappaBox) {
                                flag = ((GrappaBox) args[argn]).isDimensioned();
                            } else {
                                flag = true;
                            }
                            if (args[argn] instanceof java.awt.geom.Rectangle2D) {
                                cvt.buildBox(prtbuf, ((java.awt.geom.Rectangle2D) args[argn++]), false, flag);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Rectangle2D");
                            }
                            break;

                        case 'B':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof GrappaBox) {
                                flag = ((GrappaBox) args[argn]).isDimensioned();
                            } else {
                                flag = true;
                            }
                            if (args[argn] instanceof java.awt.geom.Rectangle2D) {
                                cvt.buildBox(prtbuf, ((java.awt.geom.Rectangle2D) args[argn++]), true, flag);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Rectangle2D");
                            }
                            break;
                        case 'c':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildChar(prtbuf, ((Character) args[argn++]).charValue());
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character");
                            }
                            break;

                        case 'd':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Number) {
                                cvt.buildInteger(prtbuf, ((Number) args[argn++]).intValue());
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Number");
                            }
                            break;

                        case 'o':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildOctal(prtbuf, ((Character) args[argn++]).charValue());
                            } else if (args[argn] instanceof Number) {
                                cvt.buildOctal(prtbuf, ((Number) args[argn++]).intValue());
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 'p':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof java.awt.geom.Point2D) {
                                cvt.buildPoint(prtbuf, ((java.awt.geom.Point2D) args[argn++]), false);
                            } else if (args[argn] instanceof java.awt.geom.Dimension2D) {
                                cvt.buildSize(prtbuf, ((java.awt.geom.Dimension2D) args[argn++]), false);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Point2D");
                            }
                            break;

                        case 'P':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof java.awt.geom.Point2D) {
                                cvt.buildPoint(prtbuf, ((java.awt.geom.Point2D) args[argn++]), true);
                            } else if (args[argn] instanceof java.awt.geom.Dimension2D) {
                                cvt.buildSize(prtbuf, ((java.awt.geom.Dimension2D) args[argn++]), true);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Point2D");
                            }
                            break;

                        case 'x':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildHex(prtbuf, ((Character) args[argn++]).charValue(), false);
                            } else if (args[argn] instanceof Number) {
                                cvt.buildHex(prtbuf, ((Number) args[argn++]).intValue(), false);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 'X':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildHex(prtbuf, ((Character) args[argn++]).charValue(), true);
                            } else if (args[argn] instanceof Number) {
                                cvt.buildHex(prtbuf, ((Number) args[argn++]).intValue(), true);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 'e':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildExp(prtbuf, ((Character) args[argn++]).charValue(), false);
                            } else if (args[argn] instanceof Number) {
                                cvt.buildExp(prtbuf, ((Number) args[argn++]).doubleValue(), false);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 'E':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildExp(prtbuf, ((Character) args[argn++]).charValue(), true);
                            } else if (args[argn] instanceof Number) {
                                cvt.buildExp(prtbuf, ((Number) args[argn++]).doubleValue(), true);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 'f':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildFloat(prtbuf, ((Character) args[argn++]).charValue());
                            } else if (args[argn] instanceof Number) {
                                cvt.buildFloat(prtbuf, ((Number) args[argn++]).doubleValue());
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 'g':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildFlex(prtbuf, ((Character) args[argn++]).charValue(), false);
                            } else if (args[argn] instanceof Number) {
                                cvt.buildFlex(prtbuf, ((Number) args[argn++]).doubleValue(), false);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 'G':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            if (args[argn] instanceof Character) {
                                cvt.buildFlex(prtbuf, ((Character) args[argn++]).charValue(), true);
                            } else if (args[argn] instanceof Number) {
                                cvt.buildFlex(prtbuf, ((Number) args[argn++]).doubleValue(), true);
                            } else {
                                throw new RuntimeException("argument " + argn + " should be a Character or Number");
                            }
                            break;

                        case 's':
                            if (args.length <= argn) {
                                throw new RuntimeException("too few arguments for format");
                            }
                            cvt.buildString(prtbuf, args[argn++].toString());
                            break;

                        case '%':
                            prtbuf.append('%');
                            break;

                        default:
                            // different compilers handle this different ways,
                            // some just do the equivalent of prtbuf.append(ch),
                            // but we will just ignore the unrecognized format
                            break;
                    }
                } else {
                    prtbuf.append(ch);
                }
            } else if (ch == '\\') {
                if (n < flen) {
                    switch (ch = format[n++]) {
                        case 'b':
                            prtbuf.append('\b');
                            break;
                        case 'f':
                            prtbuf.append('\f');
                            break;
                        case 'n':
                            prtbuf.append('\n');
                            break;
                        case 'r':
                            prtbuf.append('\r');
                            break;
                        case 't':
                            prtbuf.append('\t');
                            break;
                        case 'u':
                            if ((n + 3) < flen) {
                                if (GrappaSupport.isdigit(format[n])
                                    &&
                                    GrappaSupport.isdigit(format[n + 1])
                                    &&
                                    GrappaSupport.isdigit(format[n + 2])
                                    &&
                                    GrappaSupport.isdigit(format[n + 3])) {
                                    int uni =
                                        format[n + 3] + 16 * format[n + 2] + 256 * format[n + 1]
                                            + 4096 * format[n];
                                    prtbuf.append((char) uni);
                                    n += 4;
                                } else {
                                    prtbuf.append('u');
                                }
                            } else {
                                prtbuf.append('u');
                            }
                            break;
                        case '"':
                            prtbuf.append('\"');
                            break;
                        case '\'':
                            prtbuf.append('\'');
                            break;
                        case '\\':
                            prtbuf.append('\\');
                            break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            // need to fix this, assumes 3 digit octals
                            if ((n + 1) < flen) {
                                if (GrappaSupport.isdigit(format[n])
                                    &&
                                    GrappaSupport.isdigit(format[n + 1])) {
                                    int oct = format[n + 1] + 8 * format[n] + 64 * ch;
                                    prtbuf.append((char) oct);
                                    n += 2;
                                } else {
                                    prtbuf.append(ch);
                                }
                            } else {
                                prtbuf.append(ch);
                            }
                            break;
                    }
                } else {
                    prtbuf.append(ch);
                }
            } else {
                prtbuf.append(ch);
            }
        }

        return (prtbuf.toString());
    }

    // /////////////////////////////////////////////////////////////////////////
}

class PrintfParser implements GrappaConstants
{
    private boolean alternate;

    private boolean rightpad;

    private boolean sign;

    private boolean space;

    private boolean zeropad;

    private boolean trim;

    private int precision;

    private int width;

    private String plus;

    private char padding;

    private StringBuilder scratch;

    // /////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    // /////////////////////////////////////////////////////////////////////////

    PrintfParser()
    {
        this.scratch = new StringBuilder();
    }

    // /////////////////////////////////////////////////////////////////////////
    //
    // printfParser
    //
    // /////////////////////////////////////////////////////////////////////////

    final int parse(char cfmt[])
    {
        return (parse(cfmt, 0));
    }

    // /////////////////////////////////////////////////////////////////////////

    final int parse(char cfmt[], int n)
    {
        boolean done;
        int ch;

        //
        // Parse the conversion specification that starts at index n
        // in fmt. Results are stored in the class variables and the
        // position of the character that stopped the parse is
        // returned to the caller.
        //

        this.alternate = false;
        this.rightpad = false;
        this.sign = false;
        this.space = false;
        this.zeropad = false;
        this.trim = false;

        for (done = false; n < cfmt.length && !done; n++) {
            switch (cfmt[n]) {
                case '-':
                    this.rightpad = true;
                    break;
                case '+':
                    this.sign = true;
                    break;
                case ' ':
                    this.space = true;
                    break;
                case '0':
                    this.zeropad = true;
                    break;
                case '#':
                    this.alternate = true;
                    break;
                default:
                    done = true;
                    n--;
                    break;
            }
        }

        this.plus = (this.sign ? "+" : (this.space ? " " : ""));

        for (this.width = 0; n < cfmt.length && GrappaSupport.isdigit(ch = cfmt[n]); n++) {
            this.width = this.width * 10 + (ch - '0');
        }

        if (n < cfmt.length && cfmt[n] == '.') {
            n++;
            for (this.precision = 0; n < cfmt.length && GrappaSupport.isdigit(ch = cfmt[n]); n++) {
                this.precision = this.precision * 10 + (ch - '0');
            }
        } else {
            this.precision = -1;
        }

        this.padding = (this.zeropad && !this.rightpad) ? '0' : ' ';

        return (n);
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildChar(StringBuilder buf, int arg)
    {
        this.scratch.setLength(0);
        this.scratch.append((char) arg);
        return (strpad(buf, this.scratch.toString(), ' ', this.width, this.rightpad));
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildExp(StringBuilder buf, double arg, boolean upper)
    {
        double exp;
        double base;
        double val;
        int sign;

        this.precision = (this.precision >= 0) ? this.precision : 6;

        val = arg;
        sign = (val >= 0) ? 1 : -1;
        val = (val < 0) ? -val : val;

        if (val >= 1) {
            exp = Math.log(val) / LOG10;
            base = Math.pow(10, exp - (int) exp);
        } else {
            exp = Math.log(val / 10) / LOG10;
            base = Math.pow(10, exp - (int) exp + 1);
        }

        this.scratch.setLength(0);
        this.scratch.append(upper ? "E" : "e");
        this.scratch.append(exp > 0 ? '+' : '-');

        strpad(this.scratch, ("" + (int) (exp > 0 ? exp : -exp)), '0', 2, false);
        if (this.padding == '0' && this.precision >= 0) {
            this.padding = ' ';
        }

        return (strpad(buf, doubleToString(sign * base, this.scratch.toString()), this.padding, this.width,
            this.rightpad));
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildFlex(StringBuilder buf, double arg, boolean upper)
    {
        double exp;
        double val;
        double ival;
        StringBuilder retbuf;
        int iexp;
        int pr;

        this.trim = true;

        val = arg;
        ival = (int) arg;
        val = (val < 0) ? -val : val;

        if (val >= 1) {
            exp = Math.log(val) / LOG10;
        } else {
            exp = Math.log(val / 10) / LOG10;
        }

        iexp = (int) exp;
        this.precision = (this.precision >= 0) ? --this.precision : 5;

        if (val == ival) {
            if (this.alternate) {
                if (this.precision < 0 || iexp <= this.precision) {
                    this.precision -= iexp;
                    retbuf = buildFloat(buf, arg);
                } else {
                    retbuf = buildExp(buf, arg, upper);
                }
            } else {
                if (this.precision < 0 || iexp <= this.precision) {
                    this.precision = -1;
                    retbuf = buildInteger(buf, (int) arg);
                } else {
                    retbuf = buildExp(buf, arg, upper);
                }
            }
        } else if (iexp < -4 || iexp > this.precision) {
            retbuf = buildExp(buf, arg, upper);
        } else {
            retbuf = buildFloat(buf, arg);
        }

        return (retbuf);
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildPoint(StringBuilder buf, java.awt.geom.Point2D parg, boolean upper)
    {
        double[] arg = { 0, 0 };
        double[] exp = { 0, 0 };
        double[] val = { 0, 0 };
        double[] ival = { 0, 0 };
        int[] iexp = { 0, 0 };
        StringBuilder retbuf = null;
        int orig_precision;
        int pr;

        this.trim = true;

        arg[0] = parg.getX();
        arg[1] = (Grappa.negateStringYCoord ? -parg.getY() : parg.getY());
        val[0] = arg[0];
        val[1] = arg[1];
        orig_precision = this.precision;

        for (int i = 0; i < 2; i++) {
            this.precision = orig_precision;
            ival[i] = (int) val[i];
            val[i] = (val[i] < 0) ? -val[i] : val[i];

            if (val[i] >= 1) {
                exp[i] = Math.log(val[i]) / LOG10;
            } else {
                exp[i] = Math.log(val[i] / 10) / LOG10;
            }

            iexp[i] = (int) exp[i];
            this.precision = (this.precision >= 0) ? --this.precision : 5;

            if (val[i] == ival[i]) {
                if (this.alternate) {
                    if (this.precision < 0 || iexp[i] <= this.precision) {
                        this.precision -= iexp[i];
                        retbuf = buildFloat(buf, arg[i]);
                    } else {
                        retbuf = buildExp(buf, arg[i], upper);
                    }
                } else {
                    if (this.precision < 0 || iexp[i] <= this.precision) {
                        this.precision = -1;
                        retbuf = buildInteger(buf, (long) arg[i]);
                    } else {
                        retbuf = buildExp(buf, arg[i], upper);
                    }
                }
            } else if (iexp[i] < -4 || iexp[i] > this.precision) {
                retbuf = buildExp(buf, arg[i], upper);
            } else {
                retbuf = buildFloat(buf, arg[i]);
            }

            if (i == 0) {
                retbuf = retbuf.append(',');
                buf = retbuf;
            }
        }

        return (retbuf);
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildSize(StringBuilder buf, java.awt.geom.Dimension2D parg, boolean upper)
    {
        double[] arg = { 0, 0 };
        double[] exp = { 0, 0 };
        double[] val = { 0, 0 };
        double[] ival = { 0, 0 };
        int[] iexp = { 0, 0 };
        StringBuilder retbuf = null;
        int orig_precision;
        int pr;

        this.trim = true;

        arg[0] = parg.getWidth();
        arg[1] = parg.getHeight();
        val[0] = arg[0];
        val[1] = arg[1];
        orig_precision = this.precision;

        for (int i = 0; i < 2; i++) {
            this.precision = orig_precision;
            ival[i] = (int) val[i];
            val[i] = (val[i] < 0) ? -val[i] : val[i];

            if (val[i] >= 1) {
                exp[i] = Math.log(val[i]) / LOG10;
            } else {
                exp[i] = Math.log(val[i] / 10) / LOG10;
            }

            iexp[i] = (int) exp[i];
            this.precision = (this.precision >= 0) ? --this.precision : 5;

            if (val[i] == ival[i]) {
                if (this.alternate) {
                    if (this.precision < 0 || iexp[i] <= this.precision) {
                        this.precision -= iexp[i];
                        retbuf = buildFloat(buf, arg[i]);
                    } else {
                        retbuf = buildExp(buf, arg[i], upper);
                    }
                } else {
                    if (this.precision < 0 || iexp[i] <= this.precision) {
                        this.precision = -1;
                        retbuf = buildInteger(buf, (long) arg[i]);
                    } else {
                        retbuf = buildExp(buf, arg[i], upper);
                    }
                }
            } else if (iexp[i] < -4 || iexp[i] > this.precision) {
                retbuf = buildExp(buf, arg[i], upper);
            } else {
                retbuf = buildFloat(buf, arg[i]);
            }

            if (i == 0) {
                retbuf = retbuf.append(',');
                buf = retbuf;
            }
        }

        return (retbuf);
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildBox(StringBuilder buf, java.awt.geom.Rectangle2D parg, boolean upper, boolean dimensioned)
    {
        double[] arg = { 0, 0, 0, 0 };
        double[] exp = { 0, 0, 0, 0 };
        double[] val = { 0, 0, 0, 0 };
        double[] ival = { 0, 0, 0, 0 };
        int[] iexp = { 0, 0, 0, 0 };
        StringBuilder retbuf = null;
        int orig_precision;
        int pr;

        this.trim = true;

        if (!dimensioned) {
            arg[0] = parg.getX();
            arg[1] = parg.getY();
            arg[2] = arg[0] + arg[2];
            arg[3] = arg[1] + arg[3];
            arg[1] = (Grappa.negateStringYCoord ? -arg[1] : arg[1]);
            arg[3] = (Grappa.negateStringYCoord ? -arg[3] : arg[3]);
        } else {
            arg[0] = parg.getX();
            arg[1] = (Grappa.negateStringYCoord ? -parg.getY() : parg.getY());
            arg[2] = parg.getWidth();
            arg[3] = parg.getHeight();
        }
        val[0] = arg[0];
        val[1] = arg[1];
        val[2] = arg[2];
        val[3] = arg[3];
        orig_precision = this.precision;

        for (int i = 0; i < 4; i++) {
            this.precision = orig_precision;
            ival[i] = (int) val[i];
            val[i] = (val[i] < 0) ? -val[i] : val[i];

            if (val[i] >= 1) {
                exp[i] = Math.log(val[i]) / LOG10;
            } else {
                exp[i] = Math.log(val[i] / 10) / LOG10;
            }

            iexp[i] = (int) exp[i];
            this.precision = (this.precision >= 0) ? --this.precision : 5;

            if (val[i] == ival[i]) {
                if (this.alternate) {
                    if (this.precision < 0 || iexp[i] <= this.precision) {
                        this.precision -= iexp[i];
                        retbuf = buildFloat(buf, arg[i]);
                    } else {
                        retbuf = buildExp(buf, arg[i], upper);
                    }
                } else {
                    if (this.precision < 0 || iexp[i] <= this.precision) {
                        this.precision = -1;
                        retbuf = buildInteger(buf, (long) arg[i]);
                    } else {
                        retbuf = buildExp(buf, arg[i], upper);
                    }
                }
            } else if (iexp[i] < -4 || iexp[i] > this.precision) {
                retbuf = buildExp(buf, arg[i], upper);
            } else {
                retbuf = buildFloat(buf, arg[i]);
            }

            if (i < 3) {
                retbuf = retbuf.append(',');
                buf = retbuf;
            }
        }

        return (retbuf);
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildFloat(StringBuilder buf, double arg)
    {
        double val;
        int sign;

        this.precision = (this.precision >= 0) ? this.precision : 6;
        val = arg;

        if (this.padding == '0' && this.precision >= 0) {
            this.padding = ' ';
        }
        return (strpad(buf, doubleToString(val, ""), this.padding, this.width, this.rightpad));
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildHex(StringBuilder buf, int arg, boolean upper)
    {
        String str;

        this.scratch.setLength(0);

        str = (upper) ? Integer.toHexString(arg).toUpperCase() : Integer.toHexString(arg);

        if (this.precision > str.length()) {
            if (this.alternate) {
                this.scratch.append(upper ? "0X" : "0x");
            }
            strpad(this.scratch, str, '0', this.precision, false);
            strpad(buf, this.scratch.toString(), ' ', this.width, this.rightpad);
        } else {
            if (this.zeropad && !this.rightpad && this.precision < 0) {
                if (this.alternate) {
                    if (this.width > 2) {
                        strpad(this.scratch, str, '0', this.width - 2, this.rightpad);
                        buf.append(upper ? "0X" : "0x");
                        buf.append(this.scratch.toString());
                    } else {
                        buf.append(upper ? "0X" : "0x");
                        buf.append(str);
                    }
                } else {
                    strpad(buf, str, '0', this.width, this.rightpad);
                }
            } else {
                if (this.alternate) {
                    this.scratch.append(upper ? "0X" : "0x");
                    this.scratch.append(str);
                    str = this.scratch.toString();
                }
                strpad(buf, str, ' ', this.width, this.rightpad);
            }
        }

        return (buf);
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildInteger(StringBuilder buf, long arg)
    {
        String str;
        String sign;
        long val;

        this.scratch.setLength(0);

        val = arg;
        sign = (val >= 0) ? this.plus : "-";
        str = "" + ((val < 0) ? -val : val);

        if (this.precision > str.length()) {
            strpad(this.scratch, str, '0', this.precision, false);
            this.scratch.insert(0, sign);
        } else {
            this.scratch.append(sign);
            this.scratch.append(str);
        }

        if (this.padding == '0' && this.precision >= 0) {
            this.padding = ' ';
        }

        return (strpad(buf, this.scratch.toString(), this.padding, this.width, this.rightpad));
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildOctal(StringBuilder buf, int arg)
    {
        String str;

        this.scratch.setLength(0);

        if (this.alternate) {
            this.scratch.append('0');
        }

        this.scratch.append(Integer.toOctalString(arg));
        if (this.precision > this.scratch.length()) {
            str = this.scratch.toString();
            this.scratch.setLength(0);
            strpad(this.scratch, str, '0', this.precision, false);
        }

        if (this.padding == '0' && this.precision >= 0) {
            this.padding = ' ';
        }

        return (strpad(buf, this.scratch.toString(), this.padding, this.width, this.rightpad));
    }

    // /////////////////////////////////////////////////////////////////////////

    final StringBuilder buildString(StringBuilder buf, String arg)
    {
        String str;

        if (this.precision > 0) {
            if (this.precision < arg.length()) {
                str = arg.substring(0, this.precision);
            } else {
                str = arg;
            }
        } else {
            str = arg;
        }

        return (strpad(buf, str, this.padding, this.width, this.rightpad));
    }

    // /////////////////////////////////////////////////////////////////////////
    //
    // Private methods
    //
    // /////////////////////////////////////////////////////////////////////////

    private String doubleToString(double val, String exp)
    {
        String sign;
        double whole;
        double power;
        double frac;

        //
        // Building the resulting String up by casting to an int or long
        // doesn't always work, so we use algorithm that may look harder
        // and slower than necessary.
        //

        this.scratch.setLength(0);

        sign = (val >= 0) ? this.plus : "-";
        val = (val < 0) ? -val : val;

        whole = Math.floor(val);

        if (this.precision != 0) {
            power = Math.pow(10, this.precision);
            frac = (val - whole) * power;
            this.scratch.append((long) whole);
            String tail = ("" + (Math.round(frac)));
            if (this.trim) {
                int len = tail.length();
                int extra = 0;
                while (extra < len && tail.charAt(len - extra - 1) == '0') {
                    extra++;
                }
                if (extra == len) {
                    if (exp.length() > 0) {
                        tail = ".0";
                    } else {
                        tail = "";
                    }
                    this.precision = 0;
                } else if (extra > 0) {
                    this.scratch.append('.');
                    tail = tail.substring(0, len - extra);
                    this.precision -= extra;
                } else {
                    this.scratch.append('.');
                }
            } else {
                this.scratch.append('.');
            }
            if (this.precision > 0 && (power / 10) > frac) {
                strpad(this.scratch, tail, '0', this.precision, false);
            } else {
                this.scratch.append(tail);
            }
            this.scratch.append(exp);
        } else {
            this.scratch.append((long) whole);
            if (this.alternate && exp.length() == 0) {
                this.scratch.append('.');
            }
            this.scratch.append(exp);
        }

        if (this.zeropad && !this.rightpad) {
            String str = this.scratch.toString();
            this.scratch.setLength(0);
            strpad(this.scratch, str, '0', this.width - sign.length(), false);
        }

        this.scratch.insert(0, sign);
        return (this.scratch.toString());
    }

    // /////////////////////////////////////////////////////////////////////////

    private StringBuilder strpad(StringBuilder buf, String str, int ch, int width, boolean right)
    {
        int len;
        int n;

        if (width > 0) {
            if ((len = width - str.length()) > 0) {
                if (right) {
                    buf.append(str);
                }
                for (n = 0; n < len; n++) {
                    buf.append((char) ch);
                }
                if (!right) {
                    buf.append(str);
                }
            } else {
                buf.append(str);
            }
        } else {
            buf.append(str);
        }

        return (buf);
    }

    // /////////////////////////////////////////////////////////////////////////
}
