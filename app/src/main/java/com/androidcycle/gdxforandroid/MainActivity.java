package com.androidcycle.gdxforandroid;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String absolutePath = getFilesDir().getAbsolutePath();
        File file = new File(absolutePath + "/texture");
        file.mkdirs();
        setContentView(R.layout.activity_main);
        findViewById(R.id.start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpineFragment<SpineTwoColorPolygonBatchAnimationListener> fragment = new SpineFragment<>();
                fragment.setApplicationListenerClass(SpineTwoColorPolygonBatchAnimationListener.class);
                getSupportFragmentManager().beginTransaction().replace(R.id.spine_container, fragment).commitAllowingStateLoss();
            }
        });
        findViewById(R.id.start_btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpineFragment<SpinePolygonSpriteBatchAnimationListener> fragment = new SpineFragment<>();
                fragment.setApplicationListenerClass(SpinePolygonSpriteBatchAnimationListener.class);
                getSupportFragmentManager().beginTransaction().replace(R.id.spine_container_2, fragment).commitAllowingStateLoss();
            }
        });
        findViewById(R.id.clear_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                for (Fragment fragment : fragments) {
                    fragmentTransaction.remove(fragment);
                }
                fragmentTransaction.commitAllowingStateLoss();
            }
        });


        final TextTextureGenerator textTextureGenerator = new TextTextureGenerator(getResources());
        textTextureGenerator.setTextColor(Color.BLACK);
        textTextureGenerator.setTypeface(SpineApp.getInstance().getAssets(), "setofont.ttf");
        textTextureGenerator.setTextSize(18 * 3);
        textTextureGenerator.setAutoSizeTextTypeUniformWithConfiguration(4, 50, 1, TypedValue.COMPLEX_UNIT_SP);

        final ImageView iv = findViewById(R.id.text_gen_bitmap);
        findViewById(R.id.start_btn_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int maxWidth = 300;
                int maxHeight = 300;
                String text = "一串文字，用来测试！AbcdEfgHigk";
                Bitmap bitmap = textTextureGenerator.genBitmap(text, maxWidth, maxHeight);
                iv.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void exit() {
        Gdx.audio = null;
        Gdx.files = null;
    }
}
