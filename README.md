# Xray
[![Build Status](https://travis-ci.org/eqot/xray.svg?branch=master)](https://travis-ci.org/eqot/xray)
[![Download](https://api.bintray.com/packages/eqot/maven/xray-processor/images/download.svg)](https://bintray.com/eqot/maven/xray/_latestVersion)
[![Apache2](http://img.shields.io/badge/license-APACHE2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

An annotation which makes class methods and fields accessible for testing.


## Motivation

When I developed an Android library, I encountered a problem that an private method in a class cannot be tested since it is not accessible from outside of the class including test code.
According to [a thread in StackOverflow](http://stackoverflow.com/questions/34571/how-to-test-a-class-that-has-private-methods-fields-or-inner-classes),
the problem seems to be an well-known topic and one of solutions is reflection.
Reflection is quite powerful but decrease readability and maintainability of test code.

So, this annotation is developed to make private methods accessible by using reflection but keep readability and maintainability.


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
    testApt 'com.eqot:xray-processor:1.3.1'
    testCompile 'com.eqot:xray:1.3.1'
}
```

or

```
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    androidTestApt 'com.eqot:xray-processor:1.3.1'
    androidTestCompile 'com.eqot:xray:1.3.1'
}
```

Please note that since this annotation breaks class's information hiding,
it is strongly recommended for testing only.


## Examples

Here is an example of private methods and a field
which need to be verified if it works as expected.

```
public class Sample {
    private int mValue;

    private int add(int value0, int value1) {
        return value0 + value1;
    }

    private void throwIllegalArgumentException() throws IllegalArgumentException {
        throw new IllegalArgumentException("message");
    }
}
```

The annotation ```@Xray``` generates a wrapper class for the specified class with a postfix, ```Sample$Xray``` in this case.
The generated wrapper has the same methods as the specified class but all methods are accessible as public methods.

```
@Xray(Sample.class)
public class SampleTest {
    @Test
    public void add() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.add(1, 2);
        assertEquals(3, result);
    }
}
```

The private field is also accessible.

```
@Test
public void setAndGet() throws Exception {
    final Sample$Xray sample = new Sample$Xray();
    sample.mValue(123);
    assertEquals(123, sample.mValue());
}
```

Here is an example with JUnit4 to verify if exception occurs as expected.

```
@Rule
public final ExpectedException thrown = ExpectedException.none();

@Test
public void throwIllegalArgumentException() throws Exception {
    final Sample$Xray sample = new Sample$Xray();

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("message");

    sample.throwIllegalArgumentException();
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
