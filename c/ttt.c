#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>

char clearCommand[] = "clear";  // in *nix
// char clearCommand[] = "cls"; // in windows

// from
// https://stackoverflow.com/questions/2509679/how-to-generate-a-random-integer-number-from-within-a-range
// because pseudo-random numbers are difficult
unsigned int randInt(unsigned int min, unsigned int max) {
  int r;
  const unsigned int range = 1 + max - min;
  const unsigned int buckets = RAND_MAX / range;
  const unsigned int limit = buckets * range;

  do {
    r = rand();
  } while (r >= limit);

  return min + (r / buckets);
}

int includes(int arr[], int value) {
  int len = sizeof(arr) / sizeof(*arr);
  for (int i = 0; i < len; i++) {
    if (arr[i] == value) {
      return 1;
    }
  }
  return 0;
}

struct Cell {
  int row;
  int col;
};
struct Cell boardCells[9];

char getToken(int player) { return player == 1 ? 'X' : '@'; }

int checkCell(int board[3][3], int cellIndex) {
  struct Cell choice = boardCells[cellIndex];
  int cell = board[choice.row][choice.col];
  if (cell == 0) {
    // cell is not taken
    return 1;
  }

  return 0;
}

void setupBoard(int matrix[3][3]) {
  int iteration = 0;
  for (int rowI = 0; rowI < 3; rowI++) {
    for (int colI = 0; colI < 3; colI++) {
      matrix[rowI][colI] = 0;

      struct Cell cell;
      cell.row = rowI;
      cell.col = colI;
      boardCells[iteration] = cell;

      iteration += 1;
    }
  }
}

void printBoard(int board[3][3]) {
  int iteration = 1;

  for (int rowI = 0; rowI < 3; rowI++) {
    for (int colI = 0; colI < 3; colI++) {
      int cell = board[rowI][colI];

      if (cell == 0) {
        printf(" %d ", iteration);
      } else {
        printf(" %c ", getToken(cell));
      }

      if (colI < 2) {
        printf("|");
      }

      iteration += 1;
    }

    printf("\n");

    if (rowI < 2) {
      for (int colI = 0; colI < 3; colI++) {
        printf("---");
        if (colI < 2) {
          printf("+");
        }
      }

      printf("\n");
    }
  }

  printf("\n");
}

struct Cell movePlayer(int board[3][3]) {
  int moved = 0;
  char inputChar;
  struct Cell choice;

  while (!moved) {
    printf("Pick a cell (will ignore characters after first).\n");
    inputChar = getchar();
    getchar();
    printf("\n");

    int cellIndex = atoi(&inputChar) - 1;

    if (cellIndex < 0 || cellIndex > 8) {
      printf("Invalid cell number.\n");
      continue;
    }

    choice = boardCells[cellIndex];
    int cell = board[choice.row][choice.col];
    moved = checkCell(board, cellIndex);
    if (!moved) {
      printf("Cell is already taken by %c.\n", getToken(cell));
    }
  }

  return choice;
}

struct Cell moveSystem(int board[3][3]) {
  int moved = 0;
  int tries[9];
  int iteration = 0;
  struct Cell choice;

  while (!moved) {
    int pick = randInt(0, 8);

    if (includes(tries, pick)) {
      continue;
    }
    tries[iteration] = pick;

    choice = boardCells[pick];
    iteration += 1;
    moved = checkCell(board, pick);
  }

  return choice;
}

int didWinRow(int board[3][3], int rowI, int player) {
  for (int colI = 0; colI < 3; colI++) {
    if (board[rowI][colI] != player) {
      return 0;
    }
  }
  return 1;
}
int didWinCol(int board[3][3], int colI, int player) {
  for (int rowI = 0; rowI < 3; rowI++) {
    if (board[rowI][colI] != player) {
      return 0;
    }
  }
  return 1;
}
int didWinDiagonal(int board[3][3], int player) {
  int downDiagonal = 1;
  int upDiagonal = 1;

  for (int i = 0; i < 3; i++) {
    if (board[i][i] != player) {
      downDiagonal = 0;
    }
    if (board[2 - i][i] != player) {
      upDiagonal = 0;
    }
  }
  return upDiagonal || downDiagonal;
}
int didWin(int board[3][3], int player) {
  for (int rowI = 0; rowI < 3; rowI++) {
    if (didWinRow(board, rowI, player)) {
      return 1;
    }
  }

  for (int colI = 0; colI < 3; colI++) {
    if (didWinCol(board, colI, player)) {
      return 1;
    }
  }

  return didWinDiagonal(board, player);
}

void printScreen(int matrix[3][3]) {
  system(clearCommand);
  printf("Welcome to ttt.\n\n");
  printBoard(matrix);
}

int main(int argc, char const* argv[]) {
  // setup
  int matrix[3][3];
  setupBoard(matrix);

  printScreen(matrix);

  int userWon = 0;
  int systemWon = 0;
  int currentPlayer = 1;  // user starts
  int moves = 0;

  while (!(userWon || systemWon)) {
    struct Cell currentMove;

    if (currentPlayer == 1) {
      currentMove = movePlayer(matrix);
      moves += 1;
    } else {
      currentMove = moveSystem(matrix);
    }
    matrix[currentMove.row][currentMove.col] = currentPlayer;

    userWon = didWin(matrix, 1);
    systemWon = didWin(matrix, 2);

    if (currentPlayer == 2 || userWon) {
      printScreen(matrix);
    }

    // switch player for next turn
    currentPlayer = currentPlayer == 1 ? 2 : 1;
  }

  if (!userWon && !systemWon) {
    printf("Cat game");
  } else {
    printf("%s in %d moves.\n", userWon == 1 ? "You win" : "You loose", moves);
  }

  return 0;
}
