#include <stdio.h>

#include "flexTime.h"
#include "ProcessingUnitThd.h"
#include "BlankBuffer.h"
#include "CraneController.h"
#include "ProcessingUnitController.h"
#include "blank.h"

/* Source file for the thread that runs when a blank is put in a processing */
/* unit.  Basically waits until the unit is done processing for its minTime, */
/* then calls the crane over to pick it up.  */


void ProcessingUnitThread( struct ProcessingUnitThdArgs* args )
{
  struct blank* theBlank; /* theBlank and heldByCrane retrieved from buffer */
  int heldByCrane; /* will be -1 when retrieved since not held by crane */

  int whichCrane; /* The number of the crane that will pick up the blank */

  int nextUnitNum, currentUnitNum; /* The number of the unit the blank */
                                   /* goes to next, and the one it    */
                                   /* currently is in */

  long int processingTime;  /* Amount of time to process, in milliseconds */

  /* Coordinates of crane source and destination movement of blank */
  int sourceX, sourceY, destX, destY;

  struct timespec processTimeWait, processTimeRem;

  while( TRUE )
    {
      getBlank( args->blanksInUnitsBuf, &theBlank, &heldByCrane );

      /* read how long to process for, in milliseconds */
      processingTime = theBlank->cs.step[ theBlank->index ].minTime;

      /* wait around for processing to complete */
      /* multiply by 1000 since toTimeSpec expects microseconds */
      toTimeSpec( processingTime*1000, &processTimeWait );
      flexWait( &processTimeWait, &processTimeRem );
  
      /* Find location of unit the blank is in */
      currentUnitNum = theBlank->cs.step[ theBlank->index ].unitNumber;
      sourceX = 
	args->procUnitControllerPtr->processingUnits[ currentUnitNum-1 ].posX;

      sourceY = 
	args->procUnitControllerPtr->processingUnits[ currentUnitNum-1 ].posY;

      /*Update the index of the blank to mean we're on the next step now*/
      theBlank->index++;

      /* If not on the last step, destination coords = next unit location */
      if(theBlank->index != theBlank->cs.numSteps )
	{
	  nextUnitNum = theBlank->cs.step[ theBlank->index ].unitNumber;
	  destX = 
	    args->procUnitControllerPtr->processingUnits[ nextUnitNum-1 ].posX;
	  destY = 
	    args->procUnitControllerPtr->processingUnits[ nextUnitNum-1 ].posY;
	}
      /* otherwise, destination coords = deposit belt */
      else
	{
	  destX = 8;
	  destY = 2;
	}      

      /* Reserve a crane to take blank to its next location */
      whichCrane = request( args->craneControllerPtr, sourceX, sourceY,
			    destX, destY );

      /* Move crane over, pick up blank */
      move_xy( args->craneControllerPtr, whichCrane, sourceX, sourceY );
      get( args->craneControllerPtr, whichCrane );

      /* put blank in approproate buffer */
      if(theBlank->index == theBlank->cs.numSteps )
	{
	  /* signal that we're done with the processing units we were using */
	  signalPUCDone( args->procUnitControllerPtr, theBlank->bitmask );

	  /* put the blank in the buffer of things that need to go 
	     to deposit belt */
	  putBlank( args->blanksToDepositBuf, theBlank, whichCrane );
	}
      else
	{
	  /* put the blank in the buffer of things that need to go to a unit */
	  putBlank( args->blanksToUnitBuf, theBlank, whichCrane );
	}
    }
}
