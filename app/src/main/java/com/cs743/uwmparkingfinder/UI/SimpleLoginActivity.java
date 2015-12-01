package com.cs743.uwmparkingfinder.UI;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs743.uwmparkingfinder.HTTPManager.HttpManager;
import com.cs743.uwmparkingfinder.HTTPManager.RequestPackage;
import com.cs743.uwmparkingfinder.Utility.UTILITY;
import com.cs743.uwmparkingfinder.Parser.JSONParser;
import com.cs743.uwmparkingfinder.Session.Session;
import com.cs743.uwmparkingfinder.Structures.Building;
import com.cs743.uwmparkingfinder.Structures.User;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SimpleLoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText userName;
    private EditText password;
    private Button login;
    private RelativeLayout image;
    private LinearLayout input, button;
    private Animation fadeInImage, fadeInButton, bottomUp;
    private TextInputLayout inputLayoutName,inputLayoutPassword;
    private ViewGroup hiddenPanel;
    private static final int SECOND = 1000;
    private static final int HALF_SECOND = 500;
    private static final int QUARTER_SECOND = 250;

    //create a Progress Dialog to be used throughout Activity
    private ProgressDialog p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_login);

        //INITIALIZE ANIMATION ITEMS
        fadeInImage = new AlphaAnimation(0, 1);
        fadeInButton = new AlphaAnimation(0, 1);
        bottomUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_up_animation);
        fadeInImage.setInterpolator(new AccelerateInterpolator()); //and this
        bottomUp.setInterpolator(new DecelerateInterpolator());
        hiddenPanel = (ViewGroup)findViewById(R.id.input);
        inputLayoutName = (TextInputLayout) findViewById(R.id.text_input_username);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.text_input_password);

        //GET UI ELEMENTS
        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        image = (RelativeLayout) findViewById(R.id.image);
        input = (LinearLayout) findViewById(R.id.input);
        button = (LinearLayout) findViewById(R.id.button);

        //SET UI PROPERTIES
        userName.setCursorVisible(false);
        password.setCursorVisible(false);
        password.setHint("Password");
        userName.setHint("Username");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                userName.setCursorVisible(true);
                password.setCursorVisible(true);
                userName.requestFocus();
            }
        }, SimpleLoginActivity.SECOND * 3);

        //ANIMATIONS
        fadeInImage.setDuration(SECOND * 3);
        image.setAnimation(fadeInImage);
        fadeInButton.setStartOffset(SECOND + HALF_SECOND + QUARTER_SECOND);
        fadeInButton.setDuration(SECOND * 2);
        button.setAnimation(fadeInButton);
        hiddenPanel.startAnimation(bottomUp);
        hiddenPanel.setVisibility(View.VISIBLE);

        //ON CLICK LISTENERS
        login.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if(getUserName().getText().toString().equals("") || getUserName().getText().toString().equals(" ")){
                    inputLayoutName.setError("enter username");
                }else if(getPassword().getText().toString().equals("") || getPassword().getText().toString().equals(" ")){
                    inputLayoutPassword.setError("enter password");
                }else{
                    //webservice
                    if (isOnline()) {
                        RequestPackage p = new RequestPackage();
                        p.setMethod("GET");
                        p.setUri(UTILITY.UBUNTU_SERVER_URL);
                        p.setParam("query", "user");
                        p.setParam("username", getUserName().getText().toString());
                        new WebserviceCallOne().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
                    } else {
                        Toast.makeText(getApplicationContext(), "you are not connected to the internet", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    /**
     * Check to see whether there is an internet connection or not.
     * @return whether there is an internet connection
     */
    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private static ProgressDialog controlProgressDialog(boolean show, Context context, ProgressDialog p, String message){
        if(show){
            p = new ProgressDialog(context);
            p.setMessage(message);
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }
        else{
            p.dismiss();
        }

        return p;
    }

    public ProgressDialog getP() {
        return p;
    }

    public void setP(ProgressDialog p) {
        this.p = p;
    }

    public EditText getPassword() {
        return password;
    }

    public EditText getUserName() {
        return userName;
    }

    public void getOtherInfoFromWebservice(){
        if (isOnline()) {
            RequestPackage p = new RequestPackage();
            p.setMethod("GET");
            p.setUri(UTILITY.UBUNTU_SERVER_URL);
            p.setParam("query", "buildings");
            new WebserviceCallTwo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, p);
        } else {
            Toast.makeText(getApplicationContext(), "you are not connected to the internet", Toast.LENGTH_LONG).show();
        }
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class WebserviceCallOne extends AsyncTask<RequestPackage, String, User> {
        @Override
        protected User doInBackground(RequestPackage... params) {
            String content = HttpManager.getData(params[0]);
            return JSONParser.parseUserFeed(content);
        }

        @Override
        protected void onPostExecute(User s) {
            Session.setCurrentUser(s);
            //if null, error stacktrace will print to the log. This is expected!!
            if(Session.getCurrentUser() == null){ //username was incorrect
                SimpleLoginActivity.controlProgressDialog(false, null, SimpleLoginActivity.this.getP(), null);
                inputLayoutName.setError("username does not exist");
            }else{ //check password
                if(getPassword().getText().toString().equals(s.getPassword())){ //passwords match
                    getOtherInfoFromWebservice();
                    //start intent
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    SimpleLoginActivity.controlProgressDialog(false, null, SimpleLoginActivity.this.getP(), null);
                }else{
                    SimpleLoginActivity.controlProgressDialog(false, null, SimpleLoginActivity.this.getP(), null);
                    inputLayoutPassword.setError("password incorrect");
                }
            }
        }

        @Override
        protected void onPreExecute() {
            SimpleLoginActivity.this.setP(SimpleLoginActivity.controlProgressDialog(true, SimpleLoginActivity.this, p, "Attempting Login..."));
        }
    }

    private class WebserviceCallTwo extends AsyncTask<RequestPackage, String, List<Building>> {
        @Override
        protected List<Building> doInBackground(RequestPackage... params) {
            String content = HttpManager.getData(params[0]);
            return JSONParser.parseBuildingFeed(content);
        }
        @Override
        protected void onPostExecute(List<Building> s) {
            Session.setCurrentBuildings(s);
        }
    }


}
