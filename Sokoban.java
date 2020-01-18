
import java.util.*;
import java.io.*;

/**
 * Sokoban: This class has the code that runs the Sokoban game. Below you can
 * find methods that move the player and loads levels that we can play. Some
 * methods move the player, check if the moves are valid, moves the boxes the
 * warehouse worker wants to shift, calculates the moves it took for a player to
 * complete a level, allows the user to save the moves once the level is passed
 * and checks if the level chosen is valid. The constants referenced in this
 * file can be found in the Config.Java file.
 * 
 * 
 * @author Andrew Rodriuez-Solis
 *
 */
public class Sokoban {

	/**
	 * Prompts the user for a value by displaying prompt. Note: This method should
	 * not add a new line to the output of prompt.
	 *
	 * After prompting the user, the method will consume an entire line of input
	 * while reading an int. If the value read is between min and max (inclusive),
	 * that value is returned. Otherwise, "Invalid value." terminated by a new line
	 * is output and the user is prompted again.
	 *
	 * @param scanner The Scanner instance to read from System.in.
	 * @param prompt  The name of the value for which the user is prompted.
	 * @param min     The minimum acceptable int value (inclusive).
	 * @param min     The maximum acceptable int value (inclusive).
	 * @return Returns the value read from the user.
	 */
	public static int promptInt(Scanner scanner, String prompt, int min, int max) {
		boolean error = false;
		int valByUser = 0;
		// do-while needed to ensure we execute this part at least once
		do {
			// if error is true then the user can't play the level
			if (error) {
				System.out.println("Invalid value.");
				error = false;
			}
			System.out.print(prompt);

			if (scanner.hasNextInt()) {
				valByUser = scanner.nextInt();
			} else {
				error = true;
			}
			scanner.nextLine();

		} while (error || (error = (valByUser < min || valByUser > max)));// == true);
		return valByUser;
	}

	/**
	 * Prompts the user for an char value by displaying prompt. Note: This method
	 * should not be a new line to the output of prompt.
	 *
	 * After prompting the user, the method will read an entire line of input and
	 * return the first non-whitespace character converted to lower case.
	 *
	 * @param sc     The Scanner instance to read from System.in
	 * @param prompt The user prompt.
	 * @return Returns the first non-whitespace character (in lower case) read from
	 *         the user. If there are no non-whitespace characters read, the null
	 *         character is returned.
	 */
	public static char promptChar(Scanner sc, String prompt) {
		// turns the string into lowerCase
		String in = promptString(sc, prompt).toLowerCase();
		if (in.length() <= 0)
			return '\0';
		else
			return in.charAt(0);
	}

	/**
	 * Prompts the user for a string value by displaying prompt. Note: This method
	 * should not be a new line to the output of prompt.
	 *
	 * After prompting the user, the method will read an entire line of input,
	 * remove any leading and trailing whitespace.
	 *
	 * @param sc     The Scanner instance to read from System.in
	 * @param prompt The user prompt.
	 * @return Returns the string entered by the user, converted to lower case with
	 *         leading and trailing whitespace removed.
	 */
	public static String promptString(Scanner sc, String prompt) {
		System.out.print(prompt);
		// trims any leading white space
		return sc.nextLine().trim();
	}

	/**
	 * This method performs the following tests (in order): 1 - levelChoice > 0 2 -
	 * levels is not shorter than levelChoice, that the 2-d array at index level
	 * exists and that it contains at least 1 row. 3 - goals is not shorter than
	 * lvl, the 1-d array at index level exists and that it contains an even number
	 * of cells. 4 - the number of workers is exactly 1. 5 - the number of boxes is
	 * more than 0. 6 - the number of boxes equals the number of goals. 7 - the
	 * coordinate of each goal is valid for the given levelChoice and does not
	 * correspond to a wall cell. 8 - checks if the level that we're in has
	 * duplicate goals
	 */
	/**
	 * 
	 * @param levelChoice the level the user wants to play
	 * @param levels      the arrayList holding the actual levels to play
	 * @param goals       arrayList containing the goals
	 * @return an int to indicate that the levels are valid or not
	 */
	public static int checkLevel(int levelChoice, ArrayList<char[][]> levels, ArrayList<int[]> goals) {
		// 1
		if (levelChoice < 0)
			return 0;
		// 2
		if (levels.size() <= levelChoice || levels.get(levelChoice) == null || levels.get(levelChoice)[0] == null)
			return -1;
		// 3
		if (goals.size() <= levelChoice || goals.get(levelChoice) == null || goals.get(levelChoice).length % 2 == 1)
			return -2;
		int numWorker = 0;
		int boxCount = 0;
		for (int i = 0; i < levels.get(levelChoice).length; i++)
			for (int j = 0; j < levels.get(levelChoice)[i].length; j++) {
				if (levels.get(levelChoice)[i][j] == Config.BOX_CHAR)
					boxCount++;
				if (levels.get(levelChoice)[i][j] == Config.WORKER_CHAR
						|| levels.get(levelChoice)[i][j] == Config.WORK_GOAL_CHAR)
					numWorker++;
			}
		// 4
		if (numWorker != 1)
			return -3;
		// 5
		if (boxCount == 0)
			return -4;
		// 6
		if (boxCount != goals.get(levelChoice).length / 2)
			return -5;
		// 7
		for (int i = 0; i < goals.get(levelChoice).length - 1; i += 2) {
			int row = goals.get(levelChoice)[i];
			int col = goals.get(levelChoice)[i + 1];
			if (row < 0 || row >= levels.get(levelChoice).length || col < 0
					|| col >= levels.get(levelChoice)[row].length
					|| levels.get(levelChoice)[row][col] == Config.WALL_CHAR)
				return -6;
		}

		// 8
		for (int i = 0; i < goals.get(levelChoice).length - 1; i += 2) {
			for (int j = i + 2; j < goals.get(levelChoice).length - 1; j += 2) {
				if (goals.get(levelChoice)[i] == goals.get(levelChoice)[j]
						&& goals.get(levelChoice)[i + 1] == goals.get(levelChoice)[j + 1]) {
					return -7;
				}
			}
		}

		return 1;
	}

	/**
	 * Calculates how far and where the user wants to move to. Algorithm: This
	 * method calculates how far and where the user wants to move. This method
	 * allows the program to calculate where exactly the user wishes to go. The user
	 * can either go up, down, left or right. When a case matches then the method
	 * returns a new array with the position of how far the user wants to move. If
	 * the move is invalid then the user stays where it is and nothing changes.
	 * 
	 * @param moveStr the users input/direction they want to go
	 * @return an array that stores the players movements based on the int(position)
	 *         they have entered
	 */
	public static int[] calcMoves(String moveStr) {

		char playerMove = moveStr.length() < 1 ? '\0' : moveStr.charAt(0);

		Scanner scan = new Scanner(moveStr.substring(1));
		int magnitude = scan.hasNextInt() ? scan.nextInt() : 1;
		switch (playerMove) {
		case Config.UP_CHAR:
			return new int[] { -1 * magnitude, 0 };
		case Config.DOWN_CHAR:
			return new int[] { 1 * magnitude, 0 };
		case Config.LEFT_CHAR:
			return new int[] { 0, -1 * magnitude };
		case Config.RIGHT_CHAR:
			return new int[] { 0, 1 * magnitude };
		default:
			return new int[] { 0, 0 };
		}

	}

	/**
	 * This method checks if the moves the user picked are valid/good Algorithm: The
	 * method has statements that check whether the move the user wants to do is
	 * good or not. When the move is not good a negative value is returned which
	 * signifies that the move cannot be conducted.
	 * 
	 * @param gameBoard     the game board
	 * @param userPos       the users position
	 * @param userPosChange the change of the users position
	 * @param charVal       a char array that holds '.'/'@' or '*'/'+'
	 * @return a negative int when a move is invalid or a 1 when it is valid
	 */

	public static int movesAreGood(char[][] gameBoard, int[] userPos, int[] userPosChange, char[] charVal) {
		if (userPos == null || userPos.length != 2 || userPos[0] < 0 || userPos[1] < 0 || userPos[0] >= gameBoard.length
				|| userPos[1] >= gameBoard[userPos[0]].length)
			return -1;
		boolean matchChar = false;
		for (int i = 0; i < charVal.length; i++)
			if (gameBoard[userPos[0]][userPos[1]] == charVal[i])
				matchChar = true;
		if (!matchChar)
			return -2;
		if (userPosChange == null || userPosChange.length != 2)
			return -3;

		int[] newPos = new int[] { userPos[0] + userPosChange[0], userPos[1] + userPosChange[1] };

		if (newPos[0] < 0 || newPos[0] >= gameBoard.length || newPos[1] < 0 || newPos[1] >= gameBoard[newPos[0]].length
				|| gameBoard[newPos[0]][newPos[1]] == Config.WALL_CHAR)
			return -4;
		if (gameBoard[newPos[0]][newPos[1]] == Config.BOX_CHAR
				|| gameBoard[newPos[0]][newPos[1]] == Config.BOX_GOAL_CHAR)
			return -5;
		return 1;
	}

	/**
	 * Checks the beginning position of the user and then shifts the user. .
	 * Algorithm: Assigns the position of the board equal to the value that is true
	 * or false based on the beginning position that we are evaluating. If there is
	 * a match then assign it to the true value.
	 * 
	 * @param gameBoard    the board of the game that we're exploring
	 * @param position     the position of the user
	 * @param beginningPos user is either on the goal '+', the box on the goal '*',
	 *                     or an empty goal '.'
	 * @param userTrue     if the position returns true then we want to set the new
	 *                     position equal to the True value
	 * @param userFalse    if the position returns false, then we want to set the
	 *                     new position equal to False value
	 */
	public static void togglePos(char[][] gameBoard, int[] position, char beginningPos, char userTrue, char userFalse) {
		gameBoard[position[0]][position[1]] = (gameBoard[position[0]][position[1]] == beginningPos ? userTrue
				: userFalse);
	}

	/**
	 * This method moves the box when the user approaches it. Algorithm: This method
	 * first checks whether the moves are valid by calling movesAreGood(). An array
	 * with the characters '$' and '*' are passed to ensure that no illegal moves
	 * are happening. The togglePos()is then called after the moves are valid and
	 * then the boxes are shifted to where they need to be moved. togglePos() is
	 * called twice because we need to change the position of the user as well as
	 * the position of the box.
	 * 
	 * 
	 * @param board         the game board
	 * @param position      the position of the user
	 * @param userPosChange the change of the users position (x,y)
	 * @return an int that allows the user to move the box
	 */
	public static int shiftBox(char[][] board, int[] position, int[] userPosChange) {
		int check = movesAreGood(board, position, userPosChange, new char[] { Config.BOX_CHAR, Config.BOX_GOAL_CHAR });
		if (check < 1) {
			return check;
		}
		int[] newPos = new int[] { position[0] + userPosChange[0], position[1] + userPosChange[1] };
		togglePos(board, newPos, Config.GOAL_CHAR, Config.BOX_GOAL_CHAR, Config.BOX_CHAR);
		togglePos(board, position, Config.BOX_GOAL_CHAR, Config.GOAL_CHAR, Config.EMPTY_CHAR);
		return 1;
	}

	/**
	 * Processes the amount of moves it took a player to complete a level Algorithm:
	 * The index takes in the change of the user position and then returns a value
	 * based on what the signum returns. If the value is positive then we loop over
	 * the value until they are finally equal which then calculates what the amounts
	 * of move it took.
	 * 
	 * @param board         the board of the game the user was in
	 * @param position      the position the user was in
	 * @param userPosChange the change of the users position
	 * @return an int that states the amount of moves a user took to complete a
	 *         level
	 */
	public static int processPlayerMoves(char[][] board, int[] position, int[] userPosChange) {
		int index = (userPosChange[0] != 0) ? 0 : 1;
		int sign = Integer.signum(userPosChange[index]);
		if (sign == 0) {
			return 0;
		}
		for (int i = 0; i != userPosChange[index]; i += sign) {
			int[] step = new int[2];
			step[index] = sign;
			step[(index + 1) % 2] = 0;
			int mvChk = 0;
			if ((mvChk = movePlayer(board, position, step)) < 1) {
				return mvChk;
			}

		}
		return 1;
	}

	/**
	 * If the goal is empty '.'(goal character) or a '+'(worker on goal) then it
	 * returns false Algorithm: This method takes in the game board and checks if
	 * the location of the board is equal to an empty goal or a goal with a worker
	 * on top. If the statement is true, then we want to return false. Because the
	 * level has not been completed or started.
	 * 
	 * @param gameBoard the board of the game the user is in
	 * @return false when the goal is empty or a worker is on a goal
	 */
	public static boolean workerOnGoal(char[][] gameBoard) {
		for (int i = 0; i < gameBoard.length; i++) {
			for (int j = 0; j < gameBoard[i].length; j++) {
				if (gameBoard[i][j] == Config.GOAL_CHAR || gameBoard[i][j] == Config.WORK_GOAL_CHAR) {
					return false;
				}
			}
		}
		return true;
	}

	//
	/**
	 * Saves the name of a file when the level is completed Algorithm: When the user
	 * enters s, they are prompted to enter a filename that has saved the moves of
	 * the level chosen.
	 * 
	 * @param scanner takes input from the user to name the file
	 * @param moving  ArrayList that stores the name of the files
	 */
	public static void processSaveMove(Scanner scanner, ArrayList<String> moving) {
		String fileNane = promptString(scanner, "Enter save moves filename: ");
		try (PrintWriter outPut = new PrintWriter(new File(fileNane))) {
			for (String stringToFile : moving) {
				outPut.println(stringToFile);
			}
		} catch (FileNotFoundException c) {
			System.out.println("Error saving move file: " + fileNane);
		}
	}

	/**
	 * This method is responsible for moving the player around the board.
	 * 
	 * Algorithm: The method starts off by checking whether the moves the user has
	 * chosen are valid or not, if they are not valid then the method returns right
	 * away. If it the moves are valid then we create an array newPos that stores
	 * the newPosition of the user then the togglePos() is called to check where the
	 * user is being moved. Since we are passing array as parameters, whatever
	 * movement we do to an array here, we will do to our original array. This
	 * method does that to the users position once the moves are valid.
	 * 
	 * @param gameBoard the game the user is in
	 * @param position  the beginning position of the user
	 * @param steps     a size 2 array that has the steps of the user
	 * @return an int that dictates the players new position
	 */
	public static int movePlayer(char[][] gameBoard, int[] position, int[] steps) {
		int check = movesAreGood(gameBoard, position, steps, new char[] { Config.WORKER_CHAR, Config.WORK_GOAL_CHAR });
		if (check < 1 && check != -5)
			return check;
		int[] newPos = new int[] { position[0] + steps[0], position[1] + steps[1] };
		if (check == -5 && shiftBox(gameBoard, newPos, steps) < 1)
			return 0;
		togglePos(gameBoard, newPos, Config.GOAL_CHAR, Config.WORK_GOAL_CHAR, Config.WORKER_CHAR);
		togglePos(gameBoard, position, Config.WORK_GOAL_CHAR, Config.GOAL_CHAR, Config.EMPTY_CHAR);
		position[0] = newPos[0];
		position[1] = newPos[1];
		return 1;
	}

	/**
	 * This method reads from a file and loads the level the user wants to play
	 * Algorithm: This method is responsible for loading the levels the user will
	 * want to play. The method reads from a file and then loads the file that
	 * corresponds with what the user chose.
	 * 
	 * @param fileName name of the file
	 * @param levels   initial level values referenced from Config.java
	 * @param goals    arrayList of Goals referenced from Config.java
	 * @throws FileNotFoundException if the file/level is not found
	 */
	public static void loadLevels(String fileName, ArrayList<char[][]> levels, ArrayList<int[]> goals)
			throws FileNotFoundException {
		levels.clear();
		goals.clear();
		try (Scanner fileIn = new Scanner(new File(fileName))) {
			String nextLevel = "";
			int numLines = 0;
			int numGoals = 0;
			// Executes until it has a next line
			while (fileIn.hasNextLine()) {
				String fromFile = fileIn.nextLine();
				if (fromFile.contains(Config.WALL_CHAR + "")) {
					for (char theChar : fromFile.toCharArray()) {
						// determines the number of goals the board needs to have
						if (theChar == Config.BOX_GOAL_CHAR || theChar == Config.WORK_GOAL_CHAR
								|| theChar == Config.GOAL_CHAR) {
							numGoals++;
						}
					}
					nextLevel += fromFile + "\n";
					numLines++;
				} else {
					// if the nextLevel string is not empty then we enter this statement
					if (!nextLevel.isEmpty()) {
						char[][] nextMaze = new char[numLines][];
						int[] nextGoals = new int[numGoals * 2];
						Scanner parseMaze = new Scanner(nextLevel);

						int i = 0, k = 0;
						while (parseMaze.hasNextLine()) {
							String row = parseMaze.nextLine();
							nextMaze[i] = row.toCharArray();
							int j = -1;
							do {
								if (j >= 0) {
									if (nextMaze[i][j] == Config.BOX_GOAL_CHAR
											|| nextMaze[i][j] == Config.WORK_GOAL_CHAR
											|| nextMaze[i][j] == Config.GOAL_CHAR) {
										nextGoals[k++] = i;
										nextGoals[k++] = j;
										if (nextMaze[i][j] == Config.BOX_GOAL_CHAR) {
											nextMaze[i][j] = Config.BOX_CHAR;
										} else if (nextMaze[i][j] == Config.WORK_GOAL_CHAR) {
											nextMaze[i][j] = Config.WORKER_CHAR;
										}
									}
								}
								j++;
							} while (j < nextMaze[i].length);
							i++;
						}
						parseMaze.close();
						levels.add(nextMaze);
						goals.add(nextGoals);
						nextLevel = "";
						numLines = 0;
						numGoals = 0;
					}
				}
			}
		} catch (FileNotFoundException notFound) {
			throw notFound;
		}

	}

	/**
	 * This method assigns the user a position when the game starts Algorithm:
	 * Before the game even begins the program needs to know what level the user
	 * wants to go into, the board of the actual game and finally assigning the user
	 * a position in the game. This method does those things, by taking the level,
	 * game board and finally assigning the user a position.
	 * 
	 * @param levelByUser the level the user picks
	 * @param gameBoard   the board of the game the user is in
	 * @param usersPos    the position of the user
	 */

	public static void assignUserPos(int levelByUser, char[][] gameBoard, int[] usersPos) {
		for (int i = 0; i < Config.LEVELS.get(levelByUser).length; i++) {
			gameBoard[i] = new char[Config.LEVELS.get(levelByUser)[i].length];
			for (int j = 0; j < Config.LEVELS.get(levelByUser)[i].length; j++) {
				gameBoard[i][j] = Config.LEVELS.get(levelByUser)[i][j];

				if (gameBoard[i][j] == Config.WORKER_CHAR) {

					usersPos[0] = i;
					usersPos[1] = j;
				}
			}
		}

	}

	/**
	 * This method sets the goals for the level a user picks. Algorithm: This method
	 * takes in the level by the user, the game board and then sets the amount of
	 * goals that level should have.
	 * 
	 * @param levelByUser
	 * @param gameBoard
	 */

	public static void setGoals(int levelByUser, char[][] gameBoard) {

		for (int i = 0; i < Config.GOALS.get(levelByUser).length; i += 2) {
			int rowGoal = Config.GOALS.get(levelByUser)[i];
			int columnGoal = Config.GOALS.get(levelByUser)[i + 1];
			// If we are on a box '$' then our character will change to a '*'
			if (gameBoard[rowGoal][columnGoal] == Config.BOX_CHAR) {
				gameBoard[rowGoal][columnGoal] = Config.BOX_GOAL_CHAR;
			}
			// If we are worker '@' on a goal then our character will change to '.'
			else if (gameBoard[rowGoal][columnGoal] == Config.WORKER_CHAR) {
				gameBoard[rowGoal][columnGoal] = Config.WORK_GOAL_CHAR;
			}
			// The goal'.' will remain the same
			else {
				gameBoard[rowGoal][columnGoal] = Config.GOAL_CHAR;
			}
		}

	}

	/*
	 * Prints out the following messages based on the issues associated with
	 * choosing a level if a level is not ready to be played then the program prints
	 * out one of the following messages Algorithm: when the level is less than 1,
	 * the user is greeted with one of the messages that aligns with the case. If
	 * the level is less than one, then that level may not be playable and one of
	 * the messages below is printed.
	 * 
	 * 
	 * @param level the level chosen by the user
	 * 
	 * @param levelTest the level checked by the program to see if it works
	 */
	public static void isLevelValid(int level, int levelTest) {
		System.out.println("Error loading level!");
		String errMsg = "";
		switch (levelTest) {
		case 0:
			errMsg = "Level " + level + " must be 0 or greater!";
			break;
		case -1:
			errMsg = "Error with Config.LEVELS";
			break;
		case -2:
			errMsg = "Error with Config.GOALS";
			break;
		case -3:
			errMsg = "Level " + level + " has 0 or more than 1 worker(s).";
			break;
		case -4:
			errMsg = "Level " + level + " does not contain any boxes.";
			break;
		case -5:
			errMsg = "Level " + level + " does not have the same " + "number of boxes as goals.";
			break;
		case -6:
			errMsg = "Level " + level + " has a goal location that is a wall.";
			break;
		case -7:
			errMsg = "Level " + level + " has duplicate goals.";
			break;
		default:
			errMsg = "Unknown error.";
			break;
		}
		errMsg += "\nMaze:\n";
		for (char[] arr : Config.LEVELS.get(level)) {
			errMsg += Arrays.toString(arr) + "\n";
		}
		errMsg += "\nGoals:\n" + Arrays.toString(Config.GOALS.get(level));
		System.out.println(errMsg);

	}

	/**
	 * 
	 * Main method Algorithm: The main method is responsible for making the code
	 * work and calling all of the supporting methods, working in conjunction with
	 * each other. This method prints out the game board, the help menu, checks if
	 * levels are valid, gives errors if the level is not valid, prints out a level
	 * when a level is valid, saves the amount of moves it took to complete a level,
	 * and overall makes the game run.
	 * 
	 * @param args String array with arguments
	 */
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("Welcome to Sokoban!");
		String lvlFile = promptString(scan, "Enter file contain level list (blank for default levels): ");
		if (lvlFile.isEmpty() == false) {
			try {
				loadLevels(lvlFile, Config.LEVELS, Config.GOALS);
			} catch (Exception e) {
				System.out.print("Error while reading file: " + lvlFile + "\n");
				e.printStackTrace();
				return;
			}
		}
		char playAgain = 'n';
		do {

			int levelByUser = promptInt(scan, "Choose a level between 0 and " + (Config.LEVELS.size() - 1) + ": ", 0,
					Config.LEVELS.size() - 1);
			int lvlTest = checkLevel(levelByUser, Config.LEVELS, Config.GOALS);
			// Enters when level is invalid
			if (lvlTest < 1) {

				isLevelValid(levelByUser, lvlTest);
			} else {
				int moveCount = 0;

				ArrayList<String> moves = new ArrayList<String>();

				int[] usersPosition = { 0, 0 };

				char[][] gameBoard = new char[Config.LEVELS.get(levelByUser).length][];
				// assigning the users position
				assignUserPos(levelByUser, gameBoard, usersPosition);
				// setting the goals for the level
				setGoals(levelByUser, gameBoard);

				System.out.println("Sokoban Level " + levelByUser);
				boolean workerGoal = false;
				boolean printBoard = true;

				while ((workerGoal = workerOnGoal(gameBoard)) == false) {

					if (printBoard) {
						printGameBoard(gameBoard);
					}
					printBoard = true;

					String move = promptString(scan, "(? for help) : ");
					if (move.length() < 1)
						continue;
					else if (move.charAt(0) == Config.QUIT_CHAR)
						break;

					else if (move.charAt(0) == '?') {
						helpMenu();
						printBoard = false;
					}
					// loading the players moves in a file
					else if (move.toLowerCase().charAt(0) == 'l') {
						// CALLING PROMPT HERE
						String fileName = promptString(scan, "Enter file containing moves: ");
						try (Scanner fileIn = new Scanner(new File(fileName))) {

							while (fileIn.hasNextLine()) {
								move = fileIn.nextLine();

								int[] delta = calcMoves(move);
								if (delta[0] != delta[1]) {
									// calculates the moves the user took in said level
									processPlayerMoves(gameBoard, usersPosition, delta);
									moves.add(move);
									moveCount += Math.abs(delta[0]) + Math.abs(delta[1]);
								}
							}
						} catch (FileNotFoundException notFound) {
							System.out.println("Error loading move file: " + fileName);
						}
					} else if (move.toLowerCase().charAt(0) == 's') {
						// saves the moves
						processSaveMove(scan, moves);
					} else {
						int[] delta = calcMoves(move);
						if (delta[0] != delta[1]) {
							int movesTaken = processPlayerMoves(gameBoard, usersPosition, delta);
							if (movesTaken > 0) {
								moves.add(move);
								moveCount += Math.abs(delta[0]) + Math.abs(delta[1]);
							}
						}
					}
				}

				if (workerGoal) {

					playerWonGame(scan, gameBoard, moveCount, moves);
				}
			}
			playAgain = promptChar(scan, "Play again? (y/n) ");
		} while (playAgain == 'y');
		System.out.println("Thanks for playing!");
	}

	/**
	 * Prints the gameBoard the user chose Algorithm: this method is executed to
	 * print out the game board when an existing level is chosen by the user.
	 * 
	 * @param gameBoard the game board/level the user picked
	 */

	public static void printGameBoard(char[][] gameBoard) {
		for (int i = -1; i <= gameBoard[0].length; i++) {

			System.out.print(Config.WALL_CHAR);
		}
		for (int i = 0; i < gameBoard.length; i++) {

			System.out.print("\n" + Config.WALL_CHAR);
			for (int j = 0; j < gameBoard[i].length; j++) {
				System.out.print(gameBoard[i][j]);
			}
			System.out.print(Config.WALL_CHAR);
		}
		System.out.println();
		for (int i = -1; i <= gameBoard[gameBoard.length - 1].length; i++) {
			System.out.print(Config.WALL_CHAR);
		}
		System.out.println();

	}

	/**
	 * Prints out the help menu when the user enters '?' Algorithm: When the user is
	 * in the game and does not know how to actually play the game, then they can
	 * enter '?' to see the full help menu and know how to navigate around the
	 * level.
	 */

	public static void helpMenu() {
		System.out.println("Sokoban Help:");
		System.out.println("----------------------------------------------------------------");
		System.out.println("You need to push all the boxes so that they cover all the goals.");
		System.out.println("----------------------------------------------------------------");
		System.out.println("Board Legend:");
		System.out.println("\tEmpty floor: " + Config.EMPTY_CHAR);
		System.out.println("\tWall: " + Config.WALL_CHAR);
		System.out.println("\tGoal: " + Config.GOAL_CHAR);
		System.out.println("\tBox: " + Config.BOX_CHAR);
		System.out.println("\tBox on a goal: " + Config.BOX_GOAL_CHAR);
		System.out.println("\tWorker: " + Config.WORKER_CHAR);
		System.out.println("\tWorker on a goal: " + Config.WORK_GOAL_CHAR);
		System.out.println("----------------------------------------------------------------");
		System.out.println("Moving the worker:");
		System.out.println("\tMove up: " + Config.UP_CHAR);
		System.out.println("\tMove down: " + Config.DOWN_CHAR);
		System.out.println("\tMove left: " + Config.LEFT_CHAR);
		System.out.println("\tMove right: " + Config.RIGHT_CHAR);
		System.out.println("\tMultiple moves: direction followed by a magnitude.");
		System.out.println("\t\tExample: " + Config.UP_CHAR + "8 moves up 8 spots.");
		System.out.println("----------------------------------------------------------------");
		System.out.println("Other commands:");
		System.out.println("\tQuit: " + Config.QUIT_CHAR);
		System.out.println("\tLoad moves: l");
		System.out.println("\tSave moves: s");
		System.out.println("\tHelp menu: ?");

	}

	/**
	 * This method is executed when the player has won the game.
	 * 
	 * Algorithm: When the player has successfully completed the level they are
	 * currently in, they are greeted with these messages. The user is informed with
	 * how many moves it took them to complete the level, and then the final game
	 * board is printed to show them that they've completed the level. Finally, the
	 * user is asked if they want to save their winning strategy or not.
	 * 
	 * 
	 * @param scanner    Determines the players
	 * @param gameBoard  the level/board the player will be exploring
	 * @param moveCount  amount of moves it took for the player to complete the game
	 * @param savedMoves arrayList that stores a file with the saved moves
	 */

	public static void playerWonGame(Scanner scanner, char[][] gameBoard, int moveCount, ArrayList<String> savedMoves) {

		System.out.println("Congratulations! You won in " + moveCount + " moves!");
		printGameBoard(gameBoard);

		char saveWin = promptChar(scanner, "Save your winning strategy? (y/n)");
		if (saveWin == 'y') {
			processSaveMove(scanner, savedMoves);
		}
	}
}
