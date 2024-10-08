# Manipulator

Manipulator is a simple-to-use annotation based bytecode manipulation library which main focus is to work with the attach api. <br><br> This library is highly inspired by the [Spongepowered Mixin](https://github.com/SpongePowered/Mixin) project. <br><br> Manipulator uses [ByteBuddy](https://github.com/raphw/byte-buddy) under the hood.

<br>

### Install
Currently the project is hosted on my own repo, but you can also build it yourself
```xml
<repository>
  <id>harmvdhorst-repo</id>
  <url>https://repo.harmvdhorst.nl/releases</url>
</repository>

<dependency>
    <groupId>nl.harmvdhorst</groupId>
    <artifactId>Manipulator</artifactId>
    <version>1.0.1-BETA</version>
</dependency>
```

#### Usage
Every time you want to manipulate a class you have to create a Patch file

```java
import nl.harmvdhorst.manipulator.MethodData;
import nl.harmvdhorst.manipulator.annotations.Inject;
import nl.harmvdhorst.manipulator.annotations.Overwrite;
import nl.harmvdhorst.manipulator.annotations.Patch;
import nl.harmvdhorst.manipulator.annotations.Redirect;

// First we need to specify which class we want to patch
@Patch(type = ClassToPatch.class)
public class TestPatch {

    // to overwrite the method we have to add the Overwrite annotation and match the method name
    @Overwrite
    public void testMethod() {
        System.out.println("This is the NEW testMethod");
    }

    // to inject code at the start or the end of a method we can use the Inject annotation
    @Inject(method = "testMethod", at = Inject.At.HEAD)
    public void thisDoesNotReallyMatter() {
        System.out.println("Added at the beginning of the method");
    }

    // this is an example of the tail method 
    // We also have access to the MethodData
    @Inject(method = "testMethod", at = Inject.At.TAIL)
    public void thisDoesNotReallyMatter(MethodData data) {
        // the object of the old method
        data.thisObject;
        // the name of the old method
        data.method;
        // a list of all arguments of the old method
        data.args;

        System.out.println("Added at the end of the method");
    }

    // We can also change the return value of a method
    @Redirect
    public String getString() {
        return "New String";
    }

}


public class ClassToPatch {

    public void testMethod() {
        System.out.println("This is the testMethod");
    }

    public String getString() {
        return "String 1";
    }

}
```

Well that's it actually <br><br>

I also have made a small Utils class where i am going to add small handy things for manipulation
```java
// With this method you can inject a ByteBuddy Type into the system classloader
Utils.injectDynamicTypeIntoSystemClassloader(type);
```

## Notice
This library uses [ByteBuddy](https://github.com/raphw/byte-buddy) which is licenced under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
