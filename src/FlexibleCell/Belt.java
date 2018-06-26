import java.awt.*;

class Belt extends Item
{
  final static int p1w = 10;
  final static int p2w = 10;
  final static int wix = 10;
  final static int wiy = 15;

  Sensor start_sns;
  Sensor end_sns;
  Mark   mrk;

  boolean sflag = true;
  boolean eflag = true;

  Queue container = new Queue(10);



  public Belt(int x,int y,int l,int w,double ppm,int Id)
  {
    length = l;
    width  = w;
    id = Id;
    speed = ppm;

    int p1xp[]={  0,length,length,   0};
    int p1yp[]={  0,     0,   p1w, p1w};

    int p2xp[]={ p2w,length-p2w,length-p2w,      p2w};
    int p2yp[]={ p1w,       p1w, width-p1w,width-p1w};

    int p3xp[]={        0,   length,length,    0};
    int p3yp[]={width-p1w,width-p1w, width,width};

    int snsx1=p2w  ,snsx2=length-30;
    int snsy1=width,snsy2=width;

    int mx=p2w,my=p1w,ml=width-2*p1w;

    Polygon s[] = {new Polygon(p1xp,p1yp,4),
                   new Polygon(p2xp,p2yp,4),
                   new Polygon(p3xp,p3yp,4)};

    setShape(s);
    super.moveTo(x,y);

    start_sns = new Sensor(snsx1+x,snsy1+y);
    end_sns = new Sensor(snsx2+x,snsy2+y);
    mrk = new Mark(mx+x,my+y,ml,x,y,angle,(double)length-2*p2w);


  }


  public void paint(Graphics g)
  {
    g.setColor(Color.lightGray);
    g.fillPolygon(shape[0]);
    g.fillPolygon(shape[2]);
    g.setColor(new Color(100,180,180));
    g.fillPolygon(shape[1]);


    start_sns.paint(g);
    end_sns.paint(g);
    mrk.paint(g);

    WorkItem it =(WorkItem)container.getFirstObj();
    while (it != null)
    {
      it.paint(g);
      it = (WorkItem)container.getNextObj();
    }


  } 

  public void restore(Graphics g)
  {
    g.setColor(GrObject.cback);
    g.fillPolygon(shape[0]);
    g.fillPolygon(shape[2]);
    g.fillPolygon(shape[1]);

    start_sns.restore(g);
    end_sns.restore(g);   
  } 


  public void repaint(Graphics g)
  {
    paint(g);
  }

  public void moveTo(int x,int y)
  {
    super.moveTo(x,y);
    start_sns.moveTo(x,y);
    end_sns.moveTo(x,y);
    mrk.moveTo(x,y);

    WorkItem it =(WorkItem)container.getFirstObj();
    while (it != null)
    {
      it.moveTo(x,y);
      it = (WorkItem)container.getNextObj();
    }
  }

  public void rotate(int px,int py,double rw)
  {
    super.rotate(px,py,rw);
    start_sns.rotate(px,py,rw);
    end_sns.rotate(px,py,rw);
    mrk.rotate(px,py,rw);

    WorkItem it =(WorkItem)container.getFirstObj();
    while (it != null)
    {
      it.rotate(px,py,rw);
      it = (WorkItem)container.getNextObj();
    }
  }

  protected void switchSns(int s,WorkItem it) throws SimErrorException
  {

    switch (s)
      {
      case WorkItem.atEndSns : 
	{
	  eflag = false;    
	  end_sns.state=true;
	}
               break;
	       
      case WorkItem.atStartSns : 
	{
	  sflag = false;    
          start_sns.state=true;
	}
	break;

      case WorkItem.atTransit : 
	{
	  if (sflag) 
	    start_sns.state=false;
	  if (eflag) 
	    end_sns.state=false;
	}
	break;
	
      case WorkItem.atEnd : 
	{
	  end_sns.state=true;
	  throw new SimErrorException("WorkItem reached end of Belt",it,this);
	}
      }
    
  }


  public void advance(double dt) throws SimErrorException,CollisionException
  {

    sflag = true;
    eflag = true;

    if (state)
    {
      double range = speed*dt;
      mrk.step(range);

      WorkItem it =(WorkItem)container.getFirstObj();

      while (it != null)
	{
	  switchSns(it.step(range),it);
	  it = (WorkItem)container.getNextObj();
	}
    }

  }


  public void setState(SimState State)
  {
    state = State.TrayBeltSet[id];
  }
  
  public void getState(SimState State)
  {
    State.TrayBeltGet[id]=state;
    State.TrayBeltStart[id]=start_sns.state;
    State.TrayBeltEnd[id]=end_sns.state;
  }

  public void addWorkItem(WorkItem wi) throws SimErrorException
  {
    if (start_sns.state)
      throw new SimErrorException("Overlaying WorkItem");
    else
    {
      wi.setup(wix+posx,wiy+posy,rotx,roty,angle,
	       (double)length-2*p2w-WorkItem.lg,
	       0,(length-2*p2w-WorkItem.lg)-20,20);
      container.addElement(wi);
      start_sns.state=true;
    }
  }

  public WorkItem getWorkItem()
  {
    if (!container.isEmpty() && end_sns.state)
      {
        end_sns.state = false;
	return (WorkItem)container.remove();
      }
    else
      return null;

  }

  public boolean isEmpty()
  {
    return container.isEmpty();
  }

}


