package team6230.koiupstream.sysid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.wpi.first.wpilibj.Timer;

public class SysIdCollector {

    private final List<SysIdDataPoint> currentPoints = new ArrayList<>();
    private final Map<String, List<SysIdDataPoint>> allTests = new LinkedHashMap<>();
    private String currentKey = "";
    private final Timer timer = new Timer();

    public void startNewTest(SysIdTestType type, SysIdRoutine.Direction dir) {
        currentPoints.clear();
        currentKey = type.name() + "_" + dir.name();
        timer.restart();
    }

    public void record(double volts, double velocity, double position) {
        currentPoints.add(new SysIdDataPoint(
            timer.get(), volts, velocity, position
        ));
    }

    public void commitTest() {
        allTests.put(currentKey, new ArrayList<>(currentPoints));
    }

    public Map<String, List<SysIdDataPoint>> getAllData() {
        return Collections.unmodifiableMap(allTests);
    }
}