package View;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static View.Log4J.LOG;

public class Displayer extends Canvas {

    private int[][] maze;
    int rows, cols;
    private Pair<Integer, Integer> playerPos;
    private Pair<Integer, Integer> goalPos;
    StringProperty imageFileNameWall = new SimpleStringProperty();
    StringProperty imageFileNamePlayer = new SimpleStringProperty();
    StringProperty imageFileNameFloor = new SimpleStringProperty();
    StringProperty imageFileNameGoal = new SimpleStringProperty();
    StringProperty imageFileNameSolve = new SimpleStringProperty();
    private Pair<Integer, Integer>[] solution;

    /*
    sets the maze, its dimensions, player and goal positions, and initializes the solution.
    */
    public void drawMaze(int[][] maze) {
        setHeight(400);
        setWidth(400);
        this.maze = maze;
        rows = maze.length;
        cols = maze[0].length;
        playerPos = null;
        goalPos = null;
        solution = null;
        draw();
    }

    /*
    Helper, clears the canvas and draws the maze using the provided graphics context.
    */
    public void draw() {
        this.minHeight(0);
        this.minWidth(0);
        double canvasHeight = getHeight();
        double canvasWidth = getWidth();

        double cellHeight = canvasHeight / rows;
        double cellWidth = canvasWidth / cols;

        GraphicsContext graphicsContext = getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.setFill(Color.RED);
        drawMaze(graphicsContext, cellHeight, cellWidth, rows, cols);
    }

    /*
    draws each cell of the maze using the specified graphics context and image files for different types of cells.
    */
    private void drawMaze(GraphicsContext graphicsContext, double cellHeight, double cellWidth, int rows, int cols) {
        graphicsContext.setFill(Color.RED);

        Image wall = null;
        try {
            wall = new Image(new FileInputStream(getImageFileNameWall()));
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to load image.");
        }
        Image floor = null;
        try {
            floor = new Image(new FileInputStream(getImageFileNameFloor()));
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to load image.");
        }

        Image player = null;
        try {
            player = new Image(new FileInputStream(getImageFileNamePlayer()));
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to load image.");
        }

        Image goal = null;
        try {
            goal = new Image(new FileInputStream(getImageFileNameGoal()));
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to load image.");
        }

        Image Solve = null;
        try {
            Solve = new Image(new FileInputStream(getImageFileNameSolve()));
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to load image.");
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = j * cellWidth;
                double y = i * cellHeight;
                switch (maze[i][j]) {
                    case 0:
                        if (floor == null)
                            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                        else
                            graphicsContext.drawImage(floor, x, y, cellWidth, cellHeight);
                        break;
                    case 1:
                        if (wall == null)
                            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                        else
                            graphicsContext.drawImage(wall, x, y, cellWidth, cellHeight);
                        break;
                    case 2:
                        if (playerPos != null) {
                            if (floor == null)
                                graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                            else
                                graphicsContext.drawImage(floor, x, y, cellWidth, cellHeight);
                            break;
                        }
                        playerPos = new Pair<>(i, j);
                        if (player == null)
                            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                        else
                            graphicsContext.drawImage(player, x, y, cellWidth, cellHeight);
                        break;
                    case 3:
                        goalPos = new Pair<>(i, j);
                        if (goal == null)
                            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                        else
                            graphicsContext.drawImage(goal, x, y, cellWidth, cellHeight);
                        break;
                    case 4:
                        if (Solve == null)
                            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
                        else
                            graphicsContext.drawImage(Solve, x, y, cellWidth, cellHeight);
                        break;

                }
            }
        }

        double x = playerPos.getValue() * cellWidth;
        double y = playerPos.getKey() * cellHeight;
        if (player == null)
            graphicsContext.fillRect(x, y, cellWidth, cellHeight);
        else
            graphicsContext.drawImage(player, x, y, cellWidth, cellHeight);
    }

    /*
    draws the solution path in the maze.
    */
    public void drawSolution(Pair<Integer, Integer>[] sol) {

        try {

            solution = Arrays.copyOfRange(sol, 1, sol.length - 1);
            for (int i = 0; i < solution.length; i++) {
                maze[solution[i].getKey()][solution[i].getValue()] = 4;
            }
            draw();
        }
        catch (Exception e){
            LOG.error("Solution not possible", e);
        }
    }

    public String getImageFileNameWall() {
        return imageFileNameWall.get();
    }

    public String imageFileNameWallProperty() {
        return imageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.imageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileNamePlayer() {
        return imageFileNamePlayer.get();
    }

    public String imageFileNamePlayerProperty() {
        return imageFileNamePlayer.get();
    }

    public void setImageFileNamePlayer(String imageFileNamePlayer) {
        this.imageFileNamePlayer.set(imageFileNamePlayer);
    }

    public String getImageFileNameFloor() {
        return imageFileNameFloor.get();
    }

    public String imageFileNameFloorProperty() {
        return imageFileNameFloor.get();
    }

    public void setImageFileNameFloor(String imageFileNameFloor) {
        this.imageFileNameFloor.set(imageFileNameFloor);
    }

    public String getImageFileNameGoal() {
        return imageFileNameGoal.get();
    }

    public String imageFileNameGoalProperty() {
        return imageFileNameGoal.get();
    }

    public void setImageFileNameGoal(String imageFileNameGoal) {
        this.imageFileNameGoal.set(imageFileNameGoal);
    }

    public String getImageFileNameSolve() {
        return imageFileNameSolve.get();
    }

    public String imageFileNameSolveProperty() {
        return imageFileNameSolve.get();
    }

    public void setImageFileNameSolve(String imageFileNameSolve) {
        this.imageFileNameSolve.set(imageFileNameSolve);
    }

    public int movePlayer(int row, int col) {
        int result = 0; //Regular
        playerPos = new Pair<>(row, col);
        //If a solution exists
        if (solution != null){
            for (int i = solution.length - 1; i >= 0 ; i--) {
                if (playerPos.equals(solution[i]))
                {
                    for (int j = 0; j <= i; j++) {
                        maze[solution[j].getKey()][solution[j].getValue()] = 0;
                    }
                    solution = Arrays.copyOfRange(solution, i + 1, solution.length);

                }
            }
        }
        draw();
        if (goalPos != null)
            if (playerPos.equals(goalPos))
                result = 1; //Goal
        return result;
    }

    public Pair<Integer, Integer> getPlayerPos() {
        return playerPos;
    }

}
