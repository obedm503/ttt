extern crate rand;

// use std::{thread, time};
use rand::{thread_rng, Rng};
use std::{io::stdin, process::Command, string::String};

fn random_int(low: u32, high: u32) -> u32 {
  return thread_rng().gen_range(low, high);
}

fn clear() {
  Command::new("cls")
    .status()
    .or_else(|_| Command::new("clear").status())
    .unwrap();
}

// fn sleep(millis: u64) {
//   let ten_millis = time::Duration::from_millis(millis);
//   thread::sleep(ten_millis);
// }

fn get_token(player: &u8) -> char {
  return if player == &1 { 'X' } else { '@' };
}

struct Cell {
  row: usize,
  col: usize,
}

fn includes(vec: &Vec<u8>, value: u8) -> bool {
  for i in vec {
    match vec.get(*i as usize) {
      Some(n) => {
        if n == &value {
          return true;
        }
      }
      None => continue,
    }
  }
  return false;
}

fn check_cell(board: &Vec<Vec<u8>>, board_cells: &Vec<Cell>, cell_index: usize) -> bool {
  let choice: &Cell = match board_cells.get(cell_index) {
    Some(el) => el,
    None => return false,
  };
  let row: &Vec<u8> = match board.get(choice.row) {
    Some(n) => n,
    None => return false,
  };

  let cell: &u8 = match row.get(choice.col) {
    Some(n) => n,
    None => return false,
  };

  if cell == &0 {
    // cell is not taken
    return true;
  }

  return false;
}

fn setup_board() -> Vec<Cell> {
  let mut board_cells = vec![];

  for row in 0..3 {
    for col in 0..3 {
      let cell = Cell { row: row, col: col };
      board_cells.push(cell);
    }
  }
  return board_cells;
}

fn print_board(board: &Vec<Vec<u8>>) {
  let mut iteration = 1;

  for row_i in 0..3 {
    for col_i in 0..3 {
      let row = match board.get(row_i) {
        Some(n) => n,
        None => continue,
      };
      let cell = match row.get(col_i) {
        Some(n) => n,
        None => continue,
      };

      if cell == &0 {
        print!(" {} ", iteration);
      } else {
        print!(" {} ", get_token(cell));
      }

      if col_i < 2 {
        print!("|");
      }

      iteration += 1;
    }

    print!("\n");

    if row_i < 2 {
      for col in 0..3 {
        print!("---");
        if col < 2 {
          print!("+");
        }
      }

      print!("\n");
    }
  }

  print!("\n");
}

fn move_player(board: &Vec<Vec<u8>>, board_cells: &Vec<Cell>) -> Cell {
  loop {
    println!("Pick a cell (will ignore characters after first).");

    let mut input_char = String::new();
    match stdin().read_line(&mut input_char) {
      Ok(_) => {}
      Err(err) => {
        println!("Error: {}", err);
        continue;
      }
    }
    println!("{} input", input_char);

    print!("\n");

    let cell_index = match input_char.trim().parse::<u32>() {
      Ok(n) => n - 1,
      Err(err) => {
        println!("Error: {}", err);
        continue;
      }
    };

    if cell_index > 8 {
      println!("Invalid cell number.");
      continue;
    }

    let moved = check_cell(&board, &board_cells, cell_index as usize);

    let choice = match board_cells.get(cell_index as usize) {
      Some(n) => n,
      None => continue,
    };

    let cell = board[choice.row][choice.col];
    if moved {
      return Cell {
        row: choice.row,
        col: choice.col,
      };
    }

    println!("Cell is already taken by {}.", get_token(&cell));
  }
}

fn move_system(board: &Vec<Vec<u8>>, board_cells: &Vec<Cell>) -> Cell {
  let mut tries = vec![];

  loop {
    let pick = random_int(0, 8);

    if includes(&tries, pick as u8) {
      continue;
    }
    tries.push(pick as u8);

    let choice = match board_cells.get(pick as usize) {
      Some(n) => n,
      None => {
        continue;
      }
    };
    let moved = check_cell(&board, &board_cells, pick as usize);

    if moved {
      return Cell {
        row: choice.row,
        col: choice.col,
      };
    }
  }
}

fn did_win_row(board: &Vec<Vec<u8>>, row: usize, player: u8) -> bool {
  for col in 0..3 {
    if board[row][col] != player {
      return false;
    }
  }
  return true;
}
fn did_win_col(board: &Vec<Vec<u8>>, col: usize, player: u8) -> bool {
  for row in 0..3 {
    if board[row][col] != player {
      return false;
    }
  }
  return true;
}
fn did_win_diagonal(board: &Vec<Vec<u8>>, player: u8) -> bool {
  let mut down_diagonal = true;
  let mut up_diagonal = true;

  for i in 0..3 {
    if board[i][i] != player {
      down_diagonal = false;
    }
    if board[2 - i][i] != player {
      up_diagonal = false;
    }
  }
  return up_diagonal || down_diagonal;
}
fn did_win(board: &Vec<Vec<u8>>, player: u8) -> bool {
  for row in 0..3 {
    if did_win_row(board, row, player) {
      return true;
    }
  }

  for col in 0..3 {
    if did_win_col(board, col, player) {
      return true;
    }
  }

  return did_win_diagonal(board, player);
}

fn print_screen(matrix: &Vec<Vec<u8>>) {
  clear();
  print!("Welcome to ttt.\n\n");
  print_board(matrix);
}

fn main() {
  // setup
  let mut matrix = vec![vec![0, 0, 0], vec![0, 0, 0], vec![0, 0, 0]];
  let board_cells = setup_board();

  print_screen(&matrix);

  let mut user_won = false;
  let mut system_won = false;
  let mut current_player = 1; // user starts
  let mut moves = 0;

  loop {
    if user_won || system_won {
      break;
    }

    let current_move;

    if current_player == 1 {
      current_move = move_player(&matrix, &board_cells);
      moves += 1;
    } else {
      current_move = move_system(&matrix, &board_cells);
    }
    matrix[current_move.row][current_move.col] = current_player;

    user_won = did_win(&matrix, 1);
    system_won = did_win(&matrix, 2);

    if current_player == 2 || user_won {
      print_screen(&matrix);
    }

    // switch player for next turn
    current_player = if current_player == 1 { 2 } else { 1 };
  }

  if !user_won && !system_won {
    print!("Cat game");
  } else {
    let msg = if user_won { "You win" } else { "You loose" };
    print!("{} in {} moves.\n", msg, moves);
  }
}
