package com.example.deepfocus;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Home extends Fragment {
    private Button startButton;
    private TextView stopwatchTime, stagename;
    private CountDownTimer countDownTimer;
    private int stage = 0;
    private int supperstage = 1;
    FrameLayout frameLayout, OverAllBackGround;
    ImageView dot1, dot2, dot3, dot4;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        startButton = view.findViewById(R.id.start_button);
        stopwatchTime = view.findViewById(R.id.stopwatch_time);
        stagename = view.findViewById(R.id.stagename);
        frameLayout = view.findViewById(R.id.timer_circle);
        OverAllBackGround = view.findViewById(R.id.Overallackground);
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);
        dot4 = view.findViewById(R.id.dot4);
        TextView welcomeTextView = view.findViewById(R.id.welcomeTextView);

        // Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String emailId = user.getEmail();
            String username = emailId.replaceAll("@gmail\\.com", "");
            welcomeTextView.setText("Welcome, " + username);
        } else {
            Log.d("TAG", "No user is signed in");
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.GONE);
                stage = 0;
                dot1.setImageResource(R.drawable.dot_filled);
                supperstage = 1;
                stagename.setText("Level " + supperstage);
                countDownStarted(3000);  // Start the first countdown
            }
        });

        return view;
    }

    private void countDownStarted(int mills) {
        countDownTimer = new CountDownTimer(mills, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int min = (int) (millisUntilFinished / 1000) / 60;
                int sec = (int) (millisUntilFinished / 1000) % 60;
                String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", min, sec);
                stopwatchTime.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                stage++;

                if (stage == 1) {
                    dot2.setImageResource(R.drawable.dot_filled);
                    countDownStarted(1000);
                } else if (stage == 2) {
                    dot3.setImageResource(R.drawable.dot_filled);
                    countDownStarted(1000);
                } else if (stage == 3) {
                    dot4.setImageResource(R.drawable.dot_filled);
                    countDownStarted(1000);
                } else {
                    // Save the date when the user reaches level 2
                    if (supperstage == 2) {
                        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        Log.d("TAG", "Saving date to Firestore: " + currentDate);  // Logging for debugging
                        saveDateToFirestore(currentDate);  // Save to Firestore
                    }

                    supperstage++;  // Increment supperstage after checking for Level 2

                    if (supperstage <= 4) {
                        stage = 0;
                        stagename.setText("Level " + supperstage);
                        updateCircleForNewLevel(supperstage);
                        countDownStarted(1000);
                        dot2.setImageResource(R.drawable.dot_empty);
                        dot3.setImageResource(R.drawable.dot_empty);
                        dot4.setImageResource(R.drawable.dot_empty);
                    } else {
                        // Reset for the next session
                        stage = 0;
                        supperstage = 1;
                        startButton.setVisibility(View.VISIBLE);
                        stopwatchTime.setText("00:00");
                        stagename.setText("Level 1");
                        stagename.setTextColor(Color.parseColor("#471515"));
                        stopwatchTime.setTextColor(Color.parseColor("#471515"));
                        OverAllBackGround.setBackgroundColor(Color.parseColor("#54EB6666"));
                        frameLayout.setBackgroundResource(R.drawable.circle_shape);
                        dot1.setImageResource(R.drawable.dot_empty);
                        dot2.setImageResource(R.drawable.dot_empty);
                        dot3.setImageResource(R.drawable.dot_empty);
                        dot4.setImageResource(R.drawable.dot_empty);
                    }
                }
            }
        }.start();
    }

    private void updateCircleForNewLevel(int level) {
        if (level == 2) {
            frameLayout.setBackgroundResource(R.drawable.circle_level2);
            stagename.setTextColor(Color.parseColor("#311B92"));
            stopwatchTime.setTextColor(Color.parseColor("#311B92"));
            OverAllBackGround.setBackgroundColor(Color.parseColor("#E0F7FA"));
        } else if (level == 3) {
            frameLayout.setBackgroundResource(R.drawable.circle_level3);
            stagename.setTextColor(Color.parseColor("#006064"));
            stopwatchTime.setTextColor(Color.parseColor("#006064"));
            OverAllBackGround.setBackgroundColor(Color.parseColor("#FFF9C4"));
        } else if (level == 4) {
            frameLayout.setBackgroundResource(R.drawable.circle_level4);
            stagename.setTextColor(Color.parseColor("#33691E"));
            stopwatchTime.setTextColor(Color.parseColor("#33691E"));
            OverAllBackGround.setBackgroundColor(Color.parseColor("#F1F8E9"));
        }
    }

    // Save all the dates when the user reaches Level 2
    private void saveDateToFirestore(String date) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Get the user's unique ID
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Prepare data
            Map<String, Object> dateData = new HashMap<>();
            dateData.put("level2Dates", date); // Prepare to save as a list

            // Update Firestore document
            db.collection("users").document(userId)
                    .update("level2Dates", FieldValue.arrayUnion(date)) // Add date to the list
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Date successfully saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error saving date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            Log.d("TAG", "Firestore save function called for user: " + userId);  // Logging for debugging
        } else {
            Log.d("TAG", "No user is signed in");
        }
    }
}
