package com.example.deepfocus;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class User extends Fragment {


    private CalendarView calendarView;
    private ListenerRegistration listenerRegistration;

    public User() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        calendarView = view.findViewById(R.id.calendarView);

        // Update UI with default values


        // Fetch saved dates from Firestore
        fetchSavedDates();

        return view;
    }

    private void fetchSavedDates() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Listen for changes to the user's document
            listenerRegistration = db.collection("users").document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            List<String> level2Dates = (List<String>) documentSnapshot.get("level2Dates");
                            if (level2Dates != null && !level2Dates.isEmpty()) {
                                markDatesOnCalendar(level2Dates); // Pass the entire list of dates
                            } else {
                                calendarView.setEvents(new ArrayList<>()); // Clear calendar if no dates
                            }
                        }
                    });
        } else {
        }
    }

    private void markDatesOnCalendar(List<String> dateStrings) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<EventDay> events = new ArrayList<>();

        for (String dateString : dateStrings) {
            try {
                Date date = sdf.parse(dateString);
                if (date != null) {
                    // Create an EventDay for the calendar
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    EventDay eventDay = new EventDay(calendar, R.drawable.ic_green_dot); // Replace with your drawable resource

                    // Add the EventDay to the list of events
                    events.add(eventDay);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Set all events to the calendar view at once
        calendarView.setEvents(events);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove(); // Stop listening to Firestore updates
        }
    }
}
