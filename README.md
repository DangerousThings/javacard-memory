# javacard-memory

Javacard applet to measure memory usage. Uses `JCSystem.getAvailableMemory`.

## Compile

Install `ant` and the Javacard SDKs.

Build using `JC_HOME=SDKPATH/jc304_kit ant dist`

## Usage

Deploy to the card using `gp --install javacard-memory.cap [ --params AABBCCDD ]`, with the optional installation parameter being a 32 bit unsigned big-endian integer specifying the base free persistent space. This value will be stored and reported back later. By default, if no parameter is specified, this value is `00028F38`, which means `167736` bytes for the flexSecure. For the Apex Flex, use `00014970`, which means `84336` bytes.

Install Python3 and `pyscard`, and run `./measure.py`. Measure once before installing your applet to test, and once afterwards.

The applet return measurement data as a response to the select command. See the measure script for details.
