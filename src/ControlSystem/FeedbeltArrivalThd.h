#ifndef FEEDBELTARRIVALTHD_H
#define FEEDBELTARRIVALTHD_H

#include <semaphore.h>

#include "CraneController.h"
#include "ProcessingUnitController.h"

/* Header file for the thread that is released when an item reaches the end */
/* of the feed belt.  It stops the feed belt, reads the barcode into a     */
/* new object of type 'blank', gets a crane to pick up the blank, then    */
/* starts up the feed belt again.  The blank, as well as the number of the */
/* crane that holds the blank (should always be crane #1), is passed on to */
/* the thread that takes blanks held by cranes to units */

/* List of arguments that are passed to this thread */
/* Basically the actors and sync object(s) it uses */
struct FBArrivalHandlerThdArgs
{
  sem_t* newItemSemPtr;
  struct CraneController* craneControllerPtr;
  struct ProcessingUnitController* procUnitControllerPtr;
  struct BlankBuffer* blanksToUnitBuf;
};

/* Function Prototype of thread function */
void FeedBeltArrivalHandlerThread( struct FBArrivalHandlerThdArgs *args);

#endif
