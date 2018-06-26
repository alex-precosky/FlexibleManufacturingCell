import java.awt.*;


class CodeSensor extends GrObject
{
  Code myCode = null;
  // Hohe > 10
  // Laenge >20
  final static int hg = 16;
  final static int lg = 25;

  final static int p1xp[] = {  0, lg, lg,  0};
  final static int p1yp[] = {  0,  0, hg, hg};

  final static int p2xp[] = { 12, 20, 20, 16,  16,  20,  20,  12};
  final static int p2yp[] = {  2,  2,  6,  6,hg-6,hg-6,hg-2,hg-2};


  final static int lxp[] = {  2, 10,   10,    2};
  final static int lyp[] = {  2,  2, hg-2, hg-2};


  final	static Polygon s[] = {new Polygon(p1xp,p1yp,4),
                              new Polygon(p2xp,p2yp,8),
                              new Polygon(lxp,lyp,4)};

  public boolean state;


  public CodeSensor(int x,int y)
  {
    state = false;
    setShape(s);
    moveTo(x,y);
  }

  public void setCode(Code mc)
  {
    myCode = mc;
  }

  public Code getCode()
  {
    return myCode;
  }


  public void paint(Graphics g)
  {
    g.setColor(GrObject.cgry);
    g.fillPolygon(shape[0]);

    g.setColor(GrObject.cblck);
    g.fillPolygon(shape[1]);

    if (!state)
      g.setColor(GrObject.cgrn);
    else
      g.setColor(GrObject.cred);

    g.fillPolygon(shape[2]);
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

}
