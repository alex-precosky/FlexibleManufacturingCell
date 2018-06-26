import java.awt.*;
import java.lang.Math;
import java.lang.*;


abstract public class GrObject
{
  public final static double DPI = 2 * Math.PI;

  protected int posx;
  protected int posy;

  protected int rotx;
  protected int roty;

  protected Polygon shape[];
  protected Polygon backupShape[];

  final static Color cred = Color.red;
  final static Color cyl = Color.yellow;
  final static Color cgrn = Color.green;
  final static Color cgry = Color.gray;
  final static Color cwht = Color.white;
  final static Color cbl = Color.blue;
  final static Color clgr = Color.lightGray;
  final static Color ccy = Color.cyan;
  final static Color cdgr = Color.darkGray;
  final static Color cblck = Color.black;



  final static Color cback = Color.white;

  protected double angle;
  protected double scalefact = 1.0;

  

// -------------------------------------------------------------
// -------------------------------------------------------------
  public GrObject()
  {
    scalefact = 1.0;
    angle = 0.0;

    posx = 0;
    posy = 0;
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public GrObject(int x,int y,double Angle,double f,Polygon Shape[])
  {
    posx = x;
    posy = y;
    scalefact = f;
    angle = Angle;
    setShape(Shape);
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void setPos(int x,int y)
  {
    posx = x;
    posy = y;
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void setAngle(int a)
  {
     angle = (double)a;
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  public void setShape(Polygon Shape[])
  {
    Polygon s;

    shape = new Polygon[Shape.length];
    backupShape = new Polygon[Shape.length];

    for (int i = Shape.length-1;i>=0;i-=1)
    {
      s = Shape[i];
      shape[i] = new Polygon(s.xpoints,s.ypoints,s.npoints);
      backupShape[i] = new Polygon(s.xpoints,s.ypoints,s.npoints);
    }
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  protected void moveTo(int x,int y)
  {
    Polygon sptr,bsptr;
    int i,k;

    posx += x;
    posy += y;

    for (k = shape.length - 1; k >= 0 ; k-=1)
      {
        sptr = shape[k];

        for(i = sptr.npoints - 1 ; i>=0 ; i-=1)
          {
            sptr.xpoints[i]+=x;
            sptr.ypoints[i]+=y;
          }
      }
  }

// -------------------------------------------------------------
// -------------------------------------------------------------
  protected void place(int x,int y)
  {
    x-=posx;
    y-=posy;
    moveTo(x,y);
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void rotate(int px,int py,double rw)
  {
    Polygon sptr,bsptr;
    double tx,ty,dx,dy;
    double dpx,dpy,cra,sra,pdpx,pdpy;
    int i=0,j=0;

    rotx = px;
    roty = py;
    angle += rw;

    if (angle > GrObject.DPI)
      angle = Math.IEEEremainder(angle,DPI);

    cra = Math.cos(angle);
    sra = Math.sin(angle);

    dpx = (double)px;
    dpy = (double)py;
    pdpx = (double)(px-posx);
    pdpy = (double)(py-posy);

    while (j < shape.length)
    {

      sptr = shape[j];
      bsptr = backupShape[j];
      i = 0;

      while (i < sptr.npoints)
        {
          dx = ((double)(bsptr.xpoints[i])) - pdpx;
          dy = ((double)(bsptr.ypoints[i])) - pdpy;
	
          tx = (dx * cra - dy * sra);
          ty = (dx * sra + dy * cra);
	
	  sptr.xpoints[i] = ((int)(tx + dpx));
	  sptr.ypoints[i] = ((int)(ty + dpy));

          i += 1;
        }
        j += 1;
      }

  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public void setScale(double f)
  {
    scalefact = f;
  }


// -------------------------------------------------------------
// -------------------------------------------------------------
  public abstract void paint(Graphics g);


// -------------------------------------------------------------
// -------------------------------------------------------------
  public abstract void restore(Graphics g);


// -------------------------------------------------------------
// -------------------------------------------------------------
  public abstract void repaint(Graphics g);

}




