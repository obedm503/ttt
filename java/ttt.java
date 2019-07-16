import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

class Util {
  public static void clear() {
    try {
      if (System.getProperty("os.name").contains("Windows")) {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      } else {
        new ProcessBuilder("clear").inheritIO().start().waitFor();
      }
    } catch (IOException | InterruptedException ex) {
      ex.printStackTrace();
      System.out.print(ex);
    }
  }

  public static int rand(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }

  public static boolean includes(int[] arr, int value) {
    return Arrays.asList(arr).contains(value);
  }
}

class Check {
  static boolean row(int[][] board, int row, int player) {
    for (int col = 0; col < 3; col++) {
      if (board[row][col] != player) {
        return false;
      }
    }
    return true;
  }

  static boolean col(int[][] board, int col, int player) {
    for (int row = 0; row < 3; row++) {
      if (board[row][col] != player) {
        return false;
      }
    }
    return true;
  }

  static boolean diagonal(int[][] board, int player) {
    boolean downDiagonal = true;
    boolean upDiagonal = true;

    for (int i = 0; i < 3; i++) {

      if (board[i][i] != player) {
        downDiagonal = false;
      }
      if (board[2 - i][i] != player) {
        upDiagonal = false;
      }
    }
    return upDiagonal || downDiagonal;
  }

  static boolean win(int[][] board, int player) {
    for (int row = 0; row < 3; row++) {

      if (row(board, row, player)) {
        return true;
      }
    }

    for (int col = 0; col < 3; col++) {

      if (col(board, col, player)) {
        return true;
      }
    }

    return diagonal(board, player);
  }

  static boolean catGame(int[][] board) {
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        if (board[row][col] == 0) {
          return false;
        }
      }
    }
    return true;
  }
}

class Cell {
  int row;
  int col;

  public Cell(int row, int col) {
    this.row = row;
    this.col = col;
  }
}

class ttt {
  static String getToken(int player) {
    return player == 1 ? "X" : "@";
  }

  static boolean checkFreeCell(int[][] board, Cell[] boardCells, int cellIndex) {
    Cell choice = boardCells[cellIndex];
    int cell = board[choice.row][choice.col];
    if (cell == 0) {
      // cell is not taken
      return true;
    }

    return false;
  }

  static Cell[] setupBoard() {
    Cell[] board = new Cell[9];
    int iteration = 0;

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        Cell cell = new Cell(row, col);
        board[iteration] = cell;

        iteration += 1;
      }
    }
    return board;
  }

  static void printBoard(int[][] board) {
    int iteration = 1;

    for (int rowI = 0; rowI < 3; rowI++) {
      for (int colI = 0; colI < 3; colI++) {
        int cell = board[rowI][colI];

        if (cell == 0) {
          System.out.print(" " + iteration + " ");
        } else {
          System.out.print(" " + getToken(cell) + " ");
        }

        if (colI < 2) {
          System.out.print("|");
        }

        iteration += 1;
      }

      System.out.print("\n");

      if (rowI < 2) {
        for (int col = 0; col < 3; col++) {

          System.out.print("---");
          if (col < 2) {
            System.out.print("+");
          }
        }

        System.out.print("\n");
      }
    }

    System.out.print("\n");
  }

  static Cell movePlayer(int[][] board, Cell[] boardCells, Scanner input) {
    boolean moved = false;
    Cell choice = new Cell(0, 0);

    while (!moved) {
      System.out.print("Pick a cell (will ignore characters after first).\n");

      if (!input.hasNext()) {
        System.out.print("No next Error\n");
        System.exit(0);
      }

      String inputLine = input.nextLine().trim();
      int cellIndex = Integer.parseInt(inputLine) - 1;
      System.out.print("\n");

      if (cellIndex < 0 || cellIndex > 8) {
        System.out.print("Invalid cell number.\n");
        continue;
      }

      choice = boardCells[cellIndex];
      int cell = board[choice.row][choice.col];
      moved = checkFreeCell(board, boardCells, cellIndex);
      if (!moved) {
        System.out.print("Cell is already taken by " + getToken(cell) + ".\n");
      }
    }

    return choice;

  }

  static Cell moveSystem(int[][] board, Cell[] boardCells) {
    boolean moved = false;
    int[] tries = new int[9];
    int iteration = 0;
    Cell choice = new Cell(0, 0);

    while (!moved) {
      int pick = Util.rand(0, 8);

      if (Util.includes(tries, pick)) {
        continue;
      }
      tries[iteration] = pick;

      choice = boardCells[pick];
      iteration += 1;
      moved = checkFreeCell(board, boardCells, pick);
    }

    return choice;
  }

  static void printScreen(int[][] matrix) {
    Util.clear();
    System.out.print("Welcome to ttt.\n\n");
    printBoard(matrix);
  }

  public static void main(String[] args) {
    int[][] matrix = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    Cell[] boardCells = setupBoard();
    Scanner input = new Scanner(System.in);

    printScreen(matrix);

    boolean userWon = false;
    boolean systemWon = false;
    int currentPlayer = 1; // user starts
    int moves = 0;

    while (true) {
      if (userWon || systemWon || Check.catGame(matrix)) {
        break;
      }

      Cell currentMove;

      if (currentPlayer == 1) {
        currentMove = movePlayer(matrix, boardCells, input);
        moves += 1;
      } else {
        currentMove = moveSystem(matrix, boardCells);
      }
      matrix[currentMove.row][currentMove.col] = currentPlayer;

      userWon = Check.win(matrix, 1);
      systemWon = Check.win(matrix, 2);

      if (currentPlayer == 2 || userWon) {
        printScreen(matrix);
      }

      // switch player for next turn
      currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    if (!userWon && !systemWon) {
      System.out.print("Cat game in " + moves + " moves.\n");
    } else {
      String msg = userWon ? "You win" : "You loose";
      System.out.print(msg + " in " + moves + " moves.\n");
    }

    input.close();
  }
}