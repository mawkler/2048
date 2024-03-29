CLASSPATH = -cp classes:./junit/junit-4.12.jar:./junit/hamcrest-core-1.3.jar:.
JC = javac -Xlint:unchecked -d ./src/classes
J = java -classpath ./src/classes

run: *.java \
	./src/*.java
	$(JC) $^
	$(J) Game

install:
	mkdir -p ./src/classes

clean:
	rm -rf ./src/*.class \
	rm -rf ./src/classes
