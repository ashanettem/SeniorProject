package com.example.quickmath;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class allcalculation extends AppCompatActivity {
    private TextView num11;
    private TextView num12;
    private TextView result;
    private TextView num_of_question;
    private EditText enter_num;
    private TextView tvSign;
    private Button inputButton;
    int count = 0;
    int tryMe = 0;
    int Final_result = 0;
    int rand1, rand2;
    String email;
    String value;
    String difficulty;
    int numOfquestions;
    SharedPreferences sp;
    private ImageView gc, rx, medal;
    KonfettiView viewKonfetti;


    SoundPool soundPool;
    private int sound1;
    private int sound2;

    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon,Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_allcalculation);

        viewKonfetti = (KonfettiView)findViewById(R.id.viewKonfetti);



        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

        HomeWatcher mHomeWatcher;

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(6)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        sound1 = soundPool.load(this, R.raw.sound1, 1);
        sound2 = soundPool.load(this,R.raw.wrong_sfx,1);


        sp = getSharedPreferences("currentUser", MODE_PRIVATE);

        email = sp.getString("User", "User");
        value = getIntent().getStringExtra("value");
        difficulty = getIntent().getStringExtra("difficulty");



        numOfquestions = sp.getInt("numOfQuestions", 5);



        inputButton = findViewById(R.id.btnInput);
        result = findViewById(R.id.result);
        num11 = findViewById(R.id.num_one);
        num12 = findViewById(R.id.num_two);
        num_of_question = findViewById(R.id.tvNumOfQuestions);
        enter_num = findViewById(R.id.etInput);
        tvSign = findViewById(R.id.tvSign);
        gc = findViewById(R.id.greenCheck);
        rx = findViewById(R.id.redX);
        medal = findViewById(R.id.ivMedal);


        tocall();


        inputButton.setOnClickListener(this::calculate);
    }

    private void calculate(View view) {
        validateAnswer();
    }


    private void tocall() {
        gc.setVisibility(View.INVISIBLE);
        rx.setVisibility(View.INVISIBLE);
        enter_num.setText("");//clear the editText
        result.setText("");// clears input
        count++;

        if (count <= numOfquestions) { // how many question you want to get
            to_get_random();
            num_of_question.setText(count + "/" + numOfquestions);


        } else {
            //when game is completed play konfetti


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference gamesDB = db.collection("Games");

            Game currentGame = new Game(value, numOfquestions, email, Final_result);
            currentGame.setState("complete");
            currentGame.setChild(email);
            gamesDB.add(currentGame);

            if (Final_result >= 80) {
                soundPool.play(sound1, 1, 1, 0, 0, 1);
                medal.setImageResource(R.drawable.gold3);
                medal.setVisibility(View.VISIBLE);
                viewKonfetti.build()
                        .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(new Size(12, 5))
                        .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                        .streamFor(300, 5000L);
            }
            else if(Final_result >= 60 && Final_result < 80) {
                soundPool.play(sound1, 1, 1, 0, 0, 1);
                medal.setImageResource(R.drawable.silver_2);
                medal.setVisibility(View.VISIBLE);
                viewKonfetti.build()
                        .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(new Size(12, 5))
                        .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                        .streamFor(300, 5000L);
            }
            else {
                soundPool.play(sound2, 1, 1, 0, 0, 1);
                medal.setVisibility(View.VISIBLE);
            }

            //delay start of new activity
            new Handler().postDelayed(new Runnable(){
            @Override
            public void run () {

                Intent go_back_to_first_page = new Intent(allcalculation.this, choices.class);
                startActivity(go_back_to_first_page);
                finish();
            }
        },5000);
            Toast.makeText(this,"Complete",Toast.LENGTH_LONG).show();
        }
    }


    private void to_get_random() {

        if (difficulty.equals("easy")) {

            rand1 = new Random().nextInt(9);// random number between 0 to 5
            rand2 = new Random().nextInt(9);

            if (rand1 < rand2) {
                to_get_random();
                // Toast.makeText(this," here",Toast.LENGTH_LONG).show();


            } else if (rand1 > rand2 || rand1 == rand2) {  //make sure that random number 1 is greater than random number 2(save some time)
                num11.setText(String.valueOf(rand1));
                num12.setText(String.valueOf(rand2));

                String mychoice = getIntent().getStringExtra("value");


                if (mychoice.equals("addition")) {
                    toadd(rand1, rand2);
                } else if (mychoice.equals("multiply")) {
                    tomultiply(rand1, rand2);

                } else if (mychoice.equals("subtract")) {
                    tosubtract(rand1, rand2);

                } else if (mychoice.equals("divide")) {
                    todivide(rand1, rand2);

                } else {
                    Toast.makeText(this, " inside error", Toast.LENGTH_LONG).show();
                }


            } else {
                Toast.makeText(this, " there is an error", Toast.LENGTH_LONG).show();
            }

        }
        else if (difficulty.equals("medium")){
            rand1 = new Random().nextInt(25);
            rand2 = new Random().nextInt(25);

            if (rand1 < rand2) {
                to_get_random();
                // Toast.makeText(this," here",Toast.LENGTH_LONG).show();


            } else if (rand1 > rand2 || rand1 == rand2) {  //make sure that random number 1 is greater than random number 2(save some time)
                num11.setText(String.valueOf(rand1));
                num12.setText(String.valueOf(rand2));

                String mychoice = getIntent().getStringExtra("value");


                if (mychoice.equals("addition")) {
                    toadd(rand1, rand2);
                } else if (mychoice.equals("multiply")) {
                    tomultiply(rand1, rand2);

                } else if (mychoice.equals("subtract")) {
                    tosubtract(rand1, rand2);

                } else if (mychoice.equals("divide")) {
                    todivide(rand1, rand2);

                } else {
                    Toast.makeText(this, " inside error", Toast.LENGTH_LONG).show();
                }


            } else {
                Toast.makeText(this, " there is an error", Toast.LENGTH_LONG).show();
            }
        }
        else if (difficulty.equals("hard")){
            rand1 = new Random().nextInt(50);
            rand2 = new Random().nextInt(25);

            if (rand1 < rand2) {
                to_get_random();
                // Toast.makeText(this," here",Toast.LENGTH_LONG).show();


            } else if (rand1 > rand2 || rand1 == rand2) {  //make sure that random number 1 is greater than random number 2(save some time)
                num11.setText(String.valueOf(rand1));
                num12.setText(String.valueOf(rand2));

                String mychoice = getIntent().getStringExtra("value");


                if (mychoice.equals("addition")) {
                    toadd(rand1, rand2);
                } else if (mychoice.equals("multiply")) {
                    tomultiply(rand1, rand2);

                } else if (mychoice.equals("subtract")) {
                    tosubtract(rand1, rand2);

                } else if (mychoice.equals("divide")) {
                    todivide(rand1, rand2);

                } else {
                    Toast.makeText(this, " inside error", Toast.LENGTH_LONG).show();
                }


            } else {
                Toast.makeText(this, " there is an error", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this, "Error" + difficulty, Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, choices.class);
            startActivity(i);
        }



    }


    private void todivide(int rand1, int rand2) {
        tvSign.setText(" / ");  // this is for the sign
        if (rand1 % rand2 != 0) {
            to_get_random();
        } else {
            int the_result = rand1 / rand2;
            tryMe = the_result;
            // result.setText(String.valueOf(the_result)); //comment out
        }

    }


    private void tosubtract(int rand1, int rand2) {

        tvSign.setText(" - ");

        int the_result = rand1 - rand2;
        tryMe = the_result;
        //result.setText(String.valueOf(the_result)); //comment out

    }


    private void tomultiply(int rand1, int rand2) {
        int the_result = rand1 * rand2;
        tvSign.setText(" X ");
        tryMe = the_result;
        //result.setText(String.valueOf(the_result)); //comment out

    }


    private void toadd(int rand1, int rand2) {

        int the_result = rand1 + rand2;
        tryMe = the_result;
        //result.setText(String.valueOf(the_result)); //comment out

    }


    public void validateAnswer() {

        if (enter_num.getText().toString().length() == 0) {// make sure that the editext is not empty
            Toast.makeText(this, "enter number", Toast.LENGTH_LONG).show();
        } else {


            int input = Integer.parseInt(enter_num.getText().toString().trim());  // this part is to get the user input


            if (input == tryMe) {
                if (numOfquestions == 5) {
                    Final_result += 20;
                    Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show();//display the result
                    gc.setVisibility(View.VISIBLE);
                } else if (numOfquestions == 10) {
                    int scoreValue = 100 / numOfquestions;
                    Final_result += scoreValue;
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
                    gc.setVisibility(View.VISIBLE);
                } else {
                    int scoreValue = 100 / numOfquestions;
                    Final_result += scoreValue;
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
                    gc.setVisibility(View.VISIBLE);
                }
            } else {
                rx.setVisibility(View.VISIBLE);
            }

            if (count <= numOfquestions) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tocall();// goback and call the call method
                    }
                }, 1000);
            }
            else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        tocall();// goback and call the call method
                    }
                }, 1000);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);

        soundPool.release();
        soundPool = null;

    }

}

