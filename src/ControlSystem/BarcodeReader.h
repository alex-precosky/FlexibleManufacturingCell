#include "flexInterface.h"
#include "blank.h"

#ifndef _BARCODEREADER_H_
#define _BARCODEREADER_H_

/*file: BarcodeReader.h*/
/*This header file outlines the functions of BarcodeReader*/
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
blank* ReadBarcode(void);

#endif /* ifndef */
