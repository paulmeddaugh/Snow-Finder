# Snow-Finder
Are you and your friends spontaneous looking for something fun to do? Would you really like to jump in a pile of snow, or build snow wonders that only can stay as memories, or just watch the snow fall?

Snow Finder uses a mix of REST API calls and web scrapping to find the snow depth in a specified area in real time. It uses a free API for finding the cities within a radius of a zip code (has a limit of 10 requests per hour though), and scraps from weather.com the amound of snow that is found. It additionally has an option to search for snow in the major cities of the U.S.

It uses a very simple Java Swing GUI, running on desktop, which prompts for the option of searching and simply displays the results.