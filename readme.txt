Flight Club - ReadMe
====================
Created by Dan Burton <danb@dircon.co.uk>, 22 Aug 2002
Latest update 12 Mar 2003

* The package structure

Flightclub has several packages:

  1. framework3d 

  This package contains contains classes for building and viewing a 3d
  model. At the top of the object graph is ModelViewer. This manager
  creates all the other classes. The Flight Club client uses a subclass
  of ModelViewer at its hub.

  2. client

  This package contains classes such as Cloud and Glider that are used
  in the Flight Club client (the applet).

  3. site

  This package contains the classes for the SiteBuilder applet.

  4. server

  This package contains the classes for the networked game server.

  5. test

  This package contains unit test programs for the classes in the
  other packages. For example, running test/Clouds tests the
  client/Cloud class. 

  6. data

  This package contains stand alone classes that generate text files
  of data that are to be used by the client. For example, the
  characteristics of the different types of glider are defined in this
  package. See below for how to generate data files.

* To generate the data files for the different types of glider.

  1. Compile the three classes hangglider, paraglider and sailplane
  which live in the data package:
 
	% cd ~/xc/src/
	% javac -d ../class flightclub/data/*.java

  2. Run them:

	% cd ~/xc/class/
	% java flightclub/data/Hangglider
	% java flightclub/data/Paraglider
	% java flightclub/data/Sailplane

  3. The data file hangglider.txt is output to ~/xc/class/ which is
     where the client will look for it when it attempts to create a
     hangglider. Likewise for the other two types of glider.