#ifndef _BLANKBUFFER_H_
#define _BLANKBUFFER_H_

#include <pthread.h>
#include "blank.h"

/* BlankBuffer: */
/* This class is used to store 'blanks' and buffer them up between  */
/* stages of the pipeline architecture.  In some stage transitions, */
/* the blank is held by a crane, so also the number of the crane    */
/* holding the blank is stored so that the next stage will know.    */
/*                                                                  */
/* The buffer is implemented as a queue.  The queue is implemented  */
/* as a linked list.  New nodes enter through the rear, and leave   */
/* through the front.                                               */

/* NOTE: Thread safe! */

/* struct blankNode is a node in the linked list of queued elements */
struct blankNode
{
  blank* theBlank;  
  int heldByCrane;
  struct blankNode* next;
};

struct BlankBuffer
{
  pthread_mutex_t* lock;
  pthread_cond_t* itemAvailable;

  int numItems;

  struct blankNode *blankList; /* pointer to the first item in the list */

};

/* function: putBlank */
/* Puts a 'blank' object into the BlankBuffer, along with an optional */
/* 'heldByCrane' value if the buffer is one of blanks held by cranes */
void putBlank( struct BlankBuffer *theBuffer, 
	       struct blank* theBlank, int heldByCrane );

/* function: getBlank */
/* retrieve/remove a blank object from the BlankBuffer, and */
/*if it is held by a crane, the crane number holding it */
void getBlank( struct BlankBuffer *theBuffer, 
	       struct blank** theBlank, int* heldByCrane );

struct BlankBuffer* newBlankBuffer();

#endif /* #ifndef _BLANKBUFFER_H_ */

