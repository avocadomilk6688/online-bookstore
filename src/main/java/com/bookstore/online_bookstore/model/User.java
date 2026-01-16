package com.bookstore.online_bookstore.model;

public abstract class User {
    protected int userID;
    protected String name;
    protected String email;
    protected String password;
    protected String role;       // ADMIN / GUEST / MEMBER
    protected String memberType; // STANDARD / PREMIUM
    protected String birthDate;
    protected String address;

    public User() {}

    public User(int userID, String name,String email, String password, String role,
                String memberType, String birthDate, String address) {

        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.memberType = memberType;
        this.birthDate = birthDate;
        this.address = address;
    }

    // Getters
    public int getUserID() {
        return userID;
    }

    public String getName() { 
        return name; 
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getMemberType() { 
        return memberType; 
    }
    public String getBirthDate() { 
        return birthDate; 
    }
    public String getAddress() { 
        return address; 
    }

    // Setters
    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setMemberType(String memberType) { 
        this.memberType = memberType; 
    }

    public void setBirthDate(String birthDate) { 
        this.birthDate = birthDate;
    }

    public void setAddress(String address) { 
        this.address = address; 
    }

    public void setName(String name) { 
        this.name = name; 
    }
}

