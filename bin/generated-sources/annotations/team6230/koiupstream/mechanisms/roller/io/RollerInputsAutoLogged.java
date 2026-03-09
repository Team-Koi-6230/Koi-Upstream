package team6230.koiupstream.mechanisms.roller.io;

import java.lang.Cloneable;
import java.lang.Override;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public class RollerInputsAutoLogged extends RollerIO.RollerInputs implements LoggableInputs, Cloneable {
  @Override
  public void toLog(LogTable table) {
    table.put("Rpm", rpm);
    table.put("AppliedVolts", appliedVolts);
    table.put("CurrentAmps", currentAmps);
  }

  @Override
  public void fromLog(LogTable table) {
    rpm = table.get("Rpm", rpm);
    appliedVolts = table.get("AppliedVolts", appliedVolts);
    currentAmps = table.get("CurrentAmps", currentAmps);
  }

  public RollerInputsAutoLogged clone() {
    RollerInputsAutoLogged copy = new RollerInputsAutoLogged();
    copy.rpm = this.rpm;
    copy.appliedVolts = this.appliedVolts;
    copy.currentAmps = this.currentAmps.clone();
    return copy;
  }
}
