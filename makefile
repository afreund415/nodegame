
JFLAGS = -cp "./"
JCFLAGS = -g -cp "./"
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JCFLAGS) $*.java

CLASSES = \
 CNNode.java \
 SR.java \
 SRNode.java \
 Packet.java \
 Link.java \
 Route.java \
 Router.java 

default: classes 

classes: $(CLASSES:.java=.class)


clean:
	$(RM) *.class

run:
	java $(JFLAGS) SRNode
	