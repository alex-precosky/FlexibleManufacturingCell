
#ifndef FLEXTIME_H
#define FLEXTIME_H

#include <time.h>
extern void toTimeSpec(long int duration, struct timespec *ts) ;
extern void flexWait(struct timespec *ts, struct timespec *trem) ;


#endif

