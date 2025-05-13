package lepl.fouriertransform;

import java.util.ArrayList;

public class FourierTransform {
    public static ArrayList<Complex> dft(ArrayList<Complex> signal) {
        int s = signal.size();

        ArrayList<Complex> transformed = new ArrayList<>(s);
        // 주파수 수열 0, 1, -1, 2, -2, ...
        for (int i = 1; i < s + 1; i++) {
            int freq = i / 2 * (i % 2 == 0 ? 1 : -1);
            Complex c = new Complex();
            for (int j = 0; j < s; j++) {
                c.add(
                        Complex.unit(freq * 2 * Math.PI * j / s).mul(signal.get(j))
                );
            }
            transformed.add(c.divide(s));
        }

        return transformed;
    }

    public static ArrayList<Complex> dft(ArrayList<Complex> signal, FrequencyFunction function) {
        int s = signal.size();

        ArrayList<Complex> transformed = new ArrayList<>(s);

        for (int i = 0; i < s; i++) {
            Complex c = new Complex();
            for (int j = 0; j < s; j++) {
                c.add(
                        // 복소수는 각이 서로 반대여야 곱하면 양의 실수가 됨
                        Complex.unit(- function.get(i) * 2 * Math.PI * j / s).mul(signal.get(j))
                );
            }
            transformed.add(c.divide(s));
        }

        return transformed;
    }
}
