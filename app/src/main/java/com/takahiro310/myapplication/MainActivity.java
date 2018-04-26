package com.takahiro310.myapplication;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.smp.soundtouchandroid.SoundTouch;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements Runnable {

    private static final String LOG_TAG = "MainActivity";
    private static final int SAMPLING_RATE = 44100;
    private byte[] mByteArray = null;
    private AudioTrack mAudioTrack = null;
    private Thread mThread = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // サンプルソース
        AssetManager assetManager = getAssets();
        AssetFileDescriptor assetFileDescriptor = null;

        try {
            // assetの音声データを開く
            assetFileDescriptor = assetManager.openFd("test.wav");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // byte配列を生成し、音声データを読み込む
        mByteArray = new byte[(int)assetFileDescriptor.getLength()];
        InputStream inputStream = null;

        try {
            inputStream = assetFileDescriptor.createInputStream();
            inputStream.read(mByteArray);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Streamモードで再生を行うので、リングバッファサイズを取得
        int minBufferSizeInBytes = AudioTrack.getMinBufferSize(
                SAMPLING_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        // AudioTrackを生成する
        // (44.1kHz、ステレオ、16bitの音声データ)
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSizeInBytes,
                AudioTrack.MODE_STREAM);

        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "run() method start");
        // AudioTrackを再生開始状態にする
        mAudioTrack.play();

        SoundTouch soundTouch = new SoundTouch(
                0,
                1,
                44100,
                2,
                1.0f,
                4);
        soundTouch.putBytes(mByteArray);

        //now get the remaining bytes from the sound processor.
        int bytesReceived = 0;
        do
        {
            bytesReceived = soundTouch.getBytes(mByteArray);
            //do stuff with output.
        } while (bytesReceived != 0);
        soundTouch.finish();
        soundTouch.clearBuffer(0);

        // 音声データを書き込む
        // ※wav形式(PCM)のヘッダ情報44バイトを調整する
        mAudioTrack.write(mByteArray, 0, mByteArray.length);

        // AudioTrackを停止する
        mAudioTrack.stop();

        // AudioTrackをフラッシュする
        mAudioTrack.flush();
    }
}
