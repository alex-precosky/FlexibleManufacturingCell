/* Copyright, 2002: Sidney Fels, Dept. of ECE, UBC */

/*
   This the private data space for the flexInterface.  There are 
   all the global variables here.  Beware of name conflicts with application.
*/

#ifndef _FLEXINTERFACE_CONTROL_H
#define _FLEXINTERFACE_CONTROL_H

#include <semaphore.h>

#include "flexInterface.h"

/* parser states */

#define START 0
#define NUM_STEPS 1
#define START_STEP 2
#define STEP_DATA1 4
#define STEP_DATA2 5
#define STEP_DATA3 6
#define FIND_ORDER 7
#define ORDER 8
#define FIND_TIME 9
#define TIME 10

/* hidden data structs */
typedef struct {
  int Start_Feed_Belt;
  int Stop_Feed_Belt;
  int Code_Reader_On;
  int Work_Station_On_1;
  int Work_Station_On_2;
  int Work_Station_On_3;
  int Work_Station_On_4;
  int Crane_X_On_1;
  int Crane_X_Loc_1;
  int Crane_Y_On_1;
  int Crane_Y_Loc_1;
  int Crane_Z_Plus_1;
  int Crane_Z_Minus_1;
  int Crane_Magnet_On_1;
  int Crane_Magnet_Off_1;
  int Crane_X_On_2;
  int Crane_X_Loc_2;
  int Crane_Y_On_2;
  int Crane_Y_Loc_2;
  int Crane_Z_Plus_2;
  int Crane_Z_Minus_2;
  int Crane_Magnet_On_2;
  int Crane_Magnet_Off_2;
} FlexCommand;

/* private data for the interface */
int flex_Belt_Sensor[2];
int flex_Work_Station_Sensors[Number_Of_Processing_Units][2];
int flex_Crane_Sensors[3][Number_Of_Cranes][Number_Of_X_Sensors];
int flex_Read_Code_For_Block;
int flex_RequestForSensorStatus;

FlexCodeStatus flexCodeStatus;
FlexCommand flexCommand;

sem_t flexLock;
sem_t flexWaitForReadLock;

void flex_printSensorStatus(void);

#endif
