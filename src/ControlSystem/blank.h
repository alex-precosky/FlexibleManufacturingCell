/*file blank.h:*/
/*This header file defines the blank object */


#include "flexInterface.h"

#ifndef _BLANK_H_
#define _BLANK_H_

/* class: blank
 *
 * Blank objects store the state of a blank in the system.  It contains
 * the information stored in its barcode for reference by the threads
 * so they know where to send the blank.  An index keeps track of
 * the number of the current processing step the blank is on.
 *
 * A bitmask stores the units that the blank needs to visit.  The
 * LSB set means it needs to use unit 1, the second LSB means it needs
 * to use unit 2, etc.  
 */

/*Define the datatype for this object*/
typedef struct blank
{
  FlexCodeStatus cs;  /* Barcode data, as returned from the barcode reader */

  int index;  /* The "current" unit the blank is processing in or */
              /* is about to go to */

  unsigned char bitmask; /* The units that we need. Stored for easy use in */
                         /* reservation functions for the proc. units. */
} blank;


#endif /* ifndef */
