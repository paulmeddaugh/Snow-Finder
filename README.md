# Snow-Finder
Are you and your friends spontaneously looking for something fun to do? Would you really like to jump in a pile of snow, or build snow wonders that can only stay as memories, or just watch the snow fall?

Snow Finder uses the combination of a REST API with web scrapping to find the snow depth in a specified area in real time. It utilizes a free API for finding the cities within a radius of a U.S. zip code (has a limit of 10 requests per hour though), and scraps from wunderground.com the inches of snow and temperature that is found. It additionally has an option to search for snow in the major cities of the U.S.

It uses a very simple Java Swing GUI, running on desktop, which prompts for the method of searching, displays the results, and allows as many goes as one would like. It's directory organization reflects the IDE it was made with: Eclipse.