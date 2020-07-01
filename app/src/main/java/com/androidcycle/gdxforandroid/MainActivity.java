package com.androidcycle.gdxforandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
                getSupportFragmentManager().beginTransaction().replace(R.id.spine_container, new SpineFragment()).commitAllowingStateLoss();
            }
        });
        findViewById(R.id.start_btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.spine_container_2, new SpineFragment()).commitAllowingStateLoss();
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
    }

    @Override
    public void exit() {
        Gdx.app = null;
        Gdx.audio = null;
        Gdx.input = null;
        Gdx.files = null;
    }
}
