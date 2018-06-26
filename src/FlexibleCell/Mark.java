import java.awt.*;

public class Mark extends GrObject
{
  double way;
  double maxway;
  Polygon sshape[];

// -------------------------------------------------------------
// -------------------------------------------------------------
  Mark(int x,int y,int length,int px,int py,double Angle,double mway)
  {
    int xp[]={x,x};
    int yp[]={y,y+length};
    int bxp[]={0,0};
    int byp[]={0,length};

    shape = new Polygon[1];
    shape[0] = new Polygon(xp,yp,2);
    backupShape = new Polygon[1];
    backupShape[0] = new Polygon(bxp,byp,2);

    posx=x;
    posy=y;

    maxway = mway;
    way = 0.0;

    super.rotate(px,py,Angle);
    sshape = new Polygon[1];
    sshape[0] = new Polygon(shape[0].xpoints,shape[0].ypoints,2);
    
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void paint(Graphics g)
  {
    g.setColor(GrObject.cred);
    Polygon p = shape[0];
    g.drawLine(p.xpoints[0],p.ypoints[0],p.xpoints[1],p.ypoints[1]);
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void repaint(Graphics g)
  {
    paint(g);
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void restore(Graphics g)
  {
    g.setColor(GrObject.cback);
    Polygon p = shape[0];
    g.drawLine(p.xpoints[0],p.ypoints[0],p.xpoints[1],p.ypoints[1]);
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void rotate(int px,int py,double Angle)
  {
    super.rotate(px,py,Angle);
    Polygon s[] = shape;
    shape = sshape;
    angle-=Angle;
    super.rotate(px,py,Angle);
    shape = s;
  }

// -------------------------------------------------------------
//Da nur die Zeichen Polygone geaendert werden muss, wenn rotiert werden soll
//die Rotation vor step ausgefuehrt werden !!!
// -------------------------------------------------------------
  public void step(double p)
  {
    way += p;

    Polygon s = shape[0];
    Polygon ss = sshape[0];

    if (way >= maxway)
    {
/*
      s.xpoints[0]=ss.xpoints[0];
      s.xpoints[1]=ss.xpoints[1];
      s.ypoints[0]=ss.ypoints[0];
      s.ypoints[1]=ss.ypoints[1];
*/
      way = 0.0;
    }
    else
    {
      int dx = (int)(Math.cos(angle)*way);
      int dy = (int)(Math.sin(angle)*way);
      s.xpoints[0]=ss.xpoints[0]+dx;
      s.xpoints[1]=ss.xpoints[1]+dx;
      s.ypoints[0]=ss.ypoints[0]+dy;
      s.ypoints[1]=ss.ypoints[1]+dy;
    }
  }

}
