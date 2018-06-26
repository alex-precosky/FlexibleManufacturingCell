/* EECE 494 Assignment 6 */
/* main.c */
/* Sets up the control system for execution by setting up the sync objects */
/*    actors and threads. */

#include <stdlib.h>
#include <signal.h>
#include <semaphore.h>
#include <fcntl.h>
#include <stdio.h>

#include "flexInterface.h"
#include "BlankBuffer.h"
#include "CraneController.h"
#include "FeedbeltArrivalThd.h"
#include "ProcessingUnit.h"
#include "ProcessingUnitController.h"
#include "SensorData.h"
#include "SensorPollThread.h"
#include "BlankToUnitThd.h"
#include "BlankToDepositThd.h"
#include "ProcessingUnitThd.h"

/* Package for passing the sync objects around in main.c */
struct SyncStruct
{
  sem_t* newItemSemPtr;
  struct BlankBuffer* blanksToUnitBuf;
  struct BlankBuffer* blanksInUnitsBuf;
  struct BlankBuffer* blanksToDepositBuf;
};

/* Package for passing the pthread_t's around in main.c */
struct ThreadStruct
{
  pthread_t* feedbeltArrivalThreadPtr;
  pthread_t* sensorPollThreadPtr;
  pthread_t* takeBlankToUnitThreadPtr1;
  pthread_t* takeBlankToUnitThreadPtr2;
  pthread_t* processingUnitThreadPtr1;
  pthread_t* processingUnitThreadPtr2;
  pthread_t* processingUnitThreadPtr3;
  pthread_t* processingUnitThreadPtr4;
  pthread_t* blankToDepositThreadPtr;
};

/* Package for passing actors around in main.c */
struct ActorPackage
{
  struct CraneController* craneControllerPtr;
  struct Sensor_Data* sensorDataPtr;
  struct ProcessingUnitController* procUnitControllerPtr;
};

/* Global variables */
sem_t* newItemSem; /* Semaphore verhoganed by the SIGALRM handler */
FILE* debugLog;   /* Used for occational printline debugging */

/* Function Prototypes */
void CreateActors( struct ActorPackage *actorPackage );
void CreateSyncObjects( struct SyncStruct *syncPackage );
void CreateThreads( struct ThreadStruct *threadPackage, struct SyncStruct *syncPackage, 
					struct ActorPackage *actorPackage );
void newItemSignalHandler( int signalNumber );


int main( int argc, char* argv[])
{
  struct ThreadStruct threadPackage;
  struct SyncStruct syncPackage;
  struct ActorPackage actorPackage;
  
  /* Open up the printline debugging file */
  debugLog = fopen("debugLog", "w");

  /* Initialize the interface to the production cell */
  flexInitCommunications();

  /* Create and package up the sync objects to be passed around */
  CreateSyncObjects( &syncPackage );
  newItemSem = syncPackage.newItemSemPtr;
  fprintf( debugLog, "main() semaphore address: %p\n", (void*)newItemSem);

  fprintf(debugLog, "Sync objects created\n");
  fflush(debugLog);

  /* Set up the signal handler for new items arriving on the feedbelt */
  signal( SIGALRM, newItemSignalHandler );
  fprintf(debugLog, "SIGALRM handler installed\n");
  fflush(debugLog);


  /* Create and set-up the actors of the system */
  CreateActors( &actorPackage );
  fprintf(debugLog, "Actors created\n");
  fflush(debugLog);

  /* Launch the threads of the system */
  CreateThreads( &threadPackage, &syncPackage, &actorPackage );
  fprintf(debugLog, "Threads launched\n");
  fflush(debugLog);


  /* start up the belt and go! */
  flexSetFeedBelt( ON );
  flexUpdateTask();

}

/* Signal handler for SIGALRM signals.  Triggered by the light sensor */
/*  at the end of the feed belt */
void newItemSignalHandler( int signalNumber )
{
  sem_post(newItemSem);
}

/* Creates and initializes the system's actors */
/* Mostly means memory allocation */
void CreateActors( struct ActorPackage *actorPackage )
{

  /* Sensor Data */
  actorPackage->sensorDataPtr = (struct Sensor_Data*)
    malloc( sizeof( struct Sensor_Data ) );

  actorPackage->sensorDataPtr->lock = (pthread_mutex_t*)
    malloc( sizeof( pthread_mutex_t ) );

  pthread_mutex_init( actorPackage->sensorDataPtr->lock, NULL );
  
  /* Crane Controller */
  actorPackage->craneControllerPtr = newCraneController(); 
  actorPackage->craneControllerPtr->sensorData = actorPackage->sensorDataPtr;

  /* Processing Unit Controller (and processing units) */
  actorPackage->procUnitControllerPtr = newProcessingUnitController();

}

void CreateSyncObjects( struct SyncStruct *syncPackage )
{
  /* initialize to block on first sem_wait() */
  syncPackage->newItemSemPtr=sem_open( "newItemSem", O_CREAT,0777, 0);

  /* Set up blank buffers */
  syncPackage->blanksToUnitBuf = newBlankBuffer();
  syncPackage->blanksInUnitsBuf = newBlankBuffer();
  syncPackage->blanksToDepositBuf = newBlankBuffer();

  return;
}

void CreateThreads( struct ThreadStruct *threadPackage, struct SyncStruct *syncPackage,
					struct ActorPackage *actorPackage )
{
  /* Argument structs that get passed to threads */
  struct FBArrivalHandlerThdArgs *fbArriveArgs;
  struct SensorPollThreadArgs *sensorPollThdArgs;
  struct BlankToUnitThdArgs *blankToUnitThdArgs;
  struct ProcessingUnitThdArgs *processingUnitThdArgs;
  struct BlankToDepositThdArgs *blankToDepositThdArgs;

  /* Sensor Polling thread *****************************/

  /*package up the arguments*/
  sensorPollThdArgs = 
    (struct SensorPollThreadArgs*) malloc( sizeof( struct SensorPollThreadArgs ) );
  sensorPollThdArgs->sensorData = actorPackage->sensorDataPtr;

  /* start the thread */
  threadPackage->sensorPollThreadPtr = (pthread_t*) malloc( sizeof( pthread_t ) );
  pthread_create( threadPackage->sensorPollThreadPtr, NULL,
		  (void*) SensorPollThread, sensorPollThdArgs );

  /* Feedbelt arrival thread ********************************/

  /*package up the arguments*/
  fbArriveArgs = 
    (struct FBArrivalHandlerThdArgs*) malloc( sizeof( struct FBArrivalHandlerThdArgs ) );
  fbArriveArgs->newItemSemPtr = syncPackage->newItemSemPtr;
  fbArriveArgs->craneControllerPtr = actorPackage->craneControllerPtr;
  fbArriveArgs->blanksToUnitBuf = syncPackage->blanksToUnitBuf;
  fbArriveArgs->procUnitControllerPtr = actorPackage->procUnitControllerPtr;

  /* start the thread */
  threadPackage->feedbeltArrivalThreadPtr = (pthread_t*) malloc( sizeof( pthread_t ));
  pthread_create( threadPackage->feedbeltArrivalThreadPtr, NULL,
		  (void*) FeedBeltArrivalHandlerThread, fbArriveArgs );

  /* Take blank to unit thread **********************************/

  /* package up the arguments */
  blankToUnitThdArgs = 
    (struct BlankToUnitThdArgs*) malloc( sizeof( struct BlankToUnitThdArgs ) );
  blankToUnitThdArgs->blanksToUnitBuf = syncPackage->blanksToUnitBuf;
  blankToUnitThdArgs->blanksInUnitsBuf = syncPackage->blanksInUnitsBuf;
  blankToUnitThdArgs->craneControllerPtr = actorPackage->craneControllerPtr;
  blankToUnitThdArgs->procUnitControllerPtr = actorPackage->procUnitControllerPtr;

  /* start the threads */
  threadPackage->takeBlankToUnitThreadPtr1 = (pthread_t*) malloc( sizeof( pthread_t) );
  threadPackage->takeBlankToUnitThreadPtr2 = (pthread_t*) malloc( sizeof( pthread_t) );
  pthread_create( threadPackage->takeBlankToUnitThreadPtr1, NULL,
		 (void*) TakeBlankToUnitThread, blankToUnitThdArgs );
  pthread_create( threadPackage->takeBlankToUnitThreadPtr2, NULL,
		 (void*) TakeBlankToUnitThread, blankToUnitThdArgs );


  /* Processing Unit Thread *******************************/
  
  /* package up the arguments */
  processingUnitThdArgs = 
    (struct ProcessingUnitThdArgs*) malloc( sizeof( struct ProcessingUnitThdArgs ) );
  processingUnitThdArgs->blanksInUnitsBuf = syncPackage->blanksInUnitsBuf;
  processingUnitThdArgs->blanksToUnitBuf = syncPackage->blanksToUnitBuf;
  processingUnitThdArgs->blanksToDepositBuf = syncPackage->blanksToDepositBuf;
  processingUnitThdArgs->craneControllerPtr = actorPackage->craneControllerPtr;
  processingUnitThdArgs->procUnitControllerPtr = actorPackage->procUnitControllerPtr;

  /* start the threads */
  threadPackage->processingUnitThreadPtr1 = (pthread_t*) malloc( sizeof( pthread_t) );
  threadPackage->processingUnitThreadPtr2 = (pthread_t*) malloc( sizeof( pthread_t) );
  threadPackage->processingUnitThreadPtr3 = (pthread_t*) malloc( sizeof( pthread_t) );
  threadPackage->processingUnitThreadPtr4 = (pthread_t*) malloc( sizeof( pthread_t) );


  pthread_create( threadPackage->processingUnitThreadPtr1, NULL,
		  (void*) ProcessingUnitThread, processingUnitThdArgs );
  pthread_create( threadPackage->processingUnitThreadPtr2, NULL,
		  (void*) ProcessingUnitThread, processingUnitThdArgs );
  pthread_create( threadPackage->processingUnitThreadPtr3, NULL,
		  (void*) ProcessingUnitThread, processingUnitThdArgs );
  pthread_create( threadPackage->processingUnitThreadPtr4, NULL,
		  (void*) ProcessingUnitThread, processingUnitThdArgs );

  /* Take blank to deposit belt thread **************************/
  
  /* package up the arguments */
  blankToDepositThdArgs = 
    (struct BlankToDepositThdArgs*) malloc( sizeof( struct BlankToDepositThdArgs ) );
  blankToDepositThdArgs->blanksToDepositBuf = syncPackage->blanksToDepositBuf;
  blankToDepositThdArgs->craneControllerPtr = actorPackage->craneControllerPtr;
  blankToDepositThdArgs->procUnitControllerPtr = actorPackage->procUnitControllerPtr;
  blankToDepositThdArgs->sensorData = actorPackage->sensorDataPtr;

  /* start the thread */
  threadPackage->blankToDepositThreadPtr = (pthread_t*)  malloc( sizeof( pthread_t ) );
  pthread_create( threadPackage->blankToDepositThreadPtr, NULL,
		  (void*) BlankToDepositThd, blankToDepositThdArgs );

  return;
}

