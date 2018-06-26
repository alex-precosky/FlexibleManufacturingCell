import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;


final public class Code 
{
  public final static int MaxStationCount = 12;

  public int stationCount = 1;
  public boolean orderIndicator;

  public int stationId[] = new int[Code.MaxStationCount];
  public int min[] = new int[Code.MaxStationCount];
  public int max[] = new int[Code.MaxStationCount];

  public int tg;



  public synchronized Object clone()
  {
/*
    try 
      {
	Code c = (Code)super.clone();
        c.stationCount = stationCount;
        c.orderIndicator = orderIndicator;
	c.tg = tg;

	int i;
	for (i=0;i < min.length;i++)
          c.min[i] = min[i];
	for (i=0;i < max.length;i++)
	  c.max[i] = max[i];
	for (i=0;i < stationId.length;i++)
	  c.stationId[i] = stationId[i];

	return c;
    }
    catch (CloneNotSupportedException e)
      {
        return this;
      }
*/

	Code c = new Code();
        c.stationCount = stationCount;
        c.orderIndicator = orderIndicator;
	c.tg = tg;

	int i;
	for (i=0;i < min.length;i++)
          c.min[i] = min[i];
	for (i=0;i < max.length;i++)
	  c.max[i] = max[i];
	for (i=0;i < stationId.length;i++)
	  c.stationId[i] = stationId[i];

	return c;

  }


  public boolean load(String fname)
  {

    try {

      FileInputStream file = new FileInputStream(fname);
      DataInputStream in = new DataInputStream(file);

      stationCount = in.readInt();
      orderIndicator = in.readBoolean();

      int i;
      for (i=0;i < min.length;i++)
        min[i] = in.readInt();
      for (i=0;i < max.length;i++)
        max[i] = in.readInt();
      for (i=0;i < stationId.length;i++)
        stationId[i] = in.readInt();

      tg = in.readInt();

      file.close();
    }
    catch (Exception e)
    {
      return false;
    }

    return true;
  }

  public boolean save(String fname)
  {
    try {

      FileOutputStream file = new FileOutputStream(fname);
      DataOutputStream out = new DataOutputStream(file);

      out.writeInt(stationCount);
      out.writeBoolean(orderIndicator);

      int i;
      for (i=0;i < min.length;i++)
        out.writeInt(min[i]);
      for (i=0;i < max.length;i++)
        out.writeInt(max[i]);
      for (i=0;i < stationId.length;i++)
        out.writeInt(stationId[i]);

      out.writeInt(tg);

      file.close();

    }
    catch (Exception e)
    {
      return false;
    }

    return true;

  }

  // ------------------------------------------------------------------
  //	getMaxTimeForStation
  // ------------------------------------------------------------------
  public int getMaxTimeForStation(int id)
  {
    for (int i=0;i<stationId.length;i++)
        if (stationId[i]==id)
            return max[i];

    return -1;
  }

  // ------------------------------------------------------------------
  //	getMinTimeForStation
  // ------------------------------------------------------------------
  public int getMinTimeForStation(int id)
  {
    for (int i=0;i<stationId.length;i++)
        if (stationId[i]==id)
            return min[i];

    return 0;
  }


}
