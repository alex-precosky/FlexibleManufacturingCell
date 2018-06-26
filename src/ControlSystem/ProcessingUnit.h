/*file ProcessingUnit.h:*/
/*This header file defines the processing units*/


#include "flexInterface.h"
#include <time.h>

#ifndef _PROCESSINGUNIT_H_
#define _PROCESSINGUNIT_H_


/*Define the datatype for this object*/
/*These are stored in the ProcessingUnitController */

/* Variable names are self-documenting */
typedef struct ProcessingUnit
{
	int posX;
	int posY;
	int unitType;
	int unitNum;
} processingUnit;

#endif
