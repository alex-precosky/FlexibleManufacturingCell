#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "flexTime.h"
#include "BlankToDepositThd.h"
#include "CraneController.h"
#include "BlankBuffer.h"
#include "SensorData.h"

/* Implementation of the thread that takes items held by a */
/* crane that need to go to the deposit belt.  This thread */
/* does just that; takes things to the deposit belt */

void BlankToDepositThd( struct BlankToDepositThdArgs* args )
{
  blank* theBlank;
  int heldByCrane;
  int unitNum;

  /* Coordinates of the crane move */
  int destX, destY;

  struct timespec timeWait, timeWaitRem;

  while( TRUE )
    {
      /* waits on the sync object from previous stage in pipeline */
      getBlank( args->blanksToDepositBuf, &theBlank, &heldByCrane );

      /* check deposit belt thread to see if its free */
      toTimeSpec( Update_Period, &timeWait );
      while( args->sensorData->Belt_Sensor[1] )
	flexWait( &timeWait, &timeWaitRem );
      
      /* set up the destination location of the deposit belt */
      destX = 8;
      destY = 2;

      /* move the crane to the deposit belt, and put item down */
      move_xy( args->craneControllerPtr, heldByCrane, destX, destY );
      put( args->craneControllerPtr, heldByCrane );

      /* signal that we're done with the crane we were using */
      signalCraneDone( args->craneControllerPtr, heldByCrane );
      
      /* can now dispose of this blank object */
      free( theBlank );
    }

}
