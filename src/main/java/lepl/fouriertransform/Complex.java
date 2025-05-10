package lepl.fouriertransform;

public class Complex {
    private double real = 0, imaginary = 0;
    /**
     * @return 실수부
     */
    public double real() {
        return real;
    }
    /**
     * @return 허수부
     */
    public double imaginary() {
        return imaginary;
    }

    /**
     * @param theta rad 단위의 각도
     * @return exp(i * theta)
     */
    public static Complex unit(double theta) {
        return new Complex(Math.cos(theta), Math.sin(theta));
    }

    /**
     * 0+0i를 생성합니다.
     */
    public Complex() {}

    /**
     * @param real 복소수의 실수부
     * @param imaginary 복소수의 허수부
     */
    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
     * 이 복소수에 주어진 복소수를 더합니다.
     * @param c 더할 복소수
     * @return 자기 자신
     */
    public Complex add(Complex c) {
        this.real += c.real;
        this.imaginary += c.imaginary;
        return this;
    }
    /**
     * 이 복소수에 주어진 복소수를 곱합니다.
     * @param c 곱할 복소수
     * @return 자기 자신
     */
    public Complex mul(Complex c) {
        double t1 = real, t2 = imaginary;
        this.real = t1 * c.real - t2 * c.imaginary;
        this.imaginary = t1 * c.imaginary + t2 * c.real;
        return this;
    }
    /**
     * 이 복소수를 주어진 실수로 나눕니다.
     * @param d 나눌 수
     * @return 자기 자신
     */
    public Complex divide(double d) {
        real /= d;
        imaginary /= d;
        return this;
    }

    /**
     * 복소수의 절댓값을 반환합니다.
     * @return 복소수 절댓값(음이 아닌 실수)
     */
    public double abs() {
        return Math.sqrt(real * real + imaginary * imaginary);
    }

    /**
     * 복소평면상에서 실수축으로부터 이 수의 동경을 반환합니다.
     * @return 라디안 각도
     */
    public double angle() {
        double d = Math.acos(real / abs());
        if (imaginary < 0) d = - d;
        return d;
    }

    @Override
    public String toString() {
        return real + ((imaginary < 0) ? " " : " +") + imaginary + "i";
    }
}
