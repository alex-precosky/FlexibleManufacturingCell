import java.awt.*;



class Portal extends Item
{

  final static int ps = 10;
  final static int ped = 15;
  final static int catw = 15;
  final static int collisionPed = 3;

  PositionMatrix posmat;

  PortalArm cat1 = null;
  PortalArm cat2 = null;


  public Portal(int x,int y,int w,int h,PositionMatrix pm,int PortArmCount,double ppm,int height,double ppmUpDown,SimState state,int Id)
  {
    posmat = pm;
    id = Id;
  
    //Schiene 1
    int px1[] = {ped,w-ped,w-ped,ped};
    int py1[] = {  0,    0,   ps, ps};

    //Schiene 2
    int px2[] = {ped,w-ped,w-ped, ped};
    int py2[] = {  h,    h, h-ps,h-ps};

    //Ecke 1
    int ex1[] = {0,ped,ped,  0};
    int ey1[] = {0,  0,ped,ped};

    //Ecke 2
    int ex2[] = {w,w-ped,w-ped,  w};
    int ey2[] = {0,    0,  ped,ped};
      
    //Ecke 3
    int ex3[] = {w    ,w-ped,w-ped,w};
    int ey3[] = {h-ped,h-ped,    h,h};

    //Ecke 4
    int ex4[] = {    0,  ped,ped,0};
    int ey4[] = {h-ped,h-ped,  h,h};

    Polygon s[] = {new Polygon(ex1,ey1,4),
                   new Polygon(ex2,ey2,4),
                   new Polygon(ex3,ey3,4),
                   new Polygon(ex4,ey4,4),
                   new Polygon(px1,py1,4),
                   new Polygon(px2,py2,4)};

    setShape(s);
    super.moveTo(x,y);	

    cat1 = new PortalArm(x,y,catw,h,pm,ppm,(double)height,ppmUpDown,state,1);
    cat1.maxx = w-ped-2*catw;
    cat1.minx = 0;
    if (PortArmCount>1)
    {
      cat2 = new PortalArm(x,y,catw,h,pm,ppm,(double)height,ppmUpDown,state,2);
      cat2.maxx = w-ped-catw;
      cat2.minx = catw;
    }
  }
  

  public void paint(Graphics g)
  {

    g.setColor(GrObject.cblck);
    g.fillPolygon(shape[0]);
    g.fillPolygon(shape[1]);
    g.fillPolygon(shape[2]);
    g.fillPolygon(shape[3]);
    g.setColor(GrObject.cbl);
    g.fillPolygon(shape[4]);
    g.fillPolygon(shape[5]);
    

    cat1.paint(g);
    if (cat2 != null)
      cat2.paint(g);
  }

  public void repaint(Graphics g)
  {
    paint(g);
  }

  public void restore(Graphics g)
  {
    g.setColor(GrObject.cback);
    g.fillPolygon(shape[0]);
    g.fillPolygon(shape[1]);
    g.fillPolygon(shape[2]);
    g.fillPolygon(shape[3]);
    g.fillPolygon(shape[4]);
    g.fillPolygon(shape[5]);
    
    cat1.restore(g);
    if (cat2 != null)
      cat2.restore(g);
  }

  public void addWorkItem(WorkItem wi)
  {
  }

  public WorkItem getWorkItem()
  {
    return new WorkItem(new Code());
  }

  public WorkItem getWorkItem(int index)
  {
    WorkItem item = null;

    switch (index)
    {
      case 1 : {
                 item = cat1.container;
                 cat1.container = null;
               }
               break;
               
      case 2 : {
                 if (cat2 != null)
                 {
                   item = cat2.container;
                   cat2.container = null;
                 }
               }
               break;
    }

    return item;
  }


  public boolean isEmpty()
  {
    boolean test = (cat1.container == null);
    if (test &&(cat2 != null) )
      test = test && (cat2.container == null);

    return test;
  }

  public void getState(SimState s)
  {
    cat1.getState(s);
    if (cat2 != null)
      cat2.getState(s);
  }

  public void setState(SimState s)
  {
    cat1.setState(s);
    if (cat2 != null)
      cat2.setState(s);
  }

  public void advance(double dt) throws SimErrorException,CollisionException
  {
    //Kolissions Pruefung nur bei zwei Laufkatzen ;-)
    if (cat2 != null)
    {
      int armWidth = catw+PortalArm.msx+collisionPed;
      int pos1 = cat1.relx+armWidth;

      if (pos1>=cat2.relx)
      {
	if (cat1.destx>=cat2.destx)
        {
          cat1.destx = cat1.relx;
          cat2.destx = cat2.relx;
          throw new CollisionException();
	}
      }
  
      cat2.advance(dt);
    }

    cat1.advance(dt);
  }


}

