package edu.rowanuniversity.rufit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.rowanuniversity.rufit.rufitObjects.Goal;
import edu.rowanuniversity.rufit.rufitObjects.User;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    TextView drawerusername;
    CircleImageView userImage;
    CardView goalsCard,recentRunCard, startRunCard;
    FirebaseUser user;
    StorageReference mStorage;
    StorageReference filePath;

    ProgressDialog mProgressDialog;
    HashMap<String,Object> currentUser;
   // User currentUser;
    final String ROOT = "users";
    String text = "Welcome!";
    Toolbar toolbar;
    NavigationView navigationView;
    private GenericTypeIndicator<User<String,Object>> generic = new GenericTypeIndicator<User<String,Object>>() {};
    private static final int GALLERY_REQUEST_CODE = 991;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        toolbar = (Toolbar) findViewById(R.id.topToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        drawerusername = (TextView) header.findViewById(R.id.drawer_user_name);
        userImage = (CircleImageView) header.findViewById(R.id.userImage);
        mStorage = FirebaseStorage.getInstance().getReference().child("userImage");
        mProgressDialog = new ProgressDialog(this);

        //Allow actions when cards on dashboard are clicked
        setCardActions();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if(auth.getCurrentUser() == null){
            Intent intent = new Intent(DashboardActivity.this, SignupActivity.class);
            startActivity(intent);
        }else{
            updateUser();
        }
    }

    public void updateUser(){
        user = auth.getCurrentUser();
        //Unique UUID For each user for Database
        myRef  = database.getReference(ROOT).child(user.getUid());
        try{
            Glide.with(getApplicationContext()).using(new FirebaseImageLoader())
                    .load(mStorage.child(user.getUid()))
                    .error(R.drawable.rufit_userimage)
                    .into(userImage);


        }catch (Exception e){

        }
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Clicked Image", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateDashboardData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        drawerusername.setText(currentUser == null ? text : ((HashMap<String,Object>) currentUser.get("info")).get("username").toString());

    }



    public void onResume(){
        super.onResume();
        if(auth.getCurrentUser() == null){
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
        }else{
            updateUser();
        }
    }
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.workout_history) {
           Intent intent = new Intent(this, WorkoutHistory.class);
            startActivity(intent);

        } else if (id == R.id.add_workout) {
            Intent intent = new Intent(this, AddRunActivity.class);
            startActivity(intent);

        } else if (id == R.id.add_shoe) {
            Intent intent = new Intent(this, ShoeActivity.class);
            startActivity(intent);


        }
        //keep commented out
        //else if (id == R.id.leaderboard) {


       // }

        else if (id == R.id.goals) {

            Intent intent = new Intent(this, GoalsActivity.class);
            startActivity(intent);
        }else if(id == R.id.personalInfo){
            Intent intent = new Intent(this, PersonalInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Intent intent = new Intent(DashboardActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.settings){
           /* Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);*/
        }
        else if(id == R.id.signout){
            auth.signOut();
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Populates user's dashboard with their personal information to cards
     * //TODO : populate dashboard with user data
     */
    private void updateDashboardData(DataSnapshot d) {
        currentUser = d.getValue(generic);


        if(currentUser == null){
            drawerusername.setText(text);
        }else {

            HashMap<String, Object> temp = (HashMap<String, Object>) currentUser.get("info");
                drawerusername.setText(temp.get("username").toString());
        }

        //updateGoalsCard(d);

        HashMap<String,Object> info =  (HashMap<String,Object>) currentUser.get("info");
        //Toast.makeText(DashboardActivity.this,i, Toast.LENGTH_LONG).show();
        //updateGoalsCard();


        //User can click card to quickstart new run
        CardView startRunCard = (CardView) findViewById(R.id.cardStartRun);
    }

    private void updateGoalsCard(DataSnapshot d) {
        HashMap<String,Object> uGoals = (HashMap<String,Object>) currentUser.get("goals");
        RelativeLayout r1 = (RelativeLayout) findViewById(R.id.r1);
        RelativeLayout r2 = (RelativeLayout) findViewById(R.id.r2);
        TextView noGoal = (TextView) findViewById(R.id.noGoalGreeting);

        if(!(d.child("goals").hasChildren())){
            noGoal.setVisibility(View.VISIBLE);
            r1.setVisibility(View.GONE);
            r2.setVisibility(View.GONE);
        } else {
            Goal userGoals = new Goal();
            //userGoals = currentUser.get("goals");
            if (!(userGoals.getMilesPerWeekTarget() > 0) || !(userGoals.getRunsPerWeekTarget() > 0)) {
                noGoal.setVisibility(View.VISIBLE);
            }

            //Components for 1st goal progress
            ProgressBar goalBar1 = (ProgressBar) findViewById(R.id.goalBar1);
            TextView userGoal1 = (TextView) findViewById(R.id.goal1);
            TextView userGoalPercent1 = (TextView) findViewById(R.id.goalPercent1);
            //Components for 2st goal progress
            ProgressBar goalBar2 = (ProgressBar) findViewById(R.id.goalBar2);
            TextView userGoal2 = (TextView) findViewById(R.id.goal2);
            TextView userGoalPercent2 = (TextView) findViewById(R.id.goalPercent2);

            //Goal userGoals = d.child("goals").getValue(Goal.class);
            if (userGoals.getRunsPerWeekTarget() > 0) {
                userGoal1.setText("Runs Per Week Progress:");
                int percent1 = (userGoals.getRunsPerWeekActual() * 100) / userGoals.getRunsPerWeekTarget();
                userGoalPercent1.setText("" + percent1 + "%");
                goalBar1.setProgress(percent1);
            } else {
                r1.setVisibility(View.GONE);
            }

            if (userGoals.getMilesPerWeekTarget() > 0) {
                userGoal2.setText("Weekly Mileage Progress:");
                int percent2 = (userGoals.getMilesPerWeekActual() * 100) / userGoals.getMilesPerWeekTarget();
                userGoalPercent2.setText("" + percent2 + "%");
                goalBar2.setProgress(percent2);
            } else {
                r2.setVisibility(View.GONE);
            }
        }
    }


    public void setCardActions () {
        startRunCard = (CardView) findViewById(R.id.cardStartRun);
        recentRunCard = (CardView) findViewById(R.id.cardRecentRun);
        goalsCard = (CardView) findViewById(R.id.cardGoals);

        goalsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, GoalsActivity.class);
                startActivity(intent);
            }
        });

        startRunCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StartRunActivity.class);
                startActivity(intent);
            }
        });
        //TODO: StartRun card action
        //TODO: RecentRun card action
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            mProgressDialog.setMessage("Uploading, Please Wait");
            mProgressDialog.show();

            filePath = mStorage.child(user.getUid());
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    Picasso.with(getApplicationContext()).load(downloadUri).fit().centerCrop().into(userImage);
                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
