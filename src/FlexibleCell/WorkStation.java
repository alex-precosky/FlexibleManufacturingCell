import java.awt.*;


class WorkStation extends Item
{
  final static int Type1 = 1;
  final static int Type2 = 2;

  int wix;
  int wiy;


  Sensor active;
  Sensor loaded;

  boolean ready = true;
  int type;

  long worktime;
  long currentTime;

  WorkItem container;

  public WorkStation(int x,int y,int l,int w,long wtime,int Id)
  {
    loaded = new Sensor(x,y);
    active = new Sensor(x,y+w-Sensor.Height);

    worktime = 0;
    container = null;

    id = Id;
    wix = (l-WorkItem.lg)/2;
    wiy = (w-WorkItem.wd)/2;

    if (wtime <= 0)
    {
      type = WorkStation.Type2;
      active.state = true;
    }
    else
      type = WorkStation.Type1;

    worktime = wtime;


    //Umriss
    int p1xp[]={0,l,l,0};
    int p1yp[]={0,0,w,w};

    //Hammerkopf
    int p2xp[]={ 34, 50, 56, 34};
    int p2yp[]={  2,  2, 12, 12};

    //Stiel
    int p3xp[]={ 40, 48, 48, 40};
    int p3yp[]={ 12, 12, 34, 34};

    Polygon z = null;
    if ((id > 4)||(id < 1))
      id = 1;

    switch (id)
    {
      case 1 : {
                 int xp[]={  l-10, l-10};
                 int yp[]={  w-12, w-2};
                 z = new Polygon(xp,yp,2);
               }
               break;
      case 2 : {
                 int xp[]={  l-16, l-6, l-6,l-16,l-16,l-6};
                 int yp[]={  w-12,w-12, w-7, w-7, w-2,w-2};
                 z = new Polygon(xp,yp,6);
               }
               break;
      case 3 : {
                 int xp[]={  l-16, l-6,l-6,l-14,l-6,l-6,l-16};
                 int yp[]={  w-12,w-12,w-7, w-7,w-7,w-2, w-2};
                 z = new Polygon(xp,yp,7);
               }
               break;
      case 4 : {
                 int xp1[]={  l-16,l-16,l-6, l-6,l-6};
                 int yp1[]={  w-12, w-7,w-7,w-12,w-2};
                 z = new Polygon(xp1,yp1,5);
               }
    }

    int xp2[]={  l-10, l-10};
    int yp2[]={  w-12, w-2};

    Polygon s[] = {new Polygon(p1xp,p1yp,4),
                   new Polygon(p2xp,p2yp,4),
                   new Polygon(p3xp,p3yp,4),
		   z};

    setShape(s);
    super.moveTo(x,y);

  }


  public void paint(Graphics g)
  {
    g.setColor(GrObject.cdgr);
    g.fillPolygon(shape[0]);
    g.setColor(GrObject.clgr);
    g.fillPolygon(shape[1]);
    g.setColor(GrObject.cwht);
    g.fillPolygon(shape[2]);
    g.drawPolygon(shape[3]);

    loaded.paint(g);
    active.paint(g);

    if (container != null)
      container.paint(g);
  }

  public void repaint(Graphics g)
  {
    paint(g);
  }

  public void restore(Graphics g)
  {
    g.setColor(GrObject.cback);
    g.fillPolygon(shape[0]);
  }

  public void moveTo(int x,int y)
  {
    super.moveTo(x,y);
    loaded.moveTo(x,y);
    active.moveTo(x,y);
    if (container != null)
      container.moveTo(x,y);
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void setState(SimState s)
  {
    if (type == WorkStation.Type1)
    {
      if (s.WorkStation[id]&& ready )
      {
        currentTime = 0;
        active.state = true;
        ready = false;
      }
      s.WorkStation[id] = false;
    }      
    
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void getState(SimState s)
  {
    s.WorkStationActive[id] = active.state;
    s.WorkStationLoaded[id] = loaded.state;
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void advance(double dt) throws SimErrorException
  {
    if (type == WorkStation.Type1)
    {
      currentTime += (long)dt;
      if ((currentTime > worktime)&& !ready)
      {
       	active.state = false;
        ready = true;
        currentTime = 0;
      }

      if (container != null)
        container.advance(dt);
      
    }
    
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void addWorkItem(WorkItem wi) throws SimErrorException
  {
    if (container != null)
      throw new SimErrorException("WorkStation allready in use",wi);

    //    wi.setup(wix+posx,wiy+posy,rotx,roty,angle,0,0,0,20);
    container = wi;
    container.container(this);
    loaded.state = true;
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public WorkItem getWorkItem()
  {
    WorkItem tmp =  container;
    container = null;
    loaded.state = false;
    return tmp;
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public boolean isEmpty()
  {
    if (container == null)
      return true;

    return false;
  }


}



