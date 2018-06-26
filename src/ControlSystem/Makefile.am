CFLAGS = -g3 -O0

bin_PROGRAMS = ControlSystem
ControlSystem_SOURCES = main.c FeedbeltArrivalThd.c BarcodeReader.c \
CraneController.c SensorPollThread.c SensorData.c BlankBuffer.c BlankToUnitThd.c \
ProcessingUnitThd.c BlankToDepositThd.c ProcessingUnitController.c
ControlSystem_LDADD = ../FlexInterfaceC/libflexInterface.la
ControlSystem_LDFLAGS = -lpthread

INCLUDES = -I../FlexInterfaceC
