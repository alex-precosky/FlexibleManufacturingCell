import java.awt.*;

public class WorkItem extends GrObject
{
  public final static int atEndSns = 1;
  public final static int atStartSns = 2;
  public final static int atTransit = 4;
  public final static int atEnd = 8;


  int snsStartSt;
  int snsStartEd;
  int snsStopSt;
  int snsStopEd;

  public final static int lg = 40;
  public final static int wd = 30;

  final static int pxp[] = {  0, lg, lg,  0};
  final static int pyp[] = {  0,  0, wd, wd};

  final	static Polygon s[] = {new Polygon(pxp,pyp,4)};


  Polygon sshape[];
  public Code myCode;
  public double maxway;
  public double way = 0.0;
  int snsRangeS;

  double timer;
  boolean timerException;
  double stationTimer;
  int currentWorkStationId;
  int lastWorkStationId;
  int stationsVisited;

  int minTime;
  boolean minTimeException;

  int maxTime;
  boolean maxTimeException;

  boolean orderException;
  
  Color col = cbl;
  int startx;
  int starty;


  public WorkItem(Code cd)
  {
    angle = 0.0;
    maxway = 0.0;
    way = 0.0;
    currentWorkStationId = -1;
    lastWorkStationId = -1;
    stationsVisited = -1;
    minTimeException = false;
    maxTimeException = false;
    timerException = false;
    orderException = false;

    myCode = (Code)cd.clone();
    setShape(s);

    sshape = new Polygon[1];
    sshape[0] = new Polygon(shape[0].xpoints,shape[0].ypoints,shape[0].npoints);
  }

  public Code getCode()
  {
    return (Code)myCode.clone();
  }


  public void setup(int x,int y,int px,int py,double Angle,double mway,
		    int Start,int Stop,int Size)
  {
    maxway = mway;
    way = 0.0;

    posx=0;
    posy=0;
    angle=0.0;

    snsStartSt = Start;
    snsStartEd = Start + Size;
    snsStopSt  = Stop;
    snsStopEd  = Stop + Size;

    setShape(s);
    moveTo(x,y);
    startx = posx;
    starty = posy;

    super.rotate(px,py,Angle);
    sshape = new Polygon[1];
    sshape[0] = new Polygon(shape[0].xpoints,shape[0].ypoints,shape[0].npoints);
  }

  public void rotate(int px,int py,double Angle)
  {
    super.rotate(px,py,Angle);
// Nur noetig wenn das Band rotiert ????
/*
    Polygon s[] = shape;
    shape = sshape;
    angle-=Angle;
    super.rotate(px,py,Angle);
    shape = s;
*/
  }


// Gibt true zuruek wenn sich das Werkstueck am Ende seiner maximalen
// Strecke befindet , ansonsten false

  public void setColor(Color c)
  {
    if (!timerException)
      col = c;
  }

  public void resetColor()
  {
    if (!timerException)
        col = cbl;
  }

  public int where()
  {
    if (way > maxway)
    {
      return WorkItem.atEnd;
    }
    else
    {
      int w = (int)way;
      if ((w >= snsStopSt)&&(w <= snsStopEd))
	{
	  return WorkItem.atEndSns;
	}
      if ((w >= snsStartSt)&&(w <= snsStartEd)) 
	{
	  return WorkItem.atStartSns;
	}
    }
    return atTransit;
  }


  public int step(double p)
  {
    way += p;

    if (way > maxway)
    {
      way = 0.0;
      return WorkItem.atEnd;
    }
    else
    {
      int dx = (int)(Math.cos(angle)*way);
      int dy = (int)(Math.sin(angle)*way);
      Polygon s = shape[0];
      Polygon ss = sshape[0];
      s.xpoints[0]=ss.xpoints[0]+dx;
      s.xpoints[1]=ss.xpoints[1]+dx;
      s.ypoints[0]=ss.ypoints[0]+dy;
      s.ypoints[1]=ss.ypoints[1]+dy;
      s.xpoints[2]=ss.xpoints[2]+dx;
      s.xpoints[3]=ss.xpoints[3]+dx;
      s.ypoints[2]=ss.ypoints[2]+dy;
      s.ypoints[3]=ss.ypoints[3]+dy;
      posx = startx + dx;
      posy = starty + dy;


      int w = (int)way;
      if ((w >= snsStopSt)&&(w <= snsStopEd))
	{
	  return WorkItem.atEndSns;
	}
      if ((w >= snsStartSt)&&(w <= snsStartEd)) 
	{
	  return WorkItem.atStartSns;
	}
    }
    return atTransit;
  }


  public void paint(Graphics g)
  {
    g.setColor(col);
    g.fillPolygon(shape[0]);
    g.setColor(cblck);
    Polygon s = shape[0];
    g.drawLine(s.xpoints[0]+1,s.ypoints[0]+1,s.xpoints[2]-1,s.ypoints[2]-1);
    g.drawLine(s.xpoints[1]-1,s.ypoints[1]+1,s.xpoints[3]+1,s.ypoints[3]-1);

  }

  public void repaint(Graphics g)
  {
    paint(g);
  }

  public void restore(Graphics g)
  {
    g.setColor(cback);
    g.fillPolygon(shape[0]);
  }


  // ------------------------------------------------------------------
  //	startTimer
  // ------------------------------------------------------------------
  public void startTimer()
  {
    timer = 0.0;
  }

  // ------------------------------------------------------------------
  //	advance
  // ------------------------------------------------------------------
  public void advance(double dt) throws SimErrorException
  {
    timer += dt;
    if ((!timerException)&&(!(myCode.tg < 0))&&(timer >= myCode.tg))
    {
      timerException = true;
      col = Color.red;
      throw new SimErrorException("The maximum time has been exceeded ");
    }
    
    if (currentWorkStationId >= 0)
    {
        if (myCode.orderIndicator)
          {
            if ((!orderException)&&(currentWorkStationId != myCode.stationId[stationsVisited]))
            {
              orderException = true;
              throw new SimErrorException("workItem placed on wrong workstation("+currentWorkStationId+" right is "+myCode.stationId[stationsVisited]+")");
            }
          }

        stationTimer += dt;

        if ((!maxTimeException)&&(!(maxTime < 0))&&(stationTimer >= maxTime))
        {
          maxTimeException = true;
          col = Color.red;
          throw new SimErrorException("The maximum time has been exceeded in workstation "+currentWorkStationId);
        }
    }
    else
    {
        if ((stationTimer != 0.0)&&(!minTimeException)&&(stationTimer < minTime))
        {
          minTimeException = true;
          col = Color.red;
          throw new SimErrorException("The minimum time had not passed in workstation "+lastWorkStationId);
        }
    }
  }


  // ------------------------------------------------------------------
  //	container
  // ------------------------------------------------------------------
  public void container(Object c)
  {
    if (c instanceof WorkStation)
    {
      currentWorkStationId = ((WorkStation)c).id;
      stationsVisited++;
      stationTimer = 0.0;
      minTime = myCode.getMinTimeForStation(currentWorkStationId);
      maxTime = myCode.getMaxTimeForStation(currentWorkStationId);
      maxTimeException = false;
      minTimeException = false;
      orderException = false;
    }
    else
    {
      lastWorkStationId = currentWorkStationId;
      currentWorkStationId = -1;
    }
  }


  
}



































































