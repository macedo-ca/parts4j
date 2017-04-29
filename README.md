# parts4j - Lightweight non-intrusive module management framework
The parts4j is a lightweight non-intrusive module management framework inspired by log4j, that manages 
life-cycle, health and statistics of your modules by only adding a few lines of code to your existing modules.

## Quick start
### Step 1 - Register your module
Add the following code snippet to the main class one of your modules. 
```java
static PartType<YourClassNameHere> PART_TYPE=PartRegistry.register(YourClassNameHere.class);
Part<YourClassNameHere> PART=PART_TYPE.registerSingleton(this);
```
The above code assumes your module main class is a singleton. If that is not the case, you need to provide a unique ID of 
your module instance. If you don't have one you can use toString():
```java
static PartType<YourClassNameHere> PART_TYPE=PartRegistry.register(YourClassNameHere.class, "component");
Part<YourClassNameHere> PART=PART_TYPE.register(this,this::toString);
```
### Step 2 - Monitor your module
Add in a _parts4j.properties_ file to the root of your class-path, and add this to it:
```properties
parts4j.healthy=ok,null,200,true
parts4j.manager.stdout=parts4j.managers.SystemOutReport
parts4j.manager.stdout.frequency=60000
parts4j.manager.stdout.criteria=component
```
If you start your app now, and assuming that some of your code will create an instance of your class _YourClassNameHere_, your will 
start getting report on standard out every minute.
### Step 4 - Add health-status to report
If you already have a method call that summarizes health for your module. If you, create this mock one for demo purposes:
```java
public String getTotalFakeHealth(){
  return ((Math.random()*100)>50) ? "OK" : "bad-bad";
}
```
The health-method also needs to be registered. So, let's edit the original registration lines:
```java
static PartType<YourClassNameHere> PART_TYPE=PartRegistry.register(YourClassNameHere.class, "component");
Part<YourClassNameHere> PART=PART_TYPE.register(this,this::toString).registerHealth(this::getTotalFakeHealth);
```
Start your application again, and you will now notice how the component is reported healthy/unhealthy based on the math random.
