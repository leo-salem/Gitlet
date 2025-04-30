package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {
    private String name;
    private String Currentcommit;
    public Branch(String name, String Currentcommit) {
        this.name = name;
        this.Currentcommit = Currentcommit;
    }
    public String getName() {
        return name;
    }
    public String getCurrentcommit() {
        return Currentcommit;
    }
    public void setCurrentcommit(String Currentcommit) {
        this.Currentcommit = Currentcommit;
    }
    public void setName(String name) {
        this.name = name;
    }
}
