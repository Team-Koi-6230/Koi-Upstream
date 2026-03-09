# KOI UPSTREAM

## Setup
First please follow the installation of the [AdvantageKit](https://docs.advantagekit.org/) framework.

After AdvatangeKit's setup initialize the superstate singleton object the following way:
```java
Superstate.getInstance()
          .setSuperstateSet<YOUR ENUM>(DEFAULT STATE);
```

and then in RobotPeriodic after `CommandScheduler.getInstance().run();` you must add the following line:
```java
Superstate.getInstance().updateLogic()
```