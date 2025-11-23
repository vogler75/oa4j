
include $(API_ROOT)/ComDrv.mk

INCLUDE = $(COMDRV_INCL) -I. -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

LIBS = $(COMDRV_LIBS) $(LINKLIB) $(JAVA_HOME)/jre/lib/$(ARCH)/server/libjvm.so

OBJS = $(COMDRV_OBJS)

MYOBJS = ../LibJava/Java.o \
         ../LibJava/JNIValidator.o \
         at_rocworks_oa4j_jni_Driver.o \
         WCCOAJavaAlertService.o \
         WCCOAJavaConnection.o \
         WCCOAJavaDrv.o \
         WCCOAJavaDrvIntDp.o \
         WCCOAJavaHWMapper.o \
         WCCOAJavaHWService.o \
         WCCOAJavaMain.o \
         WCCOAJavaResources.o \
         WCCOAJavaTrans.o

PVSS00WCCOAJava: $(MYOBJS)
	@rm -f addVerInfo.o
	@$(MAKE) addVerInfo.o
	cp Java.o ../LibJava || :
	$(LINK_CMD) -o WCCOAjavadrv addVerInfo.o $(MYOBJS) $(OBJS) $(LIBS)
	gcc -shared -gcc-11 -o libWCCOAjavadrv.so *.o $(LIBS)

clean:
	@rm -f *.o WCCOAjavadrv WCCOAjavadrv.so Java.o ../LibJava/Java.o

addVerInfo.cxx: $(API_ROOT)/addVerInfo.cxx
	@cp -f $(API_ROOT)/addVerInfo.cxx addVerInfo.cxx

addVerInfo.o: $(OFILES) addVerInfo.cxx
    
