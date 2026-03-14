package team6230.koiupstream.sysid;

public class SysIdAnalyzer {

    public static SysIdResult analyze(
            List<SysIdDataPoint> quasistaticFwd,
            List<SysIdDataPoint> quasistaticBwd,
            List<SysIdDataPoint> dynamicFwd,
            List<SysIdDataPoint> dynamicBwd) {

        double[] ksKv = fitKsKv(quasistaticFwd, quasistaticBwd);
        double kS = ksKv[0];
        double kV = ksKv[1];
        double kA = fitKa(dynamicFwd, dynamicBwd, kS, kV);

        return new SysIdResult(kS, kV, kA);
    }

    // Linear regression on quasistatic data: V = kS + kV * |v|
    private static double[] fitKsKv(List<SysIdDataPoint>... datasets) {
        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
        int n = 0;

        for (var dataset : datasets) {
            for (var p : dataset) {
                if (Math.abs(p.velocityNative()) < 0.01) continue; // skip stiction region
                double x = Math.abs(p.velocityNative());
                double y = Math.abs(p.appliedVolts());
                sumX  += x;
                sumY  += y;
                sumXX += x * x;
                sumXY += x * y;
                n++;
            }
        }

        double kV = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double kS = (sumY - kV * sumX) / n;
        return new double[]{kS, kV};
    }

    // From dynamic data: kA = (V - kS*sign(v) - kV*v) / (dv/dt)
    private static double fitKa(List<SysIdDataPoint> fwd, List<SysIdDataPoint> bwd,
                                  double kS, double kV) {
        List<Double> estimates = new ArrayList<>();

        for (var dataset : List.of(fwd, bwd)) {
            for (int i = 1; i < dataset.size() - 1; i++) {
                var prev = dataset.get(i - 1);
                var curr = dataset.get(i);
                var next = dataset.get(i + 1);

                double dt = next.timestampSeconds() - prev.timestampSeconds();
                if (dt < 1e-6) continue;

                double accel = (next.velocityNative() - prev.velocityNative()) / dt;
                if (Math.abs(accel) < 0.1) continue; // skip near-zero accel (noisy)

                double v        = curr.velocityNative();
                double V        = curr.appliedVolts();
                double residual = V - kS * Math.signum(v) - kV * v;
                estimates.add(residual / accel);
            }
        }

        return estimates.stream().mapToDouble(d -> d).average().orElse(0.0);
    }
}