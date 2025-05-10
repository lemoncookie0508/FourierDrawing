package lepl.fourierdrawing;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class RotatingLine extends Line {
    public final RotatingLine parent;
    private final double length, freq, phase_const;

    public RotatingLine(RotatingLine parent, double length, double freq, double phase_const) {
        this.parent = parent;
        this.length = length;
        this.freq = freq;
        this.phase_const = phase_const;

        setStroke(Color.WHITE);
        setStrokeWidth(2.7); // 일정한 두께
        setStrokeLineCap(StrokeLineCap.ROUND); // 선 끝 둥글게

        setPhase(0);
    }

    public void setPhase(double t) {
        double sx = 0, sy = 0;
        if (parent != null) {
            parent.setPhase(t);
            sx = parent.getEndX();
            sy = parent.getEndY();
        }

        double
                ex = sx + length * Math.cos(- freq * t + phase_const),
                ey = sy + length * Math.sin(- freq * t + phase_const);

        setStartX(sx);
        setStartY(sy);
        setEndX(ex);
        setEndY(ey);
    }

    /**
     * @param indexFromLeaf 잎의 몇 번째 조상인지
     * @return 잎의 n번째 조상의 x좌표
     */
    public double getViewpointX(int indexFromLeaf) {
        if (indexFromLeaf <= 0 ) return getEndX();
        return parent.getViewpointX(indexFromLeaf - 1);
    }
    /**
     * @param indexFromLeaf 잎의 몇 번째 조상인지
     * @return 잎의 n번째 조상의 y좌표
     */
    public double getViewpointY(int indexFromLeaf) {
        if (indexFromLeaf <= 0 ) return getEndY();
        return parent.getViewpointY(indexFromLeaf - 1);
    }
}
