ZMQActor - Actor-like syntax for 0MQ sockets in Groovy
======================================================

I've been slowly learning Groovy in the past days, and when I saw [GPars' Actors](http://gpars.codehaus.org/Actor),
the fist thing that came into mind was: that's a nice syntax for sockets.

Sockets are cool, but I went further and decided to inject it with a mix of radioactive isotopes stolen from a secret
Soviet atomic research project, bombard it with 1950-era cosmic rays, and put it into the hands of a drug-addled comic
book author with a badly-disguised fetish for bulging muscles clad in spandex. Yes, I used [ØMQ](http://zeromq.org) sockets.

So, **what does this mean?**
It means you get all the robustness, scalability and speed of 0MQ, with an eye-pleasing, easy to maintain syntax.
For a simple example, see the [GPars' Actor example ported to ZMQActor](https://gist.github.com/1018752) on Gist.

"This is cool!", you say, but be advised, this is the second thing I create using Groovy (the first being Hello World),
so expect bugs, lots of them.

The overall structure of the ZMQActor class was stolen from GPars' [DefaultActor](http://git.codehaus.org/gitweb.cgi?p=gpars.git;a=blob_plain;f=src/main/groovy/groovyx/gpars/actor/DefaultActor.java;hb=HEAD),
I just adapted it to work with 0MQ sockets. It should also be noted, that a ZMQActor is **NOT** async (yet), **starting a
ZMQActor will block the execution of your script**. I have plans to work on that, but for now you can either set a timeout
on the react method or wrap the entire ZMQActor in your own thread.