package com.mitya495dev.samsungschool;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Locale currentLocale = getResources().getConfiguration().locale;
        Configuration config = new Configuration();
        config.locale = currentLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        PressEffect();

        // Обработчики для кнопок
        FrameLayout button1 = findViewById(R.id.button1);
        FrameLayout button2 = findViewById(R.id.button2);
        FrameLayout button3 = findViewById(R.id.button3);

        button1.setOnClickListener(v -> Toast.makeText(this, getString(R.string.toast_message), Toast.LENGTH_SHORT).show());
        button2.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "example@example.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        });
        button3.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://innovationcampus.ru"));
            startActivity(browserIntent);
        });

        // Тексты с анимацией
        TextView text1 = findViewById(R.id.text1);
        TextView text2 = findViewById(R.id.text2);
        TextView text3 = findViewById(R.id.text3);

        TextView[] textViews = {text1, text2, text3};

        for (TextView textView : textViews) {
            textView.post(() -> {
                int textWidth = (int) textView.getPaint().measureText(textView.getText().toString());
                Shader textShader = new LinearGradient(
                        0, 0, textWidth, 0,
                        new int[]{Color.RED, Color.GREEN, Color.BLUE},
                        null,
                        Shader.TileMode.MIRROR
                );
                textView.getPaint().setShader(textShader);

                ValueAnimator animator = ValueAnimator.ofFloat(0, textWidth);
                animator.setDuration(3000);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.addUpdateListener(animation -> {
                    float animatedValue = (float) animation.getAnimatedValue();
                    Matrix matrix = new Matrix();
                    matrix.setTranslate(animatedValue, 0);
                    textShader.setLocalMatrix(matrix);
                    textView.invalidate();
                });
                animator.start();
            });
        }

        // Инициализация VideoView
        videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
        videoView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Восстановление воспроизведения видео после возвращения в Activity
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Приостановить видео при уходе в фон
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void PressEffect() {
        int[] frameLayoutsIds = {R.id.button1, R.id.button2, R.id.button3};
        for (int id : frameLayoutsIds) {
            View view = findViewById(id);
            if (view instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) view;
                frameLayout.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            v.clearAnimation();
                            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 0.95f);
                            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 0.95f);
                            scaleDownX.setDuration(200);
                            scaleDownY.setDuration(200);
                            scaleDownX.setInterpolator(new DecelerateInterpolator());
                            scaleDownY.setInterpolator(new DecelerateInterpolator());
                            scaleDownX.start();
                            scaleDownY.start();
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            v.clearAnimation();
                            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", 1f);
                            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", 1f);
                            scaleUpX.setDuration(200);
                            scaleUpY.setDuration(200);
                            scaleUpX.setInterpolator(new AccelerateDecelerateInterpolator());
                            scaleUpY.setInterpolator(new AccelerateDecelerateInterpolator());
                            scaleUpX.start();
                            scaleUpY.start();
                            break;
                    }
                    return false;
                });
            }
        }
    }
}
