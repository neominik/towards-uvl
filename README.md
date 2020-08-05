# Towards a Universal Variability Language

This is the supplementary repository for my master's thesis with the above title.

## Slides MODEVAR 2020

The slides that Thomas and me presented at MODEVAR 2020 to initiate discussions before distributing the first questionnaire.

## Parser Library for UVL

The library containing a parser and a printer for the Universal Variability Language is maintained in a separate repository: [uvl-parser](https://github.com/neominik/uvl-parser)

It has been integrated into [FeatureIDE](https://featureide.github.io). My [fork](https://github.com/neominik/FeatureIDE) with the corresponding changes and the relevant pull requests: [1050](https://github.com/FeatureIDE/FeatureIDE/pull/1050) and [1069](https://github.com/FeatureIDE/FeatureIDE/pull/1069)

## Evaluation

This repository includes an eclipse project with the code and models used for the evaluation part of the thesis.

There are the base models as well as the submodels for the Automotive02_V4 model and the Linux kernel in version 2.6.33-rc3.

The submodels for the Linux kernel have been created from the main model and the original Kconfig files with the script in [CreateLinuxSubmodels.java](https://github.com/neominik/towards-uvl/blob/master/Evaluation/src/de/neominik/uvl/thesis/CreateLinuxSubmodels.java).

Analysis results for the number of features, max indentation level, and max sibling distance per submodel can be recreated by running the main method of [Analyze.java](https://github.com/neominik/towards-uvl/blob/master/Evaluation/src/de/neominik/uvl/thesis/Analyze.java) (Uses the `Files.writeString()` method of Java 11, but could be ported to be Java 8 compatible quickly).

For the file-size comparison, we include the models as exported by FeatureIDE next to the main xml models in their corresponding folders ([automotive](https://github.com/neominik/towards-uvl/tree/master/Evaluation/automotive) and [linux](https://github.com/neominik/towards-uvl/tree/master/Evaluation/linux)).