/*file CraneController.c:*/
/*This source file defines the functions of CraneController*/

#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#include "flexTime.h"
#include "flexInterface.h"
#include "CraneController.h"
#include "SensorData.h"

/* function: newCraneController
 * 
 * Allocates resources and initializes a new CraneController object.
 * Sets up all of the member variables EXCEPT sensorData.
 * This should be done externally, by main()
 */
struct CraneController* newCraneController()
{
  struct CraneController *newController;
  
 newController = (struct CraneController*)malloc( sizeof( struct CraneController ) );

 newController->controllerLock = 
   (pthread_mutex_t*) malloc( sizeof( pthread_mutex_t ) );

 newController->signalledDone = (pthread_cond_t*)
   malloc( sizeof( pthread_cond_t ) );

 pthread_mutex_init( newController->controllerLock, NULL );
 
 pthread_cond_init( newController->signalledDone, NULL );

 newController->bitMask1 = 0;
 newController->bitMask2 = 0;
 
 return newController;

}

/* function: request
 *
 *  request takes in the source x & y coordinates of the requesting
 *  blank and the destination x & y coordinates of the requesting blank
 *  determines the crane that can perform the movement and prepares the 
 *  system for the motion by moving the other crane out of the way. It 
 *  does this by checking bitMask1 and bitMask2 against the requested
 *  motion. Further, it is also responsible for setting these masks 
 *  as reserved before returning the crane number (either 1 or 2).
 *
 *
 * RETURN VALUE
 *  Returns the crane identifier (integer) of the crane that has been
 *  allocated to handle the request.
 *
 */
int request( CraneController *CController, int sourceX, int sourceY, 
	     int destX, int destY )
{
	int whichCrane;
	int i;
	unsigned char bitMask=0;
	int testAvail = FALSE;

	extern FILE* debugLog;

	/* First, we figure out which crane will service the request */

	/*Case when you are removing from the feedbelt*/
	if( (sourceX == 1) && (sourceY == 2) )	
		whichCrane = 1;

	/*Case when you are putting on the deposit belt*/
	else if( (destX == 8) && (destY == 2) )	
		whichCrane = 2;

	/*Case when you are moving in Y but not X on X=3*/
	else if((sourceX == 3) && (destX == 3) ) 
		whichCrane = 1;

	/*Case when you are moving in Y but not X on X=6*/
	else if((sourceX == 6) && (destX == 6) ) 
		whichCrane = 2;

	/*Crane 2 always does left to right moves between procesing units*/
	else if((sourceX == 3) && (destX == 6) ) 
		whichCrane = 2;

	/*Crane 1 always does right to left moves between procesing units*/
	else if((sourceX == 6) && (destX == 3) ) 
		whichCrane = 1;

	/* not likely to get here, but give it a crane for odd moves anyway */
	else
	  whichCrane = 2; 


	/*Get the lock for the controller*/
	pthread_mutex_lock(CController->controllerLock);

	/* Find the bitmask we need to reserve */
	for( i = 1; i <= 8; i++ )
	  {
	    /* if X pos'n i is in between sourceX and destX, inclusive... */
	    /* then set the bitMask bit for that position */
	    if( (i <= destX  && i >= sourceX) || (i <= sourceX && i >= destX) )
	      bitMask = bitMask | (0x01 << (7-i+1));

	  }

	/*Check to see if the crane required is available*/
	/* i.e. if desired crane is not currently reserved, */
	/*and the other crane doesn't have a reservation in the way */
	while (testAvail == FALSE)
	  {
	    if ( (whichCrane == 1) && (CController->bitMask1 == 0) && 
		 ((bitMask & CController->bitMask2) == 0) )
	      testAvail = TRUE;
	    else if ( (whichCrane == 2) && (CController->bitMask2 == 0) && 
		      ((bitMask & CController->bitMask1) == 0) )
	      testAvail = TRUE;
	    
	    if (testAvail == FALSE)
	      pthread_cond_wait(CController->signalledDone, 
				CController->controllerLock);
	  }
	
	/* set the reservation bits on the appropriate crane */
	if( whichCrane == 1)
	  CController->bitMask1 = bitMask;
	else
	  CController->bitMask2 = bitMask;
	
	
	
	
	/*Move the other crane out of the way*/
	/*Use the non-blocking flexSetCraneX/Y rather than blocking move_xy */
	if(whichCrane == 1 && CController->bitMask2 == 0)
	  {
	    flexSetCraneX( 2, 7 );
	    flexSetCraneY( 2, 2 );
	  }
	else if( whichCrane == 2 && CController->bitMask1 == 0)
	  {
	    flexSetCraneX( 1, 2 );
	    flexSetCraneY( 1, 2 );
	  }
	
	/*release the lock*/
	pthread_mutex_unlock(CController->controllerLock);

	return whichCrane;
}



/* function: move_xy
 *
 *  This performs the move operation on the identified
 *  crane to the location destX, destY. Blocks until the
 *  crane reaches its destination
 */
void move_xy(struct CraneController *CController, int whichCrane, 
	     int destX, int destY )
{
  struct timespec waitPeriod, timeRem;

  flexSetCraneX (whichCrane, destX);
  flexSetCraneY (whichCrane, destY);
  
  toTimeSpec(Update_Period, &waitPeriod);

  /* Wait until we're there before returning*/
  while( !CController->sensorData->Crane_Sensor[X][0][destX-1]
	 || !CController->sensorData->Crane_Sensor[Y][whichCrane-1][destY-1] )
    flexWait(&waitPeriod, &timeRem );

}

/* function: put
 *
 *  For the specified crane, lower the gripper, turn off the magnet
 *  and raise the gripper. Blocks until the put has completed.
 */
void put(struct CraneController *CController, int whichCrane)
{
  
  struct timespec waitPeriod, timeRem;
  
  toTimeSpec(Update_Period, &waitPeriod);


  flexSetCraneZ (whichCrane, DOWN);

  /* While up or not down, wait a period */
  while( CController->sensorData->Crane_Sensor[Z][whichCrane-1][0]
	 || !(CController->sensorData->Crane_Sensor[Z][whichCrane-1][1])) 
    flexWait(&waitPeriod, &timeRem );

  flexSetCraneMagnet (whichCrane, OFF);
  
  flexSetCraneZ (whichCrane, UP);

  /* While down or not up, wait a period */
  while( CController->sensorData->Crane_Sensor[Z][whichCrane-1][1]
	 || !(CController->sensorData->Crane_Sensor[Z][whichCrane-1][0]))	  
    flexWait(&waitPeriod, &timeRem );

}


/* function: get
 *
 *  For the specified crane, lower the gripper, turn on the magnet,
 *  and raise the gripper. Blocks until the get has completed.
 */
void get(struct CraneController *CController, int whichCrane)
{
  struct timespec waitPeriod, timeRem;
  
  toTimeSpec(Update_Period, &waitPeriod);
  
  /* Lower the crane */
  flexSetCraneZ (whichCrane, DOWN);
  /* While up or not down, wait a period */
  while( CController->sensorData->Crane_Sensor[Z][whichCrane-1][0]
	 || !(CController->sensorData->Crane_Sensor[Z][whichCrane-1][1]))
    flexWait(&waitPeriod, &timeRem );
  
  flexSetCraneMagnet (whichCrane, ON);
  
  /* Raise the crane */
  flexSetCraneZ (whichCrane, UP);
  /* While down or not up, wait a period */
  while( CController->sensorData->Crane_Sensor[Z][whichCrane-1][1]
	 || !(CController->sensorData->Crane_Sensor[Z][whichCrane-1][0]))
    flexWait(&waitPeriod, &timeRem );
	
}



/* function: signalCraneDone
 * 
 *  Signal the controller that we are finished with 
 *  the specified crane, and it may be requested by 
 *  someone else, this clears the mask for the specified crane.
 */
void signalCraneDone(CraneController *CController,int whichCrane)
{
    pthread_mutex_lock(CController->controllerLock);
    
    /*Remove the reservations of the crane specified by whichCrane*/
    if (whichCrane == 1)
      CController->bitMask1 = 0x00;
    else
      CController->bitMask2 = 0x00;
    
    pthread_mutex_unlock(CController->controllerLock);
    pthread_cond_signal(CController->signalledDone);
    
}


