package com.bookstore.online_bookstore.model;

public class Admin extends User {
    
    public Admin(int userID, String email, String password) {
        super(userID, email, password, 
              "ADMIN", 
              null,  // memberType
              null,  // birthDate
              null); // address
    }
}
