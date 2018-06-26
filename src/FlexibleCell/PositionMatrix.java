import java.awt.Graphics;

class PositionMatrix
{
  int MAXX = 8;
  int MAXY = 3;

  int xPosition[];
  int yPosition[];

  Item item[][] = new Item[8][3];

  public PositionMatrix()
  {
    xPosition = new int[MAXX];
    yPosition = new int[MAXY];
  }
  
  public PositionMatrix(int px[],int py[])
  {
    setPositions(px,py);
  }

  
  public void setPositions(int px[],int py[])
  {
    MAXX = px.length;
    MAXY = py.length;

    xPosition = new int[MAXX];
    yPosition = new int[MAXY];

    int x,y;
    for (x = 0;x<MAXX;x++)
      xPosition[x] = px[x];

    for (y = 0;y<MAXY;y++)
      yPosition[y] = py[y];

  }

  
  public int getXPos(int pos)
  {
    try
    {
      pos--;
      return xPosition[pos];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      return -1;
    }

  }


  public int getYPos(int pos)
  {
    try
    {
      pos--;
      return yPosition[pos];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      return -1;
    }

  }


  public int getXIndex(int p)
  {
    if (p < 0)
      return 0;

    int i=0;

    while ((i<MAXX)&&(p > xPosition[i]))
      i++;

    return i;

  }

  
  public int getYIndex(int p)
  {
    if (p < 0)
      return 0;

    int i=0;

    while ((i<MAXY)&&(p > yPosition[i]))
      i++;

    return i;

  }

  public int getXIndexEpsilon(int p,int e)
  {
    int i=0;
    
    while (i<MAXX)
      {
	if ((Math.abs(xPosition[i]-p))<=e)
	  return i+1;

	i++;
      }

    return 0;

  }

  
  public int getYIndexEpsilon(int p,int e)
  {
    int i=0;

    while (i<MAXY)
      {
	if ((Math.abs(yPosition[i]-p))<=e)
	  return i+1;

	i++;
      }

    return 0;

  }


  public void setItem(int x,int y,Item it)
  {
    item[x-1][y-1] = it;
  }
  
  public Item getItem(int x,int y)
  {
    return item[x][y];
  }

  public void paint(Graphics g)
  {
  }

}


