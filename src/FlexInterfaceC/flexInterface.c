/* Copyright, 2002: Sidney Fels, Dept. of ECE, UBC */

/* 
   This code is a simple communication interface to the Flexible
   Cell simulator.  The heart of the interface is the flexUpdateTask.
   This is a periodic task that updates the simulator every Update_Period.
   The task has to check for any data coming from the simulation.  This
   includes the status of all the sensors and possibly any bar code data.
   Then, it sends out any pending commands that were issued since the
   last update period.  All the data to and from the simulator are
   text strings delimited with [] and go through stdio.

   When an item on the feedbelt triggers the optical sensor the SIGALRM
   signal is raised for the application programmer.  The barcode is 
   activated and the bar code on the item is read.  This barcode data
   is available to the application programmer 2 update periods from the
   time the barcode reader is activated.

   Sensor data is read every update period.  However, the application programmer
   has to issue a request for data first and then call a blocking read
   to wait for the data.  A semaphore based lock is used to do the
   blocking mechanism.

   Notice this code uses shared variables that are local to this module.
   The shared variables are protected by a lock so they should be 
   thread safe.

COMPILER OPTIONS:  -DDEBUG   # turns on debugging information
                   -DSHOWTICK # prints a message every update tick 
                   -lpthread  # required 

Dependencies: flexInterfaceControl.h

*/


#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <semaphore.h>
#include <signal.h>
#include <time.h>

#include "flexInterface.h"
#include "flexInterfaceControl.h"
#include "flexTime.h"

int flex_Parse_Sensor_Status(void) ;
void flex_Parse_Code_Status(void) ;
int flex_Process_Sensor_Status(void) ;

void flex_getLine(char *line);
void flex_printCode(void) ;
void flexUpdate (void);

/* this must be called by the application programmer to initialize the
   interface */
void flexInitCommunications (void) {
    sem_init(&flexLock, 0, 1);  /* init to no wait */
    sem_init(&flexWaitForReadLock, 0, 0);  /* init to wait */
    flex_Read_Code_For_Block = FALSE;
    flex_RequestForSensorStatus = FALSE; /* user has not made a request */
}

/* this is the main periodic loop that checks the data coming from the
   simulation and sends data to the simulation */
void flexUpdateTask () {

     char line[512];
     struct timespec waitPeriod, waitPeriodRem;
     long int Period = Update_Period ;


   while(TRUE) {

#ifdef SHOWTICK
   fprintf(stderr, "***************     UPDATE SIMULATION           ******\n");
#endif 

     /* read first [ */
    flex_getLine(line);

     flex_Parse_Sensor_Status();
     if (flex_Read_Code_For_Block == TRUE) {
        /* Parse the Code block */
        flex_Parse_Code_Status();
     }

     flex_Process_Sensor_Status();

    /* get last . */
    flex_getLine(line);

    /* send updated outputs */
    flexUpdate();
    flexReset();

    /* wait one update period tick (which represents one simulation interval) */
    toTimeSpec(Update_Period, &waitPeriod);
    flexWait(&waitPeriod, &waitPeriodRem); /* this should be in absolute time i.e. a delay until statement */
  }

}


/* set sensor status to default values */
void flex_init_Sensor_Status(void) {

   int i,j;

   sem_wait(&flexLock);
   flex_Belt_Sensor[Feed_Photosensor] = FALSE;
   flex_Belt_Sensor[Deposit_Photosensor] = FALSE;
   for (i = 0; i < Number_Of_Processing_Units; i++) {
      flex_Work_Station_Sensors[i][OCCUPIED] = FALSE;
      flex_Work_Station_Sensors[i][PROCESSING] = FALSE;
   }

   for (i = 0; i < Number_Of_X_Sensors; i++) {
      flex_Crane_Sensors[X][0][i] = FALSE;
   }

   for (i = 0; i < Number_Of_Cranes; i++) {
      for (j = 0; j < Number_Of_Y_Sensors; j++) {
         flex_Crane_Sensors[Y][i][j] = FALSE;
      }
      for (j = 0; j < Number_Of_Z_Sensors; j++) {
         flex_Crane_Sensors[Z][i][j] = FALSE;
      }
   }
   sem_post(&flexLock);
}

/* check if a new item has triggered the feedbelt sensor */
int flex_Process_Sensor_Status() {

    static int Block_At_Feed_Photosensor_After_Last_Update = FALSE;

   sem_wait(&flexLock);
  
    /* only signal once for each new item */
    if ((flex_Belt_Sensor[Feed_Photosensor] == TRUE) && (Block_At_Feed_Photosensor_After_Last_Update == FALSE)) {
       /* raise signal */
 
       sem_post(&flexLock);
       raise(SIGALRM);

       /* have to release the lock here so the output command can run */
       flexSetCodeReaderOn();
       sem_wait(&flexLock);

       flex_Read_Code_For_Block = TRUE;

       Block_At_Feed_Photosensor_After_Last_Update = TRUE;
    } else if (flex_Belt_Sensor[Feed_Photosensor] == FALSE) {
       Block_At_Feed_Photosensor_After_Last_Update = FALSE;
       flex_Read_Code_For_Block = FALSE;
    } else {
       flex_Read_Code_For_Block = FALSE;
    }

   sem_post(&flexLock);
}

/* application use this function to issue a request for sensor data */
void flexRequestSensorStatus() {

     sem_wait(&flexLock);
     flex_RequestForSensorStatus = TRUE;
     sem_post(&flexLock);
}
     

/* applications use this to do a blocking read on the data after the request
   has been made */
void flexReadSensorStatus(int bs[], int wss[][2], int cs[3][Number_Of_Cranes][Number_Of_X_Sensors]) {
 
     int i,j;

     /* block on data - this is only released if the user made a read request and data has been read */
     sem_wait(&flexWaitForReadLock);

     sem_wait(&flexLock);
    
      bs[0] = flex_Belt_Sensor[0];
      bs[1] = flex_Belt_Sensor[1];

      for (i = 0; i < Number_Of_Processing_Units; i++) {
         wss[i][0] = flex_Work_Station_Sensors[i][0];
         wss[i][1] = flex_Work_Station_Sensors[i][1];
      }


      for (i = 0; i < Number_Of_X_Sensors; i++) {
         cs[X][0][i] = flex_Crane_Sensors[X][0][i];
      }

      for (i = 0; i < Number_Of_Cranes; i++) {
        for (j = 0; j < Number_Of_Y_Sensors; j++) {
            cs[Y][i][j] = flex_Crane_Sensors[Y][i][j];
        }
        for (j = 0; j < Number_Of_Z_Sensors; j++) {
            cs[Z][i][j] = flex_Crane_Sensors[Z][i][j];
        }
      }
      
     sem_post(&flexLock);
}

/*  applications use this to read the bar code data */
void flexReadCodeStatus(FlexCodeStatus *cs) {

    int i;

    sem_wait(&flexLock);
    cs->numSteps = flexCodeStatus.numSteps;
    cs->maxTime = flexCodeStatus.maxTime;
    cs->ordered = flexCodeStatus.ordered;

    for (i = 0; i < flexCodeStatus.numSteps; i++) {
      cs->step[i].unitNumber = flexCodeStatus.step[i].unitNumber;
      cs->step[i].minTime = flexCodeStatus.step[i].minTime;
      cs->step[i].maxTime = flexCodeStatus.step[i].maxTime;
   }
   sem_post(&flexLock);
}

/* this is used to parse the incoming string from the simulator - this
   parses only the sensor information which is returned each update period */
int flex_Parse_Sensor_Status(void) {

   char command[128];
     char line[100];
   int i, j;
   int charIndex;
   

  /* assume sensors are all False */
  flex_init_Sensor_Status();

  /* Belt sensors */

  sem_wait(&flexLock);
  flex_getLine(line);
  if (line[14] == 't') flex_Belt_Sensor[Feed_Photosensor] = TRUE;
  
  flex_getLine(line);
  if (line[17] == 't') flex_Belt_Sensor[Deposit_Photosensor] = TRUE;

  /* Workstation Sensors */

  for (i = 0; i < Number_Of_Processing_Units; i++) {
     flex_getLine(line);
     if (line[18] == 't') flex_Work_Station_Sensors[i][OCCUPIED] = TRUE;
     if (line[20] == 't') flex_Work_Station_Sensors[i][PROCESSING] = TRUE;
  }

  /* Crane Sensors */
  charIndex = 14;
  flex_getLine(line);
 
  /* first get X locations - this is for all the cranes */
  for (i = 0 ; i < Number_Of_X_Sensors; i++) {
     if (line[charIndex] == 't') flex_Crane_Sensors[X][0][i] = TRUE;
     charIndex += 2;
  }

  charIndex = 32;
  for (i = 0; i < Number_Of_Cranes; i++ ) {
      
      /* get the Y positions for each crane */
      for (j = 0; j < Number_Of_Y_Sensors; j++) {
         if (line[charIndex] == 't') flex_Crane_Sensors[Y][i][j] = TRUE;
         charIndex += 2;
      }

       /* now for the Z crane positions */
     charIndex += 2;
     for (j = 0; j < Number_Of_Z_Sensors; j++) {
         if (line[charIndex] == 't') flex_Crane_Sensors[Z][i][j] = TRUE;
         charIndex += 2;
     }

     charIndex += 2;
  }

   if ( flex_RequestForSensorStatus == TRUE) {;
      sem_post(&flexWaitForReadLock);
      flex_RequestForSensorStatus = FALSE;
   }

#ifdef DEBUG
   flex_printSensorStatus();
#endif
   sem_post(&flexLock);
  
   return 0;


}

/* set code status to its default state */
void flex_init_Code_Status(void) {

    int i;

   sem_wait(&flexLock);
    flexCodeStatus.numSteps = 0;
    flexCodeStatus.maxTime = 0;
    flexCodeStatus.ordered = FALSE;

    for (i = 0 ; i < MAX_NUMBER_OF_STEPS; i++) {
       flexCodeStatus.step[i].unitNumber = 0;
       flexCodeStatus.step[i].minTime = 0;
       flexCodeStatus.step[i].maxTime = 0;
   }
   sem_post(&flexLock);

}

/* parse the bar code status line.  This uses a state machine to move through
   the status line byte by byte looking for the appropriate data and 
   delimiters */
void flex_Parse_Code_Status(void) {

  char line[512];
  int i, nidx = 0, stepIdx = 0 ;
  int parseState;
  char number[50];
  


  flex_init_Code_Status();
  

  sem_wait(&flexLock);
  flex_getLine(line);

  parseState = START;
  for (i = 0; i < strlen(line); i++) {
    switch (parseState) {
      case START:
         if (line[i] == '<') parseState = NUM_STEPS;
         nidx = 0;
         break;

      case NUM_STEPS:
         if (line[i] != ',') {
            number[nidx++] = line[i];
         } else {
           number[nidx] = '\0';
           flexCodeStatus.numSteps = atoi(number);
           if (flexCodeStatus.numSteps > 0) {
              parseState = START_STEP;
              stepIdx = -1;
           } else {
              parseState = FIND_ORDER;
           }
           nidx = 0;
         }
         break;

      case START_STEP:
        if(line[i] == '<') {
           parseState = STEP_DATA1;
           stepIdx++;
        }
        break;

      case STEP_DATA1:
         if (line[i] != ',') {
            number[nidx++] = line[i];
         } else {
           number[nidx] = '\0';
           flexCodeStatus.step[stepIdx].unitNumber = atol(number);
           parseState = STEP_DATA2;
           nidx = 0;
        }
        break;

      case STEP_DATA2:
         if (line[i] != ',') {
            number[nidx++] = line[i];
         } else {
           number[nidx] = '\0';
           flexCodeStatus.step[stepIdx].minTime = atol(number);
           parseState = STEP_DATA3;
           nidx = 0;
        }
        break;

      case STEP_DATA3:
         if (line[i] != '>') {
            number[nidx++] = line[i];
         } else {
           number[nidx] = '\0';
           flexCodeStatus.step[stepIdx].maxTime = atol(number);
           if ((stepIdx + 1) == flexCodeStatus.numSteps) {
              parseState = FIND_ORDER;
           } else {
              parseState = START_STEP;
           }
           nidx = 0;
        }
        break;

      case FIND_ORDER:
         if (line[i] == ',') {
             parseState = ORDER;
         }
         break;

      case ORDER:
         if (line[i] == 't') {
             flexCodeStatus.ordered = TRUE;
         } else {
             flexCodeStatus.ordered = FALSE;
         }
         parseState = FIND_TIME;
         break;

      case FIND_TIME:
         if (line[i] == ',') {
             parseState = TIME;
             nidx = 0;
         }
         break;

      case TIME:
         if (line[i] != '>') {
            number[nidx++] = line[i];
         } else {
           number[nidx] = '\0';
           flexCodeStatus.maxTime = atol(number);
           parseState = START;
           nidx = 0;
        }
        break;
      
    }
  }

#ifdef DEBUG
  fprintf(stderr,"updateTask readItem...\n");
  flex_printCode();
#endif

   sem_post(&flexLock);

}  


/* debug routines to print out the current bar code that was read */
void flex_printCode(void) {

   int i;

   fprintf(stderr,"CodeStatus:\n");
   fprintf(stderr,"    numSteps %d\n", flexCodeStatus.numSteps);
   fprintf(stderr,"    maxTime  %ld\n", flexCodeStatus.maxTime);
   fprintf(stderr,"    ordered  %d\n", flexCodeStatus.ordered);

   for (i = 0 ; i < flexCodeStatus.numSteps; i++) {
     fprintf(stderr,"         step %d\n", i);
     fprintf(stderr,"              unitNumber %d\n", flexCodeStatus.step[i].unitNumber);
     fprintf(stderr,"              minTime %ld\n", flexCodeStatus.step[i].minTime);
     fprintf(stderr,"              maxTime %ld\n", flexCodeStatus.step[i].maxTime);
   }
}
     

   
/* function to read a line from stdin */
void flex_getLine(char *line) {
   if (scanf("%[^\n]%*c", line) == EOF) {
      fprintf(stderr, "lost connection to simulator\n");
      exit(1);
   }
}


/* debug routing to print the latest sensor data */
void flex_printSensorStatus() {

   int i,j;

   fprintf(stderr,"Feed_Photosensor = %d\n", flex_Belt_Sensor[Feed_Photosensor]);
   fprintf(stderr,"Deposit_Photosensor = %d\n", flex_Belt_Sensor[Deposit_Photosensor]);
   for (i = 0; i < Number_Of_Processing_Units; i++) {
      fprintf(stderr,"Work_Station_Sensors[%d]  OCCUPIED = %d  PROCESSING = %d\n", i, flex_Work_Station_Sensors[i][OCCUPIED], flex_Work_Station_Sensors[i][PROCESSING]);
   }

   fprintf(stderr,"Cranes: \n");
   fprintf(stderr, "X positions: ");
   for (i = 0; i < Number_Of_X_Sensors; i++) {
     fprintf(stderr,"%d ", flex_Crane_Sensors[X][0][i]);
   }
   fprintf(stderr, "\n");

   for (i = 0; i < Number_Of_Cranes; i++) {
      fprintf(stderr, "Crane %d: \n", i);

      fprintf(stderr, "   Y position: ");
      for (j = 0; j < Number_Of_Y_Sensors; j++) {
         fprintf(stderr, " %d ",  flex_Crane_Sensors[Y][i][j]);
      }
      fprintf(stderr, "\n");

      fprintf(stderr, "   Z position: ");
      for (j = 0; j < Number_Of_Z_Sensors; j++) {
         fprintf(stderr, " %d ",  flex_Crane_Sensors[Z][i][j]);
      }
      fprintf(stderr, "\n");
   }
}
    


/* Here are the interface commands to control the simulator.  These 
   are used by the application */

/* reset the command structure (not normally used by the application) */
void flexReset ()
{
   sem_wait(&flexLock);
  flexCommand.Start_Feed_Belt = FALSE;
  flexCommand.Stop_Feed_Belt = FALSE;
  flexCommand.Code_Reader_On = FALSE; 
  flexCommand.Work_Station_On_1 = FALSE;
  flexCommand.Work_Station_On_2 = FALSE;
  flexCommand.Work_Station_On_3 = FALSE;
  flexCommand.Work_Station_On_4 = FALSE;
  flexCommand.Crane_X_On_1 = FALSE;
  flexCommand.Crane_X_Loc_1 = 1;
  flexCommand.Crane_Y_On_1 = FALSE;
  flexCommand.Crane_Y_Loc_1 = 2;
  flexCommand.Crane_Z_Plus_1 = FALSE;
  flexCommand.Crane_Z_Minus_1 = FALSE;
  flexCommand.Crane_Magnet_On_1 = FALSE;
  flexCommand.Crane_Magnet_Off_1 = FALSE;
  flexCommand.Crane_X_On_2 = FALSE;
  flexCommand.Crane_X_Loc_2 = 8;
  flexCommand.Crane_Y_On_2 = FALSE;
  flexCommand.Crane_Y_Loc_2 = 2;
  flexCommand.Crane_Z_Plus_2 = FALSE;
  flexCommand.Crane_Z_Minus_2 = FALSE;
  flexCommand.Crane_Magnet_On_2 = FALSE;
  flexCommand.Crane_Magnet_Off_2 = FALSE;
  sem_post(&flexLock);

}

/* update the simulation by sending a string with the new commands to
   stdout - this is not normally used by the application */
void flexUpdate (void)
{
  char out [512];
  char temp[3];

   sem_wait(&flexLock);

  out[0] = '\0';
  
  strcat (out, "[\n");
  

  if (flexCommand.Start_Feed_Belt)    strcat (out, "FeedBeltOn\n");
  if (flexCommand.Stop_Feed_Belt)     strcat (out, "FeedBeltOff\n");
  if (flexCommand.Code_Reader_On)     strcat (out, "CodeSensorOn\n");
  if (flexCommand.Work_Station_On_1)  strcat (out, "WorkStationOn1 1000\n");
  if (flexCommand.Work_Station_On_2)  strcat (out, "WorkStationOn2 1000\n");
  if (flexCommand.Work_Station_On_3)  strcat (out, "WorkStationOn3 1000\n");
  if (flexCommand.Work_Station_On_4)  strcat (out, "WorkStationOn4 1000\n");
  if (flexCommand.Crane_X_On_1)  {
              strcat (out, "PortalX1 ");
              sprintf(temp,"%d", flexCommand.Crane_X_Loc_1);
              strcat (out, temp);
              strcat (out, "\n");
  }
  if (flexCommand.Crane_Y_On_1)  { 
              strcat (out, "PortalY1 ");
              sprintf(temp,"%d", flexCommand.Crane_Y_Loc_1);
              strcat (out, temp);
              strcat (out, "\n");
  }
  if (flexCommand.Crane_Z_Plus_1)     strcat (out, "PortalUp1\n");
  if (flexCommand.Crane_Z_Minus_1)    strcat (out, "PortalDown1\n");
  if (flexCommand.Crane_Magnet_On_1)  strcat (out, "MagnetOn1\n");
  if (flexCommand.Crane_Magnet_Off_1) strcat (out, "MagnetOff1\n");
  if (flexCommand.Crane_X_On_2)   {
              strcat (out, "PortalX2 ");
              sprintf(temp,"%d", flexCommand.Crane_X_Loc_2);
              strcat (out, temp);
              strcat (out, "\n");
  }
  if (flexCommand.Crane_Y_On_2)    {
              strcat (out, "PortalY2 ");
              sprintf(temp,"%d", flexCommand.Crane_Y_Loc_2);
              strcat (out, temp);
              strcat (out, "\n");
  }
  if (flexCommand.Crane_Z_Plus_2)     strcat (out, "PortalUp2\n");
  if (flexCommand.Crane_Z_Minus_2)    strcat (out, "PortalDown2\n");
  if (flexCommand.Crane_Magnet_On_2)  strcat (out, "MagnetOn2\n");
  if (flexCommand.Crane_Magnet_Off_2) strcat (out, "MagnetOff2\n");
  
  sem_post(&flexLock);

  strcat (out, "]");
  
  printf ("%s\n", out);
  fflush(NULL);

#ifdef DEBUG
fprintf(stderr,"sending: %s\n", out);
#endif

  
}
  
/* turn ON/OFF the feedbelt */
void flexSetFeedBelt (int what)
{
  sem_wait(&flexLock);
  if (what == ON)
    flexCommand.Start_Feed_Belt = TRUE;
  
  if (what == OFF)
    flexCommand.Stop_Feed_Belt = TRUE;
  sem_post(&flexLock);
}

/* turn on the bar code reader - normally done by the flexInterface */
void flexSetCodeReaderOn ()
{
  sem_wait(&flexLock);

  flexCommand.Code_Reader_On = TRUE;

  sem_post(&flexLock);
}

/* turn on a workstation */
void flexSetWorkStationOn (int which)
{
  sem_wait(&flexLock);
  switch (which) {
    case 1:
      flexCommand.Work_Station_On_1 = TRUE;
      break;
    case 2:
      flexCommand.Work_Station_On_2 = TRUE;
      break;
    case 3:
      flexCommand.Work_Station_On_3 = TRUE;
      break;
    case 4:
      flexCommand.Work_Station_On_4 = TRUE;
      break;
    default:
      break;
  }
  sem_post(&flexLock);
}


/* move a crane (which) to a new X position (where) */
void flexSetCraneX (int which, int where) {
  sem_wait(&flexLock);
      if (which == 1) {
          flexCommand.Crane_X_On_1 = TRUE;
          flexCommand.Crane_X_Loc_1 = where;
      }
      if (which == 2) {
          flexCommand.Crane_X_On_2 = TRUE;
          flexCommand.Crane_X_Loc_2 = where;
      }
  sem_post(&flexLock);
}

/* move a crane (which) to a new Y position (where) */
void flexSetCraneY (int which, int where) {
  sem_wait(&flexLock);
      if (which == 1) {
          flexCommand.Crane_Y_On_1 = TRUE;
          flexCommand.Crane_Y_Loc_1 = where;
      }
      if (which == 2) {
          flexCommand.Crane_Y_On_2 = TRUE;
          flexCommand.Crane_Y_Loc_2 = where;
      }
  sem_post(&flexLock);
}

/* turn OFF/ON a crane's magnet */
void flexSetCraneMagnet (int which, int how) {
  sem_wait(&flexLock);
      if (which == 1) {
        if (how == ON)  flexCommand.Crane_Magnet_On_1 = TRUE;
        if (how == OFF) flexCommand.Crane_Magnet_Off_1 = TRUE;
      }
      if (which == 2) {
        if (how == ON)  flexCommand.Crane_Magnet_On_2 = TRUE;
        if (how == OFF) flexCommand.Crane_Magnet_Off_2 = TRUE;
      }
  sem_post(&flexLock);
}

/* move a crane (which) to a new Z position (where = UP/DOWN) */
void flexSetCraneZ (int which, int where) {
  sem_wait(&flexLock);
      if (which == 1) {
        if (where == UP)   flexCommand.Crane_Z_Plus_1 = TRUE;
        if (where == DOWN) flexCommand.Crane_Z_Minus_1 = TRUE;
      }
      if (which == 2) {
        if (where == UP)   flexCommand.Crane_Z_Plus_2 = TRUE;
        if (where == DOWN) flexCommand.Crane_Z_Minus_2 = TRUE;
      }
  sem_post(&flexLock);
}

