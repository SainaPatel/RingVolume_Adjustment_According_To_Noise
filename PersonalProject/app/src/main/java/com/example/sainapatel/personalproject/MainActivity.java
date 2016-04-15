package com.example.sainapatel.personalproject;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import java.io.IOException;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

   // private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
 //   private MediaPlayer mediaPlayer=null;
    private MediaPlayer   mPlayer = null;
    TextView textView=null;
    TextView textView1=null;
    ProgressBar progress=null;
    ProgressDialog progressDialog=null;
    Toast toast;
    String text=null;

    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;

    private final static int MAX_VOLUME = 100;

    private static int soundVolume = 50;
    Switch switchButton;
    AudioManager audio ;//(AudioManager) getSystemService(Context.AUDIO_SERVICE);
    int currentVolume=0;
    final Runnable updater = new Runnable(){

        public void run(){
            updateTv();
        };
    };
    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

       // textView=(TextView) findViewById(R.id.Temp_text);
        progress=(ProgressBar) findViewById(R.id.progressBar);
        switchButton =(Switch) findViewById(R.id.switch1);
//        progressDialog=new ProgressDialog(this);
//        progressDialog.setMessage("Sound Level");
//        progressDialog.show();
        //textView1=(TextView) findViewById(R.id.Temp_text1);
        if (runner == null)
        {
            runner = new Thread(){
                public void run()
                {
                    while (runner != null)
                    {
                        try
                        {
                            Thread.sleep(1000);
                            Log.i("Noise", "Tock");
                        } catch (InterruptedException e) { };
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }
        mRecorder = new MediaRecorder();
        mPlayer=new MediaPlayer();

       // currentVolume= audio.getStreamVolume(AudioManager.STREAM_MUSIC);

//        mPlayer.create(this,R.raw.song);
//        try {
//            mPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                return false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        startRecording();
    }

    public void playMedia()
    {
        mPlayer=MediaPlayer.create(this, R.raw.song);
        mPlayer.start();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    public void onPause()
    {
        super.onPause();
        stopRecording();
    }
    private void startRecording() {
       // mPlayer.start();
       // playMedia();

        final float volume = (float) (1 - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)));
        mPlayer.setVolume(volume, volume);
        currentVolume=audio.getStreamVolume(audio.STREAM_RING);
        //textView1.setText(currentVolume);
        Log.i("volumeee", String.valueOf(currentVolume));
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile("/dev/null");
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mPlayer=MediaPlayer.create(this, R.raw.song);
        mPlayer.start();
        mRecorder.start();
    }
    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude());
        else
            return 0;
    }

    private void stopRecording() {
        mPlayer.stop();
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public void updateTv(){
        if(switchButton.isChecked())
        {
            arrangeVolume();
        }
       // textView.setText(Double.toString(soundDb(50.0)) + " dB");
        progress.setProgress((int) soundDb(50.0));
      //  progress.setMessage("");
       // textView1.setText(String.valueOf(currentVolume));
    }

    public void arrangeVolume()
    {

        currentVolume=audio.getStreamVolume(audio.STREAM_RING);
        if(currentVolume!=0) {
            Double dbValue = soundDb(50.0);
            if (dbValue >= 0 && dbValue <= 20) {

                if(currentVolume>2) {
//                    for (int i = audio.getStreamVolume(audio.STREAM_RING); i > 2; i--) {
                        Log.i("sound111111111111111111", Integer.toString(audio.getStreamVolume(audio.STREAM_RING)));
                        audio.setStreamVolume(AudioManager.STREAM_RING, 2, AudioManager.FLAG_SHOW_UI);
                    text="Ring Volume decreased";
                    toast=Toast.makeText(this,text,Toast.LENGTH_SHORT);
                    toast.show();
                      //  audio.adjustVolume();
//                        Log.i("sound22222222222222222", Integer.toString(audio.getStreamVolume(audio.STREAM_RING)));
//                    }
                }
            }else if(dbValue>=20 && dbValue<=40)
            {
                Log.i("second iffffffffffffffffffffffff",Double.toString(dbValue));
                if(currentVolume>2 && currentVolume<4)
                {
                    text="Ring volume increased";
                    toast=Toast.makeText(this,text,Toast.LENGTH_SHORT);
                    toast.show();
                }
                if(currentVolume>4) {
//                    for (int i = audio.getStreamVolume(audio.STREAM_RING); i > 4; i--) {
                        audio.setStreamVolume(AudioManager.STREAM_RING,4,AudioManager.FLAG_SHOW_UI);
                    text="Ring volume decreased";
                    toast=Toast.makeText(this,text,Toast.LENGTH_SHORT);
                    toast.show();
                    //    }
                }
            }else{
                if(currentVolume<4)
                {
//                    for(int i=audio.getStreamVolume(audio.STREAM_RING);i<7;i++)
//                    {
                        audio.setStreamVolume(AudioManager.STREAM_RING,7,AudioManager.FLAG_SHOW_UI);
                    text="Ring volume increased";
                    toast=Toast.makeText(this,text,Toast.LENGTH_SHORT);
                    toast.show();

                    // }
                }
            }
        }

    }

    public double soundDb(double ampl){
        double number=Math.log10(getAmplitudeEMA() / ampl);
        Log.d("numberrrrrrrrrrrrrr",Double.toString(number));
        return  (20 * number);
    }

    public double getAmplitudeEMA() {
        double amp =  getAmplitude();
        Log.i("AMPLITUDEEEEEEEEEEEEEEEEEEEEEEEEEE",Double.toString(amp));
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

}
