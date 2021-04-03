public class PeerInfo {

    String username, password;
    int count_downloads, count_failures;

    //constructor
    public PeerInfo(String username, String password,int count_downloads, int count_failures) {
        this.username = username; 
        this.password = password;
        this.count_downloads= count_downloads;
        this.count_failures = count_failures;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getCountDownloads() {
        return this.count_downloads;
    }

    public int getCountFailures() {
        return this.count_failures;
    }

    //TODO add setters 
}