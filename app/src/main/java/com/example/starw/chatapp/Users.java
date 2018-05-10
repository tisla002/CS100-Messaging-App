package com.example.starw.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Users extends AppCompatActivity {
    ListView usersList;
    TextView noUsersText;
    FloatingActionButton add;

    final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference thread_db = database.getReference().child("users").child(uid);

    private ArrayList<String> threads = new ArrayList<>();
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        Intent intent = getIntent();
        final String username = intent.getStringExtra("username");

        usersList = findViewById(R.id.usersList);
        noUsersText = findViewById(R.id.noUsersText);
        add = findViewById(R.id.fabButton);

        pd = new ProgressDialog(Users.this);
        pd.setMessage("Loading...");
        pd.show();

        thread_db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                        threads.add(data.getValue().toString());
                }

                doOnSuccess(threads);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    if(!threads.contains(data.getValue().toString())){
                        threads.add(data.getValue().toString());
                    }

                }

                doOnSuccess(threads);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent chat = new Intent(Users.this, Chat.class);
                chat.putExtra("thread_id", threads.get(position));
                chat.putExtra("username", username);

                startActivity(chat);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Select = new Intent(Users.this, SelectUser.class);
                Select.putExtra("username", username);
                startActivity(Select);
//                startActivity(new Intent(Users.this, SelectUser.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.profileMenu:
                startActivity(new Intent(Users.this, UserEdit.class));
                break;

            case R.id.signoutMenu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(Users.this, Login.class));
                                finish();
                            }
                        });
                break;
        }

        return true;
    }

    public void doOnSuccess(ArrayList<String> t) {// clear the list

        if (t.size() == 0) {
            noUsersText.setVisibility(View.VISIBLE);
            usersList.setVisibility(View.GONE);
        } else {
            noUsersText.setVisibility(View.GONE);
            usersList.setVisibility(View.VISIBLE);

            usersList.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    t));
        }

        pd.dismiss();
    }
}