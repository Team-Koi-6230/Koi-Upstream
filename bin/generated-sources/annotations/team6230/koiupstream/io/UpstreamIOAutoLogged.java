package team6230.koiupstream.io;

import java.lang.Cloneable;
import java.lang.Override;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public class UpstreamIOAutoLogged extends UpstreamIO implements LoggableInputs, Cloneable {
  @Override
  public void toLog(LogTable table) {
  }

  @Override
  public void fromLog(LogTable table) {
  }

  public UpstreamIOAutoLogged clone() {
    UpstreamIOAutoLogged copy = new UpstreamIOAutoLogged();
    return copy;
  }
}
