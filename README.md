# Xray
[![Download](https://api.bintray.com/packages/eqot/maven/xray/images/download.svg)](https://bintray.com/eqot/maven/xray/_latestVersion)
[![Apache2](http://img.shields.io/badge/license-APACHE2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

An annotation which makes class methods visible for testing.

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

## Sample usage

```
public class Sample {
    private int add(int value) {
        return mValue + value;
    }
}
```

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
