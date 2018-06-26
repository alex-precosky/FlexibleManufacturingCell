#include <stdio.h>
#include <time.h>

#include "flexInterface.h"
#include "FeedbeltArrivalThd.h"
#include "BlankBuffer.h"
#include "blank.h"
#include "BarcodeReader.h"
#include "CraneController.h"

void FeedBeltArrivalHandlerThread( struct FBArrivalHandlerThdArgs *args )
{
  extern FILE* debugLog;
  
  struct blank *newBlank; /* A new blank object, initialized by ReadBarcode() */

  int whichCrane; /* the number of the crane that picks up the blank (should be 1) */
  
  int firstUnit; /* the number of the first unit the blank needs to go to */
  
  int destX;    /* Coordinates of the first processing unit for the blank */
  int destY;
  
  const int feedBeltX = 1; /* X/Y coordinates of the feed belt pick-up spot */
  const int feedBeltY = 2;
  
  fprintf(debugLog, "In FeedBeltArrivalThd\n");
  
  while( TRUE )
    {
      /* This is triggered by the SIGALRM signal handler */
      sem_wait( args->newItemSemPtr );
    
      flexSetFeedBelt( OFF );
      
      /* Read the barcode into a new blank struct */
      newBlank = ReadBarcode();

      /* Read the first destination */
      firstUnit = newBlank->cs.step[0].unitNumber;
      destX = args->procUnitControllerPtr->processingUnits[ firstUnit-1 ].posX;
      destY = args->procUnitControllerPtr->processingUnits[ firstUnit-1 ].posY;
      

      fprintf( debugLog, "From FeedbeltArrivalThread:\n");
      fprintf( debugLog, "Destination Unit: %d\n", firstUnit );
      fprintf( debugLog, "Destination X: %d\n", destX );
      fprintf( debugLog, "Destination Y: %d\n", destY );

      /* Reserve the processing units */
      reserveUnits( args->procUnitControllerPtr, newBlank->bitmask );
      fprintf( debugLog, "Units Reserved\n");
      
      /* Get picked up by the crane */
      whichCrane = request( args->craneControllerPtr, feedBeltX, feedBeltY, destX, destY );
      fprintf( debugLog, "Crane reserved\n");
      move_xy( args->craneControllerPtr, whichCrane, feedBeltX, feedBeltY );
      get( args->craneControllerPtr, whichCrane );	  
      
      /* Put the blank into the buffer */
      putBlank( args->blanksToUnitBuf, newBlank, whichCrane );
      
      /* Start the feedbelt again */
      flexSetFeedBelt( ON );
      
    }
  
}
