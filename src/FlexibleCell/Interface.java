import java.io.*;
import java.awt.TextArea;


public class Interface extends Thread
{
  DataInputStream in;
  DataOutputStream out;

  int sleepAfterRead = 30;
  int sleepNoRead = 50;

  public static final boolean ASYNC = true;
  public static final boolean SYNC = false;
  public int MAXXSENSORS;
  public int MAXYSENSORS;

  boolean asynchron;

  SimState status;
  Thread parent;
  boolean debugMode;
  TextArea debugOut;
  Code code;

  public Interface()
  {
    this(ASYNC);
    code = null;
  }

  public Interface(boolean mode)
  {
    config c = new config();
    MAXXSENSORS = c.PositionMatrixRow.length;
    MAXYSENSORS = c.PositionMatrixCol.length;

    asynchron = mode;
    try {
      in = new DataInputStream(System.in);
      out = new DataOutputStream(System.out);
    }
    catch (Exception e)
    {
      System.out.println("Error opening pipes");
    }

    debugMode = false;
    code = null;
  }
 
  public void setMode(boolean b)
  {
    asynchron = b;
  }

  public void debug(boolean debugFlag,TextArea outp)
  {
    debugMode = debugFlag;
    debugOut = outp;
  }

   
  private void write(String s) throws IOException
  {
    if (debugMode)
	debugOut.insertText(s,debugOut.getText().length());

    out.writeBytes(s);
  }

  private void writeln(String s) throws IOException
  {
    if (debugMode)
	debugOut.insertText(s+"\n",debugOut.getText().length());

    out.writeBytes(s+"\n");
  }

  public void run()
  {

    while (true)
      {
	try 
	  {
	    // Steuerdaten auslesen und verarbeiten
	    String line;
	    line = in.readLine();
	    if (line != null)
	      readState(status,line);
	  }

 	catch (IOException e)
	  {
	    System.out.println("IOError");
	  }


      }
  }


  public void setCodeStateAsync(Code c)
  {
    code = (Code)c.clone();
  }

  public void startCom(SimState stat,Thread p)
  {
    status = stat;
    parent = p;
    start();
  }

  public void setStatVec(SimState stat)
  {
    status = stat;
  }

  private String str(boolean b)
  {
    return ((b) ? "t" : "f");
  }

  private int getID(String s,int l)
  {
    // Sehr unflexibel aber funtionell
    return (int)(s.charAt(l))-(int)'0';
  }


  private int getLastNum(String s)
  {
    // Sehr unflexibel aber funtionell
    String stmp = s.substring(s.length()-1,s.length());
    int x = stmp.charAt(0);
    x = x - (int)'0';
    return x;
  }


  public void readStatus(SimState s) throws IOException
  {
    String line = in.readLine();
    while (!("[".equals(line)))
      line = in.readLine();

    while (!("]".equals(line)))
      {
	readState(s,line);
	line = in.readLine();
      }

  }


  private void readState(SimState s,String line) throws IOException
  {
    if ((line.length()<=0)||(line.equals("\n"))||(line.equals("["))||(line.equals("]")))
      return;

    if (debugMode)
      debugOut.insertText("recieved: "+line+"\n",debugOut.getText().length());

    try {

      if (line.equals("GetState"))
	{
	  writeStatus(s);
	  return;
	}
      
      if ("FeedBeltOn".equals(line))
	{
	  s.TransBeltSet[1] = true;
	  return;
	}
      
      if ("FeedBeltOff".equals(line))
	{
	  s.TransBeltSet[1] = false;
	  return;
	}
      
      if ("CodeSensorOn".equals(line))
	{	
	  s.TransBeltCodeReader[1] = true;
	  return;
	}
      
      if ("PortalUp".equals(line))
	{
	  s.PortalMagnetUpDownSet[getID(line,"PortalUp".length())]=true;
	  return;
	}
      
      if ("PortalDown".equals(line))
	{
	  s.PortalMagnetUpDownSet[getID(line,"PortalDown".length())]=false;
	  return;
	}
      
      if (line.startsWith("PortalX"))
	{
	  s.PortalPosSet[getID(line,"PortalX".length())][0]=getLastNum(line);
	  return;
	}

      if (line.startsWith("PortalY"))
	{
	  s.PortalPosSet[getID(line,"PortalY".length())][1]=getLastNum(line);
	  return;
	}

      if (line.startsWith("MagnetOn"))
	{
	  s.PortalMagnet[getID(line,"MagnetOn".length())]=true;
	  return;
	}
      
      if (line.startsWith("MagnetOff"))
	{
	  s.PortalMagnet[getID(line,"MagnetOff".length())]=false;
	  return;
	}
      
      if (line.startsWith("PortalUp"))
	{
	  s.PortalMagnetUpDownSet[getID(line,"PortalUp".length())]=true;
	  return;
	}
      
      if (line.startsWith("PortalDown"))
	{
	  s.PortalMagnetUpDownSet[getID(line,"PortalDown".length())]=false;
	  return;
	}

      if (line.startsWith("WorkStationOn"))
	{
	  int length = "WorkStationOn".length();
	  int id = getID(line,length);

	  s.WorkStation[id] = true;
	//  s.WorkStationTime[id] = Integer.parseInt(line.substring(length+1,line.length()).trim());
	  return;
	  
	}

      }
      catch (Exception e)
	{
	  System.out.println("protocol error!");
	}
      if (debugMode)
	debugOut.insertText("unknown command: "+line+"\n",debugOut.getText().length());
  }


  private String getBoolPosition(int n,int c)
  {
    StringBuffer str = new StringBuffer(2*c);
    int i;

    for (i = 1;i < c; i++ )
       str.append("f,");

    str.append("f");

    if (n!=0)
      str.setCharAt(2*n-2,'t');

    return str.toString();
  }


  private String getBoolPosition2(int n1,int n2,int c)
  {
    StringBuffer str = new StringBuffer(2*c);
    int i;

    for (i = 1;i < c; i++ )
       str.append("f,");

    str.append("f");

    if (n1!=0)
      str.setCharAt(2*n1-2,'t');
    if (n2!=0)
      str.setCharAt(2*n2-2,'t');
    return str.toString();
  }


  public void writeStatus(SimState s) throws IOException
  {

    write("[\n"+
          "FeedBeltState<"+str(s.TransBeltEnd[1])+">\n"+
          "DepositBeltState<"+str(s.TrayBeltStart[1])+">\n");

    int max = s.WorkStationLoaded.length;
    int i;
    for (i = 1;i<max;i++)
      write("WorkStationState"+i+"<"+str(s.WorkStationLoaded[i])+","+str(s.WorkStationActive[i])+">\n");


      if (config.PortalArmCount>1)
      {
    	write("PortalState <("+getBoolPosition2(s.PortalPosGet[1][0],s.PortalPosGet[2][0],MAXXSENSORS)+"),("+
              getBoolPosition(s.PortalPosGet[1][1],MAXYSENSORS)+"),("+
			  str(s.PortalMagnetUp[1])+","+str(s.PortalMagnetDown[1])+"),("+
              getBoolPosition(s.PortalPosGet[2][1],MAXYSENSORS)+"),("+
			  str(s.PortalMagnetUp[2])+","+str(s.PortalMagnetDown[2])+")>\n");
      }
      else
      {
    	write("PortalState <("+getBoolPosition(s.PortalPosGet[1][0],MAXXSENSORS)+"),("+
              getBoolPosition(s.PortalPosGet[1][1],MAXYSENSORS)+"),("+
			  str(s.PortalMagnetUp[1])+","+str(s.PortalMagnetDown[1])+")>\n");
      }


    if (code == null)
      code = s.CodeState[1];

    if (code != null)
      {
	write("Code<"+code.stationCount+",");

	for (i = 0;i < code.stationCount;i++)
	  write("<"+code.stationId[i]+","+code.min[i]+","+code.max[i]+">");

	write(","+str(code.orderIndicator)+","+code.tg+">\n");
      }

    write("]\n");

    // clear last codesensor readings
    code = null;

  }

}







