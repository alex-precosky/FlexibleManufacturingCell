#include <stdio.h>
#include <time.h>

#include "CraneController.h"
#include "BlankToUnitThd.h"
#include "BlankBuffer.h"
#include "blank.h"
#include "flexInterface.h"

/* Implementation file for the thread that takes items held by a crane  */
/* that need to go to a processing unit.  This thread does just */
/* that; takes things to the deposit belt it needs to go to     */


void TakeBlankToUnitThread( struct BlankToUnitThdArgs *args )
{
  struct blank *theBlank;
  int heldByCrane;         /*The number of the crane a blank is held by*/
  int nextUnit;            /*The number of the next processing unit the */
                           /*blank needs to go to */

  int destX, destY;        /* Coordinates of destination unit */

  extern FILE* debugLog;

  while( TRUE )
    {
      /* Get a blank from the previous stage in pipeline */
      getBlank( args->blanksToUnitBuf, &theBlank, &heldByCrane );

      /* Find the destination coordinates */
      nextUnit = theBlank->cs.step[ theBlank->index ].unitNumber;
      destX = args->procUnitControllerPtr->processingUnits[ nextUnit-1 ].posX;
      destY = args->procUnitControllerPtr->processingUnits[ nextUnit-1 ].posY;

      fprintf( debugLog, "From BlankToUnitThd:\n");
      fprintf( debugLog, "Destination Unit: %d\n", nextUnit );
      fprintf( debugLog, "Destination X: %d\n", destX );
      fprintf( debugLog, "Destination Y: %d\n", destY );

      /* Perform the move to the destination unit, put in the blank, and */
      /* tell the crane controller we're done with its crane */
      move_xy( args->craneControllerPtr, heldByCrane, destX, destY );
      put( args->craneControllerPtr, heldByCrane );
      signalCraneDone( args->craneControllerPtr, heldByCrane );

      /* put blank in buffer to be processed by next pipeline stage */
      putBlank( args->blanksInUnitsBuf, theBlank, -1 );
    }


}
