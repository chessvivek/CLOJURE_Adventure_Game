# adventure

This is a text adventure game written in Clojure based on the Siebel center of CS at UIUC. Enter "lein run" to run the game.

At each turn these intructions are allowed:

[go "@"] -                      go in direction "@"
[examine room] -                examine the current room
[examine item "@"] -            examine item "@" that is already present in the inventory
[i] -                           look up inventory 
[pick up "@"] -                 add item "@" to the inventory
[drop "@"] -                    drop item "@" from the inventory
[apply action "@1" on "@2"] -   apply action "@1" on item "@2" (must be present in inventory)

To win the game, you need to gain 100+ knowledge and 100+ project completion status. Entering some rooms will help you complete the project, and there are some items that would help increase your knowledge.

At any point, running out of energy or turns makes you lose the game. You do not know how much energy you will lose as you enter a room. However, it is guaranteed the rooms that decrease your energy will always help you either gaining knowledge or completing your project.

All the best!

## Installation

Download from http://example.com/FIXME.

## Usage

FIXME: explanation

    $ java -jar adventure-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
