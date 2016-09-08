# Xray
[![Build Status](https://travis-ci.org/eqot/xray.svg?branch=master)](https://travis-ci.org/eqot/xray)
[![Download](https://api.bintray.com/packages/eqot/maven/xray-processor/images/download.svg)](https://bintray.com/eqot/maven/xray/_latestVersion)
[![Apache2](http://img.shields.io/badge/license-APACHE2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

An annotation which makes class methods accessible for testing.

## Download

Gradle:

```
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```

```
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    testApt 'com.eqot:xray-processor:0.2.0'
    testCompile 'com.eqot:xray:0.2.0'    
}
```

or

```
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    androidTestApt 'com.eqot:xray-processor:0.2.0'
    androidTestCompile 'com.eqot:xray:0.2.0'
}
```

## Sample usage

Here is a private method in a sample class which needs to be verified if it works as expected.

```
public class Sample {
    private int add(int value0, int value1) {
        return value0 + value1;
    }
}
```

The annotation ```@Xray``` generates a wrapper class for the specified class with a postfix ```$Xray```.
The generated wrapper has the same methods as the specified class but all methods are accessible as public methods.

```
@Xray(Sample.class)
public class SampleTest {
    @Test
    public void add() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.add(1, 2);
        assertEquals(result, 3);
    }
}
```

## LICENSE

    Copyright 2016 Ikuo Terado

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
