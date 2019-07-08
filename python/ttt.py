import random
from os import system, name


def clear():
    system("clear" if name == "posix" else "cls")


class Check(object):
    @staticmethod
    def row(board, row, player):
        for col in range(3):
            if board[row][col] is not player:
                return False
        return True

    @staticmethod
    def col(board, col, player):
        for row in range(3):
            if board[row][col] is not player:
                return False
        return True

    @staticmethod
    def diagonal(board, player):
        downDiagonal = True
        upDiagonal = True
        for i in range(3):
            if board[i][i] is not player:
                downDiagonal = False
            if board[2 - i][i] is not player:
                upDiagonal = False

        return upDiagonal or downDiagonal

    @staticmethod
    def win(board, player):
        for row in range(3):
            if Check.row(board, row, player):
                return True

        for col in range(3):
            if Check.col(board, col, player):
                return True

        return Check.diagonal(board, player)

    @staticmethod
    def catGame(board):
        for row in range(3):
            for col in range(3):
                if board[row][col] is 0:
                    return False
        return True


class Cell(object):
    def __init__(self, row, col):
        self.row = row
        self.col = col


def getToken(player):
    return "X" if player is 1 else "@"


def checkFreeCell(board, boardCells, cellIndex):
    choice = boardCells[cellIndex]
    cell = board[choice.row][choice.col]
    if cell is 0:
        # cell is not taken
        return True

    return False


def setupBoard():
    board = []

    for row in range(3):
        for col in range(3):
            cell = Cell(row, col)
            board.append(cell)

    return board


def printBoard(board):
    iteration = 1

    for rowI in range(3):
        for colI in range(3):
            cell = board[rowI][colI]

            if cell == 0:
                print(" " + str(iteration) + " ", end="")
            else:
                print(" " + getToken(cell) + " ", end="")

            if colI < 2:
                print("|", end="")

            iteration += 1

        print("\n")

        if rowI < 2:
            for col in range(3):
                print("---", end="")
                if col < 2:
                    print("+", end="")
            print()

    print()


def movePlayer(board, boardCells):
    moved = False
    choice = Cell(0, 0)

    while not moved:
        inputLine = input("Pick a cell (will ignore characters after first).\n")
        cellIndex = int(inputLine) - 1
        print("\n")

        if cellIndex < 0 or cellIndex > 8:
            print("Invalid cell number.\n")
            continue

        choice = boardCells[cellIndex]
        cell = board[choice.row][choice.col]
        moved = checkFreeCell(board, boardCells, cellIndex)
        if not moved:
            print("Cell is already taken by " + getToken(cell) + ".\n")

    return choice


def moveSystem(board, boardCells):
    moved = False
    tries = []
    iteration = 0
    choice = Cell(0, 0)

    while not moved:
        pick = random.randrange(0, 8)

        if tries.count(pick) > 0:
            continue

        tries.append(pick)

        choice = boardCells[pick]
        iteration += 1
        moved = checkFreeCell(board, boardCells, pick)
    return choice


def printScreen(board):
    clear()
    print("Welcome to ttt.\n\n")
    printBoard(board)


board = [[0, 0, 0], [0, 0, 0], [0, 0, 0]]
boardCells = setupBoard()

printScreen(board)

userWon = False
systemWon = False
currentPlayer = 1
# user starts
moves = 0

while True:
    if userWon or systemWon or Check.catGame(board):
        break

    currentMove = None

    if currentPlayer is 1:
        currentMove = movePlayer(board, boardCells)
        moves += 1
    else:
        currentMove = moveSystem(board, boardCells)

    board[currentMove.row][currentMove.col] = currentPlayer

    userWon = Check.win(board, 1)
    systemWon = Check.win(board, 2)

    if currentPlayer is 2 or userWon:
        printScreen(board)

    # switch player for next turn
    currentPlayer = 2 if currentPlayer is 1 else 1


if not userWon and not systemWon:
    print("Cat game in " + moves + " moves.\n")
else:
    msg = "You win" if userWon else "You loose"
    print(msg + " in " + str(moves) + " moves.\n")

