package com.example.assignment4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button submitButton, restartButton, clearButton, switchActivityButton;
    GridLayout gridLayout;
    String currentAnswer;
    int[][] stateOfBoxes;
    static final int GRID_ROWS = 5;
    static final int SCORE_TO_WIN = 10;
    int activeRowIndex;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseRef;
    LinkedList<String> listOfWords = new LinkedList<>();
    Random randomGenerator = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submitButton = findViewById(R.id.submit_BT);
        restartButton = findViewById(R.id.restart_BT);
        clearButton = findViewById(R.id.clear_BT);
        switchActivityButton = findViewById(R.id.add_word_BT);
        gridLayout = findViewById(R.id.box_grid_GL);

        submitButton.setOnClickListener(view -> processSubmitAction());
        restartButton.setOnClickListener(view -> restartGame());
        clearButton.setOnClickListener(view -> clearCurrentGame());
        switchActivityButton.setOnClickListener(view -> switchToWordAddingActivity());

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseRef = firebaseDatabase.getReference("words");
        retrieveWordsFromDatabase();

        setupGame();
    }

    private void setupGame() {
        activeRowIndex = 0;
        initializeBoxStates();
        clearAllGuesses();
        refreshGridLayout();
    }

    private void clearAllGuesses() {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof EditText) {
                EditText editText = (EditText) child;
                editText.setText("");
                editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gray));
            }
        }
    }


    private void processSubmitAction() {
        if (validateInput()) {
            evaluateUserInput();
            if (!evaluateWinCondition()) {
                if (activeRowIndex < GRID_ROWS) {
                    activeRowIndex++;
                    moveToNextRow();
                } else {
                    Toast.makeText(getApplicationContext(), "Game Over. Try Again!", Toast.LENGTH_LONG).show();
                    Intent loseIntent = new Intent(getApplicationContext(), LoseScreen.class);
                    startActivity(loseIntent);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Congratulations! You Win!", Toast.LENGTH_LONG).show();
                Intent winIntent = new Intent(getApplicationContext(), WinScreen.class);
                startActivity(winIntent);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter a valid word.", Toast.LENGTH_LONG).show();
        }
    }

    private void restartGame() {
        currentAnswer = listOfWords.get(randomGenerator.nextInt(listOfWords.size()));
        setupGame();
        Toast.makeText(getApplicationContext(), "Game Reset!", Toast.LENGTH_LONG).show();
    }

    private void clearCurrentGame() {
        setupGame();
        Toast.makeText(getApplicationContext(), "Game Cleared!", Toast.LENGTH_LONG).show();
    }

    private void switchToWordAddingActivity() {
        Intent intent = new Intent(getApplicationContext(), AddWord.class);
        startActivity(intent);
    }

    private void retrieveWordsFromDatabase() {
        databaseRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else {
                for (DataSnapshot child : task.getResult().getChildren()) {
                    String word = child.getValue(String.class);
                    listOfWords.add(word);
                }
            }

            currentAnswer = listOfWords.get(randomGenerator.nextInt(listOfWords.size()));
        });
    }

    private void initializeBoxStates() {
        stateOfBoxes = new int[GRID_ROWS+1][GRID_ROWS];
        for (int i = 0; i < GRID_ROWS+1; i++) {
            for (int j = 0; j < GRID_ROWS; j++) {
                stateOfBoxes[i][j] = -2;
            }
        }
        stateOfBoxes[0] = new int[]{0, 0, 0, 0, 0};
    }

    private void refreshGridLayout() {
        int childCount = gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            EditText editText = (EditText) gridLayout.getChildAt(i);
            int row = i / GRID_ROWS; // Calculate row index
            int col = i % GRID_ROWS; // Calculate column index

            if(row < stateOfBoxes.length && col < stateOfBoxes[row].length) {
                updateBoxAppearance(editText, stateOfBoxes[row][col]);
            }
        }
    }

    private int getChildIndex(int i) {
        int index = (activeRowIndex * GRID_ROWS) + i;
        if (index < gridLayout.getChildCount()) {
            return index;
        } else {
            return -1; // or handle this case as needed
        }
    }

    private void updateBoxAppearance(EditText editText, int state) {
        int backgroundResource;
        int textColor;
        boolean isEnabled;

        if (state == -2) {
            backgroundResource = R.drawable.gray;
            textColor = R.color.white;
            isEnabled = false;
        } else if (state == -1) {
            backgroundResource = R.drawable.darkgray;
            textColor = R.color.white;
            isEnabled = false;
        } else if (state == 0) {
            backgroundResource = R.drawable.gray;
            textColor = R.color.black;
            isEnabled = true;
        } else if (state == 1) {
            backgroundResource = R.drawable.yellow;
            textColor = R.color.white;
            isEnabled = false;
        } else if (state == 2) {
            backgroundResource = R.drawable.green;
            textColor = R.color.white;
            isEnabled = false;
        } else {
            // Default case for unexpected state
            backgroundResource = R.drawable.gray;
            textColor = R.color.black;
            isEnabled = false;
        }

        editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), backgroundResource));
        editText.setTextColor(getResources().getColor(textColor));
        editText.setEnabled(isEnabled);
    }


    private void moveToNextRow() {
        for (int i = 0; i < GRID_ROWS; i++) {
            stateOfBoxes[activeRowIndex][i] = 0;
        }
        refreshGridLayout();
    }

    private void evaluateUserInput() {
        StringBuilder inputWordBuilder = new StringBuilder();

        for (int i = 0; i < GRID_ROWS; i++) {
            EditText editText = (EditText) gridLayout.getChildAt(getChildIndex(i));
            inputWordBuilder.append(editText.getText().toString());
        }

        String inputWord = inputWordBuilder.toString().toLowerCase();
        updateBoxStates(inputWord);
    }

    private void updateBoxStates(String inputWord) {
        char[] inputChars = inputWord.toCharArray();
        char[] answerChars = currentAnswer.toCharArray();

        for (int i = 0; i < GRID_ROWS; i++) {
            EditText editText = (EditText) gridLayout.getChildAt(getChildIndex(i));
            String boxLetter = editText.getText().toString().toLowerCase();
            if (currentAnswer.contains(boxLetter)) {
                if (inputChars[i] == answerChars[i]) {
                    stateOfBoxes[activeRowIndex][i] = 2;
                } else {
                    stateOfBoxes[activeRowIndex][i] = 1;
                }
            } else {
                stateOfBoxes[activeRowIndex][i] = -1;
            }
        }
        refreshGridLayout();
    }

    private boolean evaluateWinCondition() {
        int score = 0;
        for (int state : stateOfBoxes[activeRowIndex]) {
            score += state;
        }

        if (score >= SCORE_TO_WIN) {
            setRowGreen(activeRowIndex);
            return true;
        }

        return false;
    }

    private void setRowGreen(int rowIndex) {
        Arrays.fill(stateOfBoxes[rowIndex], 2);
        refreshGridLayout();
    }

    private boolean validateInput() {
        StringBuilder inputWordBuilder = new StringBuilder();

        for (int i = 0; i < GRID_ROWS; i++) {
            EditText editText = (EditText) gridLayout.getChildAt(getChildIndex(i));
            inputWordBuilder.append(editText.getText().toString());
        }

        String inputWord = inputWordBuilder.toString().toLowerCase();
        return !inputWord.isEmpty() && inputWord.length() == GRID_ROWS;
    }
}
