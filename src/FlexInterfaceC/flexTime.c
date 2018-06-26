#include<stdio.h>
#include<time.h>
#include<errno.h>

/* duration in usec */
void toTimeSpec(long int duration, struct timespec *ts) {

       ts->tv_sec = duration / 1000000;
       ts->tv_nsec = (duration - (ts->tv_sec * 1000000)) * 1000;
fprintf(stderr,"wait %ld %ld\n", ts->tv_sec, ts->tv_nsec);
}


void flexWait(struct timespec *ts, struct timespec *trem) {
       while (nanosleep(ts, trem) == -1) {
            ts->tv_sec = trem->tv_sec;
            ts->tv_nsec = trem->tv_nsec;
       }
}
