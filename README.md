# esloq

Esloq is a startup I worked on from 2014 to 2016. All code written by me and Kenny Kuchera.

Esloq is a door lock that can be opened with a smartphone via Bluetooth. It consists of a wooden box that is mounted on the door lock cylinder. The wooden box contains the printed circuit board and a motor that actuates the lock.

A demo video of the esloq:

[![Smart Lock Demo](/images/video_thumbnail.png)](https://www.youtube.com/watch?v=yVSUPODDXtM)

Esloq uses authentication and encryption to ensure that only the people you invite can enter. Whenever you want to open a door with your phone, the phone requests a ticket from the esloq webserver. The server returns the ticket that is authenticated encrypted under a symmetric key that is shared between the door lock and the esloq webserver (the phone does not have this key). The phone then forwards this ticket to the lock which verifies its authenticity and opens the door if it's valid.

This repository contains:
- the Kicad design files to make the printed circuit board (folder /pcb/)
- the software running on the microcontroller (folder /firmware/)
- the Android app that connects with the door lock (folder /android/)
- the code running on the webserver (folder /webserver/)

The printed circuit board:

![Printed Circuit Board](/images/printed_circuit_board.jpg?raw=true "Printed Circuit Board")
