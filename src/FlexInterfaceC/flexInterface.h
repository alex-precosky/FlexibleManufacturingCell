/* Copyright, 2002: Sidney Fels, Dept. of ECE, UBC */

/*
   This code is the header for the flexInterface library.  It provides 
   a simple interface to the Flexible Cell simulator.
*/

#ifndef _FLEXINTERFACE_H
#define _FLEXINTERFACE_H

#define Update_Period 600000  /* (units in usec) - how many simulated seconds 
                   for an update; effectively, the simulation is updated 
                   every Update_Period, thus this Period translates to a 
                   flex sync interval in real-time units.
                   NOTE: if you make this too small the bar code will not
                   be read correctly.   */
#define Flex_Sync_Interval 200000  /* (units in usec) 200 msec: should match the -sync option to the simulator */

#define TRUE 1
#define FALSE 0

#define Number_Of_Cranes 2
#define Number_Of_Processing_Units 4

/* commands to control the production cell */
/* #define CRANE_X 50
#define CRANE_Y 51
#define CRANE_Z 52
#define CRANE_MAGNET 53
*/

#define UP 60
#define DOWN 61
#define ON 70
#define OFF 71

/* defines for belt sensors - these are array indices to the belt_sensor array */
#define Feed_Photosensor 0
#define Deposit_Photosensor 1

/* defines for Workstation sensors - these are array indices to the work station arrays */
#define OCCUPIED 0 /* Workstation Status index for Occupied sensor*/
#define PROCESSING 1 /* Workstation Status index for Processing sensor */

/* defines for crane sensors - the number of X sensors must always be more than
   Y or Z.  Also, there is only one set of X sensors and they are associated
   with the first crane  */
#define X 0  /* Crane sensor X dimension */
#define Y 1  /* Crane sensor Y dimension */
#define Z 2  /* Crane sensor Z dimension */
#define Number_Of_X_Sensors 8
#define Number_Of_Y_Sensors 3
#define Number_Of_Z_Sensors 2

/* each item can have a maximum of 10 processing steps */
#define MAX_NUMBER_OF_STEPS 10

/* barcode reader status structure */
typedef struct FlexStepStruct {
   int unitNumber;
   long int minTime;
   long int maxTime;
} FlexStep;

typedef struct {
   int numSteps;
   long int maxTime;
   int ordered;
   struct FlexStepStruct step[MAX_NUMBER_OF_STEPS];
} FlexCodeStatus;


/* command interface to the simulator */

/* reset the command structure (not normally used by the application) */
extern void flexReset (void);

/* this is the main periodic loop that checks the data coming from the
   simulation and sends data to the simulation.  It should be the
   last call in your main thread. */
extern void flexUpdateTask (void);

/* turn ON/OFF the feedbelt */
extern void flexSetFeedBelt (int what);

/* turn on the bar code reader - normally done by the flexInterface */
extern void flexSetCodeReaderOn (void);

/* turn on a workstation (which = 1,2,3 or 4)*/
extern void flexSetWorkStationOn (int which);

/* move a crane (which=1 or 2) to a new X position (where= 1..8) */
extern void flexSetCraneX (int which, int where);

/* move a crane (which=1 or 2) to a new Y position (where= 1..3) */
extern void flexSetCraneY (int which, int where);

/* move a crane (which=1 or 2) to a new Z position (where= UP or DOWN) */
extern void flexSetCraneZ (int which, int where);

/* turn on a crane's magnet (which=1 or 2, how = ON or OFF) */
extern void flexSetCraneMagnet (int which, int how);

/* this must be called by the application programmer to initialize the
   interface */
extern void flexInitCommunications (void) ;

/* make a request for the sensor data to be sent - this will normally be 
   followed by a read operation */
extern  void flexRequestSensorStatus(void);

/* blocking read of current sensor status */ 
extern void flexReadSensorStatus(int bs[], int wss[4][2], int cs[3][Number_Of_Cranes][Number_Of_X_Sensors]) ;

/* non-blocking read of current bar code */ 
extern void flexReadCodeStatus(FlexCodeStatus *cs) ;

#endif
