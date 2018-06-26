/*file: CraneController.h:*/
/*This header file defines the function CraneController*/

#include <pthread.h>
#include "flexInterface.h"

#ifndef _CRANE_CONTROLLER_H_
#define _CRANE_CONTROLLER_H_

/* CraneController class */
/* Handles requests to do things with the cranes.  Before doing anything, */
/* the crane should be requested with request().  This function blocks */
/* until a crane is allocated.  Then, the caller has free reign to move the */
/* crane about, and put and get objects.  When done, signalCraneDone() should */
/* be called */

typedef struct CraneController
{
  pthread_mutex_t *controllerLock;  /* lock for request and signalCraneDone */
  pthread_cond_t *signalledDone;    /* used by request and signalCraneDone */

  unsigned char bitMask1; /*Bitmask for crane 1.  See info for request() */
  unsigned char bitMask2; /*Bitmask for crane 2.  See info for request() */

  struct Sensor_Data *sensorData; /* A link to the sensor storage class. */

} CraneController;



/* function: newCraneController
 * 
 * Allocates resources and initializes a new CraneController object.
 * Sets up all of the member variables EXCEPT sensorData.
 * This should be done externally, by main()
 */
struct CraneController* newCraneController();


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
int request( CraneController *CController,int sourceX, int sourceY, int destX, int destY );


/* function: move_xy
 *
 *  This performs the move operation on the identified
 *  crane to the location destX, destY. Blocks until the
 *  crane reaches its destination
 */
void move_xy(struct CraneController *CController, int whichCrane, int destX, int destY );

/* function: put
 *
 *  For the specified crane, lower the gripper, turn off the magnet
 *  and raise the gripper. Blocks until the put has completed.
 */
void put(struct CraneController *CController, int whichCrane);


/* function: get
 *
 *  For the specified crane, lower the gripper, turn on the magnet,
 *  and raise the gripper. Blocks until the get has completed.
 */
void get(struct CraneController *CController, int whichCrane);



/* function: signalCraneDone
 * 
 *  Signal the controller that we are finished with 
 *  the specified crane, and it may be requested by 
 *  someone else, this clears the mask for the specified crane.
 */
void signalCraneDone(CraneController *CController,int whichCrane);


#endif /* #ifndef _CRANECONTROLLER_H_*/
