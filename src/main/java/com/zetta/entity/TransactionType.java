package com.zetta.entity;

public enum TransactionType {
    DEBIT,      // User owes money (To Pay)
    CREDIT,     // User is owed money (To Receive)
    TRANSFER    // Transfer between users
}

