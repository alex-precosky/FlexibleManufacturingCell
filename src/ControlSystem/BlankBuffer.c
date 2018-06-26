#include <stdlib.h>
#include <pthread.h>
#include <stdio.h>

#include "BlankBuffer.h"

/* function: newBlankBuffer     */
/* Allocates resources for a new BlankBuffer object, and */
/* initializes it to its starting state. */
struct BlankBuffer* newBlankBuffer()
{
  struct BlankBuffer* newBuffer;

  /* Allocate memory */
  newBuffer = (struct BlankBuffer*) malloc( sizeof( struct BlankBuffer ) );
  newBuffer->lock =(pthread_mutex_t*) malloc( sizeof( pthread_mutex_t ) );
  newBuffer->itemAvailable = 
    (pthread_cond_t*)malloc( sizeof( pthread_cond_t ) );

  /* Allocate syncronization tools */
  pthread_mutex_init( newBuffer->lock, NULL );
  pthread_cond_init( newBuffer->itemAvailable, NULL );

  /* Inilialize state variables */
  newBuffer->numItems = 0;
  newBuffer-> blankList = NULL;

  return newBuffer;

}

/* function: putBlank */
/* Puts a 'blank' object into the BlankBuffer, along with an optional */
/* 'heldByCrane' value if the buffer is one of blanks held by cranes */
void putBlank( struct BlankBuffer *theBuffer, struct blank* theBlank, 
	       int heldByCrane )
{
  struct blankNode* newNode, *iterator;

  pthread_mutex_lock( theBuffer->lock );

  /* Create a blankNode */
  newNode = (struct blankNode*)malloc( sizeof( struct blankNode ) );
  newNode->theBlank = theBlank;
  newNode->heldByCrane = heldByCrane;
  newNode->next = NULL;

  /* Store the blankNode */
  /* if no nodes, insert as the first node */
  if( theBuffer->numItems == 0 )
    {
      theBuffer->blankList = newNode;
    }
  /* Otherwise, traverse to last node before storing */
  else
    {

      iterator = theBuffer->blankList;
      while(iterator->next != NULL)
	iterator=iterator->next;

      iterator->next = newNode;
    }

  theBuffer->numItems++;

  pthread_mutex_unlock( theBuffer->lock );
  pthread_cond_signal( theBuffer->itemAvailable );

}

/* function: getBlank */
/* retrieve/remove a blank object from the BlankBuffer, */
/* and if it is held by a crane, the crane number holding it */
void getBlank( struct BlankBuffer *theBuffer, 
	       struct blank** theBlank, int* heldByCrane )
{
  struct blankNode *iterator;

  pthread_mutex_lock( theBuffer->lock );

  while( theBuffer->numItems == 0 )
    pthread_cond_wait( theBuffer->itemAvailable, theBuffer->lock );

  /* always returns the first node, so point iterator here */
  iterator = theBuffer->blankList;

  /* Retrieve a blankNode */
  /* If there is only one node, simply remove that one */
  /* otherwise remove the first and link up blankList to the new first node */
  if( theBuffer->numItems == 1 )
      theBuffer->blankList = NULL;
  else
      theBuffer->blankList = iterator->next;

  /* set return values */
  *theBlank = iterator->theBlank;
  *heldByCrane = iterator->heldByCrane;

  theBuffer->numItems--;

  pthread_mutex_unlock( theBuffer->lock );
}
