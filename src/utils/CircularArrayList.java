package utils;

import java.util.ArrayList;

/**
 * @descr classe utilizzata per avere une gestion di array circolare semplice
 * 
 * */
public class CircularArrayList<E> extends ArrayList<E>
{
    private static final long serialVersionUID = 1L;

    /**
     * @descr get che permette il wrap around quando si chiede l'ultimo indice
     * @param int $index
     * */
    public E get(int index)
    {
        if (index == -1)
        {
            index = size()-1;
        }

        else if (index == size())
        {
            index = 0;
        }

        return super.get(index);
    }
}