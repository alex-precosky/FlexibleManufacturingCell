// The implementation of the processing unit controller

#include <stdlib.h>
#include <pthread.h>
#include <stdio.h>

#include "ProcessingUnitController.h"


/* function: newPorcessingUnitController
 *
* Allocates the resources for a new ProcessingUnitController object
*
* RETRUNS: A pointer to the new unit is returned
*/
struct ProcessingUnitController* newProcessingUnitController()
{
  struct ProcessingUnitController* newPUnitController;

  /* Allocate the memory required */
  newPUnitController = (struct ProcessingUnitController*) 
    malloc( sizeof( struct ProcessingUnitController ) );
  
  newPUnitController->lock = ( pthread_mutex_t* ) 
    malloc( sizeof( pthread_mutex_t ) );

  newPUnitController->signalDone = ( pthread_cond_t*) 
    malloc( sizeof( pthread_cond_t ) );

  /* Initialize the synctronization tools */
  pthread_mutex_init( newPUnitController->lock, NULL );
  pthread_cond_init( newPUnitController->signalDone, NULL );


  /* Initialize the reserveByte to 0's indicating that no units are reserved */
  newPUnitController->reserveByte = 0x00;

  /* Allocate memory for the ProcessingUnit objects the controller */
  /* looks after */
  newPUnitController->processingUnits = ( struct ProcessingUnit*) 
    malloc( 4 * sizeof( struct ProcessingUnit ) );

  /* Set up each unit's location, number, and type */
  newPUnitController->processingUnits[0].posX = 3;
  newPUnitController->processingUnits[0].posY = 1;
  newPUnitController->processingUnits[0].unitNum = 1;
  newPUnitController->processingUnits[0].unitType = 1;

  newPUnitController->processingUnits[1].posX = 6;
  newPUnitController->processingUnits[1].posY = 1;
  newPUnitController->processingUnits[1].unitNum = 2;
  newPUnitController->processingUnits[1].unitType = 2;

  newPUnitController->processingUnits[2].posX = 3;
  newPUnitController->processingUnits[2].posY = 3;
  newPUnitController->processingUnits[2].unitNum = 3;
  newPUnitController->processingUnits[2].unitType = 1;

  newPUnitController->processingUnits[3].posX = 6;
  newPUnitController->processingUnits[3].posY = 3;
  newPUnitController->processingUnits[3].unitNum = 4;
  newPUnitController->processingUnits[3].unitType = 1;

  return newPUnitController;
}

/* function: reserveUnits
 *
 * Set aside the processing units specified in 'bitmask' for use.
 * Function blocks until these units become available.  The bitmask
 * just uses the 4 least-significant bytes.  The LSB corresponds to
 * processing unit 1.
 *
 * To reserve units 1, the bitmask would be 00000001.  To reserve units
 * 2 and 4, the bitmask would be 00001010.
 *
 * Blocks until the requested units become available.
 */
void reserveUnits( struct ProcessingUnitController *theController, 
		   unsigned char bitmask )
{

  //Grab the lock
  pthread_mutex_lock( theController->lock );

  /* while desired units are in use, wait around */
  while ((bitmask & theController->reserveByte) != 0)            
    pthread_cond_wait(theController->signalDone, theController->lock);  

  /* mask with a 1 with the bits corresponding to the units */
  /*that are to be reserved */
  theController->reserveByte = (theController->reserveByte | bitmask);

  pthread_mutex_unlock( theController->lock );
}

/* function: signalPUCDone
 *
 * Tells the controller that the units specified in bitmask are
 * no longer needed by the caller, and should be made available to
 * other callers that want to reserve these units
 */
void signalPUCDone( struct ProcessingUnitController* theController, 
		    unsigned char bitmask)
{
  extern FILE* debugLog;

  pthread_mutex_lock( theController->lock );
  
  /* Remove the reservation for the untits */
  bitmask = ~bitmask;
  theController->reserveByte = theController->reserveByte & bitmask;  

  pthread_mutex_unlock( theController->lock );

  /* Release a thread waiting to reserve units */
  pthread_cond_signal( theController->signalDone );
  
}
