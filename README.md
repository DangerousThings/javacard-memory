# javacard-memory

Javacard applet to measure memory usage. Uses `JCSystem.getAvailableMemory`.

## Compile

Install `ant` and the Javacard SDKs.

Build using `JC_HOME=SDKPATH/jc304_kit ant dist`

## Usage

Deploy to the card using `gp -install javacard-memory.cap`.

Install Python3 and `pyscard`, and run `./measure.py`. Measure once before installing your applet to test, and once afterwards.
