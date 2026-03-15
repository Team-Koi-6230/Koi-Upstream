# KoiUpstream — Team 6230 Shared Robot Library

A shared Java library for FRC Team 6230. Provides superstate-based subsystem coordination, AdvantageKit-integrated IO, live-tunable fields, and an on-robot characterization pipeline (SysId + LQR).

---

## Table of Contents
- [Installation](#installation)
- [Superstate System](#superstate-system)
- [UpstreamSubsystem](#upstreamsubsystem)
- [UpstreamIO](#upstreamio)
- [@Tunable](#tunable)
- [AutoTuner (SysId + LQR)](#autotuner-sysid--lqr)

---

## Installation

1. Follow the [AdvantageKit](https://docs.advantagekit.org/) installation guide.
2. Install the following vendor dependency `https://raw.githubusercontent.com/Team-Koi-6230/Koi-Upstream/refs/heads/main/KoiUpstream.json?v=6230`. Or alternativly, add the KoiUpstream sources to your robot project (local vendor dep or Gradle subproject)
3. In `robotInit()`, **before constructing any subsystems**, initialize the Superstate singleton with your state enum:

```java
Superstate.getInstance()
          .setSuperstateSet(RobotState.IDLE); // pass your default state
```

4. In `robotPeriodic()`, **after** `CommandScheduler.getInstance().run()`, call:

```java
Superstate.getInstance().updateLogic();
```

> **Warning:** `setSuperstateSet()` must be called before any `UpstreamSubsystem` is constructed. Constructing a subsystem first will cause it to subscribe to the default `DefaultStates` enum and trigger a DriverStation warning.

---

## Superstate System

The `Superstate` singleton is a robot-wide state machine. It broadcasts a *wanted state* to all registered subsystems and only commits the transition once every subsystem reports `isReady() == true`.

### How transitions work

1. `setWantedSuperstate(state)` is called (e.g. from a button binding).
2. Every subsystem's `handleSuperstate()` fires immediately — subsystems start moving toward the new state.
3. Each loop, `updateLogic()` polls every `isReady()`. Once all return `true`, the current state is committed.

### Driving transitions

```java
new JoystickButton(op, 1).onTrue(Commands.runOnce(() ->
    Superstate.getInstance().setWantedSuperstate(RobotState.SHOOTING)));
```

### Key methods

| Method | Description |
|---|---|
| `setSuperstateSet(E defaultState)` | Binds the manager to your enum. Call once before any subsystem is constructed. |
| `updateLogic()` | Polls readiness and commits transitions. Call in `robotPeriodic()`. |
| `addSubsystem(subsystem)` | Called automatically by `UpstreamSubsystem`'s constructor. |

---

## UpstreamSubsystem

Abstract base class for all robot subsystems. Extend it with three type parameters:

```java
public class ShooterSubsystem
    extends UpstreamSubsystem<RobotState, ShooterIO, ShooterIOInputsAutoLogged> {
```

| Type param | What it is |
|---|---|
| `S extends Enum<S>` | Your robot-wide state enum |
| `io extends UpstreamIO<I>` | The hardware abstraction layer for this subsystem |
| `I extends LoggableInputs` | The `@AutoLog`-generated inputs class |

### Lifecycle

`periodic()` is **final** — it calls `io.updateInputs(inputs)`, logs via AdvantageKit, then calls `update()`. Put all per-loop logic in `update()`.

### Abstract methods you must implement

| Method | Description |
|---|---|
| `void update()` | Called every loop after inputs are refreshed. |
| `boolean isReady()` | Return `true` when the subsystem has reached its wanted state. Gates superstate transitions. |

### Example

```java
public class ShooterSubsystem extends UpstreamSubsystem<RobotState, ShooterIO, ShooterIOInputsAutoLogged> {

    @Tunable private double targetRPM = 3500.0;

    public ShooterSubsystem() {
        super("Shooter", new ShooterIOInputsAutoLogged());

        addSuperstateBehaviour(RobotState.SHOOTING, () -> /* spin up */);
        addSuperstateBehaviour(RobotState.IDLE,     () -> /* coast */);
    }

    @Override
    public void update() {
        if (TunableManager.checkChanged(this)) {
            // re-apply gains after dashboard edit
        }
    }

    @Override
    public boolean isReady() {
        return Math.abs(inputs.velocityRPM - targetRPM) < 50.0;
    }

    @Override
    public boolean getIO() {
        return new ShooterIOSparkMax();
    }
}
```

---

## UpstreamIO

Hardware abstraction layer — one per subsystem. Implement it twice: real hardware and simulation. AdvantageKit's `@AutoLog` generates the inputs class.

The base `UpstreamIOInputs` already includes `appliedVolts`, `currentAmps[]`, and `tempCelsius[]`. Only add mechanism-specific fields in your subclass.

```java
public abstract class ShooterIO extends UpstreamIO<ShooterIOInputs> {

    @AutoLog
    public static class ShooterIOInputs extends UpstreamIOInputs {
        public double velocityRPM = 0.0;
    }

    public ShooterIO() { super("Shooter"); }

    public abstract void setVoltage(double volts);
}
```

> **Never** call hardware methods directly from `update()`. All hardware interaction goes through `io.*`.

---

## @Tunable

Annotate any `double`, `int`, `boolean`, or `String` field to have it automatically synced to NetworkTables. Changes made in AdvantageScope or Shuffleboard are reflected in the Java field live.

```java
@Tunable private double kP = 0.0002;
@Tunable private double targetRPM = 3500.0;
```

Call `TunableManager.checkChanged(this)` in `update()` to react to dashboard edits:

```java
@Override
public void update() {
    if (TunableManager.checkChanged(this)) {
        pid.setP(kP);
    }
}
```

`UpstreamSubsystem` and `UpstreamIO` call `TunableManager.register()` automatically in their constructors — no manual registration needed in subclasses.

Fields are published at: `/Tuning/<SubsystemName>/<fieldName>`

> **Tip:** Set `TunableManager.tuningModeEnabled = false` for competition to disable all NT writes.

---

## AutoTuner (SysId + LQR)

On-robot characterization pipeline. Run four test routines on the mechanism, then press Analyze. Feedforward constants (`kS`, `kV`, `kA`) and LQR-derived PID gains (`kP`, `kD`) are computed on-robot and published to NetworkTables.

### Step 1 — Implement SysIdMechanism

Your subsystem (or a wrapper) must implement the `SysIdMechanism` interface:

```java
public interface SysIdMechanism {
    void setVoltage(double volts);
    double getVelocity();
    double getPosition();
    void stopMotor();
}
```

### Step 2 — Configure and create the AutoTuner

```java
AutoTuner.Config config = new AutoTuner.Config();
config.type           = MechanismType.POSITION; // or VELOCITY
config.minPosition    = 0.05;  // in your encoder's native units
config.maxPosition    = 0.40;
config.tolerancePos   = 0.02;  // LQR state cost
config.toleranceVel   = 0.5;
config.toleranceVolts = 8.0;   // LQR effort cost
config.mechanismName  = "Arm";

AutoTuner tuner = new AutoTuner(armMechanism, config);
```

| Field | Default | Description |
|---|---|---|
| `type` | `VELOCITY` | `VELOCITY` or `POSITION` — determines LQR plant model |
| `minPosition` / `maxPosition` | ±∞ | Safety travel limits (test stops when hit) |
| `rampRate` | `0.25` V/s | Quasistatic ramp rate |
| `dynamicStep` | `4.0` V | Step voltage for dynamic tests |
| `maxVolts` | `7.0` V | Absolute voltage cap |
| `tolerancePos` | `0.02` | Acceptable position error for LQR Q matrix |
| `toleranceVel` | `0.5` | Acceptable velocity error for LQR Q matrix |
| `toleranceVolts` | `8.0` | Acceptable control effort for LQR R matrix |
| `dtSeconds` | `0.02` | Loop period |

### Step 3 — Bind to buttons

Hold each button to run that routine, release to stop. Run them in order.

```java
new JoystickButton(op, 1).whileTrue(tuner.quasistaticForward());
new JoystickButton(op, 2).whileTrue(tuner.quasistaticBackward());
new JoystickButton(op, 3).whileTrue(tuner.dynamicForward());
new JoystickButton(op, 4).whileTrue(tuner.dynamicBackward());

// Press once after all 4 runs to compute gains
new JoystickButton(op, 5).onTrue(tuner.analyze());
```

### Step 4 — Read the results

Results are printed to the console and published to NetworkTables under:

```
KoiUpstream/AutoTuner/<mechanismName>/kS
KoiUpstream/AutoTuner/<mechanismName>/kV
KoiUpstream/AutoTuner/<mechanismName>/kA
KoiUpstream/AutoTuner/<mechanismName>/kP
KoiUpstream/AutoTuner/<mechanismName>/kI
KoiUpstream/AutoTuner/<mechanismName>/kD
KoiUpstream/AutoTuner/<mechanismName>/lastTunedTimestamp
```

You can also retrieve results programmatically:

```java
tuner.getLastResult().ifPresent(result -> {
    System.out.println(result); // AutoTuneResult { FF: kS=... PID: kP=... }
});
```

> **Note:** `analyze()` will print an error and abort if any of the 4 routines haven't been committed yet.