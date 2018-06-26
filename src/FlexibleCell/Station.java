import java.awt.*;


class Station extends GrObject
{
  public boolean state;
  public boolean stateSns;
  public boolean stateActive;

  int length;
  int width;
  int id;
  double worktime;

  public Station(int x,int y,int l,int w,int id)
  {
    stateSns = false;
    stateActive = false;
    worktime = 0.0;

    length = l;
    width  = w;

    int p1xp[]={0,l,l,0};
    int p1yp[]={0,0,w,w};

    //Hammerkopf
    int p2xp[]={ 14, 30, 36, 14};
    int p2yp[]={  2,  2, 12, 12};

    //Stiel
    int p3xp[]={ 20, 28, 28, 20};
    int p3yp[]={ 12, 12, 34, 34};

    // Sensoren 
    int p4xp[]={ 2, 10, 10,  2};
    int p4yp[]={ 2,  2, 12, 12};

    int p5xp[]={  2, 10, 10,  2};
    int p5yp[]={ 16, 16, 26, 26};

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
                   new Polygon(p4xp,p4yp,4),
                   new Polygon(p5xp,p5yp,4),z};

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
    g.drawPolygon(shape[5]);

    if (stateSns)
      g.setColor(GrObject.cgrn);
    else
      g.setColor(GrObject.cred);

    g.fillPolygon(shape[3]);

    if (stateActive)
      g.setColor(GrObject.cgrn);
    else
      g.setColor(GrObject.cred);

    g.fillPolygon(shape[4]);


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
