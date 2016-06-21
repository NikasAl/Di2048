package ru.electronikas.diagonal.model;

/**
 * Created by nikas on 6/20/16.
 */
public class Pos {

    public int x;
    public int y;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pos pos = (Pos) o;

        if (x != pos.x) return false;
        return y == pos.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
