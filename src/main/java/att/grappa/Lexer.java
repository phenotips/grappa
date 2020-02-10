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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Hashtable;

import java_cup.runtime.Symbol;

/**
 * A class for doing lexical analysis of <i>dot</i> formatted input.
 *
 * @version $Id$
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a href="http://www.research.att.com">Research @
 *         AT&T Labs</a>
 */
public class Lexer
{
    /**
     * First character of lookahead. Set to '\n' initially (needed for initial 2 calls to advance())
     */
    private int next_char = '\n';

    /**
     * Second character of lookahead. Set to '\n' initially (needed for initial 2 calls to advance())
     */
    private int next_char2 = '\n';

    /**
     * Current line number for use in error messages. Set to -1 to account for next_char/next_char2 initialization
     */
    private int current_line = -1;

    /**
     * Character position in current line.
     */
    private int current_position = 1;

    /**
     * EOF constant.
     */
    private static final int EOF_CHAR = -1;

    /**
     * needed to handle anonymous subgraphs since parser has no precedence
     */
    private boolean haveId = false;

    /**
     * needed for retreating
     */
    private int old_char;

    private int old_position;

    boolean retreated = false;

    /**
     * Count of total errors detected so far.
     */
    @SuppressWarnings("unused")
    private int error_count = 0;

    /**
     * Count of warnings issued so far
     */
    @SuppressWarnings("unused")
    private int warning_count = 0;

    /**
     * hash tables to hold symbols
     */
    private final Hashtable<String, Integer> keywords = new Hashtable<>(32);

    private final Hashtable<Integer, Integer> char_symbols = new Hashtable<>(32);

    private final Reader inReader;

    private PrintWriter errWriter = null;

    /**
     * common StringBuilder (suggested by Ginny Travers (bbn.com))
     */
    private final StringBuilder cmnstrbuf = new StringBuilder();

    /**
     * Create an instance of <code>Lexer</code> that reads from <code>input</code> and sends error messages to
     * <code>error</code>.
     *
     * @param input input <code>Reader</code> object
     * @param error error output <code>Writer</code> object
     * @exception IllegalArgumentException whenever <code>input</code> is null
     */
    public Lexer(final Reader input, final PrintWriter error) throws IllegalArgumentException
    {
        super();
        if (input == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        this.inReader = input;
        this.errWriter = error;
    }

    /**
     * Initialize internal tables and read two characters of input for look-ahead purposes.
     *
     * @exception IOException if <code>advance()</code> does
     * @see Lexer#advance()
     */
    public void init() throws IOException
    {
        // set up the keyword table
        this.keywords.put("strict", new Integer(Symbols.STRICT));
        this.keywords.put("strictdigraph", new Integer(Symbols.STRICTDIGRAPH));
        this.keywords.put("strictgraph", new Integer(Symbols.STRICTGRAPH));
        this.keywords.put("digraph", new Integer(Symbols.DIGRAPH));
        this.keywords.put("graph", new Integer(Symbols.GRAPH));
        this.keywords.put("subgraph", new Integer(Symbols.SUBGRAPH));
        this.keywords.put("node", new Integer(Symbols.NODE));
        this.keywords.put("edge", new Integer(Symbols.EDGE));
        this.keywords.put("--", new Integer(Symbols.ND_EDGE_OP));
        this.keywords.put("->", new Integer(Symbols.D_EDGE_OP));

        // set up the table of single character symbols
        this.char_symbols.put(new Integer(';'), new Integer(Symbols.SEMI));
        this.char_symbols.put(new Integer(','), new Integer(Symbols.COMMA));
        this.char_symbols.put(new Integer('{'), new Integer(Symbols.LCUR));
        this.char_symbols.put(new Integer('}'), new Integer(Symbols.RCUR));
        this.char_symbols.put(new Integer('['), new Integer(Symbols.LBR));
        this.char_symbols.put(new Integer(']'), new Integer(Symbols.RBR));
        this.char_symbols.put(new Integer('='), new Integer(Symbols.EQUAL));
        this.char_symbols.put(new Integer(':'), new Integer(Symbols.COLON));

        // read two characters of lookahead
        advance();
        advance();
    }

    /**
     * Advance the scanner one character in the input stream. This moves next_char2 to next_char and then reads a new
     * next_char2.
     *
     * @exception IOException whenever a problem reading from <code>input</code> is encountered
     */
    public void advance() throws IOException
    {
        if (this.retreated) {
            this.retreated = false;
            final int tmp_char = this.old_char;
            this.old_char = this.next_char;
            this.next_char = this.next_char2;
            this.next_char2 = tmp_char;
        } else {
            this.old_char = this.next_char;
            this.next_char = this.next_char2;
            if (this.next_char == EOF_CHAR) {
                this.next_char2 = EOF_CHAR;
            } else {
                this.next_char2 = this.inReader.read();
            }
        }

        /*
         * want to ignore a new-line if preceeding character is a backslash
         */
        if (this.next_char == '\\' && (this.next_char2 == '\n' || this.next_char2 == '\r')) {
            this.next_char = this.next_char2;
            this.next_char2 = this.inReader.read();
            if (this.next_char == '\r' && this.next_char2 == '\n') {
                this.next_char = this.next_char2;
                this.next_char2 = this.inReader.read();
            }
            this.next_char = this.next_char2;
            this.next_char2 = this.inReader.read();
        }

        /*
         * want to treat '\r' or '\n' or '\r''\n' as end-of-line, but in all cases return only '\n'
         */
        if (this.next_char == '\r') {
            if (this.next_char2 == '\n') {
                this.next_char2 = this.inReader.read();
            }
            this.next_char = '\n';
        }
        // count this
        if (this.old_char == '\n') {
            this.current_line++;
            this.old_position = this.current_position;
            this.current_position = 1;
        } else {
            this.current_position++;
        }
    }

    private void retreat()
    {
        if (this.retreated) {
            return;
        }
        this.retreated = true;
        if (this.old_char == '\n') {
            this.current_line--;
            this.current_position = this.old_position;
        } else {
            this.current_position--;
        }
        final int tmp_char = this.next_char2;
        this.next_char2 = this.next_char;
        this.next_char = this.old_char;
        this.old_char = tmp_char;
    }

    /**
     * Emit an error message. The message will be marked with both the current line number and the position in the line.
     * Error messages are printed on print stream passed to Lexer (if any) and a GraphParserException is thrown.
     *
     * @param message the message to print.
     */
    private void emit_error(final String message)
    {
        final String output = "Lexer" + getLocation() + ": " + message;
        if (this.errWriter != null) {
            this.errWriter.println("ERROR: " + output);
        }
        this.error_count++;
        throw new GraphParserException(output);
    }

    /**
     * Get the current location in the form "[line_number(character_offser)]".
     *
     * @return info about the current position in the input
     */
    public String getLocation()
    {
        return "[" + this.current_line + "(" + this.current_position + ")]";
    }

    /**
     * Emit a warning message. The message will be marked with both the current line number and the position in the
     * line. Messages are printed on print stream passed to Lexer (if any).
     *
     * @param message the message to print.
     */
    private void emit_warn(final String message)
    {
        if (this.errWriter != null) {
            this.errWriter.println("WARNING: Lexer" + getLocation() + ": " + message);
        }
        this.warning_count++;
    }

    /**
     * Check if character is a valid id character;
     *
     * @param ch the character in question.
     */
    public static boolean id_char(final int ch)
    {
        return (Lexer.id_char((char) ch));
    }

    /**
     * Check if character is a valid id character;
     *
     * @param ch the character in question.
     */
    public static boolean id_char(final char ch)
    {
        return ((Character.isJavaIdentifierStart(ch) && Character.getType(ch) != Character.CURRENCY_SYMBOL) || Character
            .isDigit(ch) || ch == '.');
    }

    public static boolean numeral_char(final int ch) {
      return (Lexer.numeral_char((char) ch));
    }

    public static boolean numeral_char(final char ch) {
      return Character.isDigit(ch);
    }

    /**
     * Try to look up a single character symbol, returns -1 for not found.
     *
     * @param ch the character in question.
     */
    private int find_single_char(final int ch)
    {
        Integer result;

        result = this.char_symbols.get(new Integer((char) ch));
        if (result == null) {
            return -1;
        } else {
            return result.intValue();
        }
    }

    /**
     * Handle swallowing up a comment. Both old style C and new style C++ comments are handled.
     */
    private void swallow_comment() throws IOException
    {
        // next_char == '/' at this point.

        // Is it a traditional comment?
        if (this.next_char2 == '*') {
            // swallow the opener
            advance();
            advance();

            // swallow the comment until end of comment or EOF
            for (;;) {
                // if its EOF we have an error
                if (this.next_char == EOF_CHAR) {
                    emit_error("Specification file ends inside a comment");
                    return;
                }
                // if we can see the closer we are done
                if (this.next_char == '*' && this.next_char2 == '/') {
                    advance();
                    advance();
                    return;
                }
                // otherwise swallow char and move on
                advance();
            }
        }
        // is its a new style comment
        if (this.next_char2 == '/') {

            // swallow the opener
            advance();
            advance();

            // swallow to '\n', '\f', or EOF
            while (this.next_char != '\n' && this.next_char != '\f' && this.next_char != EOF_CHAR) {
                advance();
            }

            return;

        }
        // shouldn't get here, but... if we get here we have an error
        emit_error("Malformed comment in specification -- ignored");
        advance();
    }

    /**
     * Swallow up a quote string. Quote strings begin with a double quote and include all characters up to the first
     * occurrence of another double quote (there is no way to include a double quote inside a quote string). The routine
     * returns a Symbol object suitable for return by the scanner.
     */
    private Symbol do_quote_string() throws IOException
    {
        String result_str;

        // at this point we have lookahead of a double quote -- swallow that
        advance();

        synchronized (this.cmnstrbuf) {
            this.cmnstrbuf.delete(0, this.cmnstrbuf.length()); // faster than cmnstrbuf.setLength(0)!
            // save chars until we see a double quote
            while (!(this.next_char == '"')) {
                // skip line break
                if (this.next_char == '\\' && this.next_char2 == '"') {
                    advance();
                }
                // if we have run off the end issue a message and break out of loop
                if (this.next_char == EOF_CHAR) {
                    emit_error("Specification file ends inside a code string");
                    break;
                }
                // otherwise record the char and move on
                this.cmnstrbuf.append(new Character((char) this.next_char));
                advance();
            }

            result_str = this.cmnstrbuf.toString();
        }

        // advance past the closing double quote and build a return Symbol
        advance();
        this.haveId = true;
        return new Symbol(Symbols.ATOM, result_str);
    }

    /**
     * Swallow up an html-like string.  Html-like strings begin with a '<'
     * and include all characters up to the first matching occurrence of a '>'
     * The routine returns a Symbol object suitable for return by the scanner.
     */
    private Symbol do_html_string() throws IOException
    {
        String result_str;
        int angles = 0;

        synchronized (this.cmnstrbuf) {
            this.cmnstrbuf.delete(0, this.cmnstrbuf.length()); // faster than cmnstrbuf.setLength(0)!
            // save chars until we see a double quote
            do {
                if (this.next_char == EOF_CHAR) {
                    emit_error("Specification file ends inside an html string");
                    break;
                }

                if (this.next_char == '<') {
                    angles++;
                } else if (this.next_char == '>') {
                    angles--;
                }

                this.cmnstrbuf.append(new Character((char) this.next_char));
                advance();
            } while (angles > 0);

            result_str = this.cmnstrbuf.toString();
        }

        // advance past the closing double quote and build a return Symbol
        advance();
        this.haveId = true;
        return new Symbol(Symbols.ATOM, result_str);
    }

    /**
     * Process an identifier. Identifiers begin with a letter, underscore, or dollar sign, which is followed by zero or
     * more letters, numbers, underscores or dollar signs. This routine returns an Symbol suitable for return by the
     * scanner.
     */
    private Symbol do_id() throws IOException
    {
        String result_str;
        Integer keyword_num;
        final char buffer[] = new char[1];

        // next_char holds first character of id
        buffer[0] = (char) this.next_char;

        synchronized (this.cmnstrbuf) {
            this.cmnstrbuf.delete(0, this.cmnstrbuf.length()); // faster than cmnstrbuf.setLength(0)!
            this.cmnstrbuf.append(buffer, 0, 1);
            advance();

            // collect up characters while they fit in id
            while (id_char(this.next_char)) {
                buffer[0] = (char) this.next_char;
                this.cmnstrbuf.append(buffer, 0, 1);
                advance();
            }
            // extract a string and try to look it up as a keyword
            result_str = this.cmnstrbuf.toString();
        }

        keyword_num = this.keywords.get(result_str);

        // if we found something, return that keyword
        if (keyword_num != null) {
            this.haveId = false;
            return new Symbol(keyword_num.intValue());
        }

        // otherwise build and return an id Symbol with an attached string
        this.haveId = true;
        return new Symbol(Symbols.ATOM, result_str);
    }

    /**
     * Add a number of consecutive digits to the buffer.
     */
    private void do_digits() throws IOException
    {

        final char buffer[] = new char[1];

        while (numeral_char(this.next_char)) {
            buffer[0] = (char) this.next_char;
            this.cmnstrbuf.append(buffer, 0, 1);
            advance();
        }
    }

    /**
     * Add the fractional part of a numeral to the buffer.
     */
    private void do_fractional() throws IOException
    {
        do_digits();
    }

    /**
     * Add the integer and fractional part of a numeral to the buffer.
     */
    private void do_integer() throws IOException
    {

        // Current char is a digit, consume it and consecutive digits.
        do_digits();

        final char buffer[] = new char[1];

        if (this.next_char == '.') {
            // There is a fractional part. Add the decimal point to the buffer.
            buffer[0] = (char) this.next_char;
            this.cmnstrbuf.append(buffer, 0, 1);
            advance();
            // Add the fractional part to the buffer.
            do_fractional();
        }
    }

    /**
     * Add a negative numeral to the buffer.
     */
    private void do_negative() throws IOException
    {

        final char buffer[] = new char[1];

        if (this.next_char == '.') {
            // This is a negative numeral without an integer part.
            // Add the decimal point to the buffer.
            buffer[0] = (char) this.next_char;
            this.cmnstrbuf.append(buffer, 0, 1);
            advance();
            // Add the fractional part to the buffer.
            do_fractional();
        } else {
            // This negative numeral have an integer part; add it to the buffer.
            do_integer();
        }

    }

    /**
     * Process a numeral. [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? )
     */
    private Symbol do_numeral() throws IOException
    {
        String result_str;
        Integer keyword_num;
        final char buffer[] = new char[1];

        // next_char holds first character of id
        buffer[0] = (char) this.next_char;

        synchronized (this.cmnstrbuf) {
            this.cmnstrbuf.delete(0, this.cmnstrbuf.length()); // faster than cmnstrbuf.setLength(0)!
            this.cmnstrbuf.append(buffer, 0, 1);

            if (this.next_char == '-') {
                advance();
                do_negative();
            } else if (this.next_char == '.') {
                advance();
                do_fractional();
            } else {
                advance();
                do_integer();
            }

            // extract a string and try to look it up as a keyword
            result_str = this.cmnstrbuf.toString();
        }

        keyword_num = this.keywords.get(result_str);

        // if we found something, return that keyword
        if (keyword_num != null) {
            this.haveId = false;
            return new Symbol(keyword_num.intValue());
        }

        // otherwise build and return an id Symbol with an attached string
        this.haveId = true;
        return new Symbol(Symbols.ATOM, result_str);
    }

    /**
     * The actual routine to return one Symbol. This is normally called from next_token(), but for debugging purposes
     * can be called indirectly from debug_next_token().
     */
    private Symbol real_next_token() throws IOException
    {
        int sym_num;

        for (;;) {
            // look for white space
            if (this.next_char == ' ' || this.next_char == '\t' || this.next_char == '\n' ||
                this.next_char == '\f') {

                // advance past it and try the next character
                advance();
                continue;
            }

            // look for edge operator
            if (this.next_char == '-') {
                if (this.next_char2 == '>') {
                    advance();
                    advance();
                    this.haveId = false;
                    return new Symbol(Symbols.D_EDGE_OP);
                } else if (this.next_char2 == '-') {
                    advance();
                    advance();
                    this.haveId = false;
                    return new Symbol(Symbols.ND_EDGE_OP);
                }
            }

            // look for a single character symbol
            sym_num = find_single_char(this.next_char);
            if (sym_num != -1) {
                if (sym_num == Symbols.LCUR && !this.haveId) {
                    final Symbol result = new Symbol(Symbols.SUBGRAPH);
                    this.haveId = true;
                    retreat();
                    return result;
                }

                // found one -- advance past it and return a Symbol for it
                advance();
                this.haveId = false;
                return new Symbol(sym_num);
            }

            // look for quoted string
            if (this.next_char == '"') {
                return do_quote_string();
            }

            // look for html-like string
            if (this.next_char == '<') {
                return do_html_string();
            }

            // look for a comment
            if (this.next_char == '/' && (this.next_char2 == '*' || this.next_char2 == '/')) {
                // swallow then continue the scan
                swallow_comment();
                continue;
            }

            // look for an id or keyword
            if (id_char(this.next_char)) {
                return do_id();
            }

            // look for a numeral
            if (numeral_char(this.next_char) || this.next_char == '-' || this.next_char == '.') {
                return do_numeral();
            }

            // look for EOF
            if (this.next_char == EOF_CHAR) {
                this.haveId = false;
                return new Symbol(Symbols.EOF);
            }

            // if we get here, we have an unrecognized character
            emit_warn("Unrecognized character '" +
                new Character((char) this.next_char) + "'(" + this.next_char +
                ") -- ignored");

            // advance past it
            advance();
        }
    }

    /**
     * Return one Symbol. This method is the main external interface to the scanner. It consumes sufficient characters
     * to determine the next input Symbol and returns it.
     *
     * @exception IOException if <code>advance()</code> does
     */
    public Symbol next_token(final int debugLevel) throws IOException
    {
        if (debugLevel > 0) {
            final Symbol result = real_next_token();
            if (this.errWriter != null && debugLevel >= 5) {
                this.errWriter.println("DEBUG: Lexer: next_token() => " + result.sym);
            }
            return result;
        } else {
            return real_next_token();
        }
    }
}
