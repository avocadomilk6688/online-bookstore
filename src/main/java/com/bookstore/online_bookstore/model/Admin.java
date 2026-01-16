package com.bookstore.online_bookstore.model;

public class Admin extends User {
    
    public Admin(int userID,String name,String email, String password) {
        super(userID, name, email, password, 
              "ADMIN", 
              null,  // memberType
              null,  // birthDate
              null); // address
    }
}
