/*file: SensorData.c*/
/*This source file details the functions of SensorData*/

#include <pthread.h>
#include "SensorData.h"

/* function:  set_Sensor_Data
 * 
 *  set_Sensor_Data returns nothing and simply sets the values 
 *  of the specified sensor data.
 *
 * This function is probably only useful to the thread that
 * polls the sensors and fills up this object with new data.
 *
 */
void set_Sensor_Data(struct Sensor_Data *sensorData, int Belt_Sensor[2], 
		     int Work_Station_Sensor[Number_Of_Processing_Units][2], 
		     int Crane_Sensor[3][Number_Of_Cranes][Number_Of_X_Sensors])
{
  /* For loop indices for copying over data from the parameters */
  int i, j, k;

  pthread_mutex_lock( sensorData->lock );
  
  /* A bunch of for loops to copy data from parameters to class variables */
  
  for( i = 0; i <= 1; i++ )
    sensorData->Belt_Sensor[1] = Belt_Sensor[1];
  
  for( i = 0; i < Number_Of_Processing_Units; i++ )
    for( j = 0; j < 2; j++ )
      sensorData->Work_Station_Sensor[i][j] = Work_Station_Sensor[i][j];

  for( i = 0; i < 3; i++ )
    for( j = 0; j < Number_Of_Cranes; j++ )
      for( k = 0; k < Number_Of_X_Sensors; k++ )
	sensorData->Crane_Sensor[i][j][k] = Crane_Sensor[i][j][k];

  pthread_mutex_unlock( sensorData->lock );

}
