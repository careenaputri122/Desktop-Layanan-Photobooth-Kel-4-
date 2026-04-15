package model;

public class Foto {
    private int id;
    private Booking booking;
    private String filePath;

    public Foto() {}

    public Foto(int id, Booking booking, String filePath) {
        this.id = id;
        this.booking = booking;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}
