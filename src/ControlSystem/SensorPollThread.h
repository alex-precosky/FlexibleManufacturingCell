#ifndef _SENSORPOLLTHREAD_H_
#define _SENSORPOLLTHREAD_H_

#include "SensorData.h"

/* Header file for the thread that polls the sensors */
/* and puts the readings in an object for all to read from */

/* List of arguments that are passed to this thread */
/* Basically the actors and sync object(s) it uses */
struct SensorPollThreadArgs
{  
  struct Sensor_Data *sensorData;
};

/* Function prototype of thread function */
void SensorPollThread( struct SensorPollThreadArgs *args );

/* Taken from testControl.c program from 494 website.  Prints out sensor readings */
void printSensorStatus(int bs[], int wss[4][2], 
		       int cs[3][Number_Of_Cranes][Number_Of_X_Sensors]);

#endif /* #ifndef _SENSORPOLLTHREAD_H_ */
