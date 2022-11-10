package com.example.guessinggame.assignment4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.guessinggame.R;

import java.util.ArrayList;
import java.util.List;

//This activity controls selection of the word for the opponent to guess (from a list retrieved from the server)
public class SelectWordActivity extends AppCompatActivity {
    private String username;
    private EditText selected;
    private RecyclerView wordListDisplay;
    ArrayList<String> wordList;

    //On activity creation, setup activity fields
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_word);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        selected = (EditText) findViewById(R.id.selected);
        //Setup RecyclerView
        wordListDisplay = (RecyclerView) findViewById(R.id.wordList);
        wordList = new ArrayList<>();
        //Create & attach adapter class
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(wordList);
        wordListDisplay.setAdapter(adapter);
        //Set layout manager of the RecyclerView
        wordListDisplay.setLayoutManager(new LinearLayoutManager(this));
        //Run Async task to get data from server
        String url = getString(R.string.user_url);
        GetStringsTask task = new GetStringsTask(wordListDisplay, wordList);
        task.execute(url+username+"/versus/getList");
    }

    //Handle confirmation button click - start BluetoothActivity
    public void choiceConfirmed(View view) {
        Intent intent = new Intent(this, BluetoothActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("chosen word", selected.getText().toString());
        startActivityForResult(intent, 1); //1 = request code
    }

    //When the back button in the action bar is clicked, set the username as result to return to parent (Navigation) activity
    public boolean onSupportNavigateUp() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("username", username);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        return true;
    }

    //Inner class to manage ViewHolders to display list items in the RecyclerView
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private List<String> wordList;
        public final View.OnClickListener onClickListener = new View.OnClickListener() { //OnClick Listener for list items
            @Override
            public void onClick(View view) { //When an item is clicked, set it as the selected word
                int position = wordListDisplay.getChildLayoutPosition(view);
                String word = wordList.get(position);
                selected.setText(word);
            }
        };

        //Inner class to hold a view containing an array item
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            //Construct a ViewHolder
            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.item); //item TextView in list_item.xml
            }

            //Get the textView
            public TextView getTextView() {
                return textView;
            }

        }

        //Construct the adapter
        public RecyclerViewAdapter(List<String> wordList) {
            this.wordList = wordList;
        }

        //"inflate" an underlying view using the layout from list_item.xml and create a ViewHolder to hold it
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            itemView.setOnClickListener(onClickListener); //Have listener listen to the view being clicked
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        //Set the text of the view held by the ViewHolder to the string at a given position in the array
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.getTextView().setText(wordList.get(position));
        }

        //Get the size of the list
        @Override
        public int getItemCount() {
            return wordList.size();
        }
    }
}