# RipMe-Novideos
A fork of [Ripme](https://github.com/RipMeApp/ripme) with the option to skip all kinds of videos and a completely rewritten UI using JavaFX.

### About
Since [Ripme](https://github.com/RipMeApp/ripme) is licensed under the [MIT license](LICENSE.txt) which allows modification and publication and I did not like the UI look-and-feel of the original program, I decided to learn some JavaFX and re-create the complete UI from scratch.
My goal was also to be able to make more settings inside the GUI itself instead of config files (e.g. for a specific ripper, there are settings available in the config, but not in the UI). 
I also try to keep the ripper classes untouched such that changes can easily be merged.

### Roadmap
* Test UI and make sure everything works
* Create a Skin system for an easily skinnable UI with a simple set of colors, flat look-and-feel
    * Load skins from a system folder and the user folder, prioritize the user folder (subfolder of settings folder according to the xdg specification)
* Modularize Rippers using Reflections to load settings directly from the rippers themselves on program startup
    * Each ripper inherits from a parent class which has a `getSpecificSettings()` method. Each setting is added to the settings tab (which will be huge eventually)
* Replace ugly text buttons with pictograms and move the progress bar to the bottom. Add info text in the middle of the progress bar (percentage, current subreddit?)
* Add Github CI for build and deploy via Gradle Docker image
* Deploy JavaFX to FatJar?

You might want to checkout the GitHub **[Issues](../../issues)** page for further planned changes and fixes