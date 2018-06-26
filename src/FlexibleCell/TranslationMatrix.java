//Import Item;


class TranslationMatrix
{

  double angle[] = {0.419,0.628,1.466,1.676,2.513,2.723,3.560,3.770,4.608,4.817,5.655,5.864};
  public int max = angle.length;
  Item item[] = new Item[max];



  public TranslationMatrix()
  {
    int i = 0;
    while (i<max) item[i++]=null;
  }


  public int getIndex(double Angle)
  {
    if ((Angle > angle[max-1])||(Angle < 0.0))
      return 0;

    int i=0;

    while ((i<max)&&(Angle > angle[i]))
      i++;

    return i;
  }

  public double getAngle(boolean pos[])
  {
    int i = pos.length;
    try
    {
      while (!pos[--i]);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      return -1.0;
    }
    
    
    return (angle[i-1]+angle[i])/2;
    
  }

  public Item getItem(double angle)
  {
    return item[getIndex(angle)];
  }

  public void setItem(int index,Item it)
  {
    item[index]=it;
  }

}
