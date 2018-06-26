#include <stdio.h>

#include "flexTime.h"
#include "flexInterface.h"

#include "SensorPollThread.h"
#include "SensorData.h"

/* parts of this function taken from example from sample program */
/* testControl.c, as provided on 494 website */
void SensorPollThread( struct SensorPollThreadArgs *args )
{

    struct timespec pollingRate, pollingRateRem;
    struct Sensor_Data;

    int Belt_Sensor[2];
    int Work_Station_Sensor[Number_Of_Processing_Units][2];
    int Crane_Sensor[3][Number_Of_Cranes][Number_Of_X_Sensors];

    /* setup some arbitrary polling frequency to get data from the system 
       - in this example it is almost every 2 update periods - you should
       subtract off the time to process records to get a more accurate
       polling frequency */
    
    /* To request data call flexRequestSensorStatus and then the data
       will be sent back to you.  You then make a blocking read call to
       wait for the data */
    
    while (1) {

      flexRequestSensorStatus();
      
      /* this blocks waiting for the data to arrive */
      flexReadSensorStatus(Belt_Sensor, Work_Station_Sensor, Crane_Sensor);
      
      fprintf(stderr, "\n\n Got Sensor Data: \n");
      printSensorStatus(Belt_Sensor, Work_Station_Sensor, Crane_Sensor);
      
      set_Sensor_Data( args->sensorData, Belt_Sensor, Work_Station_Sensor,
		       Crane_Sensor );
      
      
      /* poll every 2 update cycles */
      toTimeSpec( 2 * Update_Period, &pollingRate);
      
      flexWait(&pollingRate, &pollingRateRem);
          
    }
}

/* Taken from website sample code */
void printSensorStatus(int bs[], int wss[4][2], 
		       int cs[3][Number_Of_Cranes][Number_Of_X_Sensors]) {
  
  int i,j;
  
  fprintf(stderr,"Feed_Photosensor = %d\n", bs[Feed_Photosensor]);
  fprintf(stderr,"Deposit_Photosensor = %d\n", bs[Deposit_Photosensor]);
  for (i = 0; i < Number_Of_Processing_Units; i++) {
    fprintf(stderr,"Work_Station_Sensors[%d]  OCCUPIED = %d  PROCESSING = %d\n", i, wss[i][OCCUPIED], wss[i][PROCESSING]);
  }
  
  fprintf(stderr,"Cranes: \n");
  fprintf(stderr, "X positions: ");
  for (i = 0; i < Number_Of_X_Sensors; i++) {
    fprintf(stderr,"%d ", cs[X][0][i]);
  }
  fprintf(stderr, "\n");
  
  for (i = 0; i < Number_Of_Cranes; i++) {
    fprintf(stderr, "Crane %d: \n", i);
    
    fprintf(stderr, "   Y position: ");
    for (j = 0; j < Number_Of_Y_Sensors; j++) {
      fprintf(stderr, " %d ",  cs[Y][i][j]);
    }
    fprintf(stderr, "\n");
    
    fprintf(stderr, "   Z position: ");
    for (j = 0; j < Number_Of_Z_Sensors; j++) {
      fprintf(stderr, " %d ",  cs[Z][i][j]);
    }
    fprintf(stderr, "\n");
  }
}
