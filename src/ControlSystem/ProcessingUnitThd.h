#ifndef _PROCESSINGUNITTHD_H_
#define _PROCESSINGUNITTHD_H_

#include "CraneController.h"
#include "BlankBuffer.h"
#include "ProcessingUnitController.h"

/* Header file for the thread that runs when a blank is put in a processing */
/* unit.  Basically waits until the unit is done processing for its minTime, */
/* then calls the crane over to pick it up.  */

/* List of arguments that are passed to this thread */
/* Basically the actors and sync object(s) it uses */
struct ProcessingUnitThdArgs{
  struct BlankBuffer* blanksInUnitsBuf;
  struct BlankBuffer* blanksToUnitBuf;
  struct BlankBuffer* blanksToDepositBuf;
  struct CraneController *craneControllerPtr;
  struct ProcessingUnitController* procUnitControllerPtr;
};

/* Function prototype of thread function */
void ProcessingUnitThread( struct ProcessingUnitThdArgs* args );


#endif /* #ifndef _PROCESSINGUNITTHD_H_ */
