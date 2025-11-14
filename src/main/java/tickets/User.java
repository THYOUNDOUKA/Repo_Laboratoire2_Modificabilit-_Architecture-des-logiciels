package tickets;

public class User {
    private int userID;
    private String name;
    private String email;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {

    }

    public User(int userID, String name, String email) {
        this.userID = userID;
        this.name = name;
        this.email = email;

    }

    public boolean isDeveloper() {
        return this instanceof Developer;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return this.userID == other.userID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userID);
    }
}
