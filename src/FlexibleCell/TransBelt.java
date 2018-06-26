import java.awt.Graphics;

public class TransBelt extends Belt
{
  CodeSensor cs;
  WorkItem wit=null;

  // maxreadturn: max Verzoegerung des CodeSensors nach Erteilung
  // des Auftrages
  final static int maxreadturn = 2;
  int readturn = -1;


  public TransBelt(int x,int y,int l,int w,double ppm,int Id)
  {
    super(x,y,l,w,ppm,Id);

    int cpx = CodeSensor.lg;
    int cpy = -CodeSensor.hg;
    cs = new CodeSensor(x+l-cpx,y+cpy);
  }


  public void paint(Graphics g)
  {
    super.paint(g);
    cs.paint(g);
  }


  public void repaint(Graphics g)
  {
    paint(g); 
  }

  public void restore(Graphics g)
  {
    super.restore(g);
    cs.restore(g);
  }


  public void moveTo(int x,int y)
  {
    super.moveTo(x,y);
    cs.moveTo(x,y);
  }


  public void rotate(int px,int py,double Angle)
  {
    super.rotate(px,py,Angle);
    cs.rotate(px,py,Angle);
  }


  public void advance(double dt) throws SimErrorException,CollisionException
  {
    if (cs.state && (readturn < 0))
    {
      WorkItem it = (WorkItem)container.getFirstObj();

      while (it != null)
	{
          if (it.where() == WorkItem.atEndSns)
	    {
	      cs.setCode(it.getCode());
	      readturn = maxreadturn;
              it = null;
	    }
	  else
	    it = (WorkItem)container.getNextObj();
	}
    }


    readturn--;
      
    if (readturn == 0)
      cs.state = false;

    super.advance(dt);

  }

  public void getState(SimState s)
  {
    //Eigenen Status setzen
    s.TransBeltGet[id]=state;
    s.TransBeltStart[id]=start_sns.state;
    s.TransBeltEnd[id]=end_sns.state;
    s.CodeState[id]=cs.getCode();
    cs.setCode(null);
  }

  public void setState(SimState s)
  {
    //Eigenen Status uebernehmen
    state = s.TransBeltSet[id];   
    if (!cs.state)
      cs.state = s.TransBeltCodeReader[id];

    s.TransBeltCodeReader[id] = false;
  }
}









