package team6230.koiupstream.mechanisms.roller.io;

import java.lang.Cloneable;
import java.lang.Override;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public class RollerInputsAutoLogged extends RollerIO.RollerInputs implements LoggableInputs, Cloneable {
  @Override
  public void toLog(LogTable table) {
    table.put("VelocityRpm", velocityRpm);
    table.put("TargetRpm", targetRpm);
    table.put("AppliedVolts", appliedVolts);
    table.put("CurrentAmps", currentAmps);
    table.put("TempCelsius", tempCelsius);
    table.put("KP", kP);
    table.put("KI", kI);
    table.put("KD", kD);
    table.put("KS", kS);
    table.put("KV", kV);
  }

  @Override
  public void fromLog(LogTable table) {
    velocityRpm = table.get("VelocityRpm", velocityRpm);
    targetRpm = table.get("TargetRpm", targetRpm);
    appliedVolts = table.get("AppliedVolts", appliedVolts);
    currentAmps = table.get("CurrentAmps", currentAmps);
    tempCelsius = table.get("TempCelsius", tempCelsius);
    kP = table.get("KP", kP);
    kI = table.get("KI", kI);
    kD = table.get("KD", kD);
    kS = table.get("KS", kS);
    kV = table.get("KV", kV);
  }

  public RollerInputsAutoLogged clone() {
    RollerInputsAutoLogged copy = new RollerInputsAutoLogged();
    copy.velocityRpm = this.velocityRpm;
    copy.targetRpm = this.targetRpm;
    copy.appliedVolts = this.appliedVolts;
    copy.currentAmps = this.currentAmps.clone();
    copy.tempCelsius = this.tempCelsius.clone();
    copy.kP = this.kP;
    copy.kI = this.kI;
    copy.kD = this.kD;
    copy.kS = this.kS;
    copy.kV = this.kV;
    return copy;
  }
}
