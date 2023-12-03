# JavaRPG

This is a very large game project I worked on at one time (Incomplete). It just uses Java and LWJGL (A native library wrapper around OpenGL) for rendering and everything else is essentially written by hand.
The game was meant to be a sorta 2D RPG with basic leveling by interaction with the environment similar to Runescape with graphics like that of Final Fantasy 6.

## Structure

Files under ``VorkEngine`` contain the framework I wrote for rendering. A huge portion of it is dedicated to graphical user interface code since the UI was hand written and simply requires
handling a ton of different use cases. Then there is code for file handing, asyncronous asset loading, abstractions on top of LWJGL for creating 2D batches for rendering, and more.

The ``Client`` depends on ``VorkEngine`` and handles the game itself. There is some nice code in there for network communication since the communication is just TCP sockets communicating using a byte
for an opcode and then reading raw bytes which have particular meaning for any given opcode. Each opcode case is subdivided into its own class under ``client/net/in`` which extends PacketListener
for the network event. All the listeners are registered with ``NetworkEventBus``. Then all gameplay related content for rendering entities, the world tiles, player pathing, chat, inventory, and
more is found under ``client/game``.

The ``Server`` code is more or less what it sounds like as it just handles the client connections and managing of client packets. The network structure is very similar to the client as it also
uses responds to opcodes and sends opcodes for different game events.
