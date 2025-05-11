package lepl.fourierdrawing;

import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lepl.fouriertransform.Complex;
import lepl.fouriertransform.FourierTransform;
import lepl.lapplication4.LApplication;
import lepl.lapplication4.LUtil;
import lepl.lapplication4.lpane.LNormalPane;
import lepl.lapplication4.lpane.LPane;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DrawingApp extends LApplication {
    /**
     * 평면 이동을 위한 마우스 위치가 저장됩니다.
     */
    private Point2D lastMousePosition;

    /**
     * 초기 씬의 가로세로 길이
     */
    private final double sceneSize = 700;
    /**
     * 초기 평면의 가로세로 길이
     */
    private final double bgSize = 3500;

    @Override
    protected LPane primaryPane() {
        LNormalPane parent = new LNormalPane();

        AnchorPane bg = new AnchorPane();
        bg.setPrefSize(bgSize, bgSize);
        bg.setBackground(LUtil.coloredBackground(Color.BLACK));

        plane.setPrefSize(bg.getPrefWidth() / 2, bg.getPrefHeight() / 2);
        plane.getTransforms().addAll(
                new Translate(bg.getPrefWidth() / 2, bg.getPrefHeight() / 2),
                new Scale(1, -1));

        // 캔버스 추가
        Canvas canvas = new Canvas(bg.getPrefWidth(), bg.getPrefHeight());
        gc = canvas.getGraphicsContext2D();

        Group group = new Group(bg, canvas, plane);
        Scale scale = new Scale(1, 1, 0, 0);
        Translate translate = new Translate((sceneSize - bgSize) / 2, (sceneSize - bgSize) / 2);
        group.getTransforms().addAll(translate, scale);
        parent.getChildren().add(group);

        // 줌 구현
        parent.setOnScroll(e -> {
            double zoomFactor = 1.05;
            double scaleFactor = (e.getDeltaY() > 0) ? zoomFactor : 1 / zoomFactor;

            // 마우스 위치 (scene 기준). 뷰포인트 있을 경우 그걸 따라 감
            double sceneX = viewpoint.get() < 0 ? e.getSceneX()
                    : parent.getLayoutX() + parent.getPrefWidth() / 2;
            double sceneY = viewpoint.get() < 0 ? e.getSceneY()
                    : parent.getLayoutY() + parent.getPrefHeight() / 2;

            // 변환 전 local 좌표
            Point2D beforeZoom = group.sceneToLocal(sceneX, sceneY);

            // 스케일 적용
            scale.setX(scale.getX() * scaleFactor);
            scale.setY(scale.getY() * scaleFactor);

            // 변환 후 local 좌표
            Point2D afterZoom = group.sceneToLocal(sceneX, sceneY);

            // 마우스가 가리키는 위치를 고정하려면 이동량을 계산해서 translate 보정
            double dx = afterZoom.getX() - beforeZoom.getX();
            double dy = afterZoom.getY() - beforeZoom.getY();

            translate.setX(translate.getX() + dx * scale.getX());
            translate.setY(translate.getY() + dy * scale.getY());

            e.consume();
        });
        // 이동 구현
        // 마우스 눌렀을 때 위치 저장
        parent.setOnMouseMoved(e -> lastMousePosition = new Point2D(e.getSceneX(), e.getSceneY()));
        // 드래그 중이면 이동
        parent.setOnMouseDragged(e -> {
            if (viewpoint.get() < 0 && !e.isControlDown()) {
                double dx = e.getSceneX() - lastMousePosition.getX();
                double dy = e.getSceneY() - lastMousePosition.getY();

                translate.setX(translate.getX() + dx);
                translate.setY(translate.getY() + dy);

                lastMousePosition = new Point2D(e.getSceneX(), e.getSceneY());
            }
        });

        // 중앙 점
        Circle center = new Circle(0, 0, 4.5, Color.RED);
        plane.getChildren().add(center);

        // 클릭 시 점 추가
        group.setOnMouseClicked(e -> {
            if (e.isControlDown() && e.getButton() == MouseButton.PRIMARY) {
                Point2D point = plane.sceneToLocal(e.getSceneX(), e.getSceneY());
                addPoint(point.getX(), point.getY());
            }
        });

        // 시점 표시 원
        Circle viewpointCircle = new Circle(0, 0, 4.5, Color.AQUA);

        // 회전선을 시간에 바인드
        time.addListener((observable, oldVal, newVal) -> {
            if (leaf != null) {
                leaf.setPhase(newVal.doubleValue());

                if (viewpoint.get() >= 0) {
                    double ex = leaf.getViewpointX(points.size() - 1 - viewpoint.get());
                    double ey = leaf.getViewpointY(points.size() - 1 - viewpoint.get());

                    viewpointCircle.setCenterX(ex);
                    viewpointCircle.setCenterY(ey);

                    Point2D afterMove = plane.localToScene(ex, ey);
                    translate.setX(translate.getX() + parent.getLayoutX() + parent.getPrefWidth() / 2 - afterMove.getX());
                    translate.setY(translate.getY() + parent.getLayoutY() + parent.getPrefHeight() / 2 - afterMove.getY());
                }
            }
        });
        // 시점 바인딩
        viewpoint.addListener((observable, oldVal, newVal) -> {
            if (newVal.intValue() >= 0) {
                if (!plane.getChildren().contains(viewpointCircle)) plane.getChildren().add(viewpointCircle);

                double ex = leaf.getViewpointX(points.size() - 1 - newVal.intValue());
                double ey = leaf.getViewpointY(points.size() - 1 - newVal.intValue());

                viewpointCircle.setCenterX(ex);
                viewpointCircle.setCenterY(ey);

                Point2D afterMove = plane.localToScene(ex, ey);
                translate.setX(translate.getX() + parent.getLayoutX() + parent.getPrefWidth() / 2 - afterMove.getX());
                translate.setY(translate.getY() + parent.getLayoutY() + parent.getPrefHeight() / 2 - afterMove.getY());
            } else {
                plane.getChildren().remove(viewpointCircle);
            }
        });

        // 시작 단축키
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.SPACE),
                this::draw);
        // 그림 초기화 단축키
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                this::clearDrawing);
        // 전체 초기화 단축키
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                this::clearPoints);
        // 시점 초기화 단축키
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN), () -> {
            viewpoint.set(-1);
            scale.setX(1);
            scale.setY(1);
            translate.setX((parent.getPrefWidth() - bgSize) / 2);
            translate.setY((parent.getPrefHeight() - bgSize) / 2);
        });
        // 시점 변경 단축키
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.PERIOD, KeyCombination.CONTROL_DOWN),
                () -> viewpoint.set((viewpoint.get() + 2) % (points.size() + 1) - 1));
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN),
                () -> viewpoint.set((points.size() + 1 + viewpoint.get()) % (points.size() + 1) - 1));
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.PERIOD, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                () -> viewpoint.set(points.size() - 1));
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                () -> viewpoint.set(-1));
        // 무한반복 단축키
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), () -> {
            if (isRepeat.get()) {
                isRepeat.set(false);
                center.setFill(Color.RED);
            }
            else {
                isRepeat.set(true);
                center.setFill(Color.YELLOW);
            }
        });
        // 풀스크린 단축키
        parent.shortcuts.put(new KeyCodeCombination(KeyCode.F11),
                () -> ((Stage) parent.getScene().getWindow()).setFullScreen(!((Stage) parent.getScene().getWindow()).isFullScreen()));

        // 단축키 안내
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        content.getChildren().addAll(
                createMenuItem("노드 추가", "Ctrl+좌클릭"),
                createMenuItem("시작/중지", "Space"),
                createMenuItem("그림 제거", "Ctrl+R"),
                createMenuItem("초기화", "Ctrl+Shift+R"),
                createMenuItem("이동", "드래그"),
                createMenuItem("확대/축소", "스크롤"),
                createMenuItem("시점 초기화", "Ctrl+O"),
                createMenuItem("풀스크린", "F11"),
                createMenuItem("무한 반복/해제", "Ctrl+S"),
                createMenuItem("다음 노드에 시점 고정", "Ctrl+."),
                createMenuItem("이전 노드에 시점 고정", "Ctrl+,"),
                createMenuItem("마지막 노드에 시점 고정", "Ctrl+Shift+."),
                createMenuItem("시점 고정 해제", "Ctrl+Shift+,")
        );
        Popup popup = new Popup();
        popup.getContent().add(content);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setAutoFix(true);

        parent.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY)
                popup.show(parent.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });

        /* 직접 점 추가
        int gap = 100, n = 5, hGap = -15;
        for (int i = n; i > -n; i--) {
            addPoint(hGap*(2*i-1) - 200, 2*i*gap - gap / 2);
            addPoint(hGap*(2*i-1) - 200, 2*i*gap - gap / 2);
            addPoint(hGap*(2*i-1) + 200, (2*i - 1)*gap - gap / 2);
            addPoint(hGap*(2*i-1) + 200, (2*i - 1)*gap - gap / 2);
        }
        */

        return parent;
    }

    /**
     * 단축키 설명 추가
     * @param text 단축키 역할
     * @param shortcut 단축키
     * @return 메뉴 아이템
     */
    private AnchorPane createMenuItem(String text, String shortcut) {
        Label left = new Label(text);
        Label right = new Label(shortcut);
        right.setStyle("-fx-text-fill: gray;");
        AnchorPane.setLeftAnchor(left, 0D);
        AnchorPane.setRightAnchor(right, 0D);
        left.setFont(LUtil.notoSansKrRegular(12));
        right.setFont(LUtil.notoSansKrRegular(12));

        AnchorPane box = new AnchorPane(left, right);
        box.setPrefWidth(250);

        box.setPadding(new Insets(4, 10, 4, 10));

        return box;
    }

    /**
     * 회전하는 선들이 추가되는 평면. 오른쪽 위가 1사분면이 되도록 변환됨
     */
    private final AnchorPane plane = new AnchorPane();
    /**
     * 캔버스에 선을 그리기 위한 도구. 좌표 변환되지 않았음
     */
    private GraphicsContext gc;

    /**
     * 데이터 세트 보관
     */
    private final ArrayList<Complex> points = new ArrayList<>();
    /**
     * 데이터 세트를 시각화하는 점 보관
     */
    private final ArrayList<Circle> dots = new ArrayList<>();
    /**
     * 가장 마지막 회전선
     */
    private RotatingLine leaf;

    /**
     * 점을 추가합니다.
     * @param x 수학적 x좌표
     * @param y 수학적 y좌표
     */
    public void addPoint(double x, double y) {
        if (isRunning.get()) return;

        points.add(new Complex(x, y));

        Circle dot = new Circle(x, y, 1.5, Color.WHITE);
        dots.add(dot);
        plane.getChildren().add(dot);

        refreshLines();
    }

    /**
     * 점을 모두 삭제하고 초기화합니다. 그리는 중에는 동작하지 않습니다.
     */
    public void clearPoints() {
        clearDrawing();
        if (isRunning.get()) return;

        points.clear();
        for (Circle c : dots) {
            plane.getChildren().remove(c);
        }
        dots.clear();

        viewpoint.set(-1);
        refreshLines();
    }
    /**
     * 그린 그림만을 없애고 초기화합니다. 그리는 중에는 동작하지 않습니다.
     */
    public void clearDrawing() {
        if (isRunning.get() && !isPaused.get()) return;
        time.set(0);
        isRunning.set(false);
        isPaused.set(false);
        gc.clearRect(0, 0, bgSize, bgSize);
    }

    /**
     * 추가된 점에 따라 다시 푸리에 변환을 실행합니다.
     */
    public void refreshLines() {
        if (isRunning.get()) return;

        while (leaf != null) {
            plane.getChildren().remove(leaf);
            leaf = leaf.parent;
        }

        ArrayList<Complex> transformed = FourierTransform.dft(points);
        for (int i = 0; i < transformed.size(); i++) {
            leaf = new RotatingLine(
                    leaf,
                    transformed.get(i).abs(),
                    ((i + 1) >> 1) * (i % 2 == 0 ? -1 : 1),
                    transformed.get(i).angle()
            );

            plane.getChildren().add(leaf);
        }
    }

    /**
     * 선 무작위 색상을 위한 랜덤 객체
     */
    private final Random r = new Random();
    /**
     * 반복 시 지우는 단계를 위한 페인트. 랜덤 색상은 검은색이 아니므로 겹치지 않음
     */
    private final Color remover = Color.BLACK;

    /**
     * 현재 시점이 몇 번 인덱스의 노드에 고정되어 있는지를 나타냅니다. 음수일 경우 고정되지 않습니다.
     */
    private final SimpleIntegerProperty viewpoint = new SimpleIntegerProperty(-1);

    /**
     * 애니메이션이 시작했는지를 나타냅니다. 일시정지해도 true 입니다.
     */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    /**
     * 애니메이션이 일시정지된 상태인지를 나타냅니다.
     */
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    /**
     * 애니메이션이 무한반복 상태인지를 나타냅니다.
     */
    private final AtomicBoolean isRepeat = new AtomicBoolean(false);

    /**
     * 선을 그리기 위해 이전 점의 좌표 보관
     */
    private final double[] last = new double[2];
    /**
     * 회전선의 위상을 결정하는 시각
     */
    private final SimpleDoubleProperty time = new SimpleDoubleProperty(0);

    /**
     * 그림을 그리는 스레드 담당 애니메이션타이머 객체
     */
    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            double t = time.get() + 0.0625 / (points.size() >> 1);

            if (t >= 2 * Math.PI) {
                if (isRepeat.get()) {
                    t -= 2 * Math.PI;
                    if (gc.getStroke().equals(remover)) {
                        gc.setStroke(Color.rgb(r.nextInt(1, 256), r.nextInt(1, 256), r.nextInt(1, 256)));
                        gc.setLineWidth(3);
                    }
                    else {
                        gc.setStroke(remover);
                        gc.setLineWidth(4.5);
                    }
                }
                else {
                    stop();
                    t = 0;
                    isRunning.set(false);
                    isPaused.set(false);
                }
            }

            time.set(t);

            // 선 끝 좌표
            double x = leaf.getEndX() + bgSize / 2;
            double y = bgSize / 2 - leaf.getEndY();

            gc.strokeLine(last[0], last[1], x, y);

            last[0] = x;
            last[1] = y;
        }
    };

    /**
     * 그림 그리기 시작
     */
    public void draw() {
        if (leaf == null) return;
        if (isRunning.get()) {
            if (isPaused.get()) {
                isPaused.set(false);
                timer.start();
            }
            else {
                isPaused.set(true);
                timer.stop();
            }
            return;
        }

        gc.clearRect(0, 0, bgSize, bgSize);
        gc.setLineWidth(3);
        gc.setStroke(Color.rgb(r.nextInt(1, 256), r.nextInt(1, 256), r.nextInt(1, 256)));

        last[0] = leaf.getEndX() + bgSize / 2;
        last[1] = bgSize / 2 - leaf.getEndY();

        time.set(0);
        isRunning.set(true);
        isPaused.set(false);
        timer.start();
    }

    /**
     * 프로그램 속성 설정
     * @return 속성
     */
    @Override
    protected Attribute attribute() {
        Attribute attribute = new Attribute();

        attribute.title = "푸리에 그림";
        attribute.icon = new Image(LUtil.getResource("images/icon.png"));

        attribute.stageWidth = sceneSize;
        attribute.stageHeight = sceneSize;

        attribute.minWidth = 200;
        attribute.minHeight = 200;

        return attribute;
    }
}
