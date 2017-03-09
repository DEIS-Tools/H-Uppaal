# SW9-Kick-ass-modelchecker
By Niklas Kirk Mouritzsen and Rasmus Holm Jensen

## Using the UPPAAL backend
When running the program, please make sure that you have the following folder structure (relative to the huppaal.jar-file). Feel free to only include binary-files for you operating system that you are using, e.g. ```bin-Win32```. These binaries are found in the UPPAAL distibution and can simply be copied over to the ```huppaal.jar``` location.

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

## Saving HUPPAAL models
When saving a HUPPAAL model, it will be saved in the ```project/``` folder located in the same directory as the .jar file. A ```Queries.json``` file can also be found here. This file contains all queries specified in the model. 

```
|-- huppaal.jar
|-- project/
|   |-- ...
|   |-- Queries.json
```
