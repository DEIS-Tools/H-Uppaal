# H-UPPAAL
**A new integrated development environment for Hierarchical Timed Automata**

-----

## Installation
Before installing H-UPPAAL, make sure that you have Java 8 and JavaFX installed. We recommend using https://www.azul.com/downloads/zulu-community/?version=java-11-lts&package=jre-fx

1. Download and extract `huppaal.zip` from [one of the releases](https://github.com/ulriknyman/H-Uppaal/releases).
2. *Optional*: Copy server binaries to the `/server/` folder (see [Using the UPPAAL backend](#using-the-uppaal-backend)). 
3. Run `huppaal.jar` by either double-clicking the file or running `java -jar huppaal.jar`.

-----

If you experience a blank white screen in H-UPPAAL, disable hardware acceleration in the VM-options ([source](https://www.reddit.com/r/javahelp/comments/84w6i6/problem_displaying_anything_with_javafx_only/))
```
-Dprism.order=sw
```

### Linux
If you are using Linux, you may have to install JavaFX with the following command:
```
$ sudo apt install openjfx
```
### Windows/OSX
If the app fails to load javafx, you can download the runtime libraries of openjfx from the [gluonhq site](https://gluonhq.com/products/javafx/)

On OSX, you might have to add an exception in the security settings panel in order to launch H-Uppaal.

## Using the UPPAAL backend
When running the program, please make sure that you have the following folder structure (relative to the `huppaal.jar`-file). Feel free to only include binary-files for you operating system that you are using, e.g. `bin-Win32`. These binaries are found in the UPPAAL distibution and can simply be copied over to the `huppaal.jar` location.

```
huppaal/
|-- huppaal.jar
|-- servers/
|   |-- bin-Linux/
|   |   |-- server
|   |   |-- socketserver
|   |   |-- verifyta
|   |-- bin-MacOS/
|   |   |-- server
|   |-- bin-Win32/
|   |   |-- server.exe
|   |   |-- verifyta.exe
```

## Saving H-UPPAAL models
When saving a HUPPAAL model, it will be saved in the `project/` folder located in the same directory as the .jar file. A `Queries.json` file can also be found here. This file contains all queries specified in the model. 

```
|-- huppaal.jar
|-- project/
|   |-- ...
|   |-- Queries.json
```

## About 
H-UPPAAL was originally developed as a student project at Aalborg University by Niklas Kirk Mouritzsen and Rasmus Holm Jensen supervised by Ulrik Nyman. H-UPPAAL is today maintained by Distributed Embedded Intelligent Systems Group at Department of Computer Science, Aalborg University. 

We would like to thanks the following people and organizations for the contributions and support of H-UPPAAL: 

  - Niklas Kirk Mouritzsen 
  - Rasmus Holm Jensen
  - Niels Vistisen
  - HMK Bilcon
  
