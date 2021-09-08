package com.alox.myreminders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {
    private static final String CHANNEL_ID = "MyReminders";

    RecyclerView ReminderRV;
    RemAdapter adapter;

    Date selection;
    Calendar nowCalendar= Calendar.getInstance();

    private TextView DialogDate;
    private EditText title;

    SharedPreferences mPreferences;

    public static MutableLiveData<String> notified = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        notified.setValue("");

        mPreferences = getSharedPreferences("rems", MODE_PRIVATE);
        selection = new Date();
        ReminderRV = findViewById(R.id.reminderRV);
        ReminderRV.setLayoutManager(new LinearLayoutManager(this));
        populateReminder();
        enableSwipeToDelete();

        MaterialCardView addBtn = findViewById(R.id.AddBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.add_rem_dialog);
                int width = (int) (getResources().getDisplayMetrics().widthPixels * .90);
                dialog.setCancelable(true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationSlide;
                dialog.getWindow().setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT);
                final EditText titleField = dialog.findViewById(R.id.tileField);
                final MaterialCardView timePickerBtn = dialog.findViewById(R.id.timePicker);
                DialogDate = dialog.findViewById(R.id.dateText);
                final MaterialCardView remindMeBtn = dialog.findViewById(R.id.saveReminderBtn);
                remindMeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addReminder(nowCalendar.getTimeInMillis(),titleField.getText().toString());
                        Toast.makeText(MainActivity.this, "Reminder Added" , Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                timePickerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        titleField.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }


                        Calendar now = Calendar.getInstance();
                        DatePickerDialog dpd = DatePickerDialog.newInstance(MainActivity.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH));
                        dpd.show(getFragmentManager(),"DatePicker");
                    }
                });
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        titleField.requestFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        titleField.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    }
                });
                dialog.show();
            }
        });

        notified.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (!s.isEmpty()) {
                    populateReminder();
                }
            }
        });

    }

    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                deleteRem(adapter.getData(position));
                adapter.removeItem(position);

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(ReminderRV);
    }

    public void populateReminder() {
        Set<String> rRem = mPreferences.getStringSet("RecentRems", null);
        if (rRem != null) {
            adapter = new RemAdapter(rRem);
            ReminderRV.setAdapter(adapter);

        }
    }

    private void addReminder(long time, String Title) {
        Set<String> rRem = mPreferences.getStringSet("RecentRems", null);
        int rCode = 0;
        if (rRem == null) {
            rRem = new HashSet<>();
            rCode = 1;
        } else {
            rCode = rRem.size() + 1;
        }
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM ''yy h:mm a", Locale.getDefault());
        String SDate = dateFormat.format(date);
        Intent intent = new Intent(this, Reminder.class);
        intent.putExtra("title",Title);
        intent.putExtra("rem",Title+"`"+SDate+"``"+rCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, rCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP,time,pendingIntent);



        SharedPreferences.Editor editor = mPreferences.edit();
        rRem.add(Title+"`"+SDate+"``"+rCode);
        editor.putStringSet("RecentRems", rRem);
        editor.apply();

        populateReminder();

    }

    private void deleteRem(String remString) {
        Set<String> rem = mPreferences.getStringSet("RecentRems",null);
        if (rem!=null) {
            rem.remove(remString);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putStringSet("RecentRems",rem);
            editor.apply();

            String rCode = remString.substring(remString.indexOf("``")+2);
            Intent intent = new Intent(this, Reminder.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(rCode), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);

            am.cancel(pendingIntent);

        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = getString(R.string.default_notification_channel_id);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar now = Calendar.getInstance();
        selection.setYear(year);
        selection.setMonth(monthOfYear);
        selection.setDate(dayOfMonth);
        TimePickerDialog tpd = TimePickerDialog.newInstance(this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false);
        tpd.setTitle("Choose Time");
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.show(getFragmentManager(),"TimePicker");

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        selection.setHours(hourOfDay);
        selection.setMinutes(minute);
        nowCalendar.set(selection.getYear(),selection.getMonth(),selection.getDate(),hourOfDay,minute,second);
        Log.e("Time",selection.getTime() + "  act: "+ nowCalendar.getTimeInMillis() + "   sys: " +System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM ''yy h:mm a");
        String choseDate = simpleDateFormat.format(selection);
        DialogDate.setText(choseDate);

    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean notNew = mPreferences.getBoolean("notNew",false);
        if (!notNew) {
            Intent welcome = new Intent(MainActivity.this,WelcomeActivity.class);
            startActivity(welcome);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateReminder();
    }
}