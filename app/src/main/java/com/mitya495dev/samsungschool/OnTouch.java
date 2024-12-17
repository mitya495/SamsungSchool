package com.mitya495dev.samsungschool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OnTouch extends View {

    private List<TrailPoint> trailPoints = new ArrayList<>();
    private List<ExplosionParticle> explosionParticles = new ArrayList<>();
    private Paint trailPaint;
    private static final int TRAIL_LENGTH = 50; // Длина шлейфа
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    public OnTouch(Context context) {
        super(context);
        init();
    }

    public OnTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trailPaint = new Paint();
        trailPaint.setStyle(Paint.Style.FILL);
        trailPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Рисуем шлейф
        for (int i = 0; i < trailPoints.size(); i++) {
            TrailPoint point = trailPoints.get(i);

            if (point.radius > 0) {
                // Использование случайного эффекта для свечения
                RadialGradient gradient = new RadialGradient(point.x, point.y, point.radius,
                        point.colorStart, point.colorEnd, Shader.TileMode.CLAMP);
                trailPaint.setShader(gradient);

                // Тень для глубины
                trailPaint.setShadowLayer(5f, 2f, 2f, point.shadowColor);

                // Рисуем точку с эффектом свечения и тени
                canvas.drawRect(point.x - point.radius / 2, point.y - point.radius / 2,
                        point.x + point.radius / 2, point.y + point.radius / 2, trailPaint);
            }
        }

        // Обновляем и рисуем частицы взрыва
        for (int i = 0; i < explosionParticles.size(); i++) {
            ExplosionParticle particle = explosionParticles.get(i);
            if (particle.alpha > 0) {
                particle.update();  // Обновляем позицию и альфа-значение
                trailPaint.setColor(particle.color);
                trailPaint.setAlpha((int) (particle.alpha * 255));  // Используем обновлённое альфа-значение
                canvas.drawCircle(particle.x, particle.y, particle.size, trailPaint);
            } else {
                explosionParticles.remove(i);  // Удаляем частицы, которые исчезли
                i--;  // Корректируем индекс после удаления элемента
            }
        }

        // Рисуем линии между точками
        drawConnectingLines(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                addTrailPoint(x, y);
                break;
        }
        return true;
    }

    private void addTrailPoint(float x, float y) {
        // Случайные параметры для новых точек
        int colorStart = getRandomColor();
        int colorEnd = getRandomColor();  // Генерируем случайный конечный цвет
        float radius = random.nextFloat() * 10 + 1;  // Случайный размер пикселя
        int shadowColor = getRandomShadowColor();

        TrailPoint newPoint = new TrailPoint(x, y, colorStart, colorEnd, radius, shadowColor);

        // Добавляем точку в начало списка
        trailPoints.add(0, newPoint);

        // Убираем точки старше 2 секунд
        handler.postDelayed(() -> {
            trailPoints.remove(newPoint);
            invalidate();
        }, 800);

        // Ограничиваем количество точек в шлейфе
        if (trailPoints.size() > TRAIL_LENGTH) {
            trailPoints.remove(trailPoints.size() - 1);
        }

        // Анимация для точки
        animatePoint(newPoint);

        // Включение молний, энергетических полей
        if (random.nextInt(5) == 0) {
            createLightningEffect(newPoint);
        }

        // Случайный шанс взрыва (для текущего пикселя)
        if (random.nextInt(50) == 0) {
            createExplosionEffect(newPoint);
        }

        // Случайный шанс для текущего пикселя разлететься на несколько других
        if (random.nextInt(45) == 0) { // Примерная вероятность 1 из 5
            explodePixel(newPoint);
        }

        invalidate();  // Перерисовываем View
    }

    private void animatePoint(TrailPoint point) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (point.radius < 15f) {  // Максимальный радиус
                    point.radius += 0.5f;
                    point.alpha = Math.max(0f, point.alpha - 0.05f);

                    // Плавное изменение цветов
                    point.colorStart = getRandomColor();  // Генерируем новый случайный начальный цвет
                    point.colorEnd = getRandomColor();    // Генерируем новый случайный конечный цвет

                    // Вихревое движение
                    point.x += (float) (Math.sin(point.angle) * 2);
                    point.y += (float) (Math.cos(point.angle) * 2);
                    point.angle += 0.1f;

                    invalidate();
                    handler.postDelayed(this, 4);  // Повторяем анимацию каждые 20 мс
                }
            }
        }, 20);
    }

    private void createLightningEffect(TrailPoint point) {
        // Создание молнии (неоновая линия)
        if (random.nextInt(2) == 0) {
            point.x += random.nextInt(20) - 10;  // Молния разлетается в случайных направлениях
            point.y += random.nextInt(20) - 10;
            point.colorStart = 0xFFFFFF00;  // Желтый неон
            point.colorEnd = 0xFFFF00FF;   // Пурпурный неон
        }
    }

    private void createExplosionEffect(TrailPoint point) {
        // Эффект взрыва (пиксель рассыпается на частицы)
        int particlesCount = 20 + random.nextInt(10); // Количество частиц в взрыве
        for (int i = 0; i < particlesCount; i++) {
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float speed = random.nextFloat() * 5 + 5; // Случайная скорость
            float particleX = point.x;
            float particleY = point.y;
            float particleSize = random.nextFloat() * 5 + 2; // Размер частицы
            int color = getRandomColor(); // Случайный цвет

            // Добавляем частицу взрыва
            ExplosionParticle particle = new ExplosionParticle(particleX, particleY, angle, speed, particleSize, color);
            explosionParticles.add(particle);
        }

        // Частицы теперь остаются и не исчезают, пока не выйдут за пределы экрана
    }

    private void explodePixel(TrailPoint point) {
        // Генерируем случайное количество частиц (например, от 5 до 15)
        int particlesCount = random.nextInt(10) + 5;
        for (int i = 0; i < particlesCount; i++) {
            // Генерация случайных направлений и параметров для частиц
            float angle = random.nextFloat() * 2 * (float) Math.PI;  // Случайный угол
            float speed = random.nextFloat() * 5 + 3;  // Случайная скорость
            float size = random.nextFloat() * 3 + 2;  // Размер частицы
            int color = getRandomColor();  // Случайный цвет для частицы

            // Создание частицы, которая появляется в точке взрыва
            ExplosionParticle particle = new ExplosionParticle(point.x, point.y, angle, speed, size, color);
            explosionParticles.add(particle);
        }

        // Частицы теперь остаются и не исчезают, пока не выйдут за пределы экрана
    }

    private void drawConnectingLines(Canvas canvas) {
        // Рисуем линии между точками шлейфа
        for (int i = 0; i < trailPoints.size() - 1; i++) {
            TrailPoint start = trailPoints.get(i);
            TrailPoint end = trailPoints.get(i + 1);

            // Создаем линию, которая соединяет точки
            trailPaint.setColor(getRandomColor());
            trailPaint.setStrokeWidth(2f);
            trailPaint.setAlpha((int) (start.alpha * 255));

            canvas.drawLine(start.x, start.y, end.x, end.y, trailPaint);
        }
    }

    private int getRandomColor() {
        // Генерация случайного цвета
        int[] colors = {0xFF00FFFF, 0xFFFF00FF, 0xFFFFFF00, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
        return colors[random.nextInt(colors.length)];
    }

    private int getRandomShadowColor() {
        int[] shadowColors = {0x44000000, 0x4400FF00, 0x44FF0000, 0x4400FFFF};
        return shadowColors[random.nextInt(shadowColors.length)];
    }

    private static class TrailPoint {
        float x, y;
        int colorStart, colorEnd;
        float radius;
        float alpha = 1f;
        int shadowColor;
        float angle = 0f;

        TrailPoint(float x, float y, int colorStart, int colorEnd, float radius, int shadowColor) {
            this.x = x;
            this.y = y;
            this.colorStart = colorStart;
            this.colorEnd = colorEnd;
            this.radius = radius;
            this.shadowColor = shadowColor;
        }
    }

    private class ExplosionParticle {
        float x, y;
        float angle;
        float speed;
        float size;
        int color;
        float alpha = 1f;  // Добавляем поле alpha

        ExplosionParticle(float x, float y, float angle, float speed, float size, int color) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.speed = speed;
            this.size = size;
            this.color = color;
        }

        void update() {
            x += (float) (Math.cos(angle) * speed);
            y += (float) (Math.sin(angle) * speed);

            // Частицы движутся вниз
            if (y > getHeight()) {
                y = getHeight();
            }

            alpha -= 0.02f;  // Уменьшаем альфа-значение, чтобы частицы исчезали со временем
            if (alpha < 0) alpha = 0;  // Не даём альфа-значению быть меньше 0
        }
    }
}
