# Scalagna

We design and implement Scalagna, a multi-tier programming environment for Scala
based on the existing JavaScript and JVM ecosystems that requires no additional
compiler changes or plugins.

## Examples

1. open an SBT terminal in this directory
2. execute the ~re-start command once the project is loaded

URL's that are currently registered in the example applications:

- Graffiti:
  - http://localhost:8080/graff/rpc
  - http://localhost:8080/graff/ws (Note: re-opening a WebSocket after a certain period of inactivity, has not yet been implemented.)
- Shoppinglist:
  - http://localhost:8080/shopping

## Structure

- ```scalamt```: library code
  - ```.js```: JavaScript specific implementations
  - ```.jvm```: JVM specific implementations
  - ```src```: shared interfaces
- ```example```: example code
