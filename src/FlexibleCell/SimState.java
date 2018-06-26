public final class SimState
{

  public boolean TransBeltCodeReader[];
  public boolean TransBeltGet[];
  public boolean TransBeltSet[];
  public boolean TransBeltStart[];
  public boolean TransBeltEnd[];

  public boolean WorkStation[];
  public long  WorkStationTime[];
  public boolean WorkStationActive[];
  public boolean WorkStationLoaded[];

  public boolean TrayBeltGet[];
  public boolean TrayBeltSet[];
  public boolean TrayBeltStart[];
  public boolean TrayBeltEnd[];


  public boolean Portal[];
  public boolean PortalMagnet[];
  public int PortalPosSet[][];
  public boolean PortalMagnetUpDownSet[];

  public int PortalPosGet[][];
  public boolean PortalMagnetUp[];
  public boolean PortalMagnetDown[];


  public Code CodeState[];

// itb #Zufuerbaender
// itw #Arbeitsbander
// itrb #Ablagebaender
// irb #Roboter

  public SimState(int itb,int itw,int itrb,int irb)
  {
    itb++;
    itw++;
    itrb++;
    irb++;

    //Zufuehrbaender Zustand
    TransBeltCodeReader = new boolean[itb];
    TransBeltGet = new boolean[itb];
    TransBeltSet = new boolean[itb];
    TransBeltStart = new boolean[itb];
    TransBeltEnd = new boolean[itb];

    //Arbeitsstationenen Zustand
    WorkStation = new boolean[itw];
    WorkStationTime = new long[itw];
    WorkStationActive = new boolean[itw];
    WorkStationLoaded = new boolean[itw];


    //Ablagebaender Zustand
    TrayBeltGet = new boolean[itrb];
    TrayBeltSet = new boolean[itrb];
    TrayBeltStart = new boolean[itrb];
    TrayBeltEnd = new boolean[itrb];


    //Portal Zustand
    Portal = new boolean[irb];
    PortalMagnet = new boolean[irb];
    //irb Portalcatindex xpos->[0],ypos->[1]
    PortalPosSet = new int[irb][2];
    PortalPosGet = new int[irb][2];
    PortalMagnetUpDownSet = new boolean[irb];
    PortalMagnetUp = new boolean[irb];
    PortalMagnetDown = new boolean[irb];

    //Codesensoren Zustand
    CodeState = new Code[itb];


    //Initialisierung
    int	i;
    for	(i=0;i<itb;i+=1)
      {
	TransBeltSet[i]=false;
	TransBeltGet[i]=false;
	TransBeltStart[i]=false;
	TransBeltEnd[i]=false;
      }

    for	(i=0;i<irb;i+=1)
      {
	PortalMagnetUp[i]=true;
      }


    for	(i=0;i<itrb;i+=1)
      {
	TrayBeltGet[i]=false;
	TrayBeltSet[i]=false;
	TrayBeltStart[i]=false;
	TrayBeltEnd[i]=false;
      }

  }


}





