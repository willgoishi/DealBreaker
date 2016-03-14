package roast.app.com.dealbreaker.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.Map;
import java.lang.String;

import roast.app.com.dealbreaker.R;
import roast.app.com.dealbreaker.models.User;
import roast.app.com.dealbreaker.models.UserQualities;
import roast.app.com.dealbreaker.util.Constants;
import roast.app.com.dealbreaker.util.DownloadImages;

public class ProfileActivity extends Fragment {
    private TextView bio_info,goodQualitiesInfo,badQualitiesInfo, personalName;
    private ImageButton imageButton;
    private String userName;
    private String key, profilePicURL;
    private DownloadImages downloadImages;


    private GoogleApiClient client;
    public static ProfileActivity newInstance(String userName) {
        ProfileActivity fragment = new ProfileActivity();
        Bundle args = new Bundle();
        //Adding the userName to the Bundle in order to use it later on
        args.putString("userName",userName);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_profile);
        if (getArguments() != null) {
            key = getString(R.string.key_UserName);
            userName = getArguments().getString(key);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize UI elements
        final View view = inflater.inflate(R.layout.activity_profile, container, false);
        initializeScreen(view);
        //setSupportActionBar(toolbar);
        //super.getView().setLabelFor(R.id.toolbar);
        //Firebase Listener to Update UI Function calls
        //Function call for listening for the user_info section: used right now for Name of User
        listenUSER_INFO();

        //Function call for listening to update the user qualities of the Firebase Database
        listenUSER_QUALITIES();

        //FireBase Reference for User Profile Images will go here
        //for now static URL
         listenUSER_PROFILE_PIC();
        //profilePicURL = "https://s3-us-west-1.amazonaws.com/dealbreaker/classpic.jpg";
        //downloadImages.execute(profilePicURL);
        return view;
    }

    private void initializeScreen(View rootView) {
        //profile_info = (TextView) rootView.findViewById(R.id.textView);
        personalName = (TextView) rootView.findViewById(R.id.nameTextView);
        bio_info = (TextView) rootView.findViewById(R.id.bioText);
        badQualitiesInfo = (TextView) rootView.findViewById(R.id.badQualitiesText);
        goodQualitiesInfo = (TextView) rootView.findViewById(R.id.goodQualitiesText);
        imageButton = (ImageButton) rootView.findViewById(R.id.imageButton);
        //Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
    }


    private void listenUSER_INFO(){
        Firebase refName_User_Profile = new Firebase(Constants.FIREBASE_URL_USERS).child(userName).child(Constants.FIREBASE_LOC_USER_INFO);

        //Add the value Event Listener so if data has already been inputted by the user then it will
        //pre-populated with existing data
        refName_User_Profile.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                // You can use getValue to deserialize the data at dataSnapshot
                User user = dataSnapshot.getValue(User.class);
                // If there was no data at the location we added the listener, then
                if (user != null) {
                    //displays first and last name of user to profile page
                    String firstAndLastName = user.getFirstName()+" "+user.getLastName();
                    personalName.setText(firstAndLastName);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void listenUSER_QUALITIES(){
        //Firebase section for Qualities
        final Firebase user_ref = new Firebase(Constants.FIREBASE_URL_USERS).child(userName).child(Constants.FIREBASE_LOC_USER_QUALITIES);

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserQualities userQualities = dataSnapshot.getValue(UserQualities.class);
                if (userQualities != null) {
                    bio_info.setText(userQualities.getBiography());
                    badQualitiesInfo.setText(userQualities.getBadQualities());
                    goodQualitiesInfo.setText(userQualities.getGoodQualities());
                } else {
                    //Will send a toast message that will pop up notifying user that there was an error
                    Toast.makeText(getContext(), "Failed to Retrieve Info!!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

    }

    public void listenUSER_PROFILE_PIC(){
        final Firebase profilePicREF = new Firebase(Constants.FIREBASE_URL_IMAGES).child(userName).child(Constants.FIREBASE_LOC_PROFILE_PIC);

        profilePicREF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               UserQualities profilePicSource = dataSnapshot.getValue(UserQualities.class);
                if(profilePicSource != null) {
                    downloadImages = new DownloadImages(imageButton,getActivity());
                    profilePicURL = profilePicSource.getProfilePic();
                    downloadImages.execute(profilePicURL);
                    downloadImages = null;
                }
                else{
                    downloadImages = new DownloadImages(imageButton,getActivity());
                    downloadImages.execute("dummyURL");
                    downloadImages = null;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }



    private boolean checkAndSendData(User user){
        return true;
    }

    private void addElements(User user){
        //Set Reference to Firebase node
        Firebase ref = new Firebase(Constants.FIREBASE_URL_USERS);
        HashMap<String, Object> updates = new HashMap<String, Object>();
        Map<String,Object> map = new ObjectMapper().convertValue(user, Map.class);
        updates.put(Constants.FIREBASE_LOC_USER_QUALITIES, map);
        ref.child(userName).updateChildren(updates);
    }

}