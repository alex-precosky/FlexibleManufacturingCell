#ifndef _BLANKTODEPOSITTHD_H_
#define _BLANKTODEPOSITTHD_H_

#include "CraneController.h"
#include "BlankBuffer.h"
#include "ProcessingUnitController.h"
#include "SensorData.h"

/* Header file for the thread that takes items held by a crane */
/* that need to go to the deposit belt.  This thread does just */
/* that; takes things to the deposit belt */

/* List of arguments that are passed to this thread */
/* Basically the actors and sync object(s) it uses */
struct BlankToDepositThdArgs
{
  struct BlankBuffer* blanksToDepositBuf;
  struct CraneController* craneControllerPtr;
  struct ProcessingUnitController* procUnitControllerPtr;
  struct Sensor_Data *sensorData;
};

/* Function prototype of thread function */
void BlankToDepositThd( struct BlankToDepositThdArgs* args );

#endif
