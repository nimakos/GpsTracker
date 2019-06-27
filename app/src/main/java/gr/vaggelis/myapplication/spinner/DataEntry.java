package gr.vaggelis.myapplication.spinner;

public class DataEntry<T> {
    public int id;
    public T data;

    /**
     * Constructor
     * @param id int
     * @param data T generic object
     */
    public DataEntry(int id, T data) {
        this.id = id;
        this.data = data;
    }

    /**
     * Constructor
     * @param data T generic object
     */
    public DataEntry(T data) {
        this(-1, data);
    }

    /**
     * Return object data to string format
     * @return string of data
     */
    public String toString() {
        return data.toString();
    }
}
