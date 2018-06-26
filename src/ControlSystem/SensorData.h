/*file SensorData.h:*/
/*This header file outlines the Sensor_Data class*/

#include <pthread.h>

#include "flexInterface.h"

#ifndef _SENSORDATA_H_
#define _SENSORDATA_H_


/* class: Sensor_Data
 *
 * Stores sensor readings.  It is periodically filled up with
 * new readings by the SensorPollThread. 
 */

/* All member variables are the same as those returned by the
 * flexible interface sensor polling function */
struct Sensor_Data
{
	pthread_mutex_t *lock;
	int Belt_Sensor[2];
	int Work_Station_Sensor[Number_Of_Processing_Units][2];
	int Crane_Sensor[3][Number_Of_Cranes][Number_Of_X_Sensors];
};

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
		     int Crane_Sensor[3][Number_Of_Cranes][Number_Of_X_Sensors]);

#endif /* #ifndef */
