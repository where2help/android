package app.iamin.iamin.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by Markus on 20.10.15.
 */
public class User {

    private int id;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private boolean admin;

    private boolean ngoAdmin;

    private String provider;

    private String uid;

    private String name;

    private String nickname;

    private String image;

    public User() {}

    public User fromJSON(JSONObject obj) throws JSONException, IOException, ParseException {
        User user = new User();
        user.setId(obj.getInt("id"));
        user.setEmail(obj.getString("email"));
        user.setFirstName(obj.getString("first_name"));
        user.setLastName(obj.getString("last_name"));
        user.setPhone(obj.getString("phone"));
        // TODO: wait for fix
        //user.setAdmin(obj.getBoolean("admin"));
        //user.setNgoAdmin(obj.getBoolean("ngo_admin"));
        user.setProvider(obj.getString("provider"));
        user.setUid(obj.getString("uid"));
        user.setName(obj.getString("name"));
        user.setNickname(obj.getString("nickname"));
        user.setImage(obj.getString("image"));
        return user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isNgoAdmin() {
        return ngoAdmin;
    }

    public void setNgoAdmin(boolean ngoAdmin) {
        this.ngoAdmin = ngoAdmin;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
