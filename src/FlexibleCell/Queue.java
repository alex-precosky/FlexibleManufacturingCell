import java.util.Vector;

public class Queue 
{
  Vector queue;
  int pos = 0;

  public Queue(int size)
  {
    queue = new Vector(size,4);
  }

  public Queue()
  {
    this(10);
  }

  public void addElement(Object e)
  {
    queue.addElement(e);
  }

  public Object removeLast()
  {
    Object tmp = queue.lastElement();
    queue.removeElementAt(queue.size()-1);
    return tmp;
  }

  public Object remove()
  {
    Object tmp = queue.firstElement();
    queue.removeElementAt(0);
    return tmp;
  }

  public boolean isEmpty()
  {
    return queue.isEmpty();
  }


  public Object getFirstObj()
  {
    Object tmp = null;
    pos = queue.size()-1;

    try {
      tmp = queue.elementAt(pos);
    } 
    catch (ArrayIndexOutOfBoundsException e)
    {
      return null;
    }
    return tmp;
  }

  public Object getNextObj()
  {
    Object tmp = null;
    pos-=1;

    try {
      tmp = queue.elementAt(pos);
    } 
    catch (ArrayIndexOutOfBoundsException e)
    {
      return null;
    }
    return tmp;
  }

}

