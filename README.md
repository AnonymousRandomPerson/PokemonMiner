# PokemonMiner
Used to mine information from [Bulbapedia](http://bulbapedia.bulbagarden.net/wiki/Main_Page) to the Pixelmon database, from the Pixelmon database to the [Pixelmon wiki](http://pixelmonmod.com/wiki/index.php?title=Main_Page), and for some miscellaneous utility involving the Pixelmon database or wiki.
## Usage
Main file: Controller.java  
Arguments:
* ability: Gets wikicode for an Ability article.
* article: Gets wikicode for a Pokémon article.
* available: Gets wikicode for the [Available Pokémon](http://pixelmonmod.com/wiki/index.php?title=Available_Pok%C3%A9mon) article.
* availableprogress: Gets wikicode for the [Available Pokémon/Progress](http://pixelmonmod.com/wiki/index.php?title=Available_Pok%C3%A9mon/Progress) article.
* biome: Gets wikicode for a biome article and its rarity template.
* egg: Gets queries for Generation 6 Egg move data for Generation 6 Pokémon.
* egggroup: Gets wikicode for an Egg Group article.
* eggold: Gets queries for Generation 6 Egg move data for Generation 1-5 Pokémon.
* event: Gets queries for new Generation 6 event moves.
* evyield: Gets wikicode for the [EV yield](http://pixelmonmod.com/wiki/index.php?title=EV_yield) article.
* fixegggroups: Gets queries to fix an issue with parsing certain Egg Groups for Generation 6 Pokémon.
* fixspecial: Gets queries to fix an issue with parsing the base Special Attack and Special Defense EVs for Generation 6 Pokémon.
* forms: Gets queries for Castform and Deoxys forms.
* langmove: Gets translated move names and descriptions.
* levelup: Gets queries for Generation 6 level-up moves.
* mega: Gets queries for Mega Evolution stats.
* move: Gets queries for Generation 6 moves.
* movearticle: Gets wikicode for a move article.
* moveids: Gets wikicode for the [Move IDs](http://pixelmonmod.com/wiki/index.php?title=Move_IDs) article.
* newmoves: Gets queries for Generation 6 modified TM/tutor move compatibility.
* pictures: Gets wikicode for the [Shiny Pokémon/Pictures](http://pixelmonmod.com/wiki/index.php?title=Shiny_Pok%C3%A9mon/Pictures) article.
* pokedex: Gets Pokédex entries.
* pokemon: Gets queries for Generation 6 Pokémon stats.
* shiny: Converts the file names of Shiny Pokémon images from Pixelmon source to wiki-friendly names.
* spawnlocation: Gets wikicode for the [Spawn location](http://pixelmonmod.com/wiki/index.php?title=Spawn_location) article.
* tm: Gets queries for Generation 6 TM/HM move compatibilities.
* tutor: Gets queries for Generation 6 tutor move compatibilities.
* tutorlist: Gets wikicode for the list of moves in the [Move tutor](http://pixelmonmod.com/wiki/index.php?title=Move_tutor) article.
* type: Gets wikicode for a type article.
* unavailable: Converts unavailable Pokémon minisprites to grayscale.

## Not included in this repository
* Pixelmon database
* Pixelmon English language translation file
* Enum source files from the Pixelmon mod source
* Pokémon minisprites
