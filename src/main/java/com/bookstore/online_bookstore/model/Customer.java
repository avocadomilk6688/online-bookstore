package com.bookstore.online_bookstore.model;

public class Customer extends User {
    
    public Customer(int userID, String name, String email, String password,
                    String memberType, String birthDate, String address) {    

        super(userID, name, email, password, "MEMBER", memberType, birthDate, address);
        
    }
}