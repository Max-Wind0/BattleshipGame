package com.example.battleshipgame;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    enum Difficulty {
        EASY,
        NORMAL,
        MULTIPLAYER
    }

    public enum AttackResult {
        MISS,
        HIT,
        UNKNOWN
    }

    private Difficulty opponentDifficulty = Difficulty.EASY;
    private static final int GRID_SIZE = 10;
    private boolean isPlayerTurn = true;
    private TextView statusText;
    private final List<Ship> playerShips = new ArrayList<>();
    private final List<Ship> opponentShips = new ArrayList<>();
    private final AttackResult[][] playerHits = new AttackResult[GRID_SIZE][GRID_SIZE];
    private final AttackResult[][] opponentHits = new AttackResult[GRID_SIZE][GRID_SIZE];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        String mode = getIntent().getStringExtra("mode");

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                playerHits[i][j] = AttackResult.UNKNOWN;
                opponentHits[i][j] = AttackResult.UNKNOWN;
            }
        }

        if (mode != null) {
            switch (mode) {
                case "easyOpponent":
                    opponentDifficulty = Difficulty.EASY;
                    break;
                case "normalOpponent":
                    opponentDifficulty = Difficulty.NORMAL;
                    break;
                case "multiplayer":
                    opponentDifficulty = Difficulty.MULTIPLAYER;
                    break;
            }
        }

        GridLayout playerBoard = findViewById(R.id.playerBoard);
        GridLayout opponentBoard = findViewById(R.id.opponentBoard);
        statusText = findViewById(R.id.statusText);

        generateShips(playerShips, "player");
        generateShips(opponentShips, "opponent");

        fillGrid(playerBoard, playerShips, true);
        fillGrid(opponentBoard, opponentShips, false);
    }

    public class Ship {
        private final int size;
        private final int row;
        private final int col;
        private final boolean isVertical;
        private final String owner;
        private final List<Cell> cells;

        public Ship(int size, int row, int col, boolean isVertical, String owner) {
            this.size = size;
            this.row = row;
            this.col = col;
            this.isVertical = isVertical;
            this.owner = owner;
            this.cells = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                if (isVertical) {
                    cells.add(new Cell(row + i, col));
                } else {
                    cells.add(new Cell(row, col + i));
                }
            }
        }

        public boolean hit(int row, int col) {
            for (Cell cell : cells) {
                if (cell.row == row && cell.col == col) {
                    cell.isHit = true;
                    if (isSunk()){
                        markMissesAroundSunkShip(this);
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean isSunk() {
            for (Cell cell : cells) {
                if (!cell.isHit) {
                    return false;
                }
            }
            return true;
        }

        public class Cell {
            int row;
            int col;
            boolean isHit;

            public Cell(int row, int col) {
                this.row = row;
                this.col = col;
                this.isHit = false;
            }
        }
    }

    private void generateShips(List<Ship> shipsList, String owner) {
        Random random = new Random();
        int[] shipSizes = {1, 1, 1, 1, 2, 2, 2, 3, 3, 4};

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(GRID_SIZE);
                int col = random.nextInt(GRID_SIZE);
                boolean isVertical = random.nextBoolean();

                if (canPlaceShip(row, col, size, isVertical, shipsList)) {
                    Ship ship = new Ship(size, row, col, isVertical, owner);
                    shipsList.add(ship);
                    placed = true;
                }
            }
        }
    }

    private boolean canPlaceShip(int row, int col, int size, boolean isVertical, List<Ship> shipsList) {
        if (isVertical && row + size > GRID_SIZE) return false;
        if (!isVertical && col + size > GRID_SIZE) return false;

        for (Ship ship : shipsList) {
            for (Ship.Cell cell : ship.cells) {
                for (int i = -1; i <= size; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (isVertical) {
                            int checkRow = row + i;
                            int checkCol = col + j;
                            if (checkRow >= 0 && checkRow < GRID_SIZE && checkCol >= 0 && checkCol < GRID_SIZE) {
                                if (checkRow == cell.row && checkCol == cell.col) {
                                    return false;
                                }
                            }
                        }
                        else {
                            int checkRow = row + j;
                            int checkCol = col + i;
                            if (checkRow >= 0 && checkRow < GRID_SIZE && checkCol >= 0 && checkCol < GRID_SIZE) {
                                if (checkRow == cell.row && checkCol == cell.col) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void fillGrid(GridLayout gridLayout, List<Ship> shipsList, boolean isPlayerBoard) {
        gridLayout.removeAllViews();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Button button = createButton(row, col, gridLayout.getId());
                gridLayout.addView(button);
                button.setBackgroundColor(Color.GRAY);

                if (isPlayerBoard && opponentDifficulty != Difficulty.MULTIPLAYER) {
                    for (Ship ship : shipsList) {
                        for (Ship.Cell cell : ship.cells) {
                            if (cell.row == row && cell.col == col) {
                                button.setBackgroundColor(Color.BLUE);
                                break;
                            }
                        }
                    }
                } else {
                    for (Ship ship : shipsList) {
                        for (Ship.Cell cell : ship.cells) {
                            if (cell.row == row && cell.col == col) {
                                button.setBackgroundColor(Color.GRAY);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private Button createButton(int row, int col, int boardId) {
        Button button = new Button(this);
        int size = (int) (getResources().getDisplayMetrics().density * 25);
        button.setMinWidth(size);
        button.setMinHeight(size);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.rowSpec = GridLayout.spec(row);
        params.columnSpec = GridLayout.spec(col);
        params.setMargins(2, 2, 2, 2);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> onCellClicked(row, col, boardId));

        return button;
    }

    private void onCellClicked(int row, int col, int boardId) {
        if ((boardId == R.id.playerBoard && isPlayerTurn) || (boardId == R.id.opponentBoard && !isPlayerTurn)) {
            return;
        }

        if (boardId == R.id.opponentBoard) {
            GridLayout gridLayoutAI = findViewById(boardId);
            Button button = (Button) gridLayoutAI.getChildAt(row * GRID_SIZE + col);
            ColorDrawable background = (ColorDrawable) button.getBackground();
            int buttonColor = background.getColor();

            if (buttonColor == Color.RED || buttonColor == Color.BLACK) {
                return;
            }

            boolean isHit = false;
            for (Ship ship : opponentShips) {
                if (ship.hit(row, col)) {
                    isHit = true;
                    break;
                }
            }

            updateBoard(boardId, row, col, isHit);

            if (areAllShipsDestroyed(opponentShips)) {
                statusText.setText("----> Игрок победил! <----");
                disableAllButtons();
            } else {
                toggleTurn();
            }

        } else if (boardId == R.id.playerBoard) {
            GridLayout gridLayout = findViewById(boardId);
            Button button = (Button) gridLayout.getChildAt(row * GRID_SIZE + col);
            ColorDrawable background = (ColorDrawable) button.getBackground();
            int buttonColor = background.getColor();

            if (buttonColor == Color.RED || buttonColor == Color.BLACK) {
                return;
            }

            boolean isHit = false;
            for (Ship ship : playerShips) {
                if (ship.hit(row, col)) {
                    isHit = true;
                    break;
                }
            }

            updateBoard(boardId, row, col, isHit);

            if (areAllShipsDestroyed(playerShips)) {
                statusText.setText("----> Оппонент победил! <----");
                disableAllButtons();
            } else {
                toggleTurn();
            }
        }
    }



    private void updateBoard(int boardId, int row, int col, boolean isHit) {
        GridLayout gridLayout = findViewById(boardId);
        Button button = (Button) gridLayout.getChildAt(row * GRID_SIZE + col);

        if (boardId == R.id.playerBoard) {
            if (isHit) {
                button.setBackgroundColor(Color.RED);
                opponentHits[row][col] = AttackResult.HIT;
            } else {
                button.setBackgroundColor(Color.BLACK);
                opponentHits[row][col] = AttackResult.MISS;
            }
        } else if (boardId == R.id.opponentBoard) {
            if (isHit) {
                button.setBackgroundColor(Color.RED);
                playerHits[row][col] = AttackResult.HIT;
            } else {
                button.setBackgroundColor(Color.BLACK);
                playerHits[row][col] = AttackResult.MISS;
            }
        }
    }

    private boolean areAllShipsDestroyed(List<Ship> ships) {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    private int lastHitRow = -1;
    private int lastHitCol = -1;

    private void opponentTurn() {
        Random random = new Random();
        GridLayout opponentBoard = findViewById(R.id.opponentBoard);
        final int[] row = {-1};
        final int[] col = {-1};

        switch (opponentDifficulty) {
            case NORMAL:
                new Handler().postDelayed(() -> {
                    if (lastHitRow != -1 && lastHitCol != -1) {
                        int[] directions = {-1, 1};

                        for (int dir : directions) {
                            int newRow = lastHitRow + dir;
                            int newCol = lastHitCol;

                            if (isValidMove(newRow, newCol)) {
                                row[0] = newRow;
                                col[0] = newCol;
                                break;
                            }

                            newRow = lastHitRow;
                            newCol = lastHitCol + dir;

                            if (isValidMove(newRow, newCol)) {
                                row[0] = newRow;
                                col[0] = newCol;
                                break;
                            }
                        }
                    }

                    if (row[0] == -1 || col[0] == -1) {
                        do {
                            row[0] = random.nextInt(GRID_SIZE);
                            col[0] = random.nextInt(GRID_SIZE);
                        } while (opponentHits[row[0]][col[0]] != AttackResult.UNKNOWN);
                    }
                    attackCell(row[0], col[0]);
                    for (int i = 0; i < opponentBoard.getChildCount(); i++) {
                        Button button = (Button) opponentBoard.getChildAt(i);
                        button.setEnabled(true);
                    }
                }, 1000);
                break;
            case MULTIPLAYER:
                toggleTurn();
                break;
            default:
                new Handler().postDelayed(() -> {
                    do {
                        row[0] = random.nextInt(GRID_SIZE);
                        col[0] = random.nextInt(GRID_SIZE);
                    } while (opponentHits[row[0]][col[0]] != AttackResult.UNKNOWN);
                    attackCell(row[0], col[0]);
                    for (int i = 0; i < opponentBoard.getChildCount(); i++) {
                        Button button = (Button) opponentBoard.getChildAt(i);
                        button.setEnabled(true);
                    }
                }, 1000);
                break;
        }
    }


    private void attackCell(int row, int col) {
        boolean isHit = false;

        for (Ship ship : playerShips) {
            if (ship.hit(row, col)) {
                isHit = true;
                break;
            }
        }

        if (isHit) {
            opponentHits[row][col] = AttackResult.HIT;
        } else {
            opponentHits[row][col] = AttackResult.MISS;
        }

        updateBoard(R.id.playerBoard, row, col, isHit);

        if (isHit) {
            lastHitRow = row;
            lastHitCol = col;
        }

        if (areAllShipsDestroyed(playerShips)) {
            statusText.setText("----> Оппонент победил! <----");
            GridLayout playerBoard = findViewById(R.id.playerBoard);
            GridLayout opponentBoard = findViewById(R.id.opponentBoard);
            int childCount = playerBoard.getChildCount();
            for (int i = 0; i < childCount; i++) {
                Button button1 = (Button) playerBoard.getChildAt(i);
                button1.setEnabled(false);
                Button button2 = (Button) opponentBoard.getChildAt(i);
                button2.setEnabled(false);
            }
        } else {
            toggleTurn();
        }
    }


    private boolean isValidMove(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            return false;
        }
        if (opponentHits[row][col] != AttackResult.UNKNOWN) {
            return false;
        }
        return true;
    }

    private void toggleTurn() {
        isPlayerTurn = !isPlayerTurn;
        updateTurnText();

        GridLayout playerBoard = findViewById(R.id.playerBoard);
        GridLayout opponentBoard = findViewById(R.id.opponentBoard);

        if (opponentDifficulty != Difficulty.MULTIPLAYER) {
            for (int i = 0; i < playerBoard.getChildCount(); i++) {
                Button button = (Button) playerBoard.getChildAt(i);
                button.setEnabled(false);
            }
        }
        for (int i = 0; i < opponentBoard.getChildCount(); i++) {
            Button button = (Button) opponentBoard.getChildAt(i);
            button.setEnabled(isPlayerTurn);
        }

        if (!isPlayerTurn && opponentDifficulty != Difficulty.MULTIPLAYER) {
            opponentTurn();
        }
    }



    private void updateTurnText() {
        if (isPlayerTurn) {
            statusText.setText("Игрок атакует Оппонента");
        } else {
            statusText.setText("Оппонент атакует Игрока");
        }
    }
    private void markMissesAroundSunkShip(Ship sunkShip) {
        for (Ship.Cell cell : sunkShip.cells) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int row = cell.row + i;
                    int col = cell.col + j;

                    if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
                        if (isCellPartOfShip(row, col, sunkShip)) continue;
                        if (Objects.equals(sunkShip.owner, "player")){
                            if (opponentHits[row][col] != AttackResult.HIT) {
                                opponentHits[row][col] = AttackResult.MISS;
                                updateBoard(R.id.playerBoard, row, col, false);
                            }
                        }
                        else {
                            playerHits[row][col] = AttackResult.MISS;
                            updateBoard(R.id.opponentBoard, row, col, false);
                        }
                    }
                }
            }
        }
    }

    private boolean isCellPartOfShip(int row, int col, Ship ship) {
        for (Ship.Cell cell : ship.cells) {
            if (cell.row == row && cell.col == col) {
                return true;
            }
        }
        return false;
    }

    private void disableAllButtons() {
        GridLayout playerBoard = findViewById(R.id.playerBoard);
        GridLayout opponentBoard = findViewById(R.id.opponentBoard);

        for (int i = 0; i < playerBoard.getChildCount(); i++) {
            Button button1 = (Button) playerBoard.getChildAt(i);
            button1.setEnabled(false);
        }
        for (int i = 0; i < opponentBoard.getChildCount(); i++) {
            Button button2 = (Button) opponentBoard.getChildAt(i);
            button2.setEnabled(false);
        }
    }

}
