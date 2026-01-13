package com.bookstore.online_bookstore.model;

public class Customer extends User {

 public Customer(int userID, String email, String password,
                String memberType, String birthDate, String address) {        
    super(userID, email, password,
              "MEMBER",      // Correct role
              memberType,    // STANDARD / PREMIUM
              birthDate, 
              address);
    }
}

