public final class config
{
  // general : ms = millisecond

  // count of travelling cranes: allowed values [1,2]
  final static public int PortalArmCount = 2;

  // length of portal in pixel
  final static public int PortalLength = 466;
  final static public int PortalHeight = 50;
  

  // speed of travelling crane [n] in pixels per ms
  final static public double PortalArmSpeed = 0.0805;
  final static public double PortalUpDownSpeed = 0.06;

  // X and Y spring of the portal in pixels
  final static public int PortalXOffset  = 168;
  final static public int PortalYOffset  = 20;

  // coordinates in pixels relative to the position of portal
  final static public int PositionMatrixRow[] = {18,78,138,198,258,318,378,438};
  final static public int PositionMatrixCol[] = {60,120,180};

  // Initial PortalPosition
  final static public int PortalCrane1XPos = 1;
  final static public int PortalCrane1YPos = 2;
  final static public int PortalCrane2XPos = 8;
  final static public int PortalCrane2YPos = 2;


  // count of workstations [2,4]
  final static public int WorkStationCount = 4;
  // time to work for workstation[n] in ms (values < 0 mean infinity)
  final static public long WorkStationTime[] = {3000,-1,-1,5000};
  // position of the workstations relativ to PostionMatrixRow/Col x/y
  final static public int WorkStationPosition[] = {3,1,3,3,6,1,6,3};


  // count of feedbelts
  final static public int TransportBeltCount = 1;

  // length of a feedbelt in pixels
  final static public int TransportBeltLength = 200;

  // speed of a feedbelt in pixels/ms
  final static public double TransportBeltSpeed = 0.05;



  // count of depositbelts 
  final static public int TrayBeltCount = 1;

  // length of a depositbelt in pixels
  final static public int TrayBeltLength = 200;

  // speed of a depositbelt in pixels/ms
  final static public double TrayBeltSpeed = 0.1;

  // delay time between states in ms
  final static public int SimulationAsychronUpdateDelay = 50;

  // frameratedelay in ms
  final static public int FrameUpdateDelay = 30;


  // count of predefined workitem configurations frames
  final static public int WorkItemConfigFrameCount = 10;

  // Enable/Disable DirectSync-Mode (no minimal waiting in Syncmode)
  final static public boolean DirectSync = true;

}
