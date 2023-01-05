[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Naereen/StrapDown.js/graphs/commit-activity) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Windows](https://svgshare.com/i/ZhY.svg)](https://svgshare.com/i/ZhY.svg)

---

# RoflBoard

> Simple soundboard

![Screenshot](docs/screenshot.png)
*Screenshot*

Features:

* Soundboard
    * Configs can be saved / loaded as JSON file
    * Sounds can be played fully / only when pressed / toggled
    * Supports key combinations
    * Keymap preview
    * Warnings: sound length, starting silence length, errors
* Voice changer
    * Transmits voice to output device on key press
    * Pitch, Gain, HighPass filters

Current limitations:

* Only supports `.wav` files
* Requires JRE 17+ with JavaFX on non-Windows platforms (e.g. Azul Zulu)

## How to use

* On windows: download and run `.msi` file from Releases
* On linux:
    * Download JRE 17+ with JavaFX (e.g. Azul Zulu)
    * Download `.zip` file and run script in `bin` directory