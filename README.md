# 🐟 KoiUpstream — Team 6230 Shared Robot Library

KoiUpstream is a highly structured, AdvantageKit-integrated robot framework designed for FRC Team 6230. It enforces deterministic code, streamlines hardware abstraction, and synchronizes robot mechanisms using a global state machine.

---

## 📖 Table of Contents
1. [Quick Start: UpstreamedRobot Template](#-quick-start-upstreamedrobot-template)
2. [Installation (Vendor Dependency)](#-installation-vendor-dependency)
3. [Project Architecture](#-project-architecture)
4. [The Superstate System](#-the-superstate-system)
5. [Hardware Abstraction (UpstreamIO)](#-hardware-abstraction-upstreamio)
6. [Live Tuning (`@Tunable`)](#-live-tuning-tunable)
7. [Conditional Actions](#-conditional-actions)
8. [Drivebase & Input Utilities](#-drivebase--input-utilities)

---

## 🚀 Quick Start: UpstreamedRobot Template

The fastest way to start a new robot project with KoiUpstream is to use our pre-configured skeleton template. It comes with AdvantageKit, KoiUpstream, and a basic swerve drivebase structure already set up.

1. Navigate to the **[UpstreamedRobot Repository](https://github.com/Team-Koi-6230/UpstreamedRobot)**.
2. Click the green **Use this template** button (or "Create a new repository").
3. Clone your new repository to your local machine.
4. Open the project in WPILib VS Code and build the code to fetch all dependencies.

---

## 📦 Installation (Vendor Dependency)

If you are adding KoiUpstream to an *existing* project, it requires [AdvantageKit](https://docs.advantagekit.org/) to be installed first.

**To add KoiUpstream as a Vendor Dependency:**
1. Open WPILib VS Code.
2. Click the WPILib icon (or press `Ctrl+Shift+P`) and select **Manage Vendor Libraries**.
3. Select **Install new libraries (online)**.
4. Paste the following URL:
   `https://raw.githubusercontent.com/Team-Koi-6230/Koi-Upstream/refs/heads/main/KoiUpstream.json`
5. Build your project.

---

## 🏗️ Project Architecture

KoiUpstream enforces a strict separation of logic and hardware to ensure AdvantageKit log replay works perfectly. 

* **`UpstreamSubsystem.java`:** The brain. It handles the logic, state machines, and calculations. It never talks to hardware directly.
* **`UpstreamIO.java`:** The interface. It defines the inputs (sensor data) and default methods to interact with the hardware.
* **Real/Sim IO Implementations:** The classes that actually talk to the SparkMax/TalonFX or the physics simulator, implementing your `UpstreamIO` interface.

---

## 🧠 The Superstate System

Instead of each subsystem managing its own independent state, KoiUpstream relies on a centralized `Superstate` singleton to coordinate the entire robot. When a new state is requested, it is broadcasted to all subsystems. The global state only officially updates once every subsystem's `isReady()` method returns true.

### Initializing the Superstate
You **must** initialize the Superstate before constructing any subsystems.

```java
// 1. Define your robot's global states
public enum RobotState {
    IDLE, 
    INTAKING, 
    SHOOTING
}

// 2. In Robot.java or RobotContainer.java
public void robotInit() {
    // MUST be called before constructing any UpstreamSubsystem!
    Superstate.getInstance().setSuperstateSet(RobotState.IDLE);
    
    // Now it is safe to instantiate subsystems
    shooterSubsystem = new ShooterSubsystem(new ShooterIOSparkMax());
}

public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    // Updates the global state machine after commands run
    Superstate.getInstance().updateLogic();
}
```

---

## 🔌 Hardware Abstraction (UpstreamIO)

To create a subsystem, extend `UpstreamSubsystem` and provide it with your IO layer.

### 1. The IO Interface
```java
import org.littletonrobotics.junction.AutoLog;
import team6230.koiupstream.io.UpstreamIO;

public interface ShooterIO extends UpstreamIO<ShooterIO.ShooterIOInputs> {
    
    @AutoLog
    public static class ShooterIOInputs extends UpstreamIO.UpstreamIOInputs {
        public double velocityRPM = 0.0;
        public double appliedVolts = 0.0;
    }

    public default void setVoltage(double volts) {}
}
```

### 2. The Subsystem
```java
import team6230.koiupstream.subsystems.UpstreamSubsystem;

public class ShooterSubsystem extends UpstreamSubsystem<RobotState, ShooterIO, ShooterIOInputsAutoLogged> {

    private final ShooterIO hardwareIO;
    private double targetRPM = 3000.0;

    public ShooterSubsystem(ShooterIO io) {
        super("Shooter", new ShooterIOInputsAutoLogged());
        this.hardwareIO = io;

        // Map global states to specific hardware actions
        addSuperstateBehaviour(RobotState.SHOOTING, () -> io.setVoltage(12.0));
        addSuperstateBehaviour(RobotState.IDLE,     () -> io.setVoltage(0.0));
        
        // Fallback if the current state isn't mapped
        addDefaultSuperstateBehaviour(() -> io.setVoltage(0.0));
    }

    @Override
    public void update() {
        // Custom periodic logic goes here
    }

    @Override
    public boolean isReady() {
        // Gates the superstate transition until ready
        return Math.abs(inputs.velocityRPM - targetRPM) < 50.0;
    }

    @Override
    protected ShooterIO getIO() {
        return this.hardwareIO;
    }
}
```

---

## 🎛️ Live Tuning (`@Tunable`)

KoiUpstream includes a reflection-based live tuning system. You can edit constants on the fly via AdvantageScope without redeploying code.

```java
import team6230.koiupstream.tunable.Tunable;
import team6230.koiupstream.tunable.TunableManager;

public class ShooterSubsystem extends UpstreamSubsystem<RobotState, ShooterIO, ShooterIOInputsAutoLogged> {

    @Tunable private double kP = 0.1;
    @Tunable private double targetRPM = 3000.0;

    @Override
    public void update() {
        // Syncs dashboard changes to local variables automatically
        if (TunableManager.checkChanged(this)) {
            System.out.println("Gains updated! New target: " + targetRPM);
            // Re-apply PID gains to your hardware here if necessary
        }
    }
    // ...
}
```

---

## ⚡ Conditional Actions

Conditional Actions are "fire-and-forget" listeners. They keep your update loops clean by evaluating a condition and firing a single callback once before safely destroying themselves.

```java
public void scheduleIndexerFeed() {
    // Checks the condition every loop. 
    // Once true, runs the action and removes itself from the queue.
    registerConditionalAction(new ConditionalAction(
        () -> this.isReady(),            // The Condition
        () -> indexerIO.setVoltage(12.0) // The Action
    ));
}
```

---

## 🚗 Drivebase & Input Utilities

KoiUpstream provides built-in utilities for swerve drives, including state-based drive modes and mathematically shaped controller inputs.

### `KoiController` & `UpstreamDrivebase`

```java
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import team6230.koiupstream.subsystems.UpstreamDrivebase;
import team6230.koiupstream.utils.KoiController;
import team6230.koiupstream.utils.SwerveInputStream;

public class DriveSubsystem extends UpstreamDrivebase<RobotState> {
    
    public DriveSubsystem(KoiController driverController) {
        // 1. Pass the shaped, rate-limited, coordinate-mapped inputs to the drivebase
        super(new SwerveInputStream(
            driverController::getSwerveDrive, 
            driverController::getSwerveStrafe, 
            driverController::getSwerveTurn
        ));
        
        // 2. Define standard driving behavior
        registerDefaultDrive((x, y, omega) -> {
            return new ChassisSpeeds(x.getAsDouble(), y.getAsDouble(), omega.getAsDouble());
        });
        
        // 3. Override input behavior for specific states (e.g., auto-aiming)
        registerDriveMode(RobotState.SHOOTING, (x, y, omega) -> {
            return new ChassisSpeeds(x.getAsDouble(), y.getAsDouble(), calculateAutoAimOmega());
        });
    }

    @Override
    protected void updateInputs() {
        // Update gyro and swerve module inputs here
    }

    @Override
    public void runVelocity(ChassisSpeeds speeds) {
        // Command your swerve modules using the calculated speeds
    }
}
```

```java
// In RobotContainer.java
// Port 0, 0.1 deadband, 3.0 translation slew rate, 3.0 rotation slew rate
KoiController driver = new KoiController(0, 0.1, 3.0, 3.0);
```