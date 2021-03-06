package roast.app.com.dealbreaker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseApp;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Hashtable;

import roast.app.com.dealbreaker.models.User;
import roast.app.com.dealbreaker.util.Constants;

public class InitialRoamingAttributes extends AppCompatActivity {

    public String mAgeWanted, mGenderWanted, mSexualOrientationWanted, mLocation, mHeightWanted;
    private EditText ageRoamingText, heightRoamingText, sexualOrientationRoamingText;
    private Long ageValue, heightValue;
    private RadioButton maleButton, femaleButton;
    private Button sendRoamingValues;
    public String userName;
    private Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_roaming);

        ageRoamingText=(EditText) findViewById(R.id.et_roaming_age);
        maleButton = (RadioButton) findViewById(R.id.radioButtonMaleRoaming);
        femaleButton = (RadioButton) findViewById(R.id.radioButtonFemaleRoaming);
        sexualOrientationRoamingText=(EditText)findViewById(R.id.et_roaming_sexual_or);
        heightRoamingText=(EditText) findViewById(R.id.et_roaming_height);
        sendRoamingValues = (Button) findViewById(R.id.finished_roaming_attribute_button);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Desired Match Information");


        // Grab intent
        Bundle arg = getIntent().getExtras();
        userName = arg.getString(getString(R.string.key_UserName));

        // Grab the user's location and assigns it to the member mLocation
        grabUserLocation();

        // Run when the set button is pressed
        sendRoamingValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grabData();
                boolean validData = isDataValid();  // Returns true if the data is valid
                if (validData) {
                    setData();

                    Intent intent = new Intent(InitialRoamingAttributes.this, InitialScreen.class);
                    startActivity(intent);
                }
            }
        });

    }

    // Grabs the user's location from the user info branch.
    public void grabUserLocation(){
        Firebase userURL = new Firebase(Constants.FIREBASE_URL_USERS).child(userName).child(Constants.FIREBASE_LOC_USER_INFO).child("location");

        userURL.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLocation = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // Grab the data from the activity and assign the data to the activity members
    private void grabData(){
        mAgeWanted = ageRoamingText.getText().toString();

        if(maleButton.isChecked()) {
            mGenderWanted = "male";
        }
        else if(femaleButton.isChecked()){
            mGenderWanted = "female";
        }

        mSexualOrientationWanted = sexualOrientationRoamingText.getText().toString();
        mHeightWanted = heightRoamingText.getText().toString();
    }

    // Check if the data is valid. Returns true when valid.
    public boolean isDataValid(){

    if(TextUtils.isEmpty(mAgeWanted) || !TextUtils.isDigitsOnly(mAgeWanted)  || mAgeWanted == null ) {
            ageRoamingText.setError("Age must be entered");
            return false;
        }

        else if((!femaleButton.isChecked() && !maleButton.isChecked()) || mGenderWanted == null || ((!mGenderWanted.equals("male") && (!mGenderWanted.equals("female"))))){
            maleButton.setError("Sex must be chosen!");
            return false;
        }
        else if(TextUtils.isEmpty(mSexualOrientationWanted) || mSexualOrientationWanted == null || ((!mSexualOrientationWanted.equals("straight")&&(!mSexualOrientationWanted.equals("bisexual"))&&(!mSexualOrientationWanted.equals("gay"))))){
            sexualOrientationRoamingText.setError("Sexual orientation cannot be empty and must be gay, straight, or bisexual!");
            return false;
        }
        else if( TextUtils.isEmpty(mHeightWanted) || mHeightWanted == null || !TextUtils.isDigitsOnly(mHeightWanted)){
            heightRoamingText.setError("Height cannot be empty!");
            return false;
        }
        else{
            int age = Integer.valueOf(mAgeWanted);
            if(age < 18 ){
                ageRoamingText.setError("Must be 18 years or older!");
                return false;
            }
            else if(age >= 130){
                ageRoamingText.setError("Must be less than 130 years old!");
                return false;
            }
        ageValue = Long.valueOf(mAgeWanted);
        heightValue = Long.valueOf(mHeightWanted);
        }

        return true;
    }

    // Set the user's data to the Firebase roaming branch
    public void setData(){
        Firebase roamingURL = new Firebase(Constants.FIREBASE_URL_ROAMING).child(userName);

        roamingURL.child("age").setValue(ageValue);
        roamingURL.child("sex").setValue(mGenderWanted);
        roamingURL.child("sexualOrientation").setValue(mSexualOrientationWanted);
        roamingURL.child("height").setValue(heightValue);

    }
}