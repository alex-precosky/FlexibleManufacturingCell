// This is the processing unit controller

#ifndef _PUCONT_H_
#define _PUCONT_H_

#include <pthread.h>
#include "ProcessingUnit.h"

/* class: ProcessingUnitController */
/* This stores the ProcessingUnit objects, and handles their reservation */
/* When a client wishes to use some processing units, they should be    */
/* reserved first with reserveUnits().  This will block until the units */
/* become available.  When done with the units, signalPUCdone() should  */
/* be called */


struct ProcessingUnitController
{
  unsigned char reserveByte; /* A bitmask which stores which units are "in use" */
  pthread_mutex_t* lock;     /* Sync tools for the reservation related functions */
  pthread_cond_t* signalDone;
  struct ProcessingUnit *processingUnits; /* Dynamic array of the processing untis */
                                          /* that make up the system               */
};

/* function: newProcessingUnitController
 *
* Allocates the resources for a new ProcessingUnitController object
*
* RETRUNS: A pointer to the new unit is returned
*/
struct ProcessingUnitController* newProcessingUnitController();

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
void reserveUnits( struct ProcessingUnitController* theController, 
		   unsigned char bitmask );

/* function: signalPUCDone
 *
 * Tells the controller that the units specified in bitmask are
 * no longer needed by the caller, and should be made available to
 * other callers that want to reserve these units
 */
void signalPUCDone( struct ProcessingUnitController* theController, 
		    unsigned char bitmask );


#endif /* #ifndef _PUCONT_H_ */

