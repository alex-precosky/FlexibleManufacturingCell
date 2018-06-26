import java.awt.*;

class Carrier extends GrObject
{
  Sensor magState;
  PortalArm pa;

  public double relz = 0.0;
  public double maxz = 50;
  public double speed = 0.8;
  //  static final int pptz = maxz/4;
  static Color col = new Color(200,200,100);

  // hoch = true , runter = false
  public boolean updown = true;

  // x,y Definieren die obere linke Ecke
  public Carrier(int x,int y,int w,int h,double height,double ppm,PortalArm p)
  {
    pa = p;
    speed = ppm;
    maxz = height;

    int mxp[] = {0,w,w,0};
    int myp[] = {0,0,h,h};

    Polygon s[]={new Polygon(mxp,myp,4)};
    setShape(s);

    super.moveTo(x,y);
    magState = new Sensor(x,y-Sensor.Height);
  }

  public void paint(Graphics g)
  {
    g.setColor(col);
    g.fillPolygon(shape[0]);

    magState.state = pa.magnet;
    magState.paint(g);
  }

  public void repaint(Graphics g)
  {
    paint(g);
  }

  public void restore(Graphics g)
  {
    g.setColor(GrObject.cback);
    g.fillPolygon(shape[0]);

    magState.restore(g);
  }

  
  public void moveTo(int x,int y)
  {
    super.moveTo(x,y);
    magState.moveTo(x,y);
  }

  public void step(double dt)
  {
    // move carrier up/down
    double relMovement = dt*speed;
    if (updown)
    {
      relz -= relMovement;
      if (relz < 0)
	relz = 0;
    }
    else
    {
      relz += relMovement;
      if (relz > maxz) 
	relz = maxz;
    }
  }

  
}







public class PortalArm extends Item
{
  public final static int msx = 10;
  //Geschwindigkeit in Pixel pro Takt
  public int epsilon = 3;

  // size of the height bar;
  public int pHeight = 36;

  public WorkItem container = null;  //new WorkItem(new Code());
  public boolean magnet = false;

  //Position
  public int relx;
  //Zielposition
  public int destx;

  //Gueltiger Bereich
  public int maxx;
  public int minx;
  public int maxy;
  public int miny;


  //Magnet Position auf der Laufkatze
  public int rely;
  //Zielposition des Magneten
  public int desty;
  
  Carrier cr;  
  PositionMatrix pm;
  
  boolean up;
  boolean down;

  boolean moveX = false;
  boolean moveY = false;
  int sensorIdX = 0;
  int sensorIdY = 0;

  int epsilonX = 3;
  int epsilonY = 3;

  public PortalArm(int x,int y,int w,int h,PositionMatrix p,double ppm,double height,double cppm,SimState state,int Id)
  {
    id = Id;
    pm = p;

    speed = ppm;

    int px[] = {0,w,w,0};
    int py[] = {0,0,h,h};


    Polygon s[]={new Polygon(px,py,4)};
    setShape(s);

    

    miny = 0;
    maxy = h;

    magnet = state.PortalMagnet[id];
    destx = pm.getXPos(state.PortalPosSet[id][0]);
    desty = pm.getYPos(state.PortalPosSet[id][1]);
    relx = destx;
    rely = desty;
    sensorIdX = pm.getXIndexEpsilon(relx,epsilonX);
    sensorIdY = pm.getYIndexEpsilon(rely,epsilonY);

    cr = new Carrier(x+relx-msx/2,y+rely,w+msx,msx*2,height,cppm,this);

    super.moveTo(x+relx,y);

    up = true;
    down = false;
    cr.updown = up;

  }


  public void moveTo(int x,int y)
  {
    super.moveTo(x,y);
    cr.moveTo(x,y);
    if (container != null)
      container.moveTo(x,y);
  }

  public void paint(Graphics g)
  {
    if (container != null)
      {
	if (!down)
          container.setColor(Color.orange);
        else
	  container.setColor(Color.cyan);

	container.paint(g);
      }

    g.setColor(GrObject.cgrn);
    g.fillPolygon(shape[0]);

    cr.paint(g);
    
    g.setColor(GrObject.cred);
    g.fillRect(posx+3,posy+2,4,(int)((cr.relz/cr.maxz)*pHeight));
    g.setColor(GrObject.cblck);
    g.drawRect(posx+2,posy+2,5,pHeight);

    
  }

  public void repaint(Graphics g)
  {
    paint(g);
  }

  public void restore(Graphics g)
  {
    if (container != null)
      container.restore(g);

    cr.restore(g);
    g.setColor(GrObject.cback);
    g.fillPolygon(shape[0]);
    
  }

  public void addWorkItem(WorkItem wi) throws SimErrorException
  {
    if (wi == null)
      return;

    if (container != null)
      throw new SimErrorException("Overlaying WorkItem");

    container = wi;
    wi.container(this);
    
  }

  public WorkItem getWorkItem()
  {
    WorkItem item = container;
    item.resetColor();
    container = null;
    return item;
  }


  public boolean isEmpty()
  {
    if (container == null)
      return true;

    return false;
  }

  public void getState(SimState s)
  {
    s.PortalMagnet[id] = magnet;

    s.PortalPosGet[id][0] = sensorIdX;
    s.PortalPosGet[id][1] = sensorIdY;

    s.PortalMagnetUp[id] = up;
    s.PortalMagnetDown[id] = down;
  }

  public void setState(SimState s)
  {
    magnet = s.PortalMagnet[id];
    destx = pm.getXPos(s.PortalPosSet[id][0]);
    desty = pm.getYPos(s.PortalPosSet[id][1]);
    cr.updown = s.PortalMagnetUpDownSet[id];
  }

  public void advance(double dt) throws SimErrorException
  {

    //Ubergabe des Werkstueckes
    //Wenn Magnet unten und an und kein Teil am Magneten 
    //dann uebernehme ein Teil von dem unten liegenden Geraet
    if (magnet && down && (container == null))
    {
      Item item = pm.getItem(pm.getXIndex(relx),pm.getYIndex(rely));
      if (item != null)
      {
        WorkItem witem = item.getWorkItem();
        addWorkItem(witem);
        if ((witem != null)&&(item instanceof TransBelt))
          witem.startTimer();

        
      }
    }

    // Wenn Magnet aus und Magnet haelt Teil dann ubergebe
    // Teil an Geraet oder Umgebung
    if (!magnet && container != null)
    {
      Item item = pm.getItem(pm.getXIndex(relx),pm.getYIndex(rely));
      if (item != null)
	{
          item.addWorkItem(getWorkItem());
	  if (!down)
	    throw new SimErrorException("Workitem released in upper position");

	}
      else
        throw new SimErrorException("Workitem released on floor",container,this);
    }

    cr.step(dt);
    if (cr.relz <= 0)
      up = true;
    else 
      up = false;

    if (cr.relz >= cr.maxz)
      down = true;
    else 
      down = false;


    int range = (int)(speed*dt);
    //Bewegung des Armes
    int delta = relx-destx;
      
    if (delta < 0)
    {
      //LK links vom Ziel
      delta = -delta;
      if (delta >= range)
      {
        //Bewege nach rechts
        if (!up)
	  {
	    destx = relx;
	    throw new SimErrorException("Portal"+id+" can't move in lower Position",null,this);
	  }
        relx+=range;
        moveTo(range,0);
        moveX = true;

	sensorIdX = pm.getXIndexEpsilon(relx,epsilonX);

      }
      else
      {
	// Aufgenaue Position setzen
	if (delta != 0)
	{
	  relx+=delta;
	  moveTo(delta,0);
	}

	sensorIdX = pm.getXIndexEpsilon(relx,epsilonX);

	moveX = false;
      }
    }
    else
    {
      //LK rechts vom Ziel
      if (delta >= range)
      {
        //Bewege nach links
        if (!up)
	  {
	    destx = relx;
	    throw new SimErrorException("Portal"+id+" can't move in lower Position",null,this);
	  }
        relx-=range;
        moveTo(-range,0);
        moveX = true;

	sensorIdX = pm.getXIndexEpsilon(relx,epsilonX);

      }
      else
      {
	// Aufgenaue Position setzen
	if (delta != 0)
	{
	  relx-=delta;
	  moveTo(-delta,0);
	}
	sensorIdX = pm.getXIndexEpsilon(relx,epsilonX);

	moveX = false;
      }

    }

    //Bewegung des Magneten
    delta = rely-desty;
    if (delta < 0)
    {
      //Magnet ueber Ziel
      delta = -delta;
      if (delta >= range)
      {
        //Bewege nach unten
        if (!up)
	  {
	    desty = rely;
	    throw new SimErrorException("Portal"+id+" can't move in lower Position",null,this);
	  }
        rely+=range;
        cr.moveTo(0,range);
        if (container != null)
    	  container.moveTo(0,range);

	sensorIdY = pm.getYIndexEpsilon(rely,epsilonY);

	moveY = true;
      }      
      else
      {
	// Aufgenaue Position setzen
	if (delta != 0)
	{
	  rely+=delta;
	  moveTo(0,delta);
	}

	sensorIdY = pm.getYIndexEpsilon(rely,epsilonY);
	
	moveY = false;
      }
    }
    else
    {
      //Magnet unter Ziel
      if (delta >= epsilon)
      {
        //Bewege nach oben
        if (!up)
	{
	  desty = rely;
	  throw new SimErrorException("Portal"+id+" can't move in lower Position",null,this);
	}

        rely-=range;
        cr.moveTo(0,-range);
        if (container != null)
    	  container.moveTo(0,-range);

	sensorIdY = pm.getYIndexEpsilon(rely,epsilonY);

	moveY = true;

      }      
      else
      {
	// Aufgenaue Position setzen
	if (delta != 0)
	{
	  rely-=delta;
	  moveTo(0,-delta);
	}

	sensorIdY = pm.getYIndexEpsilon(rely,epsilonY);
	
	moveY = false;
      }

      
    }

    if (container != null)
      container.advance(dt);

  }


}







