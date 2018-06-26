import java.awt.*;

public class Sensor extends GrObject
{
  public final static int Height = 10;

  final static int pxp[] = {  0, 20, 20,  0};
  final static int pyp[] = {  0,  0, 10, 10};

  final static int lxp[] = {  8, 18, 18,  8};
  final static int lyp[] = {  2,  2,  8,  8};


  final	static Polygon s[] = {new Polygon(pxp,pyp,4),
                              new Polygon(lxp,lyp,4)};


  public boolean state;

  int dlight = 8;


  public Sensor()
  {
    state = false;
    setShape(shape);
  }

  public Sensor(int x,int y)
  {
    state = false;
    setShape(s);
    moveTo(x,y);

  }

  public void paint(Graphics g)
  {
    g.setColor(GrObject.cgry);
    g.fillPolygon(shape[0]);

    if (!state)
      g.setColor(GrObject.cgrn);
    else
      g.setColor(GrObject.cred);

    g.fillPolygon(shape[1]);

  }

  public void repaint(Graphics g)
  {
    if (!state)
      g.setColor(cgrn);
    else
      g.setColor(cred);

    g.fillPolygon(shape[1]);
  }

  public void restore(Graphics g)
  {
    g.setColor(GrObject.cback);
    g.fillPolygon(shape[0]);
  }

  public void setState(boolean st)
  {
    state = st;
  }
}
