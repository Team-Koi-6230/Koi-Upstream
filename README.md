# KOI UPSTREAM

## Superstate based subsystems

### Setup
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

<hr/>

## Mechanism auto-tuner

### example usage

```java
AutoTuner.Config armConfig = new AutoTuner.Config();
armConfig.type           = MechanismType.POSITION;
// // should be in the configured units of the motor encoder
armConfig.minPosition    = 0.05; 
armConfig.maxPosition    = 0.40;
armConfig.tolerancePos   = 0.02;
armConfig.toleranceVel   = 0.5;
armConfig.toleranceVolts = 8.0;
armConfig.mechanismName  = "Arm";

AutoTuner armTuner = new AutoTuner(armSubsystem, armConfig);

// bind to buttons (hold to run, release to stop)
new JoystickButton(op, 1).whileTrue(armTuner.quasistaticForward());
new JoystickButton(op, 2).whileTrue(armTuner.quasistaticBackward());
new JoystickButton(op, 3).whileTrue(armTuner.dynamicForward());
new JoystickButton(op, 4).whileTrue(armTuner.dynamicBackward());

// press once after all 4 runs
new JoystickButton(op, 5).onTrue(armTuner.analyze())
```