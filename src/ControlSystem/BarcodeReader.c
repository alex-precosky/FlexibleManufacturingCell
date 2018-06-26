#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#include "BarcodeReader.h"
#include "blank.h"
#include "flexInterface.h"
#include "flexTime.h"

/*file: BarcodeReader.c*/
/*This source file details the functions of BarcodeReader*/
/* The purpose of this class is to create a new 'blank' object based */
/* on the barcode information of the barcode in front of the physical */
/* barcode reader. */



/*  blank* ReadBarcode(void);
 *
 *  ReadBarcode reads the barcode from the blank in front of the
 *  barcode reader, and returns a new blank object with the stored
 *  information from the barcode.
 *
 * RETURN VALUE
 *  A blank with information from the barcode read contained within.
 *
 */
blank* ReadBarcode(void)
{
  blank* temp;        /* pointer to point to the new blank we create and return */
  FlexCodeStatus cs;  /* the barcode information */
  int i;              /* for loop index */

  struct timespec barCodeReaderWait, barCodeReaderRem;

  extern FILE* debugLog;

  temp = (blank*) malloc( sizeof( blank ) );

  /* The code appears after two update cycles, so wait that long */
  toTimeSpec(2 * Update_Period, &barCodeReaderWait);
  flexWait(&barCodeReaderWait, &barCodeReaderRem); 	    

  /*Call the flexInterface function to read barcode*/
  flexReadCodeStatus( &cs );

  /* Print results of flexReadCodeStatus */
  fprintf( debugLog, "flexCodeReadStatus returned: \n");
  fprintf( debugLog, "First unit: %d", cs.step[0].unitNumber );

  /* Dirty hack alert... we found it neceessary to put this in here to ensure */
  /* the feed belt stopped (even though we already call this in the thread that */
  /* runs when a blank triggers the light sensor... */
  flexSetFeedBelt( OFF );

  /*Store the FlexCodeStatus data in the new blank*/
  temp->cs = cs;
  temp->index = 0;
  temp->bitmask = 0x00;

  fprintf( debugLog, "BarcodeReader: Blank needs to go to %d units\n", temp->cs.numSteps );

  /* For each process the blank needs to go to, set a bit in its bitmask that */
  /* describes which units it needs to visit */
  for( i = 0; i < temp->cs.numSteps; i++ )
    {
	switch (temp->cs.step[i].unitNumber)
	  {
	  case 1:
	    temp->bitmask = temp->bitmask | 0x01;
	    break;
	  case 2:
	    temp->bitmask = temp->bitmask | 0x02;
	    break;
	  case 3:
	    temp->bitmask = temp->bitmask | 0x04;
	    break;
	  case 4:
	    temp->bitmask = temp->bitmask | 0x08;
	    break;
	  default:
	    break;

	  }
    }

  fprintf( debugLog, "Bitmask for blank: %d\n", temp->bitmask );
  fflush(debugLog );

  return temp;
}


