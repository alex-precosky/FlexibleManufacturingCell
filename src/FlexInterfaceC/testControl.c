/* Copyright, 2002: Sidney Fels, Dept. of ECE, UBC */

/* this code show an example of how to interface to the flexible cell
   simulator using the flexInterface library.   It effectively sets up
   two threads to control the simulation.  

   Thread 1: This thread handles the
   situation when an item has activated the optical sensor on the 
   feedbelt.  The signal just unlocks a lock so that it completes quickly.  
   There is a thread waiting on the lock and gets released so that it
   can handle the feedbelt quickly.  Notice that a SIGALRM is generated
   whenever an item trips the optical sensor.  However, we don't do
   everything in the signal handler since it will intefere with the
   main thread of execution (which is the periodic task that is running
   the communication interface).  So, it is best to use the signal handler
   to release a waiting task.

   Thread 2: This is a polling thread that is requesting and getting
   the sensor status at some frequency.  In this example it samples the
   sensors every 2 update periods (which is 2 * sync interval).

Dependency:  This code was created to work with the flexInterface library.
             The library header file is flexInterface.h.


Compiling options required:   -lflexInterface -lm -lpthread

*/

#include<stdio.h>
#include<unistd.h>
#include<time.h>
#include<signal.h>
#include<semaphore.h>
#include<errno.h>
#include<string.h>
#include <fcntl.h>
#include <pthread.h>

#include "flexTime.h"
#include "flexInterface.h"

void readCode(void *dummy);
void newItemArrival(int signum);
void pollSensors (void *dummy) ;
void printCode(FlexCodeStatus *code) ;
void printSensorStatus(int bs[], int wss[4][2], int cs[3][Number_Of_Cranes][Number_Of_X_Sensors]) ;

/* lock that is used to release the thread waiting for items to appear */
sem_t *newItem;

/* the sample main program:
     it sets up the two threads and then calls the flexUpdateTask procedure
     that manages the communications with the simulator 
*/
int main(int argc, char *argv[]) {

    pthread_t handleNewItemThread, pollSensorsThread;

    /* the communication thread uses SIGALRM to indicate that a newItem is available */
    signal(SIGALRM, newItemArrival);

    /* initialize the communication interface */
    flexInitCommunications();

    newItem = sem_open("myse2m", O_CREAT, 7, 0);  /* set up to wait */
	perror(NULL);

    /* set up thread to deal with new item arrivals */
    pthread_create(&handleNewItemThread, NULL, readCode, NULL); 

    /* set up polling thread to request new sensor data every so often */
    pthread_create(&pollSensorsThread, NULL, pollSensors, NULL); 


    /* EXAMPLE ONLY: do something to show the simulator connection is working */
    /* turn on the feed belt */
     flexReset();
fprintf(stderr,"turn on feedbelt \n");
     flexSetFeedBelt(ON); 
    /* move crane 1 to x=3 y = 3 */
fprintf(stderr,"move crane 1 X\n");
    flexSetCraneX(1, 3);
fprintf(stderr,"move crane 1 Y\n");
    flexSetCraneY(1, 3);

/* start the background loop for updating the simulator - this should always
   be the last call of your main thread */
   flexUpdateTask();

 
}


/* signal handler that is notified when a new item arrives */
void newItemArrival(int signum) {
    sem_post(newItem);
	fprintf(stderr, "Released semaphore newItem\n");
}

/* Thread that is release for each new arrival.  Note that it turns off the
   feedbelt so items don't fall on the floor. */
void readCode(void *dummy) {
   
    FlexCodeStatus code;
    struct timespec barCodeReaderWait, barCodeReaderRem;

    while (1) {
       sem_wait(newItem);

       /* turn off feedbelt ASAP */
       flexSetFeedBelt(OFF);

       fprintf(stderr, "\n *******  new item has arrived \n");

       /* read code - you have to wait for two simulator updates to 
          make sure enough time has elapsed before getting the data back */
       toTimeSpec(2 * Update_Period, &barCodeReaderWait);
       flexWait(&barCodeReaderWait, &barCodeReaderRem); 


       flexReadCodeStatus(&code);
       fprintf(stderr,"*** printing out code \n");
       printCode(&code);
    }
}


/* Thread the polls the sensor values */
void pollSensors (void *dummy) {

    struct timespec pollingRate, pollingRateRem;

    int Belt_Sensor[2];
    int Work_Station_Sensor[Number_Of_Processing_Units][2];
    int Crane_Sensor[3][Number_Of_Cranes][Number_Of_X_Sensors];

    /* setup some arbitrary polling frequency to get data from the system 
        - in this example it is almost every 2 update periods - you should
        subtract off the time to process records to get a more accurate
        polling frequency */

    /* To request data call flexRequestSensorStatus and then the data
       will be sent back to you.  You then make a blocking read call to
       wait for the data */

    while (1) {

       flexRequestSensorStatus();

       /* this blocks waiting for the data to arrive */
       flexReadSensorStatus(Belt_Sensor, Work_Station_Sensor, Crane_Sensor);

       fprintf(stderr, "\n\n Got Sensor Data: \n");
       printSensorStatus(Belt_Sensor, Work_Station_Sensor, Crane_Sensor);

       /* poll every 2 update cycles */
       toTimeSpec( 2 * Update_Period, &pollingRate);

       flexWait(&pollingRate, &pollingRateRem);
          
    }

}

       
/* helpful functions to see the bar code read */
void printCode(FlexCodeStatus *code) {

   int i;

   fprintf(stderr,"CodeStatus:\n");
   fprintf(stderr,"    numSteps %d\n", code->numSteps);
   fprintf(stderr,"    maxTime  %ld\n", code->maxTime);
   fprintf(stderr,"    ordered  %d\n", code->ordered);

   for (i = 0 ; i < code->numSteps; i++) {
     fprintf(stderr,"         step %d\n", i);
     fprintf(stderr,"              unitNumber %d\n", code->step[i].unitNumber);
     fprintf(stderr,"              minTime %ld\n", code->step[i].minTime);
     fprintf(stderr,"              maxTime %ld\n", code->step[i].maxTime);
   }
}

/* helpful functions to see the status of the sensors that you read */
void printSensorStatus(int bs[], int wss[4][2], int cs[3][Number_Of_Cranes][Number_Of_X_Sensors]) {

   int i,j;

   fprintf(stderr,"Feed_Photosensor = %d\n", bs[Feed_Photosensor]);
   fprintf(stderr,"Deposit_Photosensor = %d\n", bs[Deposit_Photosensor]);
   for (i = 0; i < Number_Of_Processing_Units; i++) {
      fprintf(stderr,"Work_Station_Sensors[%d]  OCCUPIED = %d  PROCESSING = %d\n", i, wss[i][OCCUPIED], wss[i][PROCESSING]);
   }

   fprintf(stderr,"Cranes: \n");
   fprintf(stderr, "X positions: ");
   for (i = 0; i < Number_Of_X_Sensors; i++) {
     fprintf(stderr,"%d ", cs[X][0][i]);
   }
   fprintf(stderr, "\n");

   for (i = 0; i < Number_Of_Cranes; i++) {
      fprintf(stderr, "Crane %d: \n", i);

      fprintf(stderr, "   Y position: ");
      for (j = 0; j < Number_Of_Y_Sensors; j++) {
         fprintf(stderr, " %d ",  cs[Y][i][j]);
      }
      fprintf(stderr, "\n");

      fprintf(stderr, "   Z position: ");
      for (j = 0; j < Number_Of_Z_Sensors; j++) {
         fprintf(stderr, " %d ",  cs[Z][i][j]);
      }
      fprintf(stderr, "\n");
   }
}
