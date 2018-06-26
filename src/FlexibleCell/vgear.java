 import java.awt.*;
import java.io.*;

class IO
{
  public final static DataInputStream in = new DataInputStream(System.in);
  public final static DataOutputStream out = new DataOutputStream(System.out);

  byte buffer[]=new byte[2048];

  public void skipInput()
  {
    try {
      System.in.read(buffer,0,System.in.available());
    }
    catch (IOException e)
      {
      }    
  }


  public String readln()
  {
    try {
      return in.readLine();
    }
    catch (IOException e)
      {
        return null;
      }
  }

  synchronized public String read()
  {
    return new String("");
  }

  synchronized public void writeln(String s)
  {
    try {
      out.writeBytes(s+"\n");
    }
    catch (IOException e)
    {
    }
  }

  synchronized public void write(String s)
  {
    try {
      out.writeBytes(s);
    }
    catch (IOException e)
    {
    }
  }

}


class transPanel extends Panel
{
  int id;
  IO io;

  public transPanel(int Id,IO Io)
  {
    id = Id;
    io = Io;
    
    Checkbox cb;
    add(cb = new Checkbox("Transport Belt"));
    add(new Button("CodeSensor"));
  }

  public boolean action(Event ev,Object arg)
  {
    if (ev.target instanceof Button)
      {
	io.writeln("[");
	io.writeln("CodeSensorOn");
	io.writeln("]");
        return true;
      }

    if (ev.target instanceof Checkbox)
      {
	io.writeln("[");
        if (((Checkbox)ev.target).getState())
	  io.writeln("FeedBeltOn");
	else
	  io.writeln("FeedBeltOff");
	io.writeln("]");
        return true;
      }

    return false;
  }

}



class workPanel extends Panel
{
  IO io;
  TextField tf[];

  workPanel(int no,IO Io)
  {
    io = Io;
    //    tf = new TextField[no+1];
    Button cb;
    Panel p;
    int i;
//    setLayout(new GridLayout(1,0,0,5));
    
    for(i = 1;i<=no;i++)
    {
      p = new Panel();
      //      p.add(tf[i] = new TextField("0",4));
      p.add(cb = new Button("WorkStation"+i));
      add(p);
    }    
  }


  public boolean action(Event ev,Object arg)
  {
    if (ev.target instanceof Button)
    {
      int ix = Integer.parseInt((((Button)ev.target).getLabel()).substring("WorkStation".length()));
      io.writeln("[");
      io.writeln("WorkStationOn"+ix); //+" "+(tf[ix].getText()).trim());
      io.writeln("]");
      return true;
    }
    return false;
  }  
}



class portPanel extends Panel
{
  int id;
  Choice ch1;
  Choice ch2;
  IO io;  

  public portPanel(int no,int px,int py,int sx,int sy,IO Io)
  {
    io = Io;
    id = no;

    int i;
    Panel p;
//    setLayout(new GridLayout(1,0,0,5));

    p = new Panel();
    p.add(new Label("Portal"+no));
    add(p);

    Panel cp = new Panel();
    cp.setLayout(new GridLayout(1,0,0,5));
    p = new Panel();
    p.add(new Label("X:"));
    ch1 = new Choice();
    for (i = 1;i<=px;i++)
      ch1.addItem("x"+i);    
    p.add(ch1);
    cp.add(p);
    ch1.select(sx-1);

    p = new Panel();
    p.add(new Label("Y:"));
    ch2 = new Choice();
    for (i = 1;i<=py;i++)
      ch2.addItem("y"+i);    
    p.add(ch2);
    cp.add(p);
    ch2.select(sy-1);

    p = new Panel();
    p.add(new Button("MoveTo"));
    cp.add(p);
    add(cp);

    Checkbox cb;
    p = new Panel();
    cb = new Checkbox("Magnet on/off");
    p.add(cb);
    add(p);

    CheckboxGroup cbg = new CheckboxGroup();

    p = new Panel();
    cb = new Checkbox("Up",cbg,true);
    p.add(cb);
    cb = new Checkbox("Down",cbg,false);
    p.add(cb);
    add(p);
    
  }

  public boolean action(Event ev,Object arg)
  {
    if (ev.target instanceof Button)
    {
      int x = ch1.getSelectedIndex() + 1;
      int y = ch2.getSelectedIndex() + 1;
      io.writeln("[");
      io.writeln("PortalX"+id+" "+x);
      io.writeln("PortalY"+id+" "+y);
      io.writeln("]");
      return true;
    }

    if (ev.target instanceof Checkbox)
    {
      String label = ((Checkbox)ev.target).getLabel();
      if ("Up".equals(label))
      {
        io.writeln("[");
        io.writeln("PortalUp"+id);
        io.writeln("]");
      }
      else
      if ("Down".equals(label))
      {
        io.writeln("[");
        io.writeln("PortalDown"+id);
        io.writeln("]");
      }
      else
      {
        io.writeln("[");

	io.write("Magnet");
        if (((Checkbox)ev.target).getState())
          io.writeln("On"+id);
        else
          io.writeln("Off"+id);

        io.writeln("]");
      }
      return true;
    }

    return false;
  }
}




public class vgear extends Frame implements Runnable
{
  IO interf;
  static Thread myThread;

// -------------------------------------------------------------
// -------------------------------------------------------------
  private void handleMenu(String item,Object id)
  {

    /*
    if (item.equals(""))
      {
	return;
      }
    */



    if (item.equals("Quit"))
      {
        myThread.stop();
        myThread = null;
	System.exit(0);
	return;
      }
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public boolean handleEvent(Event evt)
  {

    String label;
    switch (evt.id)
      {
	case Event.WINDOW_DESTROY:
	  {
             myThread.stop();
             myThread = null;
	     System.exit(0);
	     return true;
	  }

	case Event.ACTION_EVENT:
	  {
	    if (evt.target instanceof MenuItem)
		{
		  label	= ((MenuItem)(evt.target)).getLabel();
		  handleMenu(label,evt.target);
		}
	    else if (evt.target instanceof Button)
		{
		  label	= ((Button)(evt.target)).getLabel();
		  handleMenu(label,evt.target);
		}
            else if (evt.target instanceof Choice)
                {
                  int i = ((Choice)evt.target).getSelectedIndex();
                }

	    return true;
	  }

	default:
	  return false;

    }
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void initUserInterface()
  {
    setTitle("JavaSimulation - ControlPanel 1.2");
    setBackground(Color.white);
    resize(700,250);

    interf = new IO();

    setLayout(new GridLayout(0,1,0,5));
    portPanel pp1 = new portPanel(1,8,3,1,2,interf);
    add(pp1);
    portPanel pp2 = new portPanel(2,8,3,8,2,interf);
    add(pp2);

    transPanel tp = new transPanel(1,interf);
    add(tp);
    workPanel wp = new workPanel(4,interf);
    add(wp);

    MenuBar MBar = new MenuBar();
    Menu m = new Menu("Interface");
    m.add(new MenuItem("Quit"));
    MBar.add(m);

    setMenuBar(MBar);

    show();
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void run()
  {
    while (true)
      {
	interf.skipInput();
	try
	  {
	    myThread.sleep(500);
	  }
	catch  (InterruptedException e)
	  {
	    return;
	  }
      }
  }
   
// -------------------------------------------------------------
// -------------------------------------------------------------


  public static void main(String argv[])
  {
    vgear neu = new vgear();
    neu.initUserInterface();
    myThread = new Thread(neu);
    myThread.setPriority(Thread.MIN_PRIORITY);
    myThread.start();

  }
}















