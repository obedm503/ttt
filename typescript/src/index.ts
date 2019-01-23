import * as readline from "readline";

const input = (prompt: string, reader: readline.Interface): Promise<string> =>
  new Promise(resolve => {
    reader.question(prompt, x => {
      resolve(x);
    });
  });

const rand = (min: number, max: number) =>
  Math.floor(Math.random() * max) + min;

class Check {
  static row(board: number[][], row: number, player: number): boolean {
    for (let col = 0; col < 3; col++) {
      if (board[row][col] != player) {
        return false;
      }
    }
    return true;
  }
  static col(board: number[][], col: number, player: number): boolean {
    for (let row = 0; row < 3; row++) {
      if (board[row][col] != player) {
        return false;
      }
    }
    return true;
  }
  static diagonal(board: number[][], player: number): boolean {
    let downDiagonal = true;
    let upDiagonal = true;

    for (let i = 0; i < 3; i++) {
      if (board[i][i] != player) {
        downDiagonal = false;
      }
      if (board[2 - i][i] != player) {
        upDiagonal = false;
      }
    }
    return upDiagonal || downDiagonal;
  }
  static win(board: number[][], player: number): boolean {
    for (let row = 0; row < 3; row++) {
      if (Check.row(board, row, player)) {
        return true;
      }
    }

    for (let col = 0; col < 3; col++) {
      if (Check.col(board, col, player)) {
        return true;
      }
    }

    return Check.diagonal(board, player);
  }
  static catGame(board: number[][]): boolean {
    for (let row = 0; row < 3; row++) {
      for (let col = 0; col < 3; col++) {
        if (board[row][col] == 0) {
          return false;
        }
      }
    }
    return true;
  }
}

class Cell {
  row: number;
  col: number;

  constructor(row: number, col: number) {
    this.row = row;
    this.col = col;
  }
}

const getToken = (player: number) => (player == 1 ? "X" : "@");

const checkFreeCell = (
  board: number[][],
  boardCells: Cell[],
  cellIndex: number
): boolean => {
  const choice = boardCells[cellIndex];
  const cell = board[choice.row][choice.col];
  if (cell == 0) {
    // cell is not taken
    return true;
  }

  return false;
};

const setupBoard = (): Cell[] => {
  const board: Cell[] = [];

  for (let row = 0; row < 3; row++) {
    for (let col = 0; col < 3; col++) {
      const cell = new Cell(row, col);
      board.push(cell);
    }
  }
  return board;
};

const print = (str: string) => {
  process.stdout.write(str);
};

const printBoard = (board: number[][]) => {
  let iteration = 1;

  for (let rowI = 0; rowI < 3; rowI++) {
    for (let colI = 0; colI < 3; colI++) {
      const cell = board[rowI][colI];

      if (cell == 0) {
        print(" " + iteration + " ");
      } else {
        print(" " + getToken(cell) + " ");
      }

      if (colI < 2) {
        print("|");
      }

      iteration += 1;
    }

    print("\n");

    if (rowI < 2) {
      for (let col = 0; col < 3; col++) {
        print("---");
        if (col < 2) {
          print("+");
        }
      }

      print("\n");
    }
  }

  print("\n");
};

const movePlayer = async (
  board: number[][],
  boardCells: Cell[],
  reader: readline.Interface
): Promise<Cell> => {
  let moved = false;
  let choice = new Cell(0, 0);

  while (!moved) {
    const inputLine = await input(
      "Pick a cell (will ignore characters after first).\n",
      reader
    );
    const cellIndex = parseInt(inputLine, 10) - 1;
    print("\n");

    if (cellIndex < 0 || cellIndex > 8) {
      print("Invalid cell number.\n");
      continue;
    }

    choice = boardCells[cellIndex];
    const cell = board[choice.row][choice.col];
    moved = checkFreeCell(board, boardCells, cellIndex);
    if (!moved) {
      print("Cell is already taken by " + getToken(cell) + ".\n");
    }
  }

  return choice;
};

const moveSystem = (board: number[][], boardCells: Cell[]): Cell => {
  let moved = false;
  const tries: number[] = [];
  let iteration = 0;
  let choice = new Cell(0, 0);

  while (!moved) {
    const pick = rand(0, 8);

    if (tries.includes(pick)) {
      continue;
    }
    tries[iteration] = pick;

    choice = boardCells[pick];
    iteration += 1;
    moved = checkFreeCell(board, boardCells, pick);
  }

  return choice;
};

const printScreen = (board: number[][]) => {
  console.clear();
  print("Welcome to ttt.\n\n");
  printBoard(board);
};

(async () => {
  const board = [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
  const boardCells = setupBoard();
  const reader = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });

  printScreen(board);

  let userWon = false;
  let systemWon = false;
  let currentPlayer = 1; // user starts
  let moves = 0;

  while (true) {
    if (userWon || systemWon || Check.catGame(board)) {
      break;
    }

    let currentMove: Cell;

    if (currentPlayer == 1) {
      currentMove = await movePlayer(board, boardCells, reader);
      moves += 1;
    } else {
      currentMove = moveSystem(board, boardCells);
    }
    board[currentMove.row][currentMove.col] = currentPlayer;

    userWon = Check.win(board, 1);
    systemWon = Check.win(board, 2);

    if (currentPlayer == 2 || userWon) {
      printScreen(board);
    }

    // switch player for next turn
    currentPlayer = currentPlayer == 1 ? 2 : 1;
  }

  if (!userWon && !systemWon) {
    print("Cat game in " + moves + " moves.\n");
  } else {
    const msg = userWon ? "You win" : "You loose";
    print(msg + " in " + moves + " moves.\n");
  }

  reader.close();
})();
