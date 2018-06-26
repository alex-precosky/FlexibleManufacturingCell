public class SimErrorException extends Exception
{
  public Item sender;
  public String error;
  public Object item = null;

  SimErrorException()
  {
    sender = null;
    error = "Error";
  }

  SimErrorException(String e)
  {
    sender = null;
    error = e;
  }

  SimErrorException(String e,Object o)
  {
    sender = null;
    error = e;
    item = o;
  }

  SimErrorException(String e,Object o,Item Sender)
  {
    sender = Sender;
    error = e;
    item = o;
  }
  
}
