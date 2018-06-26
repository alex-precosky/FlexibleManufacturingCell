#ifndef BLANKTOUNITTHD_H
#define BLANKTOUNITTHD_H


#include "BlankBuffer.h"
#include "CraneController.h"
#include "ProcessingUnitController.h"

/* Header file for the thread that takes items held by a crane  */
/* that need to go to a processing unit.  This thread does just */
/* that; takes things to the deposit belt it needs to go to     */

/* List of arguments that are passed to this thread */
/* Basically the actors and sync object(s) it uses */
struct BlankToUnitThdArgs
{
  struct BlankBuffer* blanksToUnitBuf;
  struct BlankBuffer* blanksInUnitsBuf;
  struct CraneController* craneControllerPtr;
  struct ProcessingUnitController* procUnitControllerPtr;
};

/* Function prototype of thread function */
void TakeBlankToUnitThread( struct BlankToUnitThdArgs* args );

#endif /* #ifnded BLANKTOUNITTHD_H */
