import java.awt.*;
import java.io.*;
import java.util.*;


class Anim extends Thread
{
  public static final int GrSizeX = 780;
  public static final int GrSizeY = 300;
  public int delay;
  public int yOffset;

  Frame GrWindow;
  Graphics Screen;
  Graphics GrScreen;
  Image ScrBuffer;
  Toolkit toolkit;

  GrObject elements[];
  int maxElements;

  public Graphics init(GrObject elements_[],int FrameDelay,int yOffset_)
  {
    int i;
    delay = FrameDelay;
    yOffset = yOffset_;
    maxElements = elements_.length;
    
    elements = new GrObject[maxElements];
    for(i=0;i<maxElements;i++)
      elements[i] = elements_[i];
    

    GrWindow = new Frame("Simulation "+JavaSim.Version);
    GrWindow.setBackground(Color.white);
    GrWindow.resize(GrSizeX,GrSizeY);
    GrWindow.show();

    GrScreen = GrWindow.getGraphics();
    toolkit = GrWindow.getToolkit();

    ScrBuffer = GrWindow.createImage(GrSizeX,GrSizeY);
    Screen = ScrBuffer.getGraphics();

    for(i=0;i<maxElements;i++)
    {
      //Zeichnen
      elements[i].paint(Screen);
    }

    GrScreen.drawImage(ScrBuffer,0,yOffset,null);

    setPriority(Thread.NORM_PRIORITY-(Thread.NORM_PRIORITY-Thread.MIN_PRIORITY)/6);
    return Screen;
  }


  public void paint()
  {
    Screen.clearRect(0,yOffset,GrSizeX,GrSizeY-yOffset);

    int i;
    for(i=0;i<maxElements;i++)
      {
        //Zeichnen
        elements[i].paint(Screen);
      }
    
    GrScreen.drawImage(ScrBuffer,0,yOffset,null);
  }

  public void run()
  {
    int i;

    while (true)
    {
      //DELAY

      Screen.clearRect(0,yOffset,GrSizeX,GrSizeY-yOffset);
      try
      {
	sleep(delay);
      }
      catch  (InterruptedException e)
      {
	return;
      }
      //DELAY
      for(i=0;i<maxElements;i++)
	{
	  //Zeichnen
	  elements[i].paint(Screen);
	}
      
      GrScreen.drawImage(ScrBuffer,0,yOffset,null);
      toolkit.sync();
    }
  }

}


public class JavaSim extends Frame implements Runnable
{
  static final int HSize = 360;
  static final int VSize = 360;
  public static final String Version = "1.51";
  static final int maxSize = 65536;
  static final int MAXCODES = 10;
  static int SYNCTURNDELAY = 50;



  boolean RunningFlag;

  Graphics Screen;

  MenuBar MBar;
  TextArea ConOut;
  Thread myThread;
  Anim GrOut;

  int pos;
  int transIx;
  int trayIx;
  int relTurn;
  int maxTurn;

  Interface IO;
  static boolean mode = Interface.ASYNC;

  CodeFrame currentCode;
  CodeFrame codes[];

  SetupDialog setupDlg;

  Frame debugView;
  boolean debugMode;
  TextArea DebugOut;

  Item items[];
  int maxItems = 12; //Anzahl der Elemente in der Simulation
  Vector testVector = null;
  boolean testing = false;
  int testIndex = 0;
  long testTime = 0;

  SimState state;

  int turnDelay;
  long simTime;
  

// -------------------------------------------------------------
// Konstruktor
// -------------------------------------------------------------
  public JavaSim()
  {
    IO = new Interface();
    GrOut = new Anim();
  
    RunningFlag	= true;
    myThread = null;


    init();
    show();
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void init()
  {
    setBackground(Color.white);
    setTitle("JavaSim "+Version);

    resize(HSize,VSize);

    initWorld();
    initMenu();
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  private void initWorld()
  {
    config c = new config();
    int ipos = 0;
    PositionMatrix pm = new PositionMatrix(c.PositionMatrixRow,c.PositionMatrixCol);

    maxItems = c.TransportBeltCount+c.WorkStationCount+c.TrayBeltCount+1;
    items = new	Item[maxItems];

    //#Zufuehrbaender,#Arbeitsstationen,#Ablagebaender,#Portallaufkatzen
    state = new SimState(c.TransportBeltCount,c.WorkStationCount,c.TrayBeltCount,c.PortalArmCount);

    //Simulationselemente erzeugen und positionieren

    //Zufuehrbaender
    //TransBelt(posx,posy,Laenge,Breite,ppm,Id)
    items[ipos] = new TransBelt(10,110,c.TransportBeltLength+20,70,c.TransportBeltSpeed,1);
    transIx = ipos;
    pm.setItem(1,2,items[ipos]);
    ipos++;


    for (int index = 0;index < c.WorkStationCount;index++)
      {
    	int ix,iy,px,py;

	    //Bearbeitungs-Stationen
    	//WorkStation(posx,posy,Laenge,Breite,Bearbeitszeit,Id)
        ix = c.WorkStationPosition[index<<1];;
        iy = c.WorkStationPosition[(index<<1)+1];
        px = c.PositionMatrixRow[ix-1]+c.PortalXOffset-40;
        py = c.PositionMatrixCol[iy-1]+c.PortalYOffset-30;
    	items[ipos] = new WorkStation(px,py,80,60,c.WorkStationTime[index],index+1);
	    pm.setItem(ix,iy,items[ipos]);
        ipos++;
      }

    //Initialzustand setzen
    state.PortalPosSet[1][0] = c.PortalCrane1XPos;
    state.PortalPosSet[1][1] = c.PortalCrane1YPos;
    state.PortalMagnetUpDownSet[1] = true;
    if (c.PortalArmCount>1)
      {
	state.PortalPosSet[2][0] = c.PortalCrane2XPos;
	state.PortalPosSet[2][1] = c.PortalCrane2YPos;
	state.PortalMagnetUpDownSet[2] = true;
      }

    state.PortalMagnetUpDownSet[1] = true;
    state.TrayBeltSet[1] = true;


    //Ablagebaender
    //Belt(posx,posy,Laenge,Breite,ppm,Id)
    items[ipos] = new Belt(560,110,c.TrayBeltLength+20,70,c.TrayBeltSpeed,1);
    trayIx = ipos;
    pm.setItem(8,2,items[ipos]);
    ipos++;

    //Portal mit zwei Laufkatzen
    items[ipos++] = new Portal(c.PortalXOffset,c.PortalYOffset,c.PortalLength,240,pm,c.PortalArmCount,c.PortalArmSpeed,c.PortalHeight,c.PortalUpDownSpeed,state,1);


    for(int i=0;i<maxItems;i++)
    {
      items[i].setState(state);
      //      items[i].getState(state);
    }

    turnDelay = c.SimulationAsychronUpdateDelay;
    Screen = GrOut.init(items,c.FrameUpdateDelay,20);

    //Kontrollvariablen fuer einfuegen on Teilen in die Simulation
    maxTurn = (int)((double)WorkItem.lg/(turnDelay*items[transIx].getSpeed()));
    relTurn = maxTurn + 1;

  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  private void initMenu()
  {
    config c = new config();

    Choice ch = new Choice();

    setupDlg = new SetupDialog(this,false);
    //    setupDlg.addNotify();

    codes = new CodeFrame[c.WorkItemConfigFrameCount];
    for (int i = 0;i < c.WorkItemConfigFrameCount;i++)
      {
        String name = "Item"+(i+1);
	codes[i] = new CodeFrame(name);
        ch.addItem(name);
	setupDlg.addItem(name,codes[i]);
      };

    currentCode = codes[0];

    //    setLayout(new GridLayout(0,1,3,0));
    setLayout(new BorderLayout());
    Panel p,cp;

    debugView = new Frame("Debug View");
    debugView.setLayout(new GridLayout(1,1,0,0));
    debugView.add(DebugOut = new TextArea());
    debugView.resize(VSize,HSize);
    debugMode=false;

    cp = new Panel();
    cp.setLayout(new GridLayout(0,1));

    p = new Panel();
    p.setLayout(new GridLayout(0,1));
    p.add(new Button("Insert Item"));
    cp.add(p);

    p = new Panel();
    p.add(new Label("Current WorkItemType :"));
    p.add(ch);
    cp.add(p);

    p = new Panel();
    p.setLayout(new GridLayout(0,5));
    p.add(new Button("Config Item"));
    p.add(new Button("Deposit Belt"));
    p.add(new Button("Setup Test"));
    p.add(new Button("Start Test"));
    p.add(new Button("Stop Test"));
    cp.add(p);

    add("North",cp);

    p = new Panel();
    p.setLayout(new GridLayout(0,1));
    p.add(ConOut = new TextArea());
    add("Center",p);


    MBar = new MenuBar();
    Menu m = new Menu("Interface");
    m.add(new MenuItem("Quit"));
    m.add(new MenuItem("Debug"));
    MBar.add(m);

    m =	new Menu("Command");
    m.add(new MenuItem("Insert Item"));
    m.add(new MenuItem("Config Item"));
    MBar.add(m);

    setMenuBar(MBar);
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void start()
  {
    if (myThread == null)
      {
	myThread = new Thread(this);

	// Kommunikationsthread starten
        if (mode == Interface.ASYNC)
	  {
	    IO.setPriority(Thread.MIN_PRIORITY);
	    IO.startCom(state,myThread);
	    GrOut.start();
	    myThread.setPriority(Thread.NORM_PRIORITY);
	  }
	else
	  myThread.setPriority(Thread.MIN_PRIORITY);


        //Thread zur graphische Ausgabe starten
	myThread.start();

	
      }
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void stop()
  {
    // Kill Thread
    RunningFlag	= false;

    myThread.stop();
    myThread = null;
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void destroy()
  {
    return;
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void print(String inp)
  {
    if (ConOut.getText().length() > maxSize)
      {
	ConOut.setText("");
	pos = 0;
      }

    ConOut.insertText(inp,pos);
    pos	+= inp.length();
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  private void insertItem()
  {
    if (relTurn > maxTurn)
    {
      relTurn = 0;
      try {
        items[transIx].addWorkItem(new WorkItem(currentCode.getCode()));
      }
      catch (SimErrorException e)
      {
        return;
      }

    }

    if (!mode)
    {
      GrOut.paint();
    }

  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  private void testGenerator(long dt)
  {
    testTime += dt;

    TestItem tItem = (TestItem)testVector.elementAt(testIndex);
    if (testTime > tItem.time)	
    {
      try {
        items[transIx].addWorkItem(new WorkItem(((CodeFrame)tItem.obj).getCode()));
      }
      catch (SimErrorException e)
      {
        return;
      }

      relTurn = 0;
      testTime = 0;
      testIndex++;
      if (testIndex == testVector.size())
        testIndex = 0;
    }

    if (!mode)
    {
      GrOut.paint();
    }

  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void run()
  {
    int i = 0;
    long time = SYNCTURNDELAY;
    double dt = (double)time;
    long oldTime = System.currentTimeMillis();

    while (RunningFlag)
    {

	if (mode || !config.DirectSync)
	{
	    //DELAY
    		try
	      	{
			myThread.sleep(turnDelay);
    		}
	      	catch  (InterruptedException e)
      		{
			return;
	      	}
      	//DELAY
	}

	if (mode)
	{
		time = (System.currentTimeMillis() - oldTime);
		dt = (double)time;
		oldTime = System.currentTimeMillis();
	}

      //Neuen Status setzen
      for(i=0;i<maxItems;i++)
      {
        items[i].setState(state);
      }


	if (testing)
	{
		testGenerator(time);
	}
      

      //Simulation einen Takt weiter schalten
      for(i=0;i<maxItems;i++)
      {
        try 
        {
          items[i].advance(dt);
        }
        catch (SimErrorException e)
        {
          if (e.item != null)
            e.sender.getWorkItem();

          //Fehlermeldungen !!!
          if ((e.sender != items[trayIx]))
    	    print("Error : "+e.error+"\n");

        }
        catch (CollisionException e)
        {
          print("Error : Collision\n");
        }

        //Status ermitteln
        items[i].getState(state);

      }

      //Code ausgeben
      Code c = state.CodeState[1];
      if (c != null)
      {
        print("Code scanned:"+"\n#stations: "+c.stationCount+"\nwhole time: "+c.tg+"ms\n");
        if (c.orderIndicator)
          print("order : yes\n");
	  	else
          print("order : no\n");

	    for (i = 0;i < c.stationCount;i++)
	      print("workstation. Nr. "+c.stationId[i]+" : minT :"+c.min[i]+"ms maxT :"+c.max[i]+"ms\n");

        IO.setCodeStateAsync(c);
      }
      
      relTurn+=1;

      if (!mode)
      {
	    GrOut.paint();
	    //DELAY
	    try 
	    {
	      IO.writeStatus(state);
	      IO.readStatus(state);
	    } 
	    catch (IOException e)
	    {
	      System.out.println("IOError");
	    }
      }

      

    }


  }

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

    if (item.equals("Insert Item"))
      {
        insertItem();
	return;
      }

    if (item.equals("Deposit Belt"))
      {
        //Ablageband ein/aus
        if (state.TrayBeltGet[1])
          state.TrayBeltSet[1] = false;
        else
          state.TrayBeltSet[1] = true;

	return;
      }

    if (item.equals("Config Item"))
      {
        if (currentCode.isShowing())
          currentCode.hide();
        else
          currentCode.show();

	return;
      }

    if (item.equals("Debug"))
      {
        if (debugView.isShowing())
	{
          debugView.hide();
	  IO.debug(false,DebugOut);
	  debugMode=false;
	}
        else
	{
          debugView.show();
	  IO.debug(true,DebugOut);
	  debugMode=true;
	}
	return;
      }

    if (item.equals("Setup Test"))
      {
        if (!setupDlg.isShowing())
          setupDlg.show();
//	  System.out.println("Test");
      }

    if (item.equals("Start Test"))
      {

	Vector test = setupDlg.getTestVector();

	if (test == null)
	{
	  if (!setupDlg.isShowing())
	    setupDlg.show();
	}
	else
	{
	  testIndex = 0;
	  testVector = test;
	  testing = true;
	}

	return;
      }

    if (item.equals("Stop Test"))
      {
	testing = false;
	return;
      }



    if (item.equals("Quit"))
      {
	stop();
	System.exit(0);
	return;
      }

  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public boolean handleEvent(Event evt)
  {
    switch (evt.id)
      {
	case Event.WINDOW_DESTROY:
	  {
	     stop();
	     System.exit(0);
	     return true;
	  }

	case Event.ACTION_EVENT:
	  {
	    String target = evt.target.getClass().getName();

	    String label;

	    if (evt.target instanceof MenuItem)
		{
		  label	= ((MenuItem)(evt.target)).getLabel();
		  handleMenu(label,evt.target);
		}

	    if (evt.target instanceof Button)
		{
		  label	= ((Button)(evt.target)).getLabel();
		  handleMenu(label,evt.target);
		}

            if (evt.target instanceof Choice)
            {
              int i = ((Choice)evt.target).getSelectedIndex();
              currentCode = codes[i];
            }

	    return true;
	  }

	default:
	  return false;

    }
  }



// -------------------------------------------------------------
// -------------------------------------------------------------
  public static	void main(String args[])
  {
    if (args.length >= 1)
    {
      if (args[0].startsWith("-sync")||args[0].startsWith("-SYNC"))
	{
	  mode = Interface.SYNC;
	  if (args.length >= 2)
            SYNCTURNDELAY = Integer.parseInt(args[1]);
	}
    }

    JavaSim sim	= new JavaSim();
    sim.start();

  }

}

