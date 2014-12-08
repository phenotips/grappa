package att.grappa.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Presents an Iterator as an Enumeration.
 *
 * @param <E> the type of data in the Iterator/Enumeration
 * @version $Id$
 */
public class IteratorEnumeration<E> implements Enumeration<E>
{
    private final Iterator<E> iterator;

    public IteratorEnumeration(Iterator<E> iterator)
    {
        this.iterator = iterator;
    }

    public boolean hasMoreElements()
    {
        return this.iterator.hasNext();
    }

    public E nextElement()
    {
        return this.iterator.next();
    }
}
