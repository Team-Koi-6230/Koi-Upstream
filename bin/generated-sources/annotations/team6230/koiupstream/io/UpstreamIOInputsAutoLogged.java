package team6230.koiupstream.io;

import java.lang.Cloneable;
import java.lang.Override;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public class UpstreamIOInputsAutoLogged extends UpstreamIO.UpstreamIOInputs implements LoggableInputs, Cloneable {
  @Override
  public void toLog(LogTable table) {
    table.put("AppliedVolts", appliedVolts);
    table.put("CurrentAmps", currentAmps);
    table.put("TempCelsius", tempCelsius);
  }

  @Override
  public void fromLog(LogTable table) {
    appliedVolts = table.get("AppliedVolts", appliedVolts);
    currentAmps = table.get("CurrentAmps", currentAmps);
    tempCelsius = table.get("TempCelsius", tempCelsius);
  }

  public UpstreamIOInputsAutoLogged clone() {
    UpstreamIOInputsAutoLogged copy = new UpstreamIOInputsAutoLogged();
    copy.appliedVolts = this.appliedVolts;
    copy.currentAmps = this.currentAmps.clone();
    copy.tempCelsius = this.tempCelsius.clone();
    return copy;
  }
}
