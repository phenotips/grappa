package att.grappa.util;

import java.util.Enumeration;
import java.util.Iterator;

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
